package hk.ust.felab.rase.agent;

public interface Agent {
	public double[] getAlt() throws InterruptedException;

	public void putSample(double[] sample) throws InterruptedException;
}
