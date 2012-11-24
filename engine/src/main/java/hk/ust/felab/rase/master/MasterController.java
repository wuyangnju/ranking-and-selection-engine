package hk.ust.felab.rase.master;

import hk.ust.felab.rase.agent.AgentService;
import hk.ust.felab.rase.conf.ClusterConf;
import hk.ust.felab.rase.conf.RasConf;
import hk.ust.felab.rase.util.GsonUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

@Controller
public class MasterController {

	private transient final Logger log = Logger.getLogger(getClass());

	@Resource(name = GsonUtil.GSON_FLOAT)
	private Gson gsonFloat;

	@Resource(name = GsonUtil.GSON_BRIEF)
	private Gson gsonDes;

	@Resource
	private MasterService masterService;

	@Resource
	private AgentService agentService;

	private double[] stringToArgList(String line) {
		String[] argsStr = line.split(" ");
		double[] args = new double[argsStr.length];
		for (int i = 0; i < argsStr.length; i++) {
			args[i] = Double.parseDouble(argsStr[i]);
		}
		return args;
	}

	@RequestMapping(value = ClusterConf.ACTIVATE_MASTER, method = RequestMethod.POST)
	@ResponseBody
	public String activateMaster(@RequestParam int masterAltBufSize,
			@RequestParam int masterSampleBufSize, @RequestParam boolean min,
			@RequestParam double alpha, @RequestParam double delta,
			@RequestParam int n0, @RequestParam boolean fix,
			@RequestParam MultipartFile altsConf) {

		ClusterConf.get().masterAltBufSize = masterAltBufSize;
		ClusterConf.get().masterSampleBufSize = masterSampleBufSize;
		RasConf.get().min = min;
		RasConf.get().alpha = alpha;
		RasConf.get().delta = delta;
		RasConf.get().n0 = n0;
		RasConf.get().fix = fix;

		try {
			ArrayList<double[]> altsArgs = new ArrayList<double[]>();
			BufferedReader br = new BufferedReader(new InputStreamReader(
					altsConf.getInputStream()));
			String line = null;
			while ((line = br.readLine()) != null) {
				double[] systemArgs = stringToArgList(line);
				altsArgs.add(systemArgs);
			}
			RasConf.get().k = altsArgs.size();
			RasConf.get().a = (-1.0)
					/ delta
					* Math.log(2.0 * alpha / (RasConf.get().k - 1) * 1.0);
			RasConf.get().b = delta / 2.0;
			masterService.activate(altsArgs.toArray(new double[][] {}));
		} catch (Exception e) {
			return e.toString();
		}

		ClusterConf.get().isMaster = true;
		return gsonDes.toJson(ClusterConf.get()) + "\n"
				+ gsonDes.toJson(RasConf.get()) + "\n";
	}

	@RequestMapping(value = ClusterConf.GET_ALTS, method = RequestMethod.POST)
	@ResponseBody
	public String getAlts(@RequestParam int demand) {
		log.debug(demand);
		double[][] alts;
		String altsJson;
		try {
			alts = masterService.getAlts(demand);
			log.debug(alts);
			altsJson = gsonFloat.toJson(alts);
			log.debug(altsJson);
			return altsJson;
		} catch (InterruptedException e) {
			return e.toString();
		}
	}

	@RequestMapping(value = ClusterConf.PUT_SAMPLES, method = RequestMethod.POST)
	@ResponseBody
	public String putSample(@RequestParam String samplesJson) {
		log.debug(samplesJson);
		double[][] samples = gsonFloat.fromJson(samplesJson, double[][].class);
		log.debug(samples);
		try {
			masterService.putSamples(samples);
		} catch (JsonSyntaxException e) {
			return e.toString();
		} catch (InterruptedException e) {
			return e.toString();
		}
		return "";
	}

	@RequestMapping(value = ClusterConf.RAS_STATUS, method = RequestMethod.GET)
	@ResponseBody
	public String rasStatus() {
		StringBuilder rasStatus = new StringBuilder(masterService.rasStatus());
		if (ClusterConf.get().isMaster) {
			rasStatus.append("master: " + masterService.bufStatus());
		}
		if (ClusterConf.get().isAgent) {
			rasStatus.append("agent: " + agentService.bufStatus());
		}
		return rasStatus.toString();
	}

	@RequestMapping(value = ClusterConf.RAS_RESULT, method = RequestMethod.GET)
	@ResponseBody
	public String result() {
		return String.valueOf(masterService.result());
	}
}
