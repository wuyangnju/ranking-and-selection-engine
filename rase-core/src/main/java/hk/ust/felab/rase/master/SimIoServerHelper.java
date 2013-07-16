package hk.ust.felab.rase.master;

import hk.ust.felab.rase.SimInput;
import hk.ust.felab.rase.SimOutput;

public interface SimIoServerHelper {
	public int simInputCount();
	public SimInput takeSimInputNet() throws InterruptedException;
	public void putSimOutputNet(SimOutput simOutput) throws InterruptedException;
}
