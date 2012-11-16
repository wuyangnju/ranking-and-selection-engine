package hk.ust.felab.rase.slave;

import hk.ust.felab.rase.agent.AgentService;
import hk.ust.felab.rase.conf.ClusterConf;
import hk.ust.felab.rase.conf.RasConf;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class SlaveThread implements Runnable {

	private transient final Logger log = Logger.getLogger(getClass());

	private transient final Logger perf1;
	/**
	 * We can't log the sample # at spesific timestamp. We can only log the
	 * timestamp when sample # reach some level, then plot sample #
	 * culmulatively against time.
	 * 
	 * @see src/main/resources/log4j.properties
	 */
	private transient final Logger perf2;

	private SampleGenerator sampleGenerator;
	private AgentService agentService;
	private long sampleCount = 0;

	public SlaveThread(int slaveId, int subStreamId, AgentService agentService)
			throws IllegalArgumentException, SecurityException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException,
			ClassNotFoundException, IOException {
		this.sampleGenerator = (SampleGenerator) Class
				.forName(
						getClass().getPackage().getName() + ".impl."
								+ RasConf.get().sampleGenerator)
				.getConstructor(Integer.class).newInstance(subStreamId);
		this.agentService = agentService;

		perf1 = Logger.getLogger("slave.perf1." + slaveId);
		perf1.addAppender(new FileAppender(new PatternLayout("%m"),
				ClusterConf.LOG_DIR + "/slave_perf1_" + slaveId + ".csv",
				false, true, 16192));

		perf2 = Logger.getLogger("slave.perf2." + slaveId);
		perf2.addAppender(new FileAppender(new PatternLayout("%m"),
				ClusterConf.LOG_DIR + "/slave_perf2_" + slaveId + ".csv",
				false, true, 16192));
	}

	@Override
	public void run() {
		perf1.trace(System.currentTimeMillis() + "\n");
		double[] altSystem;
		double[] sampleAndSimTime;
		while (true) {
			altSystem = null;
			try {
				altSystem = agentService.getAlt();
				perf1.trace(System.currentTimeMillis() + ",");
				sampleAndSimTime = sampleGenerator.generate(altSystem);
				perf1.trace(System.currentTimeMillis() + ",");
				agentService.putSample((int) altSystem[0], sampleAndSimTime[0],
						sampleAndSimTime[1],
						(int) altSystem[altSystem.length - 1]);
			} catch (InterruptedException e) {
				log.warn(e, e);
				continue;
			} finally {
				perf1.trace(System.currentTimeMillis() + "\n");
			}
			sampleCount++;
			if (sampleCount % RasConf.get().sampleCountStep == 0) {
				perf2.trace(System.currentTimeMillis() + "," + sampleCount
						+ "\n");
			}
		}
	}

}
