package hk.ust.felab.rase;

public interface SimHelper{

    public SimOutput[] sim(int[] altIDs) throws InterruptedException;
    public int phantomSim(int[] altIDs) throws InterruptedException;

    public int asyncSim(int[] altIDs) throws InterruptedException;

    public SimOutput takeSimOutput() throws InterruptedException;
    public SimOutput[] takeSimOutputs(int n, boolean block) throws InterruptedException;

}
