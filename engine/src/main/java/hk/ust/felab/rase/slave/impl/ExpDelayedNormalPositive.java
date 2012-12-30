package hk.ust.felab.rase.slave.impl;

import hk.ust.felab.rase.slave.SampleGenerator;
import umontreal.iro.lecuyer.probdist.NormalDist;
import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

public class ExpDelayedNormalPositive implements SampleGenerator {
	private MRG32k3a mrg32k3a;
	private NormalDist normalDist = new NormalDist();

	public ExpDelayedNormalPositive(Integer subStreamId) {
		mrg32k3a = new MRG32k3a();
		for (int i = 0; i < subStreamId; i++) {
			mrg32k3a.resetNextSubstream();
		}
	}

	@Override
	public double[] generate(double[] alt) {
		double z = NormalGen.nextDouble(mrg32k3a, 0, 1);
		double sample = alt[1] + alt[2] * z;
		int sleep = (int) ((-1.0) * alt[3] * Math.log(1.0 - normalDist.cdf01(z)));
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return new double[] { sample, sleep };
	}
}
