package hk.ust.felab.rase.master.impl;

import hk.ust.felab.rase.master.AltBase;

public class Alt extends AltBase {
	private int id;
	private double[] args;
	private double[] data;

	public Alt(double[] altArgs) {
		// altArgs[0]: id
		id = (int) altArgs[0];

		// altArgs[1]: mu
		// altArgs[2]: sigma
		// altArgs[3]: numofsys
		// altArgs[4]: delta
		// altArgs[5]: sleepmu
		// altArgs[6]: sleepsigma
		args = new double[altArgs.length - 1];
		System.arraycopy(altArgs, 1, args, 0, args.length);

		data = new double[5];
		data[0] = 0;// count, number of samples
		data[1] = 0;// sum
		data[2] = 0;// sumofsquare
		data[3] = 0;// sample mean
		data[4] = 0;// sample variance
	}

	@Override
	protected long doAddSample(double[] sample) {
		data[0]++;
		data[1] += sample[0];
		data[2] += Math.pow(sample[0], 2);
		data[3] = data[1] / data[0];
		data[4] = (data[2] - data[0] * Math.pow(data[3], 2)) / (data[0] - 1);
		return (long) data[0];
	}

	@Override
	protected int getId() {
		return id;
	}

	@Override
	protected double[] getArgs() {
		return args;
	}

	@Override
	protected double[] getData() {
		return data;
	}

}
