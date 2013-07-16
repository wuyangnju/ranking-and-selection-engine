package hk.ust.felab.rase.util;

public class ArraysUtil {
	public static double[] stringToDoubleArray(String line) {
		if (line.isEmpty()) {
			return new double[] {};
		}
		String[] argsStr = line.split(" ");
		double[] args = new double[argsStr.length];
		for (int i = 0; i < argsStr.length; i++) {
			args[i] = Double.parseDouble(argsStr[i]);
		}
		return args;
	}
}
