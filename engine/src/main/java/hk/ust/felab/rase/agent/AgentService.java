package hk.ust.felab.rase.agent;

import hk.ust.felab.rase.conf.ClusterConf;
import hk.ust.felab.rase.conf.RasConf;
import hk.ust.felab.rase.master.MasterService;
import hk.ust.felab.rase.slave.SlaveThread;
import hk.ust.felab.rase.util.GsonUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.Resource;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

@Service
public class AgentService {

	private transient final Logger log = Logger.getLogger(getClass());

    private transient final Logger bufLog = Logger.getLogger("agent.buf");

	@Resource(name = GsonUtil.GSON_FLOAT)
	private Gson gson;

	@Resource
	private MasterService masterService;

	private HttpClient clientPull = new HttpClient();
	private HttpClient clientPush = new HttpClient();

	private ArrayList<Runnable> slaveThreads = new ArrayList<Runnable>();

	private BlockingQueue<double[]> altBuf;
	private BlockingQueue<double[]> sampleBuf;

	public void activate() throws IllegalArgumentException, SecurityException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException,
			ClassNotFoundException, IOException {

		altBuf = new LinkedBlockingQueue<double[]>(
				ClusterConf.get().agentAltBufSize);
		sampleBuf = new LinkedBlockingQueue<double[]>(
				ClusterConf.get().agentSampleBufSize);

		Executor executor = Executors
				.newFixedThreadPool(ClusterConf.get().slaveLocalCount + 2);
		executor.execute(new PullAltsThread());
		executor.execute(new PushSamplesThread());
		for (int i = 0; i < ClusterConf.get().slaveLocalCount; i++) {
			int slaveId = ClusterConf.get().slaveIdOffset + i;
			slaveThreads.add(new SlaveThread(slaveId, RasConf.get().trialId
					* ClusterConf.get().slaveTotalCount + slaveId, this));
			executor.execute(slaveThreads.get(i));
		}
	}

	public double[] getAlt() throws InterruptedException {
		return altBuf.take();
	}

	void pullAlts(int demand) {
		double[][] alts = null;
		if (ClusterConf.get().isMaster) {
			try {
				alts = masterService.getAlts(demand);
			} catch (InterruptedException e) {
				log.error(e, e);
			}
		} else {
			PostMethod method = new PostMethod(ClusterConf.get().masterUrl
					+ ClusterConf.GET_ALTS);
			method.addParameter("demand", String.valueOf(demand));
			String altSystemsJson = null;
			try {
				clientPull.executeMethod(method);
				BufferedReader br = new BufferedReader(new InputStreamReader(
						method.getResponseBodyAsStream()));
				altSystemsJson = br.readLine();
				br.close();
			} catch (HttpException e) {
				log.error(e, e);
			} catch (IOException e) {
				log.error(e, e);
			}
			try {
				alts = gson.fromJson(altSystemsJson, double[][].class);
			} catch (Exception e) {
				log.error(altSystemsJson);
			}
		}
		for (double[] alt : alts) {
			altBuf.offer(alt);
		}
	}

	class PullAltsThread implements Runnable {
		@Override
		public void run() {
			int demand;
			while (true) {
				demand = ClusterConf.get().agentAltBufSize - altBuf.size();
				if (demand > ClusterConf.get().agentAltBufSize / 2) {
					pullAlts(demand);
				} else {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						log.warn(e, e);
						continue;
					}
				}
			}
		}
	}

	public void putSample(int altId, double sample, double simTime)
			throws InterruptedException {
		sampleBuf.put(new double[] { altId, sample, simTime });
        bufLog.trace(altBuf.size() + "," + sampleBuf.size() + "\n");
	}

	void pushSamples(double[][] samples) {
		if (ClusterConf.get().isMaster) {
			try {
				masterService.putSamples(samples);
			} catch (InterruptedException e) {
				log.error(e, e);
			}
		} else {
			PostMethod method = new PostMethod(ClusterConf.get().masterUrl
					+ ClusterConf.PUT_SAMPLES);
			String samplesJson = gson.toJson(samples);
			method.addParameter("samplesJson", samplesJson);
			try {
				clientPush.executeMethod(method);
			} catch (HttpException e) {
				log.error(e, e);
			} catch (IOException e) {
				log.error(e, e);
			}
		}
	}

	class PushSamplesThread implements Runnable {
		@Override
		public void run() {
			ArrayList<double[]> samples;
			int batchSize;
			while (true) {
				if ((batchSize = sampleBuf.size()) > 0) {
					samples = new ArrayList<double[]>(batchSize);
					for (int i = 0; i < batchSize; i++) {
						samples.add(sampleBuf.poll());
					}
					pushSamples(samples.toArray(new double[][] {}));
				} else {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						log.warn(e, e);
						continue;
					}
				}
			}
		}
	}

	public String bufStatus() {
		String altBufStatus = "altBuf: " + altBuf.size() + "/"
				+ ClusterConf.get().agentAltBufSize + "\n";
		String sampleBufStatus = "sampleBuf: " + sampleBuf.size() + "/"
				+ ClusterConf.get().agentSampleBufSize + "\n";
		return altBufStatus + sampleBufStatus;
	}

}