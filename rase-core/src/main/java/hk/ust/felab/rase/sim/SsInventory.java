package hk.ust.felab.rase.sim;

import hk.ust.felab.rase.Sim;
import umontreal.iro.lecuyer.randvar.PoissonGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

public class SsInventory implements Sim {

	@Override
	public double[] sim(double[] alt, double[] args, long[] seed) {
		int warmup, period, lambda;
		if (args.length == 0) {
			warmup = (int) args[0];
			period = (int) args[1];
			lambda = (int) args[2];
		} else {
			warmup = 100;
			period = 30;
			lambda = 25;
		}

		MRG32k3a mrg32k3a = new MRG32k3a();
		mrg32k3a.setSeed(seed);

		int inv = (int) alt[2];
		int inv_next = 0;
		for (int i = 1; i <= warmup; i++) {
			int demand = PoissonGen.nextInt(mrg32k3a, lambda);
			if (inv < alt[1]) {
				inv_next = (int) alt[2];
			} else {
				inv_next = inv;
			}
			inv = inv_next - demand;
		}
		int cost = 0;
		for (int i = 1; i <= period; i++) {
			int demand = PoissonGen.nextInt(mrg32k3a, lambda);
			if (inv < alt[1]) {
				inv_next = (int) alt[2];
				cost = cost + 32 + 3 * ((int) alt[2] - inv);
			} else {
				inv_next = inv;
			}
			if (inv_next >= demand) {
				cost = cost + inv_next - demand;
			} else {
				cost = cost + 5 * (demand - inv_next);
			}
			inv = inv_next - demand;
		}
		return new double[] { cost / (period * 1.0) };
	}
}
