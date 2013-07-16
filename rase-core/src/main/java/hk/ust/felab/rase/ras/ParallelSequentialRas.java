package hk.ust.felab.rase.ras;

import hk.ust.felab.rase.Ras;
import hk.ust.felab.rase.SimHelper;
import hk.ust.felab.rase.SimOutput;

public class ParallelSequentialRas implements Ras {

	@Override
	public int ras(double[][] alts, double[] args, SimHelper simHelper)
			throws InterruptedException {
		// setup
		int k = alts.length;
		double alpha = args[0];
		double delta = args[1];
		int n0 = (int) args[2];
		double a = (-1.0) / delta * Math.log(2.0 * alpha / (k - 1) * 1.0);

		// sampling
		int[] altIDs = new int[k];
		for (int i = 0; i < n0; i++) {
			for (int j = 0; j < k; j++) {
				altIDs[j] = j;
			}
			simHelper.asyncSim(altIDs);
			simHelper.phantomSim(new int[] { k });
		}

		boolean[] surviving = new boolean[k];
		int survivingCount = k;
		for (int i = 0; i < k; i++) {
			surviving[i] = true;
		}
		int readyCount = 0;
		int[] count = new int[k];
		double[] sum = new double[k];
		double[] sumOfSquare = new double[k];
		for (int i = 0; i < k; i++) {
			count[i] = 0;
			sum[i] = 0;
			sumOfSquare[i] = 0;
		}
		double[] mean = new double[k];
		double[] S2 = new double[k];

		while (survivingCount > 1) {
			SimOutput simOutput = simHelper.takeSimOutput();
			while (simOutput.altID != k && !surviving[simOutput.altID]) {
				simOutput = simHelper.takeSimOutput();
			}
			int altID = simOutput.altID;

			if (altID != k) {
				count[altID]++;
				sum[altID] += simOutput.result[0];
				sumOfSquare[altID] += Math.pow(simOutput.result[0], 2);

				if (count[altID] < n0) {
					continue;
				}

				if (count[altID] == n0) {
					readyCount++;
				}

				mean[altID] = sum[altID] / count[altID];
				S2[altID] = (sumOfSquare[altID] - count[altID] * mean[altID]
						* mean[altID])
						/ (count[altID] - 1);

			} else {
				// get p
				// re-sampling
				altIDs = new int[survivingCount];
				int p = 0;
				for (int i = 0; i < k; i++) {
					if (surviving[i]) {
						altIDs[p++] = i;
					}
				}
				simHelper.asyncSim(altIDs);
				simHelper.phantomSim(new int[] { k });
			}

			// elimination
			if (readyCount > 2) {
				for (int i = 0; i < k; i++) {
					for (int j = 0; j < k; j++) {
						if (i != j && surviving[i] && surviving[j]) {
							if (mean[i] - mean[j] < Math.min(0, -a / delta
									* (S2[i] / count[i] + S2[j] / count[j])
									+ delta / 2)) {
								surviving[i] = false;
								survivingCount--;
							}
						}
					}
				}
			}
		}

		for (int i = 0; i < k; i++) {
			if (surviving[i]) {
				return i;
			}
		}
		return -1;
	}
}
