package hk.ust.felab.rase.ras;

import hk.ust.felab.rase.Ras;
import hk.ust.felab.rase.SimHelper;
import hk.ust.felab.rase.SimOutput;

public class AsymptoticParallelSequentialRas implements Ras {

	@Override
	public int ras(double[][] alts, double[] args, SimHelper simHelper)
			throws InterruptedException {
		// setup
		int k = alts.length;
		double alpha = args[0];
		double delta = args[1];
		int n0 = (int) args[2];
		double a = -1 * Math.log(2 * alpha / (k - 1));

		// first stage
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
		double[] mean = new double[k];
		double[] S2 = new double[k];

		for (SimOutput simOutput : simOutputs) {
			int i = simOutput.altID;
			count[i]++;
			sum[i] += simOutput.result[0];
			sumOfSquare[i] += Math.pow(simOutput.result[0], 2);
		}

		boolean[] surviving = new boolean[k];
		for (int i = 0; i < k; i++) {
			surviving[i] = true;
		}
		int survivingCount = k;

		altIDs = new int[survivingCount];
		int p = 0;
		for (int i = 0; i < k; i++) {
			if (surviving[i]) {
				altIDs[p++] = i;
			}
		}
		simHelper.asyncSim(altIDs);
		simHelper.phantomSim(new int[] { k });

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
			} else {
				// get p
				// re-sampling
				altIDs = new int[survivingCount];
				p = 0;
				for (int i = 0; i < k; i++) {
					if (surviving[i]) {
						altIDs[p++] = i;
					}
				}
				simHelper.asyncSim(altIDs);
				simHelper.phantomSim(new int[] { k });

				for (int i = 0; i < k; i++) {
					mean[i] = sum[i] / count[i];
					S2[i] = (sumOfSquare[i] - count[i] * mean[i] * mean[i])
							/ (count[i] - 1);
				}

				// elimination
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
