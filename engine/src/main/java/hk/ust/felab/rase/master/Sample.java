package hk.ust.felab.rase.master;

public class Sample {
	private double[] sampleArr;

	public Sample(double[] sampleArr) {
		this.sampleArr = sampleArr;
	}

	public int altId() {
		return (int) sampleArr[0];
	}

	public double sample() {
		return sampleArr[1];
	}

	public long simTime() {
		return (long) sampleArr[2];
	}
}
