package hk.ust.felab.rase.master;

import hk.ust.felab.rase.common.Conf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Controller
public class MasterController {
	private transient final Log log = LogFactory.getLog(this.getClass());

	private Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues()
			.create();

	@Resource
	private MasterService masterService;

	@RequestMapping(value = Conf.README, method = RequestMethod.GET)
	@ResponseBody
	public String readme() {
		String msg = gson.toJson(new Conf());
		log.info(msg);
		return msg;
	}

	private double[] stringToArgList(String line) {
		String[] argsStr = line.split(" ");
		double[] args = new double[argsStr.length];
		for (int i = 0; i < argsStr.length; i++) {
			args[i] = Double.parseDouble(argsStr[i]);
		}
		return args;
	}

	@RequestMapping(value = Conf.ACTIVATE_MASTER, method = RequestMethod.POST)
	@ResponseBody
	public String activateMaster(@RequestParam int slaveCount,
			@RequestParam MultipartFile rasConf) {
		double[] rasArgs = null;
		double[][] altsArgs = null;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					rasConf.getInputStream()));

			String line = br.readLine();
			rasArgs = stringToArgList(line);

			ArrayList<double[]> altsArgsList = new ArrayList<double[]>();
			while ((line = br.readLine()) != null) {
				double[] systemArgs = stringToArgList(line);
				altsArgsList.add(systemArgs);
			}
			altsArgs = new double[altsArgsList.size()][];
			for (int i = 0; i < altsArgsList.size(); i++) {
				altsArgs[i] = altsArgsList.get(i);
			}
		} catch (IOException e) {
			log.error(e, e);
		}

		masterService.activate(slaveCount, rasArgs, altsArgs);
		Conf.masterActivated = true;
		return "";
	}

	@RequestMapping(value = Conf.REQUEST_TASK, method = RequestMethod.GET)
	@ResponseBody
	public String requestTask(@RequestParam int slaveId) {
		return gson.toJson(masterService.requestTask(slaveId));
	}

	@RequestMapping(value = Conf.SUBMIT_SAMPLE, method = RequestMethod.POST)
	@ResponseBody
	public String submitSample(@RequestParam String samplesJson) {
		log.debug(samplesJson);
		long sampleCount = masterService.submitSample(gson.fromJson(
				samplesJson, double[][].class));
		return String.valueOf(sampleCount);
	}

	@RequestMapping(value = Conf.RAS_SURVIVAL_COUNT, method = RequestMethod.GET)
	@ResponseBody
	public String rasSurvivalCount() {
		return String.valueOf(masterService.survivalCount());
	}

	@RequestMapping(value = Conf.RAS_DELETED_STATUS, method = RequestMethod.GET)
	@ResponseBody
	public String rasDeletedStatus() {
		return gson.toJson(masterService.deletedSnapshot());
	}

	@RequestMapping(value = Conf.RAS_STATUS, method = RequestMethod.GET)
	@ResponseBody
	public String rasStatus() {
		return gson.toJson(masterService.altSnapshot());
	}

}
