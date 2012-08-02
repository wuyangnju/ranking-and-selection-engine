package hk.ust.felab.rase.agent;

import hk.ust.felab.rase.common.Conf;
import hk.ust.felab.rase.master.MasterService;
import hk.ust.felab.rase.slave.SlaveThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Resource;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

@Service
public class AgentService {

	private transient final Log log = LogFactory.getLog(this.getClass());

	private int altBufSize;

	@Resource
	private MasterService masterService;

	@Resource
	private HttpClient client;

	@Resource
	private Gson gson;

	private ArrayList<SlaveThread> slaveThreads = new ArrayList<SlaveThread>();

	private ArrayList<BlockingQueue<double[]>> altBuf = new ArrayList<BlockingQueue<double[]>>(
			2);
	private int curAltBuf = 0;
	private ReentrantLock altLock = new ReentrantLock(true);

	private BlockingQueue<double[]> sampleBuf;
	private Object sampleSignal = new Object();

	public void activate(String sampleGenerator, int altBufSize,
			int sampleBufSize, int sampleCountStep)
			throws IllegalArgumentException, SecurityException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException,
			ClassNotFoundException {

		this.altBufSize = altBufSize;

		for (int i = 0; i < 2; i++) {
			altBuf.set(i, new LinkedBlockingQueue<double[]>(altBufSize));
		}
		pullAlts(0);

		sampleBuf = new LinkedBlockingQueue<double[]>(sampleBufSize);

		Executor executor = Executors
				.newFixedThreadPool(Conf.localSlaveCount + 1);

		executor.execute(new PushSampleThread());

		for (int i = 0; i < Conf.localSlaveCount; i++) {
			slaveThreads.add(new SlaveThread(Conf.slaveIdOffset + i,
					sampleGenerator, sampleCountStep, this));
			executor.execute(slaveThreads.get(i));
		}

		new Thread(new AsyncPullAlts(1)).start();
	}

	public double[] getAlt() throws InterruptedException {
		altLock.lock();
		double[] alt = altBuf.get(curAltBuf).poll();
		try {
			if (alt != null) {
				return alt;
			} else {
				new Thread(new AsyncPullAlts(curAltBuf)).start();
				curAltBuf = (curAltBuf + 1) % 2;
				return altBuf.get(curAltBuf).take();
			}
		} finally {
			altLock.unlock();
		}
	}

	void pullAlts(int altBufIndex) {
		double[][] alts = null;
		if (Conf.masterActivated) {
			alts = masterService.getAlts(altBufSize);
		} else {
			PostMethod method = new PostMethod(Conf.masterUrl + Conf.GET_ALTS);
			method.addParameter("altBufSize", String.valueOf(altBufSize));
			String altSystemsJson = null;
			try {
				client.executeMethod(method);
				BufferedReader br = new BufferedReader(new InputStreamReader(
						method.getResponseBodyAsStream()));
				altSystemsJson = br.readLine();
			} catch (HttpException e) {
				log.error(e, e);
			} catch (IOException e) {
				log.error(e, e);
			}
			alts = gson.fromJson(altSystemsJson, double[][].class);
		}
		for (double[] alt : alts) {
			altBuf.get(altBufIndex).offer(alt);
		}
	}

	public void putSample(long altId, double sample)
			throws InterruptedException {
		sampleBuf.put(new double[] { altId, sample });
		sampleSignal.notify();
	}

	void pushSample(double[][] samples) {
		if (Conf.masterActivated) {
			masterService.putSamples();
		} else {
			PostMethod method = new PostMethod(Conf.masterUrl
					+ Conf.SUBMIT_SAMPLE);
			String samplesJson = gson.toJson(samples);
			method.addParameter("samplesJson", samplesJson);
			try {
				client.executeMethod(method);
			} catch (HttpException e) {
				log.error(e, e);
			} catch (IOException e) {
				log.error(e, e);
			}
		}
	}

	class AsyncPullAlts implements Runnable {
		private int altBufIndex;

		public AsyncPullAlts(int altBufIndex) {
			this.altBufIndex = altBufIndex;
		}

		@Override
		public void run() {
			pullAlts(this.altBufIndex);
		}

	}

	class PushSampleThread implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					ArrayList<double[]> samples;
					int batchSize;
					while ((batchSize = sampleBuf.size()) > 0) {
						samples = new ArrayList<double[]>();
						for (int i = 0; i < batchSize; i++) {
							samples.add(sampleBuf.poll());
						}
						pushSample(samples.toArray(new double[][] {}));
					}
					sampleSignal.wait();
				} catch (InterruptedException e) {
					log.error(e, e);
				}
			}
		}
	}

}