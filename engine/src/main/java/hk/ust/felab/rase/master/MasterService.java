package hk.ust.felab.rase.master;

import hk.ust.felab.rase.conf.ClusterConf;
import hk.ust.felab.rase.conf.RasConf;
import hk.ust.felab.rase.util.GsonUtil;
import hk.ust.felab.rase.util.IndexedPriorityQueue;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class MasterService {
	private transient final Logger log = Logger.getLogger(getClass());
	private transient final Logger perf1 = Logger.getLogger("master.perf1");
	private transient final Logger perf2 = Logger.getLogger("master.perf2");
	private transient final Logger sampleLog = Logger
			.getLogger("master.sample");
	private transient final Logger siftLog = Logger.getLogger("master.sift");
	private transient final Logger elimationLog = Logger
			.getLogger("master.elimation");
	private transient final Logger elimation2Log = Logger
			.getLogger("master.elimation2");
	private transient final Logger bufLog = Logger.getLogger("master.buf");
	private transient final Logger resultLog = Logger.getLogger("ras.result");

	private double[][] altsArgs;

	private Alt[] alts;

	private Queue<Alt> altRepo;
	private BlockingQueue<Alt> altBuf;
	private BlockingQueue<double[]> sampleBuf;

	private volatile int survivalCount;
	private IndexedPriorityQueue<Alt> alts1;
	private IndexedPriorityQueue<Alt> alts2;
	private IndexedPriorityQueue<Alt> alts3;

	private AtomicInteger result = new AtomicInteger(-1);

	private long sampleCount = 0;
	private int elimationCount = 0;
	private volatile int secondStageCount = 0;

	/**
	 * 
	 * @param sign
	 *            1(selecting min) or -1(selecting max)
	 */
	public void activate(double[][] altsArgs) throws IllegalArgumentException,
			SecurityException, InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException,
			ClassNotFoundException {
		this.altsArgs = altsArgs;

		survivalCount = RasConf.get().k;

		alts = new Alt[RasConf.get().k];

		for (int i = 0; i < RasConf.get().k; i++) {
			alts[i] = new Alt(this.altsArgs[i]);
		}

		altRepo = new LinkedList<Alt>();
		for (int i = 0; i < RasConf.get().n0; i++) {
			altRepo.addAll(Arrays.asList(alts));
		}

		altBuf = new LinkedBlockingQueue<Alt>(
				ClusterConf.get().masterAltBufSize);
		sampleBuf = new LinkedBlockingQueue<double[]>(
				ClusterConf.get().masterSampleBufSize);

		alts1 = new IndexedPriorityQueue<Alt>(RasConf.get().k,
				new Comparator<Alt>() {
					@Override
					public int compare(Alt alt1, Alt alt2) {
						return (int) (alt1.key1() - alt2.key1());
					}
				}, 0);
		alts2 = new IndexedPriorityQueue<Alt>(RasConf.get().k,
				new Comparator<Alt>() {
					@Override
					public int compare(Alt alt1, Alt alt2) {
						return (int) (alt2.key2() - alt1.key2());
					}
				}, 1);
		alts3 = new IndexedPriorityQueue<Alt>(RasConf.get().k,
				new Comparator<Alt>() {
					@Override
					public int compare(Alt alt1, Alt alt2) {
						return (int) (alt1.key3() - alt2.key3());
					}
				}, 2);

		new Thread(new ProduceAltThread(), "Master - produce alt").start();
		new Thread(new ConsumeSampleThread(), "Master - consume sample")
				.start();
	}

	public double[][] getAlts(int demand) throws InterruptedException {
		double[][] retAlts = new double[demand][];
		for (int i = 0; i < demand; i++) {
			Alt alt = altBuf.take();
			// weak consistency, not critical
			while (!alt.isSurviving()) {
				alt = altBuf.take();
			}
			retAlts[i] = alt.argSnapshot();
		}
		return retAlts;
	}

	private int index = 0;

	class ProduceAltThread implements Runnable {

		private void indexInc() {
			index++;
			if (index == RasConf.get().k) {
				index = 0;
			}
		}

		@Override
		public void run() {
			while (true) {
				try {
					if (!altRepo.isEmpty()) {
						altBuf.put(altRepo.poll());
					} else {
						while (!alts[index].isSurviving()) {
							indexInc();
						}
						Alt alt = alts[index];
						if (secondStageCount < RasConf.get().k) {
							altBuf.put(alts[index]);
						} else {
							int batch = Math
									.max(1,
											(int) (alt.thetaOverY() / Alt.minThetaOverSqrtY));
							for (int i = 0; i < batch; i++) {
								altBuf.put(alts[index]);
							}
						}
						indexInc();
					}
				} catch (InterruptedException e) {
					log.warn(e, e);
					continue;
				}
			}
		}
	}

	public void putSamples(double[][] samples) throws InterruptedException {
		for (double[] sample : samples) {
			sampleBuf.put(sample);
		}
	}

	private void rasSuccess() {
		resultLog.info(GsonUtil.gsonDes().toJson(ClusterConf.get()) + "\n");
		resultLog.info(GsonUtil.gsonDes().toJson(RasConf.get()) + "\n");
		resultLog.info(RasConf.get().trialId + "," + alts1.peek().getId()
				+ "\n");
		logAfterProcessSample();
		siftLog.trace("\n");
		LogManager.shutdown();
		result.set(alts1.peek().getId());
		synchronized (this) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				log.warn(e, e);
			}
		}
	}

	private void remove(Alt alt) {
		int[] sift = new int[3];
		try {
			sift[0] = alts1.myRemove(alt);
			sift[1] = alts2.myRemove(alt);
			sift[2] = alts3.myRemove(alt);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println(alt.toString());
		}
		siftLog.trace("," + sift[0] + "," + sift[1] + "," + sift[2]);
		alt.setSurviving(false);
		elimationLog.trace(System.currentTimeMillis() + "," + alt.getId()
				+ "\n");
		elimationCount++;
		survivalCount--;
		if (survivalCount == 1) {
			rasSuccess();
		}
	}

	void processSample(int altId, int sampleId, double sample, long simTime) {
		int[] sift = new int[] { 0, 0, 0 };
		double k1, k2, k3;
		Alt alt0 = alts[altId - 1], alt1;
		if (!alt0.isSurviving()) {
			siftLog.trace("-1\n");
			return;
		}
		if (alt0.num() > RasConf.get().n0) {
			k1 = alt0.key1();
			k2 = alt0.key2();
			k3 = alt0.key3();
			alt0.addSample(sampleId, sample, simTime);

			if (alt0.key1() < k1) {
				sift[0] += alts1.siftUp(alt0);
			} else {
				sift[0] += alts1.siftDown(alt0);
			}
			if (alt0.key2() > k2) {
				sift[1] += alts2.siftUp(alt0);
			} else {
				sift[1] += alts2.siftDown(alt0);
			}
			if (alt0.key3() < k3) {
				sift[2] += alts3.siftUp(alt0);
			} else {
				sift[2] += alts3.siftDown(alt0);
			}
		} else if (alt0.num() == RasConf.get().n0) {
			alt0.addSample(sampleId, sample, simTime);
			sift[0] += alts1.myOffer(alt0);
			sift[1] += alts2.myOffer(alt0);
			sift[2] += alts3.myOffer(alt0);
		} else if (alt0.num() < RasConf.get().n0) {
			alt0.addSample(sampleId, sample, simTime);
		}

		if (alt0.num() == RasConf.get().n0) {
			secondStageCount++;
		}

		siftLog.trace(sift[0] + "," + sift[1] + "," + sift[2]);

		if (alts1.size() < 2) {
			siftLog.trace("\n");
			return;
		}
		// eliminate by mean immediately
		alt1 = alts1.peek();
		if (alt0 == alt1) {
			alt1 = alts1.peekSecond();
		}
		while ((alt0.key1() + alt1.key1()) < (RasConf.get().b / RasConf.get().a)) {
			if (RasConf.get().min ? (alt0.mean() > alt1.mean())
					: (alt0.mean() < alt1.mean())) {
				remove(alt0);
				siftLog.trace("\n");
				return;
			} else {
				remove(alt1);
				if (alts1.size() < 2) {
					siftLog.trace("\n");
					return;
				}
				alt1 = alts1.peek();
				if (alt0 == alt1) {
					alt1 = alts1.peekSecond();
				}
			}
		}

		if (RasConf.get().min) {
			if (alts1.size() < 2) {
				siftLog.trace("\n");
				return;
			}
			// eliminate others
			alt1 = alts2.peek();
			if (alt0 == alt1) {
				alt1 = alts2.peekSecond();
			}
			// while (alt1.moreThan(alt0, a, b)) {
			while (alt0.lessThan(alt1)) {
				remove(alt1);
				if (alts1.size() < 2) {
					siftLog.trace("\n");
					return;
				}
				alt1 = alts2.peek();
				if (alt0 == alt1) {
					alt1 = alts2.peekSecond();
				}
			}

			if (alts1.size() < 2) {
				siftLog.trace("\n");
				return;
			}
			// survive itself
			alt1 = alts3.peek();
			if (alt0 == alt1) {
				alt1 = alts3.peekSecond();
			}
			// if (alt0.moreThan(alt1, a, b)) {
			if (alt1.lessThan(alt0)) {
				remove(alt0);
				siftLog.trace("\n");
				return;
			}
		} else {
			if (alts1.size() < 2) {
				siftLog.trace("\n");
				return;
			}
			// eliminate others
			alt1 = alts3.peek();
			if (alt0 == alt1) {
				alt1 = alts3.peekSecond();
			}
			while (alt1.lessThan(alt0)) {
				remove(alt1);
				if (alts1.size() < 2) {
					siftLog.trace("\n");
					return;
				}
				alt1 = alts3.peek();
				if (alt0 == alt1) {
					alt1 = alts3.peekSecond();
				}
			}

			if (alts1.size() < 2) {
				siftLog.trace("\n");
				return;
			}
			// survive itself
			alt1 = alts2.peek();
			if (alt0 == alt1) {
				alt1 = alts2.peekSecond();
			}
			if (alt0.lessThan(alt1)) {
				remove(alt0);
				siftLog.trace("\n");
				return;
			}
		}
		siftLog.trace("\n");
	}

	private void logAfterProcessSample() {
		perf1.trace(System.currentTimeMillis() + "\n");
		elimation2Log.trace(elimationCount + "\n");
		bufLog.trace(altBuf.size() + "," + sampleBuf.size() + "\n");
		sampleCount++;
		if (sampleCount % RasConf.get().sampleCountStep == 0) {
			perf2.trace(System.currentTimeMillis() + "," + sampleCount + ","
					+ survivalCount + "," + secondStageCount + "\n");
		}
	}

	class ConsumeSampleThread implements Runnable {
		@Override
		public void run() {
			double[] sample = null;
			perf1.trace("," + System.currentTimeMillis() + "\n");
			while (true) {
				try {
					sample = sampleBuf.take();
					sampleLog.trace((int) sample[0] + "," + sample[1] + ","
							+ sample[2] + "\n");
					elimationCount = 0;
					perf1.trace(System.currentTimeMillis() + ",");
					processSample((int) sample[0], (int) sample[3], sample[1],
							(long) sample[2]);
				} catch (InterruptedException e) {
					log.warn(e, e);
					continue;
				} finally {
					logAfterProcessSample();
				}
			}
		}
	}

	public String rasStatus() {
		if (survivalCount > 10) {
			return String.valueOf(survivalCount);
		} else {
			StringBuilder sb = new StringBuilder("");
			sb.append("survivalCount: " + survivalCount + " secondStageCount: "
					+ secondStageCount + " ");
			for (Alt alt : alts) {
				if (alt.isSurviving()) {
					sb.append(alt.toString());
				}
			}
			return sb.toString();
		}
	}

	public String bufStatus() {
		String altBufStatus = "altBuf: " + altBuf.size() + "/"
				+ ClusterConf.get().masterAltBufSize + "\n";
		String sampleBufStatus = "sampleBuf: " + sampleBuf.size() + "/"
				+ ClusterConf.get().masterSampleBufSize + "\n";
		return altBufStatus + sampleBufStatus;
	}

	public int result() {
		return result.get();
	}
}
