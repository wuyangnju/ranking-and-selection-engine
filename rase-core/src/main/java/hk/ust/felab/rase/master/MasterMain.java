package hk.ust.felab.rase.master;

import hk.ust.felab.rase.Ras;
import hk.ust.felab.rase.Sim;
import hk.ust.felab.rase.sim.MatlabSim;
import hk.ust.felab.rase.slave.Slave;
import hk.ust.felab.rase.util.ArraysUtil;
import hk.ust.felab.rase.util.RaseClassLoader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.LogLog;

public class MasterMain {

	public static void main(String[] args) throws IOException,
			ClassNotFoundException, IllegalAccessException,
			InstantiationException, ExecutionException, InterruptedException,
			SecurityException, NoSuchMethodException {

		/*
		 * start parsing command line args
		 */
		int argsI = 0;

		// distributed?
		int port = Integer.parseInt(args[argsI++]);
		int slaveCount = Integer.parseInt(args[argsI++]);

		// ranking-and-selection
		String rasClass = args[argsI++];
		double[] rasArgs = ArraysUtil.stringToDoubleArray(args[argsI++]);

		// alternatives
		double[][] alts = loadAltsFromFile(args[argsI++]);

		// simulation
		String simType = args[argsI++]; // java or matlab
		String simClass = args[argsI++];
		double[] simArgs = ArraysUtil.stringToDoubleArray(args[argsI++]);
		int simThreadNum = Integer.parseInt(args[argsI++]);

		// repeat r&s XX times
		int repeatTime = Integer.parseInt(args[argsI++]);

		// log directory
		String logDir = args[argsI++];

		/*
		 * start transaction scripts
		 */
		System.setProperty("log.dir", logDir + "/0");
		PropertyConfigurator.configure(ClassLoader
				.getSystemResourceAsStream("log4j.properties"));

		final MasterCore masterCore = new MasterCore(alts, simArgs, null);

		SimIoServer simIoServer = null;
		if (port > 0) { // -1 for single machine only
			simIoServer = new SimIoServer(masterCore, slaveCount);
			simIoServer.start(port);
		}

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
		ExecutorService executorService = Executors
				.newFixedThreadPool(sims.length);
		for (Sim sim : sims) {
			executorService.execute(new Slave(sim, masterCore));
		}

		LogLog.setQuietMode(true);

		for (int i = 0; i < repeatTime; i++) {
			long t1 = System.currentTimeMillis();
			String res = repeatOnce(masterCore, rasClass, alts, rasArgs,
					simArgs, logDir + "/" + i);
			long t2 = System.currentTimeMillis();
			System.out.println(i + ", " + res + ", " + (t2 - t1) + "ms");
		}

		if (simIoServer != null) {
			simIoServer.setRasDone();
			simIoServer.getCountDownLatch().await();
			simIoServer.shutdown();
		}

		executorService.shutdownNow();
	}

	private static String repeatOnce(MasterCore masterCore,
			final String rasClass, final double[][] alts,
			final double[] rasArgs, double[] simArgs, String logDir)
			throws IOException, ClassNotFoundException, IllegalAccessException,
			InstantiationException, ExecutionException, InterruptedException,
			SecurityException, NoSuchMethodException {

		System.setProperty("log.dir", logDir);
		PropertyConfigurator.configure(ClassLoader
				.getSystemResourceAsStream("log4j.properties"));

		masterCore.prepare();

		Ras ras = (Ras) Class.forName(rasClass).newInstance();
		int result = ras.ras(alts, rasArgs, masterCore);

		LogManager.shutdown();

		return result + masterCore.report();
	}

	private static double[][] loadAltsFromFile(String file) throws IOException {
		if (file.isEmpty()) {
			return null;
		}
		ArrayList<double[]> altsArgs = new ArrayList<double[]>();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(file)));
		String line = null;
		int len = 0;
		while ((line = br.readLine()) != null) {
			double[] altArgs = ArraysUtil.stringToDoubleArray(line);
			if (altArgs.length < len) {
				System.err.println("arg count not equal\nlast line is empty?");
				System.exit(-1);
			}
			len = altArgs.length;
			altsArgs.add(altArgs);
		}
		br.close();
		return altsArgs.toArray(new double[][] {});
	}

}
