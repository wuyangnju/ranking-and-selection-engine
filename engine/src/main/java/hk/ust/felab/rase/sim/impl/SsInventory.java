package hk.ust.felab.rase.sim.impl;

import hk.ust.felab.rase.sim.SampleGen;
import umontreal.iro.lecuyer.randvar.PoissonGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

public class SsInventory implements SampleGen {
	public static final int warmup = 100;
	public static final int period = 30;
	public static final int lambda = 25;

	private MRG32k3a poissonMrg32k3a;

	public SsInventory(Integer slaveId) {
		poissonMrg32k3a = new MRG32k3a();
		for (int i = 0; i < slaveId; i++) {
			poissonMrg32k3a.resetNextSubstream();
		}
	}

	@Override
	public double[] generate(double[] alt) {
		int inv = (int) alt[2];
		int inv_next = 0;
		for (int i = 1; i <= warmup; i++) {
			int demand = PoissonGen.nextInt(poissonMrg32k3a, lambda);
			if (inv < alt[1]) {
				inv_next = (int) alt[2];
			} else {
				inv_next = inv;
			}
			inv = inv_next - demand;
		}

		int cost = 0;
		for (int i = 1; i <= period; i++) {
			int demand = PoissonGen.nextInt(poissonMrg32k3a, lambda);
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
