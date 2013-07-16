package hk.ust.felab.rase.slave;

import hk.ust.felab.rase.SimInput;
import hk.ust.felab.rase.SimOutput;

public interface SlaveHelper {
	public SimInput takeSimInput() throws InterruptedException;
	public double[] getSimArgs();
	public void putSimOutput(SimOutput simOutput) throws InterruptedException;
}
