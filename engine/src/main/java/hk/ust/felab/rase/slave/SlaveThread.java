package hk.ust.felab.rase.slave;

import hk.ust.felab.rase.common.Conf;
import hk.ust.felab.rase.slave.impl.SampleGenerator2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;

public class SlaveThread implements Runnable {

	private transient final Log log = LogFactory.getLog(this.getClass());
	private Gson gson = new Gson();
	private HttpClient client = new HttpClient();

	private SlaveSignal signal = SlaveSignal.None;

	public SlaveSignal getSignal() {
		return signal;
	}

	public void setSignal(SlaveSignal signal) {
		this.signal = signal;
	}

	private int slaveId;
	private SampleGenerator sampleGenerator;

	public SlaveThread(int slaveId) {
		this.slaveId = slaveId;
		this.sampleGenerator = new SampleGenerator2(slaveId);
	}

	private double[][] requestTask() throws HttpException, IOException {
		GetMethod method = new GetMethod(Conf.masterUrl + Conf.REQUEST_TASK
				+ "?slaveId=" + slaveId);
		int retCode = client.executeMethod(method);
		BufferedReader br = new BufferedReader(new InputStreamReader(
				method.getResponseBodyAsStream()));
		String altSystemsJson = br.readLine();
		log.debug("Slave " + slaveId + " requests task with return code "
				+ retCode + " and result " + altSystemsJson);
		return gson.fromJson(altSystemsJson, double[][].class);
	}

	private long submitSamples(double[][] samplesWithSysId)
			throws HttpException, IOException {
		PostMethod method = new PostMethod(Conf.masterUrl + Conf.SUBMIT_SAMPLE);
		String samplesJson = gson.toJson(samplesWithSysId);
		method.addParameter("samplesJson", samplesJson);
		int retCode = client.executeMethod(method);
		BufferedReader br = new BufferedReader(new InputStreamReader(
				method.getResponseBodyAsStream()));
		String submitRetCodeJson = br.readLine();
		log.debug("Slave " + slaveId + " submit sample "
				+ gson.toJson(samplesJson) + "with return code " + retCode
				+ " and result " + submitRetCodeJson);
		return Long.parseLong(submitRetCodeJson);
	}

	@Override
	public void run() {
		try {
			while (true) {
				signal.action(this);
				// TODO: performance refactor needed!
				double[][] altSystems = requestTask();
				for (double[] altSystem : altSystems) {
					double[] samples = sampleGenerator.generate(altSystem);
					double[] samplesWithSysId = new double[samples.length + 1];
					samplesWithSysId[0] = altSystem[0];
					System.arraycopy(samples, 0, samplesWithSysId, 1,
							samples.length);
					double[][] submit = new double[1][];
					submit[0] = samplesWithSysId;
					submitSamples(submit);
				}
			}
		} catch (HttpException e) {
			log.error(e, e);
		} catch (IOException e) {
			log.error(e, e);
		}
	}
}
