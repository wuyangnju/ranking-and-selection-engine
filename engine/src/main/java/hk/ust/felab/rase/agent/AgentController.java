package hk.ust.felab.rase.agent;

import hk.ust.felab.rase.common.Conf;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AgentController {
	private transient final Log log = LogFactory.getLog(this.getClass());

	@Resource
	private AgentService agentService;

	@RequestMapping(value = Conf.ACTIVATE_AGENT, method = RequestMethod.POST)
	@ResponseBody
	public String activateAgent(@RequestParam String masterHost,
			@RequestParam int masterPort, @RequestParam int slaveIdOffset,
			@RequestParam int localSlaveCount,
			@RequestParam String sampleGenerator,
			@RequestParam int sampleCountStep) {
		Conf.masterHost = masterHost;
		Conf.masterPort = masterPort;
		Conf.masterUrl = "http://" + Conf.masterHost + ":" + Conf.masterPort
				+ "/";
		Conf.slaveIdOffset = slaveIdOffset;
		Conf.localSlaveCount = localSlaveCount;
		try {
			agentService.activate(sampleGenerator, sampleCountStep);
		} catch (Exception e) {
			return e.getMessage();
		}
		Conf.agentActivated = true;
		log.info("Agent activated with slave from " + Conf.slaveIdOffset
				+ " to " + (Conf.slaveIdOffset + Conf.localSlaveCount) + ".");
		return "";
	}
}
