package hk.ust.felab.rase.master.impl;

import hk.ust.felab.rase.master.Master;
import hk.ust.felab.rase.sim.Sim;
import hk.ust.felab.rase.slave.Slave;
import hk.ust.felab.rase.vo.SimInput;
import hk.ust.felab.rase.vo.SimOutput;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import umontreal.iro.lecuyer.randvar.UniformIntGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

public class MasterImpl implements Master {

	/**
	 * t, altID, args, seed
	 */
	private transient final Logger inputSeq = Logger.getLogger("input_seq");

	/**
	 * t, altID, result
	 */
	private transient final Logger outputSeq = Logger.getLogger("output_seq");

	private double[][] alts;
	private ExecutorService slaveExecutor;
	private BlockingQueue<SimInput> simInputs = new LinkedBlockingQueue<SimInput>();
	private BlockingQueue<SimOutput> simOutputs = new LinkedBlockingQueue<SimOutput>();

	private int syncIdSeq = 0;

	private MRG32k3a mrg32k3a = new MRG32k3a();
	private UniformIntGen seedGen = new UniformIntGen(mrg32k3a, 1,
			Integer.MAX_VALUE);

	private synchronized long[] nextSeed() {
		long[] seed = new long[6];
		for (int i = 0; i < 6; i++) {
			seed[i] = seedGen.nextInt();
		}
		return seed;
	}

	public MasterImpl(double[][] alts, List<Sim> sims) {
		this.alts = alts;
		slaveExecutor = Executors.newFixedThreadPool(sims.size());
		for (Sim sim : sims) {
			slaveExecutor.execute(new Slave(sim, this));
		}
	}

	public void stopSlaves() {
		slaveExecutor.shutdownNow();
	}

	@Override
	public SimOutput[] sim(int[] altIDs) throws InterruptedException {
		SimOutput[] ret = new SimOutput[altIDs.length];
		int syncId = syncIdSeq++;
		for (int altID : altIDs) {
			SimInput simInput = new SimInput(syncId, altID, alts[altID],
					nextSeed());
			simInputs.put(simInput);
			if (inputSeq.isInfoEnabled()) {
				inputSeq.info(System.currentTimeMillis() + ", " + simInput);
			}
		}
		for (int i = 0; i < ret.length;) {
			SimOutput simOutput = simOutputs.take();
			if (simOutput.syncID != syncId) {
				simOutputs.put(simOutput);
			} else {
				ret[i++] = simOutput;
			}
		}
		return ret;
	}

	@Override
	public int asyncSim(int[] altIDs) throws InterruptedException {
		int syncId = syncIdSeq;
		for (int altID : altIDs) {
			SimInput simInput = new SimInput(syncId, altID, alts[altID],
					nextSeed());
			simInputs.put(simInput);
			if (inputSeq.isInfoEnabled()) {
				inputSeq.info(System.currentTimeMillis() + ", " + simInput);
			}
		}
		return simOutputs.size();
	}

	@Override
	public int phantomSim(int[] altIDs) throws InterruptedException {
		for (int altID : altIDs) {
			SimInput simInput = new SimInput(-1, altID, null, null);
			if (inputSeq.isInfoEnabled()) {
				inputSeq.info(System.currentTimeMillis() + ", " + simInput);
			}

			SimOutput simOutput = new SimOutput(-1, altID, new double[] { 0 });
			simOutputs.put(simOutput);
		}
		return simOutputs.size();
	}

	@Override
	public SimOutput takeSimOutput() throws InterruptedException {
		SimOutput simOutput = simOutputs.take();
		return simOutput;
	}

	@Override
	public SimOutput[] takeSimOutputs(int n, boolean nonBlock)
			throws InterruptedException {
		if (nonBlock) {
			n = Math.min(n, simOutputs.size());
		}
		SimOutput[] outputs = new SimOutput[n];
		for (int i = 0; i < n; i++) {
			outputs[i] = simOutputs.take();
		}
		return outputs;
	}

	@Override
	public SimInput takeSimInput() throws InterruptedException {
		return simInputs.take();
	}

	@Override
	public void putSimOutput(SimOutput simOutput) throws InterruptedException {
		if (outputSeq.isInfoEnabled()) {
			outputSeq.info(System.currentTimeMillis() + ", " + simOutput);
		}
		simOutputs.put(simOutput);
	}

}
