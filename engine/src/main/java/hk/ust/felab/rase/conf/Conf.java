package hk.ust.felab.rase.conf;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;

public class Conf {
	private static Conf instance;

	private Conf() {

	}

	private String sampleGenerator;

	private int sampleCountStep;

	private double[][] alts;

	/**
	 * number of alternatives(alts)
	 */
	private int k;

	private boolean min;

	private double alpha;

	private double delta;

	/**
	 * a = (-1.0) / delta * Math.log(2 - 2 * Math.pow((1 - alpha), (1.0 / (k -
	 * 1) * 1.0)));
	 */
	private double a;

	/**
	 * b = delta / 2.0;
	 */
	private double b;

	private int n0;

	private boolean fix;

	private int trialCount;

	public String getSampleGenerator() {
		return sampleGenerator;
	}

	public int getSampleCountStep() {
		return sampleCountStep;
	}

	public double[][] getAlts() {
		return alts;
	}

	public int getK() {
		return k;
	}

	public boolean isMin() {
		return min;
	}

	public double getAlpha() {
		return alpha;
	}

	public double getDelta() {
		return delta;
	}

	public double getA() {
		return a;
	}

	public double getB() {
		return b;
	}

	public int getN0() {
		return n0;
	}

	public boolean isFix() {
		return fix;
	}

	public int getRepeatTime() {
		return trialCount;
	}

	public static Conf current() {
		return instance;
	}

	public static void loadFromCmdArgs(String[] args)
			throws FileNotFoundException, IOException {
		instance = new Conf();

		Properties rasConf = new Properties();
		rasConf.load(new FileInputStream(args[0]));

		instance.sampleGenerator = rasConf.getProperty("sampleGenerator");

		instance.sampleCountStep = Integer.parseInt(rasConf
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
		instance.alts = altsArgs.toArray(new double[][] {});

		instance.k = altsArgs.size();

		instance.min = Boolean.parseBoolean(rasConf.getProperty("min"));

		instance.alpha = Double.parseDouble(rasConf.getProperty("alpha"));

		instance.delta = Double.parseDouble(rasConf.getProperty("delta"));

		instance.a = (-1.0) / instance.delta
				* Math.log(2.0 * instance.alpha / (instance.k - 1) * 1.0);

		instance.b = instance.delta / 2.0;

		instance.n0 = Integer.parseInt(rasConf.getProperty("n0"));

		instance.fix = Boolean.parseBoolean(rasConf.getProperty("fix"));

		instance.trialCount = Integer.parseInt(rasConf
				.getProperty("trialCount"));
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
