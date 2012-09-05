package hk.ust.felab.rase.agent;

import hk.ust.felab.rase.conf.ClusterConf;
import hk.ust.felab.rase.conf.RasConf;
import hk.ust.felab.rase.util.GsonUtil;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

@Controller
public class AgentController {
	@Resource(name = GsonUtil.GSON_BRIEF)
	private Gson gson;

	@Resource
	private AgentService agentService;

	@RequestMapping(value = ClusterConf.ACTIVATE_AGENT, method = RequestMethod.POST)
	@ResponseBody
	public String activateAgent(@RequestParam int trialId,
			@RequestParam String masterHost, @RequestParam int masterPort,
			@RequestParam int agentAltBufSize,
			@RequestParam int agentSampleBufSize,
			@RequestParam int slaveIdOffset, @RequestParam int slaveLocalCount,
			@RequestParam int slaveTotalCount,
			@RequestParam String sampleGenerator,
			@RequestParam int sampleCountStep) {

		RasConf.get().trialId = trialId;

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

	@RequestMapping(value = ClusterConf.README, method = RequestMethod.GET)
	@ResponseBody
	public String readme() {
		return "ranking and selection engine\n";
	}
}
