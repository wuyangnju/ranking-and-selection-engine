package hk.ust.felab.rase.sim;

import hk.ust.felab.rase.Sim;
import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

public class NormalSim implements Sim {

	@Override
	public double[] sim(double[] alt, double[] args, long[] seed) {
		int delay;
		if (args.length != 0) {
			delay = (int) args[0];
		} else {
			delay = 0;
		}

		MRG32k3a mrg32k3a = new MRG32k3a();
		mrg32k3a.setSeed(seed);

		if (delay != 0) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return new double[] { NormalGen.nextDouble(mrg32k3a, alt[1], alt[2]) };
	}

}
