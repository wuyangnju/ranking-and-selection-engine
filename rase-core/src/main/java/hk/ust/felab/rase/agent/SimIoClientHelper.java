package hk.ust.felab.rase.agent;

import hk.ust.felab.rase.SimInput;

public interface SimIoClientHelper {
	public void putSimInputNet(SimInput simInput) throws InterruptedException;
	public void unsetWaitingInput();
	public void unsetWaitingOutput();
}
