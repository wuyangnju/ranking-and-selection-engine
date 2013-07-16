package hk.ust.felab.rase.sim;

import hk.ust.felab.rase.Sim;
import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

public class NormalSim implements Sim {

	@Override
	public double[] sim(double[] alt, double[] args, long[] seed) {
		MRG32k3a mrg32k3a = new MRG32k3a();
		mrg32k3a.setSeed(seed);
		return new double[] { NormalGen.nextDouble(mrg32k3a, alt[1], alt[2]) };
	}

}
