package hk.ust.felab.rase.ras;

import hk.ust.felab.rase.Ras;
import hk.ust.felab.rase.SimHelper;
import hk.ust.felab.rase.SimOutput;

public class AsyncRinottRas implements Ras {

	/**
	 * paper: "Recent Advances in Ranking and Selection" author: Seong-Hee Kim,
	 * Barry L. Nelson
	 */
	@Override
	public int ras(double[][] alts, double[] args, SimHelper simHelper)
			throws InterruptedException {

		// setup
		int k = alts.length;
		// double alpha = args[0];
		double delta = args[1];
		int n0 = (int) args[2];

		// init
		double h = args[3];

		int[] altIds = new int[k * n0];
		for (int i = 0; i < n0; i++) {
			for (int j = 0; j < k; j++) {
				altIds[i * k + j] = j;
			}
		}
		simHelper.asyncSim(altIds);
		int samplingCount = k * n0;

		int[] count = new int[k];
		double[] sum = new double[k];
		double[] sumOfSquare = new double[k];
		double mean, S2;
		int N;

		while (samplingCount > 0) {
			SimOutput simOutput = simHelper.takeSimOutput();
			int i = simOutput.altID;
			count[i] += 1;
			sum[i] += simOutput.result[0];
			sumOfSquare[i] += Math.pow(simOutput.result[0], 2);
			samplingCount--;
			if (count[i] == n0) {
				mean = sum[i] / count[i];
				S2 = (sumOfSquare[i] - count[i] * mean * mean) / (count[i] - 1);
				N = Math.max(n0, (int) Math.ceil(h * h * S2 / delta / delta));
				if (N > n0) {
					int[] extraAltIds = new int[N - n0];
					for (int j = 0; j < extraAltIds.length; j++) {
						extraAltIds[j] = i;
					}
					simHelper.asyncSim(extraAltIds);
					samplingCount += extraAltIds.length;
				}
			}
		}

		double maxMean = Integer.MIN_VALUE;
		int maxMeanI = -1;
		for (int i = 0; i < k; i++) {
			mean = sum[i] / count[i];
			if (maxMean < mean) {
				maxMean = mean;
				maxMeanI = i;
			}
		}
		return maxMeanI;
	}

}
