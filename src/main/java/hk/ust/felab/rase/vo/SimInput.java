package hk.ust.felab.rase.vo;

import hk.ust.felab.rase.util.ArraysUtil;

public class SimInput {
	public int syncID;
	public int altID;
	public double[] args;
	public long[] seed;

	public SimInput(int syncID, int altID, double[] args, long[] seed) {
		this.syncID = syncID;
		this.altID = altID;
		this.args = args;
		this.seed = seed;
	}

	@Override
	public String toString() {
		return altID + ", " + ArraysUtil.toString(args) + ", "
				+ ArraysUtil.toString(seed);
	}

}
