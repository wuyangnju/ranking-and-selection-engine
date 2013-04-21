package hk.ust.felab.rase.vo;

import hk.ust.felab.rase.util.ArraysUtil;

public class SimOutput {

	public int syncID;
	public int altID;
	public double[] result;

	public SimOutput(int syncID, int altID, double[] result) {
		this.syncID = syncID;
		this.altID = altID;
		this.result = result;
	}

	@Override
	public String toString() {
		return altID + ", " + ArraysUtil.toString(result);
	}

}
