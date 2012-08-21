package hk.ust.felab.rase.slave.impl;

import hk.ust.felab.rase.slave.SampleGenerator;
import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

public class Normal implements SampleGenerator {
	private MRG32k3a normalMrg32k3a;

	public Normal(Integer subStreamId) {
		normalMrg32k3a = new MRG32k3a();
		for (int i = 0; i < subStreamId; i++) {
			normalMrg32k3a.resetNextSubstream();
		}
	}

	@Override
	public double[] generate(double[] alt) {
		return new double[] {
				NormalGen.nextDouble(normalMrg32k3a, alt[1], alt[2]), 1 };
	}
}
