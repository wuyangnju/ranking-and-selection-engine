package hk.ust.felab.rase;

import java.io.Serializable;

public class SimOutput implements Serializable{
	private static final long serialVersionUID = 1L;
	
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
