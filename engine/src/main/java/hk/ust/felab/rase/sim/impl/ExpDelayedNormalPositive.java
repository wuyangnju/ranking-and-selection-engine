package hk.ust.felab.rase.sim.impl;

import hk.ust.felab.rase.sim.SampleGen;
import umontreal.iro.lecuyer.probdist.NormalDist;
import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

public class ExpDelayedNormalPositive implements SampleGen {
	private MRG32k3a mrg32k3a, mrg32k3aTmp;

	public ExpDelayedNormalPositive(Integer subStreamId) {
		mrg32k3a = new MRG32k3a();
		mrg32k3aTmp = new MRG32k3a();
		for (int i = 0; i < subStreamId; i++) {
			mrg32k3a.resetNextSubstream();
			mrg32k3aTmp.resetNextSubstream();
		}
	}

	@Override
	public double[] generate(double[] alt) {
		double z = NormalGen.nextDouble(mrg32k3a, 0, 1);
		double sample = alt[1] + alt[2] * z;
		int sleep = (int) ((-1.0) * alt[3] * Math
				.log(1.0 - NormalDist.cdf01(z)));
		for (int i = 0; (i < sleep) && (i < 1000); i++) {
			double zTmp = NormalGen.nextDouble(mrg32k3aTmp, 0, 1);
			double sampleTmp = alt[1] + alt[2] * zTmp;
			int sleepTmp = (int) ((-1.0) * alt[3] * Math.log(1.0 - NormalDist
					.cdf01(zTmp)));
		}
		return new double[] { sample, Math.max(1, sleep) };
	}
}
