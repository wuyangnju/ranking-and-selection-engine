package hk.ust.felab.rase.conf;

public class RasConf {
	private static final RasConf instance = new RasConf();

	private RasConf() {

	}

	public static RasConf get() {
		return instance;
	}

	/**
	 * number of alternatives(alts)
	 */
	public int k;

	public boolean min;

	public double alpha;

	public double delta;

	/**
	 * a = (-1.0) / delta * Math.log(2 - 2 * Math.pow((1 - alpha), (1.0 / (k -
	 * 1) * 1.0)));
	 */
	public double a;

	/**
	 * b = delta / 2.0;
	 */
	public double b;

	public int n0;

	public boolean fix;

	public int trialId;

}
