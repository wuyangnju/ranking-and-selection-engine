package hk.ust.felab.rase;

import hk.ust.felab.rase.master.impl.MasterImpl;
import hk.ust.felab.rase.ras.Ras;
import hk.ust.felab.rase.sim.Sim;
import hk.ust.felab.rase.util.ArraysUtil;
import hk.ust.felab.rase.util.RaseClassLoader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.LogLog;

public class Headquarters {
	public static final String RAS_PKG = "hk.ust.felab.rase.ras.impl";

	public static void main(String[] args) throws IOException,
			ClassNotFoundException, IllegalAccessException,
			InstantiationException, ExecutionException, InterruptedException {
		int argsI = 0;
		String altsStr = args[argsI++];
		String rasClassName = args[argsI++];
		String rasClassFile = args[argsI++];
		String rasArgsStr = args[argsI++];
		String simClassName = args[argsI++];
		String simClassFile = args[argsI++];
		String simArgsStr = args[argsI++];
		String repeatTimeStr = args[argsI++];
		String logDirStr = args[argsI++];

		double[][] alts = loadAltsFromFile(altsStr);

		RaseClassLoader classLoader = new RaseClassLoader(rasClassName,
				rasClassFile);
		classLoader.loadClass(rasClassName);
		Ras ras = (Ras) Class.forName(rasClassName, true, classLoader).newInstance();

		double[] rasArgs = ArraysUtil.stringToDoubleArray(rasArgsStr);

		int simThreadNum;
		if (argsI < args.length) {
			simThreadNum = Integer.parseInt(args[argsI++]);
		} else {
			simThreadNum = Runtime.getRuntime().availableProcessors();
		}

		List<Sim> sims = new LinkedList<Sim>();
		for (int i = 0; i < simThreadNum; i++) {
			classLoader = new RaseClassLoader(simClassName, simClassFile);
			classLoader.loadClass(simClassName);
			sims.add((Sim) Class.forName(simClassName, true, classLoader)
					.newInstance());
		}

		double[] simArgs = ArraysUtil.stringToDoubleArray(simArgsStr);

		LogLog.setQuietMode(true);

		int repeatTime = Integer.parseInt(repeatTimeStr);
		for (int i = 0; i < repeatTime; i++) {
			String logDir = logDirStr + "/" + i;
			int res = repeatOnce(alts, ras, rasArgs, sims, simArgs, logDir);
			System.out.println(i + ", " + res);
		}
	}

	private static int repeatOnce(double[][] alts, Ras ras, double[] rasArgs,
			List<Sim> sims, double[] simArgs, String logDir)
			throws IOException, ClassNotFoundException, IllegalAccessException,
			InstantiationException, ExecutionException, InterruptedException {

		System.setProperty("log.dir", logDir);
		PropertyConfigurator.configure(ClassLoader
				.getSystemResourceAsStream("log4j.properties"));

		MasterImpl master = new MasterImpl(alts, sims);
		int result = ras.ras(alts, rasArgs, master);
		master.stopSlaves();

		LogManager.shutdown();

		return result;
	}

	private static double[][] loadAltsFromFile(String file) throws IOException {
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
