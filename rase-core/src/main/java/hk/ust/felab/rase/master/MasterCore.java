package hk.ust.felab.rase.master;

import hk.ust.felab.rase.SimHelper;
import hk.ust.felab.rase.SimInput;
import hk.ust.felab.rase.SimOutput;
import hk.ust.felab.rase.slave.SlaveHelper;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import umontreal.iro.lecuyer.randvar.UniformIntGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

public class MasterCore implements SimHelper, SlaveHelper, SimIoServerHelper {
	/**
	 * t, altId, args, seed
	 */
	private transient Logger inputSeq = null;

	/**
	 * t, altId, result
	 */
	private transient Logger outputSeq = null;

	private BlockingQueue<SimInput> simInputs;
	private BlockingQueue<SimOutput> simOutputs;

	private double[][] alts;
	private double[] simArgs;

	private int repId = -1;
	private int syncIdSeq = 0;

	private MRG32k3a mrg32k3a;
	private UniformIntGen seedGen;

	private synchronized long[] nextSeed() {
		long[] seed = new long[6];
		for (int i = 0; i < 6; i++) {
			seed[i] = seedGen.nextInt();
		}
		return seed;
	}

	public MasterCore(double[][] alts, double[] simArgs, long[] masterSeed) {
		this.alts = alts;
		this.simArgs = simArgs;

		simInputs = new LinkedBlockingQueue<SimInput>();
		simOutputs = new LinkedBlockingQueue<SimOutput>();

		mrg32k3a = new MRG32k3a();
		if (masterSeed != null) {
			mrg32k3a.setSeed(masterSeed);
		}
		seedGen = new UniformIntGen(mrg32k3a, 1, Integer.MAX_VALUE);
	}

	public void prepare() {
		inputSeq = Logger.getLogger("input_seq");
		outputSeq = Logger.getLogger("output_seq");
		simInputs.clear();
		simOutputs.clear();
		repId++;
	}

	@Override
	public SimOutput[] sim(int[] altIDs) throws InterruptedException {
		SimOutput[] ret = new SimOutput[altIDs.length];
		int syncId = syncIdSeq++;
		for (int altID : altIDs) {
			SimInput simInput = new SimInput(repId, syncId, altID, alts[altID],
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
		int syncId = syncIdSeq++;
		for (int altID : altIDs) {
			SimInput simInput = new SimInput(repId, syncId, altID, alts[altID],
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
			SimInput simInput = new SimInput(repId, -1, altID, null, null);
			if (inputSeq.isInfoEnabled()) {
				inputSeq.info(System.currentTimeMillis() + ", " + simInput);
			}

			SimOutput simOutput = new SimOutput(-1, altID, new double[] { 0 });
			simOutputs.put(simOutput);
			if (outputSeq.isInfoEnabled()) {
				outputSeq.info(System.currentTimeMillis() + ", " + simOutput);
			}
		}
		return simOutputs.size();
	}

	@Override
	public SimOutput takeSimOutput() throws InterruptedException {
		SimOutput simOutput = simOutputs.take();
		return simOutput;
	}

	/**
	 * only used by ras in single thread
	 */
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
	public int simInputCount() {
		return simInputs.size();
	}

	@Override
	public SimInput takeSimInput() throws InterruptedException {
		return simInputs.take();
	}

	@Override
	public double[] getSimArgs() {
		return simArgs;
	}

	@Override
	public void putSimOutput(SimOutput simOutput) throws InterruptedException {
		if (outputSeq.isInfoEnabled()) {
			outputSeq.info(System.currentTimeMillis() + ", " + simOutput);
		}
		simOutputs.put(simOutput);
	}

	@Override
	public SimInput takeSimInputNet() throws InterruptedException {
		return simInputs.take();
	}

	@Override
	public void putSimOutputNet(SimOutput simOutput)
			throws InterruptedException {
		if (outputSeq.isInfoEnabled()) {
			outputSeq.info(System.currentTimeMillis() + ", " + simOutput);
		}
		simOutputs.put(simOutput);
	}

}
