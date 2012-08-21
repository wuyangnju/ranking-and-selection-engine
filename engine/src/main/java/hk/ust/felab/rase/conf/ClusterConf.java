package hk.ust.felab.rase.conf;

public class ClusterConf {
	public static final String ACTIVATE_MASTER = "activateMaster";
	public static final String GET_ALTS = "getAlts";
	public static final String PUT_SAMPLES = "putSamples";
	public static final String RAS_STATUS = "rasStatus";
	public static final String RAS_RESULT = "rasResult";

	public static final String ACTIVATE_AGENT = "activateAgent";
	public static final String README = "readme";

	private static ClusterConf instance = new ClusterConf();

	private ClusterConf() {

	}

	public static ClusterConf get() {
		return instance;
	}

	public boolean isMaster = false;
	public boolean isAgent = false;

	public String masterHost;
	public int masterPort;
	public String masterUrl;
	public int masterAltBufSize;
	public int masterSampleBufSize;

	public int agentAltBufSize;
	public int agentSampleBufSize;

	public int slaveIdOffset;
	public int slaveLocalCount;
	public int slaveTotalCount;
	public String slaveSampleGenerator;
	public int slaveSampleCountStep;

}
