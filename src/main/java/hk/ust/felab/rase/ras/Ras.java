package hk.ust.felab.rase.ras;

import hk.ust.felab.rase.master.Master;

public interface Ras {
    public int ras(double[][] alts, double[] args, Master master) throws InterruptedException;
}
