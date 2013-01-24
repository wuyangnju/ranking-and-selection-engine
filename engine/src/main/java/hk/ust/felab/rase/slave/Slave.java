package hk.ust.felab.rase.slave;

import hk.ust.felab.rase.agent.Agent;
import hk.ust.felab.rase.conf.ClusterConf;
import hk.ust.felab.rase.conf.RasConf;
import hk.ust.felab.rase.sim.SampleGen;
import hk.ust.felab.rase.util.SampleGenClassLoader;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class Slave {
	private transient final Logger perf1;
	/**
	 * We can't log the sample # at spesific timestamp. We can only log the
	 * timestamp when sample # reach some level, then plot sample #
	 * culmulatively against time.
	 * 
	 * @see src/main/resources/log4j.properties
	 */
	private transient final Logger perf2;

	private int slaveId;
	private SampleGen sampleGen;
	private Agent agent;
	private long sampleCount = 0;
	private Runnable thread;

	public Slave(int slaveId, Agent agent) throws Exception {
		this.slaveId = slaveId;

		String className = "hk.ust.felab.rase.sim.impl."
				+ RasConf.get().sampleGenerator;
		SampleGenClassLoader classLoader = new SampleGenClassLoader();
		classLoader.loadClass(className);
		this.sampleGen = (SampleGen) Class
				.forName(className, true, classLoader)
				.getConstructor(Integer.class).newInstance(slaveId);

		this.agent = agent;

		perf1 = Logger.getLogger("slave.perf1." + slaveId);
		perf1.addAppender(new FileAppender(new PatternLayout("%m"),
				ClusterConf.LOG_DIR + "/slave_perf1_" + slaveId + ".csv",
				false, false, 16192));

		perf2 = Logger.getLogger("slave.perf2." + slaveId);
		perf2.addAppender(new FileAppender(new PatternLayout("%m"),
				ClusterConf.LOG_DIR + "/slave_perf2_" + slaveId + ".csv",
				false, false, 16192));

		thread = new ConsumeAltProduceSampleThread();
	}

	public Runnable getThread() {
		return thread;
	}

	class ConsumeAltProduceSampleThread implements Runnable {

		private double[] packSample(double altId, double[] sample) {
			double[] res = new double[sample.length + 1];
			res[0] = altId;
			System.arraycopy(sample, 0, res, 1, sample.length);
			return res;
		}

		@Override
		public void run() {
			Thread.currentThread().setName(
					"Slave " + slaveId + " - consume alt, produce sample");

			long perf1End = System.currentTimeMillis(), perf2Start = perf1End, perf1Start;
			double[] alt, sample;
			while (true) {
				try {
					perf1Start = perf1End;
					alt = agent.getAlt();
					perf1End = System.currentTimeMillis();
					perf1.trace((perf1End - perf1Start) + ",");

					perf1Start = perf1End;
					sample = sampleGen.generate(alt);
					perf1End = System.currentTimeMillis();
					perf1.trace((perf1End - perf1Start) + ",");

					perf1Start = perf1End;
					agent.putSample(packSample(alt[0], sample));
					perf1End = System.currentTimeMillis();
					perf1.trace((perf1End - perf1Start) + "\n");

					sampleCount++;
					if (sampleCount % RasConf.get().sampleCountStep == 0) {
						perf2.trace((System.currentTimeMillis() - perf2Start)
								+ "," + sampleCount + "\n");
					}
				} catch (InterruptedException e) {
					return;
				}
			}
		}
	}
}
