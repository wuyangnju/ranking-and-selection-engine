package hk.ust.felab.rase.ras;

import hk.ust.felab.rase.Ras;
import hk.ust.felab.rase.SimHelper;
import hk.ust.felab.rase.SimOutput;

import java.util.LinkedList;
import java.util.List;

public class VectorFillingKnRas implements Ras {

	@Override
	public int ras(double[][] alts, double[] args, SimHelper simHelper)
			throws InterruptedException {
		// setup
		int k = alts.length;
		double alpha = args[0];
		double delta = args[1];
		int n0 = (int) args[2];
		double h2 = (n0 - 1)
				* (Math.pow(2 * alpha / (k - 1), -2.0 / (n0 - 1)) - 1);

		@SuppressWarnings("unchecked")
		List<Double>[] vectors = (List<Double>[]) new List[k];
		for (int i = 0; i < k; i++) {
			vectors[i] = new LinkedList<Double>();
		}

		// first stage
		int[] altIDs = new int[k * n0];
		for (int i = 0; i < n0; i++) {
			for (int j = 0; j < k; j++) {
				altIDs[i * k + j] = j;
			}
		}
		SimOutput[] simOutputs = simHelper.sim(altIDs);

		// variance estimation
		for (SimOutput simOutput : simOutputs) {
			int i = simOutput.altID;
			vectors[i].add(simOutput.result[0]);
		}
		double[] count = new double[k];
		double[] sum = new double[k];
		double[] mean = new double[k];
		double[][] sij2 = new double[k][k];
		for (int i = 0; i < k; i++) {
			count[i] = n0;
			sum[i] = 0;
			for (int j = 0; j < n0; j++) {
				sum[i] += vectors[i].get(j);
			}
			mean[i] = sum[i] / n0;
		}
		for (int i = 0; i < k; i++) {
			for (int j = 0; j < k; j++) {
				if (i != j) {
					double tmp = 0;
					for (int l = 0; l < n0; l++) {
						tmp += Math.pow(vectors[i].get(l) - vectors[j].get(l)
								- mean[i] + mean[j], 2);
					}
					sij2[i][j] = tmp / (n0 - 1);
				}
			}
		}
		for (int i = 0; i < k; i++) {
			vectors[i].clear();
		}

		// second stage
		boolean[] surviving = new boolean[k];
		for (int i = 0; i < k; i++) {
			surviving[i] = true;
		}
		int survivingCount = k;
		int r = n0;

		for (int j = 0; j < k; j++) {
			if (!surviving[j]) {
				continue;
			}
			for (int i = 0; i < k; i++) {
				if (i != j && surviving[i] && surviving[j]) {
					if (mean[i] - mean[j] < Math.min(0, -h2 * sij2[i][j] / 2
							/ r / delta + delta / 2)) {
						surviving[i] = false;
						survivingCount--;
						continue;
					}
				}
			}
		}

		while (survivingCount > 1) {
			// sim
			altIDs = new int[survivingCount];
			int p = 0;
			for (int i = 0; i < k; i++) {
				if (surviving[i]) {
					altIDs[p++] = i;
				}
			}
			simHelper.asyncSim(altIDs);
			// take output
			SimOutput simOutput = simHelper.takeSimOutput();
			while (!surviving[simOutput.altID]) {
				simOutput = simHelper.takeSimOutput();
			}
			int altID = simOutput.altID;
			vectors[altID].add(simOutput.result[0]);
			
			boolean ready = false;
			if (vectors[altID].size() == 1) {
				ready = true;
				for (int i = 0; i < k; i++) {
					if (surviving[i]) {
						if (vectors[i].size() == 0) {
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
						sum[i] += vectors[i].remove(0);
						count[i]++;
						mean[i] = sum[i] / count[i];
					}
				}

				for (int j = 0; j < k; j++) {
					if (!surviving[j]) {
						continue;
					}
					for (int i = 0; i < k; i++) {
						if (i != j && surviving[i] && surviving[j]) {
							if (mean[i] - mean[j] < Math.min(0, -h2 * sij2[i][j] / 2
									/ r / delta + delta / 2)) {
								surviving[i] = false;
								survivingCount--;
								continue;
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
