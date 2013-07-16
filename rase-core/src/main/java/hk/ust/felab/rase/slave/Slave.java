package hk.ust.felab.rase.slave;

import hk.ust.felab.rase.Sim;
import hk.ust.felab.rase.SimInput;
import hk.ust.felab.rase.SimOutput;

import org.apache.log4j.Logger;

public class Slave implements Runnable {

	/**
	 * t1; t2, simInput; t3, simOutput
	 */
	private transient final Logger log = Logger.getLogger("workers");

	private Sim sim;
	private SlaveHelper agent;

	public Slave(Sim sim, SlaveHelper agent) {
		this.sim = sim;
		this.agent = agent;
	}

	@Override
	public void run() {
		while (true) {
			try {
				StringBuilder logLine = null;

				if (log.isTraceEnabled()) {
					logLine = new StringBuilder();
					logLine.append(System.currentTimeMillis() + "; ");
				}

				SimInput simIn = agent.takeSimInput();

				if (log.isTraceEnabled()) {
					logLine.append(System.currentTimeMillis() + ", " + simIn
							+ "; ");
				}

				double[] simResult = sim.sim(simIn.alt, agent.getSimArgs(),
						simIn.seed);
				SimOutput simOut = new SimOutput(simIn.syncId, simIn.altId,
						simResult);

				if (log.isTraceEnabled()) {
					logLine.append(System.currentTimeMillis() + ", " + simOut);
				}

				agent.putSimOutput(simOut);

				if (log.isTraceEnabled()) {
					log.trace(logLine.toString());
				}
			} catch (InterruptedException e) {
				return;
			}
		}
	}

}
