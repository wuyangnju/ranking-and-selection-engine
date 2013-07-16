package hk.ust.felab.rase.agent;

import hk.ust.felab.rase.Sim;
import hk.ust.felab.rase.sim.MatlabSim;
import hk.ust.felab.rase.slave.Slave;
import hk.ust.felab.rase.util.ArraysUtil;
import hk.ust.felab.rase.util.RaseClassLoader;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.LogLog;

public class AgentMain {

	/**
	 * @param args
	 * @throws InterruptedException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public static void main(String[] args) throws InterruptedException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException, SecurityException, NoSuchMethodException {

		/*
		 * start parsing command line args
		 */
		int argsI = 0;

		// master
		String server = args[argsI++];
		int port = Integer.parseInt(args[argsI++]);

		// simulation
		String simType = args[argsI++]; // java or matlab
		String simClass = args[argsI++];
		double[] simArgs = ArraysUtil.stringToDoubleArray(args[argsI++]);
		int simThreadNum = Integer.parseInt(args[argsI++]);

		// log directory
		String logDir = args[argsI++];

		// queue management policy
		int simInputS2 = Integer.parseInt(args[argsI++]);
		int simInputS1 = Integer.parseInt(args[argsI++]);
		int simOutputS2 = Integer.parseInt(args[argsI++]);

		Sim[] sims = new Sim[simThreadNum];
		if ("java".equals(simType)) {
			RaseClassLoader.addInterface(Sim.class.getName());
			for (int i = 0; i < simThreadNum; i++) {
				RaseClassLoader classLoader = new RaseClassLoader();
				classLoader.loadClass(simClass);
				sims[i] = (Sim) Class.forName(simClass, true, classLoader)
						.newInstance();
			}
		} else if ("matlab".equals(simType)) {
			String[] fields = simClass.split(" ");
			String matlabClassStr = fields[0];
			String matlabMethodStr = fields[1];
			Method matlabMethod = Class.forName(matlabClassStr).getMethod(
					matlabMethodStr, int.class, Object[].class);
			for (int i = 0; i < simThreadNum; i++) {
				Object matlabObject = Class.forName(matlabClassStr)
						.newInstance();
				sims[i] = new MatlabSim(matlabObject, matlabMethod);
			}
		} else {
			System.err.println("simType error");
			System.exit(-1);
		}

		/*
		 * start transaction scripts
		 */
		LogLog.setQuietMode(true);
		System.setProperty("log.dir", logDir);
		PropertyConfigurator.configure(ClassLoader
				.getSystemResourceAsStream("log4j.properties"));
		
		AgentCore slaveCore = new AgentCore(simArgs, simInputS2, simInputS1,
				simOutputS2);

		final SimInputClient simInputClient = new SimInputClient(slaveCore);
		simInputClient.start(server, port);
		final SimOutputClient simOutputClient = new SimOutputClient(slaveCore);
		simOutputClient.start(server, port);

		ExecutorService executorService = Executors
				.newFixedThreadPool(sims.length + 2);
		// they should be like execute(new XxThread(client, slaveCore));
		executorService.execute(slaveCore.takeSimInputThread(simInputClient));
		executorService.execute(slaveCore.putSimOutputThread(simOutputClient));
		for (Sim sim : sims) {
			executorService.execute(new Slave(sim, slaveCore));
		}
		
		SimFinishClient simFinishClient = new SimFinishClient();
		simFinishClient.start(server, port);
		simFinishClient.isFinish();
		SimFinishClient.getCountDownLatch().await();
		
		LogManager.shutdown();

		simInputClient.shutdown();
		simOutputClient.shutdown();
		simFinishClient.shutdown();
		executorService.shutdownNow();
	}

}
