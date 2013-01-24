package hk.ust.felab.rase.slave;

import hk.ust.felab.rase.sim.SampleGen;
import hk.ust.felab.rase.sim.impl.ThreeStageSim;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class TestThreeStage {

	private static double[] stringToAltArgs(String line) {
		String[] argsStr = line.split(" ");
		double[] args = new double[argsStr.length];
		for (int i = 0; i < argsStr.length; i++) {
			args[i] = Double.parseDouble(argsStr[i]);
		}
		return args;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		ArrayList<double[]> altsArgs = new ArrayList<double[]>();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(
						"src/main/op-scripts/conf/three_stage.5.alts")));
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

		SampleGen sampleGen = new ThreeStageSim(0);
		for (double[] altArg : altsArgs) {
			for (int i = 0; i < 10; i++) {
				System.out.println(Arrays.toString(sampleGen.generate(altArg)));
			}
		}
	}

}
