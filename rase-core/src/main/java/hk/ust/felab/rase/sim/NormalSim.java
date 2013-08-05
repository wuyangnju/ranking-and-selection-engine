package hk.ust.felab.rase.sim;

import hk.ust.felab.rase.Sim;
import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

public class NormalSim implements Sim {

	@Override
	public double[] sim(double[] alt, double[] args, long[] seed) {
		boolean willDelay;
		int delay;
		if (args.length != 0) {
			willDelay = true;
			delay = (int) args[0];
		} else {
			willDelay = false;
			delay = 0;
		}

		MRG32k3a mrg32k3a = new MRG32k3a();
		mrg32k3a.setSeed(seed);

		if (willDelay) {
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return new double[] { NormalGen.nextDouble(mrg32k3a, alt[1], alt[2]) };
	}

}
