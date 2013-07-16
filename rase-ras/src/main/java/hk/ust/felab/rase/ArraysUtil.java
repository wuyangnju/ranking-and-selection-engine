package hk.ust.felab.rase;

public class ArraysUtil {
	
	public static String toString(double[] a) {
		if (a == null)
			return "null";
		int iMax = a.length - 1;
		if (iMax == -1)
			return "";

		StringBuilder b = new StringBuilder();
		for (int i = 0;; i++) {
			b.append(a[i]);
			if (i == iMax)
				return b.toString();
			b.append(" ");
		}
	}

	public static String toString(long[] a) {
		if (a == null)
			return "null";
		int iMax = a.length - 1;
		if (iMax == -1)
			return "";

		StringBuilder b = new StringBuilder();
		for (int i = 0;; i++) {
			b.append(a[i]);
			if (i == iMax)
				return b.toString();
			b.append(" ");
		}
	}
	
}
