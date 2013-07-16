package hk.ust.felab.rase.agent;

import hk.ust.felab.rase.SimInput;
import hk.ust.felab.rase.SimOutput;
import hk.ust.felab.rase.slave.SlaveHelper;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

public class AgentCore implements SlaveHelper, SimIoClientHelper {

	private transient final Logger log = Logger.getLogger("slave.core");
	/**
	 * t, altId, args, seed
	 */
	protected transient Logger inputSeq;

	/**
	 * t, altId, result
	 */
	protected transient Logger outputSeq;

	private double[] simArgs;
	private int simInputS2;
	private int simInputS1;

	protected BlockingQueue<SimInput> simInputs;
	protected BlockingQueue<SimOutput> simOutputs;

	private AtomicBoolean waitingInput = new AtomicBoolean(false);
	private AtomicBoolean waitingOutput = new AtomicBoolean(false);

	public AgentCore(double[] simArgs, final int simInputS2,
			final int simInputS1, final int simOutputS2) {
		inputSeq = Logger.getLogger("input_seq");
		outputSeq = Logger.getLogger("output_seq");

		this.simArgs = simArgs;
		this.simInputS2 = simInputS2;
		this.simInputS1 = simInputS1;

		simInputs = new LinkedBlockingQueue<SimInput>(simInputS2);
		simOutputs = new LinkedBlockingQueue<SimOutput>(simOutputS2);
	}

	public Thread takeSimInputThread(final SimInputClient simInputClient) {
		return new Thread(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setName("take-sim-input-thread");
				while (true) {
					try {
						if (!waitingInput.get()
								&& simInputs.size() < simInputS1) {
							simInputClient.takeSimInputs(simInputS2
									- simInputS1);
							if (log.isDebugEnabled()) {
								log.debug("input req sent: "
										+ (simInputS2 - simInputS1));
							}
							waitingInput.set(true);
						} else {
							Thread.sleep(1);
						}
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		});
	}

	public Thread putSimOutputThread(final SimOutputClient simOutputClient) {
		return new Thread(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setName("put-sim-output-thread");
				while (true) {
					try {
						if (!waitingOutput.get() && simOutputs.size() > 0) {
							SimOutput[] simOutputArr = new SimOutput[simOutputs
									.size()];
							for (int i = 0; i < simOutputArr.length; i++) {
								simOutputArr[i] = simOutputs.take();
							}
							simOutputClient.putSimOutputs(simOutputArr);
							if (log.isDebugEnabled()) {
								log.debug("output sent: " + simOutputArr.length);
							}
							waitingOutput.set(true);
						} else {
							Thread.sleep(1);
						}
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		});
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
	public void putSimInputNet(SimInput simInput) throws InterruptedException {
		simInputs.put(simInput);
	}

	@Override
	public void unsetWaitingInput() {
		waitingInput.set(false);
	}

	@Override
	public void unsetWaitingOutput() {
		waitingOutput.set(false);
	}

}
