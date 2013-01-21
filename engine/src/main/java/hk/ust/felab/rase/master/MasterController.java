package hk.ust.felab.rase.master;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class MasterController {

	private transient final Logger log = Logger.getLogger(getClass());

	private Gson gsonFloat;

	private Master masterService;

	public String getAlts(int demand) {
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

	public String putSample(String samplesJson) {
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
}
