package hk.ust.felab.rase;

import java.io.Serializable;

public class SimInput implements Serializable {
	private static final long serialVersionUID = 1L;

	public int repId;
	public int syncId;

	public int altId;
	public double[] alt;
	public long[] seed;

	public SimInput(int repId, int syncId, int altId, double[] alt, long[] seed) {
		this.repId = repId;
		this.syncId = syncId;
		this.altId = altId;
		this.alt = alt;
		this.seed = seed;
	}

	@Override
	public String toString() {
		return altId + ", " + ArraysUtil.toString(alt) + ", "
				+ ArraysUtil.toString(seed);
	}

}
