package hk.ust.felab.rase.conf;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;

public class ConfLoader {

	public static void loadConf(String[] args) throws Exception {
		Properties rasConf = new Properties();
		rasConf.load(new FileInputStream(args[0]));

		RasConf.get().sampleGenerator = rasConf.getProperty("sampleGenerator");

		RasConf.get().sampleCountStep = Integer.parseInt(rasConf
				.getProperty("sampleCountStep"));

		ArrayList<double[]> altsArgs = new ArrayList<double[]>();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[1])));
		String line = null;
		int len = 0;
		while ((line = br.readLine()) != null) {
			double[] altArgs = stringToAltArgs(line);
			if (altArgs.length < len) {
				System.err.println("arg count not equal\nlast line is empty?");
				System.exit(-1);
			}
			len = altArgs.length;
			altsArgs.add(altArgs);
		}
		br.close();

		RasConf.get().alts = altsArgs.toArray(new double[][] {});
		RasConf.get().k = altsArgs.size();

		RasConf.get().min = Boolean.parseBoolean(rasConf.getProperty("min"));
		RasConf.get().alpha = Double.parseDouble(rasConf.getProperty("alpha"));
		RasConf.get().delta = Double.parseDouble(rasConf.getProperty("delta"));
		RasConf.get().a = (-1.0)
				/ RasConf.get().delta
				* Math.log(2.0 * RasConf.get().alpha / (RasConf.get().k - 1)
						* 1.0);
		RasConf.get().b = RasConf.get().delta / 2.0;
		RasConf.get().n0 = Integer.parseInt(rasConf.getProperty("n0"));
		RasConf.get().fix = Boolean.parseBoolean(rasConf.getProperty("fix"));
		RasConf.get().trialCount = Integer.parseInt(rasConf
				.getProperty("trialCount"));

		ClusterConf.get().masterAltBufSize = 8096;
		ClusterConf.get().masterSampleBufSize = 8096;
		ClusterConf.get().slaveIdOffset = 0;
		ClusterConf.get().slaveLocalCount = 1;
	}

	private static double[] stringToAltArgs(String line) {
		String[] argsStr = line.split(" ");
		double[] args = new double[argsStr.length];
		for (int i = 0; i < argsStr.length; i++) {
			args[i] = Double.parseDouble(argsStr[i]);
		}
		return args;
	}
}
