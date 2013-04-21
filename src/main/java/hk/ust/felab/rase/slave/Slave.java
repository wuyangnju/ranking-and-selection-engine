package hk.ust.felab.rase.slave;

import hk.ust.felab.rase.agent.Agent;
import hk.ust.felab.rase.sim.Sim;
import hk.ust.felab.rase.vo.SimInput;
import hk.ust.felab.rase.vo.SimOutput;

import org.apache.log4j.Logger;

public class Slave implements Runnable {

	/**
	 * t1; t2, simInput; t3, simOutput
	 */
	private transient final Logger log = Logger.getLogger("slaves");

	private Sim sim;
	private Agent agent;

	public Slave(Sim sim, Agent agent) {
		this.sim = sim;
		this.agent = agent;
	}

	@Override
	public void run() {
		while (true) {
			try {
				StringBuilder logLine = null;

				if (log.isInfoEnabled()) {
					logLine = new StringBuilder();
					logLine.append(System.currentTimeMillis() + "; ");
				}

				SimInput simIn = agent.takeSimInput();

				if (log.isInfoEnabled()) {
					logLine.append(System.currentTimeMillis() + ", " + simIn
							+ "; ");
				}

				double[] simResult = sim.sim(simIn.args, simIn.seed);
				SimOutput simOut = new SimOutput(simIn.syncID, simIn.altID,
						simResult);

				if (log.isInfoEnabled()) {
					logLine.append(System.currentTimeMillis() + ", " + simOut);
				}

				agent.putSimOutput(simOut);

				log.info(logLine.toString());
			} catch (InterruptedException e) {
				return;
			}
		}
	}

}
