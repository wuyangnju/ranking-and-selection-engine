package hk.ust.felab.rase.ras;

import hk.ust.felab.rase.Ras;
import hk.ust.felab.rase.SimHelper;
import hk.ust.felab.rase.SimOutput;

import java.util.LinkedList;
import java.util.List;

public class TableFillingRas implements Ras {

	@Override
	public int ras(double[][] alts, double[] args, SimHelper simHelper)
			throws InterruptedException {
		// setup
		int k = alts.length;
		double alpha = args[0];
		double delta = args[1];
		int n0 = (int) args[2];
		double h2 = (n0 - 1)
				* (Math.pow(2 * alpha / (k - 1), -2 / (n0 - 1)) - 1);

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

		// second stage
		int r = n0;
		boolean[] surviving = new boolean[k];
		int survivingCount = k;
		for (int i = 0; i < k; i++) {
			surviving[i] = true;
		}
		for (int i = 0; i < k; i++) {
			for (int j = 0; j < k; j++) {
				if (i != j && surviving[i] && surviving[j]) {
					if (mean[i] - mean[j] < Math.min(0, -h2 / 2 / delta
							* (S2[i] + S2[j] + delta / 2 * r))) {
						surviving[i] = false;
						survivingCount--;
					}
				}
			}
		}

		@SuppressWarnings("unchecked")
		List<Double>[] tableToFill = (List<Double>[]) new List[k];
		for (int i = 0; i < k; i++) {
			tableToFill[i] = new LinkedList<Double>();
		}
		while (survivingCount > 1) {
			altIDs = new int[survivingCount];
			int p = 0;
			for (int i = 0; i < k; i++) {
				if (surviving[i]) {
					altIDs[p++] = i;
				}
			}
			simHelper.sim(altIDs);
			SimOutput simOutput = simHelper.takeSimOutput();
			while (!surviving[simOutput.altID]) {
				simOutput = simHelper.takeSimOutput();
			}
			int altID = simOutput.altID;
			tableToFill[altID].add(simOutput.result[0]);
			boolean ready = false;
			if (tableToFill[altID].size() == 1) {
				ready = true;
				for (int i = 0; i < k; i++) {
					if (surviving[i]) {
						if (tableToFill[i].size() == 0) {
							ready = false;
							break;
						}
					}
				}
			}
			if (ready) {
				r++;
				for (int i = 0; i < k; i++) {
					if (surviving[i]) {
						sum[i] += tableToFill[i].remove(0);
						count[i]++;
						mean[i] = sum[i] / count[i];
					}
				}
				for (int i = 0; i < k; i++) {
					for (int j = 0; j < k; j++) {
						if (i != j && surviving[i] && surviving[j]) {
							if (mean[i] - mean[j] < Math.min(0, -h2 / 2 / delta
									* (S2[i] + S2[j] + delta / 2 * r))) {
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
