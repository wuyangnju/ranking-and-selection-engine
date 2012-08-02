package hk.ust.felab.rase.slave;

import hk.ust.felab.rase.agent.AgentService;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SlaveThread implements Runnable {

	private transient final Log log = LogFactory.getLog(this.getClass());

	private transient final Log perf1 = LogFactory.getLog("slave.perf1");
	/**
	 * We can't log the sample # at spesific timestamp. We can only log the
	 * timestamp when sample # reach some level, then plot sample #
	 * culmulatively against time.
	 * 
	 * @see src/main/resources/log4j.properties
	 */
	private transient final Log perf2 = LogFactory.getLog("slave.perf2");

	private SampleGenerator sampleGenerator;
	private AgentService agentService;
	private int sampleCountStep;
	private long sampleCount = 0;

	public SlaveThread(int slaveId, String sampleGenerator,
			int sampleCountStep, AgentService agentService)
			throws IllegalArgumentException, SecurityException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException,
			ClassNotFoundException {
		this.sampleGenerator = (SampleGenerator) Class.forName(sampleGenerator)
				.getConstructor(Integer.class).newInstance(slaveId);
		this.sampleCountStep = sampleCountStep;
		this.agentService = agentService;
	}

	@Override
	public void run() {
		while (true) {
			perf1.trace(System.currentTimeMillis() + ",");
			double[] altSystem = null;
			try {
				altSystem = agentService.getAlt();
			} catch (InterruptedException e) {
				log.warn(e, e);
				continue;
			}
			perf1.trace(System.currentTimeMillis() + ",");
			double sample = sampleGenerator.generate(altSystem)[0];
			perf1.trace(System.currentTimeMillis() + "\n");
			try {
				agentService.putSample((long) altSystem[0], sample);
			} catch (InterruptedException e) {
				log.warn(e, e);
				continue;
			}
			sampleCount++;
			if (sampleCount % sampleCountStep == 0) {
				perf2.trace(System.currentTimeMillis() + "," + sampleCount
						+ "\n");
			}
		}
	}
}
