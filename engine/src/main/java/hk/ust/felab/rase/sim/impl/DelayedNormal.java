package hk.ust.felab.rase.sim.impl;

import hk.ust.felab.rase.sim.SampleGen;
import umontreal.iro.lecuyer.randvar.ExponentialGen;
import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

public class DelayedNormal implements SampleGen {
	private MRG32k3a expMrg32k3a;
	private MRG32k3a normalMrg32k3a;

	public DelayedNormal(Integer subStreamId) {
		expMrg32k3a = new MRG32k3a();
		normalMrg32k3a = new MRG32k3a();
		for (int i = 0; i < subStreamId; i++) {
			expMrg32k3a.resetNextSubstream();
			normalMrg32k3a.resetNextSubstream();
		}
	}

	@Override
	public double[] generate(double[] alt) {
		int sleep = (int) ExponentialGen.nextDouble(expMrg32k3a, 0.005);
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return new double[] {
				NormalGen.nextDouble(normalMrg32k3a, alt[1], alt[2]), sleep };
	}
}
