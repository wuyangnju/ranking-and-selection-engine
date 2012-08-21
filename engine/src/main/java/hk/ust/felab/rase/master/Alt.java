package hk.ust.felab.rase.master;

import hk.ust.felab.rase.conf.RasConf;
import hk.ust.felab.rase.util.GsonUtil;
import hk.ust.felab.rase.util.Indexed;

import java.util.concurrent.atomic.AtomicBoolean;

public class Alt implements Indexed {
	private int id;
	private AtomicBoolean surviving = new AtomicBoolean(true);
	private double[] args;
	private double[] data;
	private int[] indexInPriorityQueue = new int[] { -1, -1, -1 };

	public static double minThetaOverSqrtY = Double.MAX_VALUE;

	/**
	 * ConsumeSampleThread and ProduceAltThread will race on Alt. with only arg
	 * snapshot(without alt/data snapshot), the only conflict is r/w surviving.
	 */

	public Alt(double[] altArgs) {
		// altArgs[0]: id
		id = (int) altArgs[0];

		// altArgs[1]: mu
		// altArgs[2]: sigma
		// altArgs[3]: numofsys
		// altArgs[4]: delta
		// altArgs[5]: sleepmu
		// altArgs[6]: sleepsigma
		args = new double[altArgs.length - 1];
		System.arraycopy(altArgs, 1, args, 0, args.length);

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
		return surviving.get();
	}

	public void setSurviving(boolean surviving) {
		this.surviving.set(surviving);
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
		if (RasConf.get().fix) {
			if (data[0] < RasConf.get().n0) {
				update(sample, simTime);
			} else if (data[0] == RasConf.get().n0) {
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
		return Math.sqrt(var() / data[5]);
	}

	public long num() {
		return (long) data[0];
	}

	public double mean() {
		return data[1] / data[0];
	}

	public double var() {
		if (RasConf.get().fix) {
			return data[3];
		} else {
			return (data[2] - data[1] * data[1] / data[0]) / (data[0] - 1);
		}
	}

	public double key1() {
		return var() / data[0];
	}

	public double key2() {
		return mean() - RasConf.get().a * var() / data[0];
	}

	public double key3() {
		return mean() + RasConf.get().a * var() / data[0];
	}

	public boolean lessThan(Alt o) {
		return key3() < o.key2() + RasConf.get().b;
	}

	@Override
	public String toString() {
		return GsonUtil.gsonDes().toJson(this);
	}

}
