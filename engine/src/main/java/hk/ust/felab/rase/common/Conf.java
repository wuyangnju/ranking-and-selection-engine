package hk.ust.felab.rase.common;

public class Conf {
	public static final String README = "readme";
	public static final String ACTIVATE_MASTER = "activateMaster";
	public static final String ACTIVATE_AGENT = "activateAgent";

	public static final String REGISTER_SLAVE = "registerSlave";

	public static final String RAS_STATUS = "rasStatus";
	public static final String RAS_SURVIVAL_COUNT = "rasSurvivalCount";
	public static final String RAS_DELETED_STATUS = "rasDeletedStatus";

	public static final String INIT_RAS = "initRAS";
	public static final String START_RAS = "startRAS";

	/**
	 * @parameter localSlaveId
	 * @return altSystems:2d array, a[i][0]: alt system id
	 */
	public static final String REQUEST_TASK = "requestTask";

	/**
	 * @parameter samplesWithSysId:2d array, a[i][0]: alt system id
	 * @return int: such as SYSTEM_NOT_FOUND
	 */
	public static final String SUBMIT_SAMPLE = "submitSample";

	public static final long SYSTEM_NOT_FOUND = -1;

	public static int jettyPort;
	public static String jettyContextPath;
	public static String jettyWar;
	public static boolean masterActivated = false;
	public static boolean agentActivated = false;
	public static String masterHost;
	public static int masterPort;
	public static String masterUrl;
	public static int localSlaveCount;
	public static int slaveIdOffset;
}
