package hk.ust.felab.rase.ras;

import hk.ust.felab.rase.Ras;
import hk.ust.felab.rase.SimHelper;
import hk.ust.felab.rase.SimOutput;

public class RinottRas implements Ras {

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

		int[] altIDs = new int[k * n0];
		for (int i = 0; i < n0; i++) {
			for (int j = 0; j < k; j++) {
				altIDs[i * k + j] = j;
			}
		}

		SimOutput[] simOutputs = simHelper.sim(altIDs);

		int[] count = new int[k];
		double[] sum = new double[k];
		double[] sumOfSquare = new double[k];
		for (int i = 0; i < k; i++) {
			count[i] = n0;
			sum[i] = 0;
			sumOfSquare[i] = 0;
		}
		for (SimOutput simOutput : simOutputs) {
			int altID = simOutput.altID;
			sum[altID] += simOutput.result[0];
			sumOfSquare[altID] += Math.pow(simOutput.result[0], 2);
		}
		double[] mean = new double[k];
		double[] S2 = new double[k];
		for (int i = 0; i < k; i++) {
			mean[i] = sum[i] / count[i];
			S2[i] = (sumOfSquare[i] - count[i] * mean[i] * mean[i])
					/ (count[i] - 1);
		}

		int[] N = new int[k];
		int maxN = Integer.MIN_VALUE;
		for (int i = 0; i < k; i++) {
			N[i] = Math.max(n0, (int) Math.ceil(h * h * S2[i] / delta / delta));
			if (maxN < N[i]) {
				maxN = N[i];
			}
		}

		// stopping rule
		if (n0 < maxN) {
			int[] repeat = new int[k];
			int extraCount = 0;
			for (int i = 0; i < k; i++) {
				repeat[i] = Math.max(N[i] - n0, 0);
				extraCount += repeat[i];
			}
			altIDs = new int[extraCount];
			int p = 0;
			for (int i = 0; i < k; i++) {
				for (int j = 0; j < repeat[i]; j++) {
					altIDs[p++] = i;
				}
			}

			simOutputs = simHelper.sim(altIDs);

			for (SimOutput simOutput : simOutputs) {
				int altID = simOutput.altID;
				count[altID]++;
				sum[altID] += simOutput.result[0];
			}
		}
		double maxMean = Integer.MIN_VALUE;
		int maxMeanI = -1;
		for (int i = 0; i < k; i++) {
			mean[i] = sum[i] / count[i];
			if (maxMean < mean[i]) {
				maxMean = mean[i];
				maxMeanI = i;
			}
		}
		return maxMeanI;
	}

}
