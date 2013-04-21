package hk.ust.felab.rase.master;

import hk.ust.felab.rase.agent.Agent;
import hk.ust.felab.rase.vo.SimOutput;

public interface Master extends Agent{

    public SimOutput[] sim(int[] altIDs) throws InterruptedException;
    public int phantomSim(int[] altIDs) throws InterruptedException;

    public int asyncSim(int[] altIDs) throws InterruptedException;

    public SimOutput takeSimOutput() throws InterruptedException;
    public SimOutput[] takeSimOutputs(int n, boolean block) throws InterruptedException;

}
