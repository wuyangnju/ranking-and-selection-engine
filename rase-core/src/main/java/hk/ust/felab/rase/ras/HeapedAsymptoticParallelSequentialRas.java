package hk.ust.felab.rase.ras;

import hk.ust.felab.rase.Ras;
import hk.ust.felab.rase.SimHelper;
import hk.ust.felab.rase.SimOutput;
import hk.ust.felab.rase.util.Indexed;
import hk.ust.felab.rase.util.IndexedPriorityQueue;

import java.util.Comparator;

import org.apache.log4j.Logger;

class Alt implements Indexed {
	private Conf conf;

	private int id;

	private boolean surviving = true;

	public Alt prev, next;

	private double[] args;
	private double[] data;
	private int[] indexInPriorityQueue = new int[] { -1, -1, -1 };

	public static double minThetaOverSqrtY = Double.MAX_VALUE;

	public Alt(int id, double[] altArgs, Conf conf) {
		this.id = id;
		this.args = altArgs;
		this.conf = conf;
		// data[0]: count, number of samples
		// data[1]: sum
		// data[2]: sumofsquare
		// data[3]: fixedVar
		// data[4]: sumOfSimTime
		// data[5]: fixedAvgSimTime
		data = new double[] { 0, 0, 0, 0, 0, 0 };
	}

	public int getId() {
		return id;
	}

	public boolean isSurviving() {
		return surviving;
	}

	public void setSurviving(boolean surviving) {
		this.surviving = surviving;
	}

	private void update(double sample, long simTime) {
		data[2] += sample * sample;
		data[4] += simTime;
	}

	private void fix() {
		data[3] = (data[2] - data[1] * data[1] / data[0]) / (data[0] - 1);
		data[5] = data[4] / data[0];
		if (thetaOverY() < minThetaOverSqrtY) {
			minThetaOverSqrtY = thetaOverY();
		}
	}

	public long addSample(double sample, long simTime) {
		data[0]++;
		data[1] += sample;
		if (conf.fix) {
			if (data[0] < conf.n0) {
				update(sample, simTime);
			} else if (data[0] == conf.n0) {
				update(sample, simTime);
				fix();
			}
		} else {
			update(sample, simTime);
		}
		return (long) data[0];
	}

	/**
	 * @return {id, args[0],args[1]...}
	 */
	public double[] argSnapshot() {
		double[] snapshot = new double[args.length + 1];
		snapshot[0] = id;
		System.arraycopy(args, 0, snapshot, 1, args.length);
		return snapshot;
	}

	public int getIndex(int keyToIndex) {
		return indexInPriorityQueue[keyToIndex];
	}

	public void setIndex(int keyToIndex, int index) {
		indexInPriorityQueue[keyToIndex] = index;
	}

	public double thetaOverY() {
		return Math.sqrt(var() / simTimeMean());
	}

	public long num() {
		return (long) data[0];
	}

	public double mean() {
		return data[1] / data[0];
	}

	public double simTimeMean() {
		if (conf.fix) {
			return data[5];
		} else {
			return data[4] / data[0];
		}
	}

	public double var() {
		if (conf.fix) {
			return data[3];
		} else {
			return (data[2] - data[1] * data[1] / data[0]) / (data[0] - 1);
		}
	}

	public double key1() {
		return var() / data[0];
	}

	public double key2() {
		return mean() - conf.a * var() / data[0];
	}

	public double key3() {
		return mean() + conf.a * var() / data[0];
	}

	public boolean lessThan(Alt o) {
		return key3() < o.key2() + conf.b;
	}

}

class Conf {
	public int k;
	public boolean min;
	public double alpha;
	public double delta;
	/**
	 * a = (-1.0) / delta * Math.log(2 - 2 * Math.pow((1 - alpha), (1.0 / (k -
	 * 1) * 1.0)));
	 */
	public double a;
	/**
	 * b = delta / 2.0;
	 */
	public double b;
	public int n0;
	public boolean fix;
}

public class HeapedAsymptoticParallelSequentialRas implements Ras {

	private transient final Logger siftLog = Logger.getLogger(getClass());

	private transient final Logger elimationLog = Logger.getLogger(getClass());

	private void remove(Alt alt) {
		if (!alt.isSurviving())
			return;
		alt.setSurviving(false);
		// delete current node from a doubly linked list
		alt.prev.next = alt.next;
		alt.next.prev = alt.prev;
		eliminatedCount++;

		siftLog.trace("," + alts1.myRemove(alt) + "," + alts2.myRemove(alt)
				+ "," + alts3.myRemove(alt));
		elimationLog.trace(sampleCount + "," + alt.getId() + "\n");
	}

	private void adoptSample(Alt alt, SimOutput simOutput) {
		double sample = simOutput.result[0];
		long simTime = 0;
		int[] sift = new int[] { 0, 0, 0 };
		double k1, k2, k3;

		if (alt.num() > conf.n0 - 1) {
			k1 = alt.key1();
			k2 = alt.key2();
			k3 = alt.key3();
			alt.addSample(sample, simTime);
			if (alt.key1() < k1) {
				sift[0] += alts1.siftUp(alt);
			} else {
				sift[0] += alts1.siftDown(alt);
			}
			if (alt.key2() > k2) {
				sift[1] += alts2.siftUp(alt);
			} else {
				sift[1] += alts2.siftDown(alt);
			}
			if (alt.key3() < k3) {
				sift[2] += alts3.siftUp(alt);
			} else {
				sift[2] += alts3.siftDown(alt);
			}
		} else if (alt.num() == conf.n0 - 1) {
			alt.addSample(sample, simTime);
			sift[0] += alts1.myOffer(alt);
			sift[1] += alts2.myOffer(alt);
			sift[2] += alts3.myOffer(alt);
			secondStageCount++;
		} else if (alt.num() < conf.n0 - 1) {
			alt.addSample(sample, simTime);
		}

		siftLog.trace(sift[0] + "," + sift[1] + "," + sift[2]);
	}

	private void checkElimination1(Alt alt) {
		if (alts1.size() < 2) {
			return;
		}
		// eliminate by mean immediately
		Alt alt1 = alts1.peekExcept(alt);
		while ((alt.key1() + alt1.key1()) < (conf.b / conf.a)) {
			if (conf.min ? (alt.mean() > alt1.mean()) : (alt.mean() < alt1
					.mean())) {
				remove(alt);
				return;
			} else {
				remove(alt1);
				if (alts1.size() < 2) {
					return;
				}
				alt1 = alts1.peekExcept(alt);
			}
		}
	}

	private void checkElimination2(Alt alt0) {
		if (alts1.size() < 2) {
			return;
		}
		if (conf.min) {
			// eliminate others
			Alt alt1 = alts2.peekExcept(alt0);
			while (alt0.lessThan(alt1)) {
				remove(alt1);
				if (alts1.size() < 2) {
					return;
				}
				alt1 = alts2.peekExcept(alt0);
			}

			if (alts1.size() < 2) {
				return;
			}
			// survive itself
			alt1 = alts3.peekExcept(alt0);
			if (alt1.lessThan(alt0)) {
				remove(alt0);
				return;
			}
		} else {
			// eliminate others
			Alt alt1 = alts3.peekExcept(alt0);
			while (alt1.lessThan(alt0)) {
				remove(alt1);
				if (alts1.size() < 2) {
					return;
				}
				alt1 = alts3.peekExcept(alt0);
			}

			if (alts1.size() < 2) {
				return;
			}
			// survive itself
			alt1 = alts2.peekExcept(alt0);
			if (alt0.lessThan(alt1)) {
				remove(alt0);
				return;
			}
		}
	}

	private void consumeSample(SimOutput simOutput) {
		Alt alt = alts0[simOutput.altID];
		if (!alt.isSurviving()) {
			siftLog.trace("-1");
			return;
		}
		adoptSample(alt, simOutput);
		if (alt.num() < conf.n0) {
			return;
		}
		checkElimination1(alt);
		checkElimination2(alt);
	}

	private Conf conf;
	private Alt[] alts0;
	private IndexedPriorityQueue<Alt> alts1, alts2, alts3;
	int sampleCount = 0;
	int secondStageCount = 0;
	int eliminatedCount = 0;

	@Override
	public int ras(double[][] alts, double[] args, SimHelper simHelper)
			throws InterruptedException {
		// setup
		int k = alts.length;
		double alpha = args[0];
		double delta = args[1];
		int n0 = (int) args[2];
		double a = -1 * Math.log(2 * alpha / (k - 1));

		conf = new Conf();
		conf.a = a / delta;
		conf.b = delta / 2;
		conf.alpha = alpha;
		conf.delta = delta;
		conf.fix = false;
		conf.k = k;
		conf.min = false;
		conf.n0 = n0;

		// first stage
		int[] altIDs = new int[k * n0];
		for (int i = 0; i < n0; i++) {
			for (int j = 0; j < k; j++) {
				altIDs[i * k + j] = j;
			}
		}
		SimOutput[] simOutputs = simHelper.sim(altIDs);

		alts0 = new Alt[k];
		for (int i = 0; i < k; i++) {
			alts0[i] = new Alt(i, alts[i], conf);
		}
		for (int i = 0; i < k; i++) {
			alts0[i].prev = alts0[(i - 1 + k) % k];
			alts0[i].next = alts0[(i + 1) % k];
		}

		alts1 = new IndexedPriorityQueue<Alt>(k, new Comparator<Alt>() {
			@Override
			public int compare(Alt alt1, Alt alt2) {
				return alt1.key1() > alt2.key1() ? 1 : -1;
			}
		}, 0);
		alts2 = new IndexedPriorityQueue<Alt>(k, new Comparator<Alt>() {
			@Override
			public int compare(Alt alt1, Alt alt2) {
				return alt1.key2() < alt2.key2() ? 1 : -1;
			}
		}, 1);
		alts3 = new IndexedPriorityQueue<Alt>(k, new Comparator<Alt>() {
			@Override
			public int compare(Alt alt1, Alt alt2) {
				return alt1.key3() > alt2.key3() ? 1 : -1;
			}
		}, 2);

		for (SimOutput simOutput : simOutputs) {
			consumeSample(simOutput);
		}

		// re-sampling
		altIDs = new int[k - eliminatedCount];
		int p = 0;
		for (int i = 0; i < k; i++) {
			if (alts0[i].isSurviving()) {
				altIDs[p++] = i;
			}
		}
		simHelper.asyncSim(altIDs);
		simHelper.phantomSim(new int[] { k });
		while (k - eliminatedCount > 1) {
			SimOutput simOutput = simHelper.takeSimOutput();
			while (simOutput.altID != k && !alts0[simOutput.altID].isSurviving()) {
				simOutput = simHelper.takeSimOutput();
			}
			int altID = simOutput.altID;

			if (altID != k) {
				consumeSample(simOutput);
			} else {
				// re-sampling
				altIDs = new int[k - eliminatedCount];
				p = 0;
				for (int i = 0; i < k; i++) {
					if (alts0[i].isSurviving()) {
						altIDs[p++] = i;
					}
				}
				simHelper.asyncSim(altIDs);
				simHelper.phantomSim(new int[] { k });
			}
		}

		for (int i = 0; i < k; i++) {
			if (alts0[i].isSurviving()) {
				return i;
			}
		}
		return -1;
	}
}
