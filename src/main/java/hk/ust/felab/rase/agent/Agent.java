package hk.ust.felab.rase.agent;

import hk.ust.felab.rase.vo.SimInput;
import hk.ust.felab.rase.vo.SimOutput;

public interface Agent {
	public SimInput takeSimInput() throws InterruptedException;
	public void putSimOutput(SimOutput simOutput) throws InterruptedException;
}
