package hk.ust.felab.rase.agent;

import hk.ust.felab.rase.conf.ClusterConf;
import hk.ust.felab.rase.conf.RasConf;
import hk.ust.felab.rase.util.GsonUtil;

import javax.annotation.Resource;

import com.google.gson.Gson;

public class AgentController {
	@Resource(name = GsonUtil.GSON_BRIEF)
	private Gson gson;

	@Resource
	private AgentService agentService;

	public String activateAgent( int trialId,
			 String masterHost,  int masterPort,
			 int agentAltBufSize,
			 int agentSampleBufSize,
			 int slaveIdOffset,  int slaveLocalCount,
			 int slaveTotalCount,
			 String sampleGenerator,
			 int sampleCountStep) {

		RasConf.get().trialCount = trialId;

		ClusterConf.get().masterHost = masterHost;
		ClusterConf.get().masterPort = masterPort;
		ClusterConf.get().masterUrl = "http://" + masterHost + ":" + masterPort
				+ "/";

		ClusterConf.get().agentAltBufSize = agentAltBufSize;
		ClusterConf.get().agentSampleBufSize = agentSampleBufSize;

		ClusterConf.get().slaveIdOffset = slaveIdOffset;
		ClusterConf.get().slaveLocalCount = slaveLocalCount;
		ClusterConf.get().slaveTotalCount = slaveTotalCount;
		RasConf.get().sampleGenerator = sampleGenerator;
		RasConf.get().sampleCountStep = sampleCountStep;

		try {
			agentService.activate();
		} catch (Exception e) {
			return e.toString();
		}

		ClusterConf.get().isAgent = true;

		return gson.toJson(ClusterConf.get()) + "\n";
	}

	public String readme() {
		return "ranking and selection engine\n";
	}
}
