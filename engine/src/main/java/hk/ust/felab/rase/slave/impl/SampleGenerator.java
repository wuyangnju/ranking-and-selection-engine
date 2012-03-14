package hk.ust.felab.rase.slave.impl;

import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

public class SampleGenerator {
	private MRG32k3a mrg32k3a;
	private MRG32k3a sleepstream;

	public SampleGenerator(int slaveId) {
		mrg32k3a = new MRG32k3a();
		sleepstream = new MRG32k3a();
		for (int i = 0; i < slaveId; i++) {
			mrg32k3a.resetNextSubstream();
			sleepstream.resetNextSubstream();
		}
	}

	public double[] generate(double[] alt) {
		// data[0]: id
		// data[1]: mu
		// data[2]: sigma
		// data[3]: numofsys
		// data[4]: delta
		// data[5]: sleepmu
		// data[6]: sleepsigma
		double mu = alt[1], sigma = Math.sqrt(alt[2]), sleepmu = alt[5], sleepsigma = alt[6];
		double[] samples = new double[1];
		samples[0] = NormalGen.nextDouble(mrg32k3a, mu, sigma);
		try {
			Thread.sleep((long) Math.max(0, NormalGen.nextDouble(sleepstream,
					sleepmu * 1000, sleepsigma * 1000)));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return samples;
	}

}
