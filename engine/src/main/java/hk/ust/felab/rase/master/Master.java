package hk.ust.felab.rase.master;

import hk.ust.felab.rase.agent.Agent;
import hk.ust.felab.rase.conf.Conf;
import hk.ust.felab.rase.util.IndexedPriorityQueue;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

public class Master implements Agent {
	private static final int ALT_BUF_SIZE = 1024;
	private static final int SAMPLE_BUF_SIZE = 1024;

	private transient final Logger perf1 = Logger.getLogger("master.perf1");
	private transient final Logger perf2 = Logger.getLogger("master.perf2");
	private transient final Logger sampleLog = Logger
			.getLogger("master.sample");
	private transient final Logger siftLog = Logger.getLogger("master.sift");
	private transient final Logger elimationLog = Logger
			.getLogger("master.elimation");
	private transient final Logger resultLog = Logger.getLogger("ras.result");
	private transient final Logger getAltLog = Logger
			.getLogger("master.getalt");
	private transient final Logger prepAltLog = Logger
			.getLogger("master.prepalt");

	private Lock altsLock = new ReentrantLock();
	private Alt[] alts;

	private BlockingQueue<Alt> altBuf;
	private BlockingQueue<Sample> sampleBuf;

	private IndexedPriorityQueue<Alt> alts1;
	private IndexedPriorityQueue<Alt> alts2;
	private IndexedPriorityQueue<Alt> alts3;

	private int sampleCount = 0;
	private int secondStageCount = 0;
	private int eliminatedCount = 0;

	private Runnable altProducer;
	private Callable<Integer> sampleConsumer;

	public Master() {

		alts = new Alt[Conf.current().getAlts().length];

		for (int i = 0; i < Conf.current().getAlts().length; i++) {
			alts[i] = new Alt(Conf.current().getAlts()[i]);
		}

		for (int i = 0; i < alts.length; i++) {
			alts[i].prev = alts[(i - 1 + alts.length) % alts.length];
			alts[i].next = alts[(i + 1) % alts.length];
		}

		altBuf = new LinkedBlockingQueue<Alt>(ALT_BUF_SIZE);

		sampleBuf = new LinkedBlockingQueue<Sample>(SAMPLE_BUF_SIZE);

		alts1 = new IndexedPriorityQueue<Alt>(Conf.current().getK(),
				new Comparator<Alt>() {
					@Override
					public int compare(Alt alt1, Alt alt2) {
						return alt1.key1() > alt2.key1() ? 1 : -1;
					}
				}, 0);
		alts2 = new IndexedPriorityQueue<Alt>(Conf.current().getK(),
				new Comparator<Alt>() {
					@Override
					public int compare(Alt alt1, Alt alt2) {
						return alt1.key2() < alt2.key2() ? 1 : -1;
					}
				}, 1);
		alts3 = new IndexedPriorityQueue<Alt>(Conf.current().getK(),
				new Comparator<Alt>() {
					@Override
					public int compare(Alt alt1, Alt alt2) {
						return alt1.key3() > alt2.key3() ? 1 : -1;
					}
				}, 2);

		this.altProducer = new ProduceAltThread();
		this.sampleConsumer = new ConsumeSampleThread();
	}

	public Runnable getAltProducer() {
		return altProducer;
	}

	public Callable<Integer> getSampleConsumer() {
		return sampleConsumer;
	}

	/**
	 * If this method is not called, the default initial seed is (12345, 12345,
	 * 12345, 12345, 12345, 12345). If it is called, the first 3 values of the
	 * seed must all be less than m1 = 4294967087, and not all 0; and the last 3
	 * values must all be less than m2 = 4294944443, and not all 0.
	 */
	private class ProduceAltThread implements Runnable {

		@Override
		public void run() {
			Thread.currentThread().setName("Master - produce alt");
			try {
				for (int i = 0; i < Conf.current().getN0(); i++) {
					// do not use batch add like
					// altBuf.addAll(Arrays.asList(alts));
					// in case batch size(k) exceeds altBuf size.
					for (Alt alt : alts) {
						altBuf.put(alt);
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Alt alt = alts[0].prev;
			while (true) {
				try {
					altsLock.lock();
					alt = alt.next;
					while (!alt.isSurviving()) {
						alt = alt.next;
					}
				} finally {
					altsLock.unlock();
				}
				try {
					if (secondStageCount < Conf.current().getK()) {
						prepAltLog.trace(alt.getId() + "," + 1 + "\n");
						altBuf.put(alt);
					} else {
						double batch = alt.thetaOverY() / Alt.minThetaOverSqrtY;
						batch = Math.max(1, batch);
						prepAltLog.trace(alt.getId() + "," + batch + "\n");
						for (int j = 0; j < batch; j++) {
							altBuf.put(alt);
						}
					}
				} catch (InterruptedException e) {
					return;
				}
			}
		}
	}

	private class ConsumeSampleThread implements Callable<Integer> {
		private void remove(Alt alt) {
			if (!alt.isSurviving())
				return;
			try {
				altsLock.lock();
				alt.setSurviving(false);
				// delete current node from a doubly linked list
				alt.prev.next = alt.next;
				alt.next.prev = alt.prev;
			} finally {
				altsLock.unlock();
			}
			eliminatedCount++;

			siftLog.trace("," + alts1.myRemove(alt) + "," + alts2.myRemove(alt)
					+ "," + alts3.myRemove(alt));
			elimationLog.trace(sampleCount + "," + alt.getId() + "\n");
		}

		private void adoptSample(Alt alt, Sample sampleObj) {
			double sample = sampleObj.sample();
			long simTime = sampleObj.simTime();
			int[] sift = new int[] { 0, 0, 0 };
			double k1, k2, k3;

			if (alt.num() > Conf.current().getN0() - 1) {
				k1 = alt.key1();
				k2 = alt.key2();
				k3 = alt.key3();
				alt.addSample(sample, simTime);
				if (alt.key1() < k1) {
					sift[0] += alts1.siftUp(alt);
				} else {
					sift[0] += alts1.siftDown(alt);
				}
				if (alt.key2() > k2) {
					sift[1] += alts2.siftUp(alt);
				} else {
					sift[1] += alts2.siftDown(alt);
				}
				if (alt.key3() < k3) {
					sift[2] += alts3.siftUp(alt);
				} else {
					sift[2] += alts3.siftDown(alt);
				}
			} else if (alt.num() == Conf.current().getN0() - 1) {
				alt.addSample(sample, simTime);
				sift[0] += alts1.myOffer(alt);
				sift[1] += alts2.myOffer(alt);
				sift[2] += alts3.myOffer(alt);
				secondStageCount++;
			} else if (alt.num() < Conf.current().getN0() - 1) {
				alt.addSample(sample, simTime);
			}

			siftLog.trace(sift[0] + "," + sift[1] + "," + sift[2]);
		}

		private void checkElimination1(Alt alt) {
			if (alts1.size() < 2) {
				return;
			}
			// eliminate by mean immediately
			Alt alt1 = alts1.peekExcept(alt);
			while ((alt.key1() + alt1.key1()) < (Conf.current().getB() / Conf
					.current().getA())) {
				if (Conf.current().isMin() ? (alt.mean() > alt1.mean()) : (alt
						.mean() < alt1.mean())) {
					remove(alt);
					return;
				} else {
					remove(alt1);
					if (alts1.size() < 2) {
						return;
					}
					alt1 = alts1.peekExcept(alt);
				}
			}
		}

		private void checkElimination2(Alt alt0) {
			if (alts1.size() < 2) {
				return;
			}
			if (Conf.current().isMin()) {
				// eliminate others
				Alt alt1 = alts2.peekExcept(alt0);
				while (alt0.lessThan(alt1)) {
					remove(alt1);
					if (alts1.size() < 2) {
						return;
					}
					alt1 = alts2.peekExcept(alt0);
				}

				if (alts1.size() < 2) {
					return;
				}
				// survive itself
				alt1 = alts3.peekExcept(alt0);
				if (alt1.lessThan(alt0)) {
					remove(alt0);
					return;
				}
			} else {
				// eliminate others
				Alt alt1 = alts3.peekExcept(alt0);
				while (alt1.lessThan(alt0)) {
					remove(alt1);
					if (alts1.size() < 2) {
						return;
					}
					alt1 = alts3.peekExcept(alt0);
				}

				if (alts1.size() < 2) {
					return;
				}
				// survive itself
				alt1 = alts2.peekExcept(alt0);
				if (alt0.lessThan(alt1)) {
					remove(alt0);
					return;
				}
			}
		}

		private void consumeSample(Sample sampleObj) {
			Alt alt = alts[sampleObj.altId() - 1];
			if (!alt.isSurviving()) {
				siftLog.trace("-1");
				return;
			}
			adoptSample(alt, sampleObj);
			if (alt.num() < Conf.current().getN0()) {
				return;
			}
			checkElimination1(alt);
			checkElimination2(alt);
		}

		@Override
		public Integer call() throws Exception {
			Thread.currentThread().setName("Master - consume sample");
			long start = System.currentTimeMillis(), perf1End = start, perf1Start;
			Sample sample = null;
			while (true) {
				perf1Start = perf1End;
				sample = sampleBuf.take();
				perf1End = System.currentTimeMillis();
				perf1.trace((perf1End - perf1Start) + ",");

				sampleCount++;
				sampleLog.trace(sample.altId() + "," + sample.sample() + ","
						+ sample.simTime() + "\n");

				perf1Start = perf1End;
				consumeSample(sample);
				perf1End = System.currentTimeMillis();
				perf1.trace((perf1End - perf1Start) + "\n");

				siftLog.trace("\n");
				if (sampleCount % Conf.current().getSampleCountStep() == 0) {
					perf2.trace((System.currentTimeMillis() - start) + ","
							+ sampleCount + "," + secondStageCount + ","
							+ eliminatedCount + "," + altBuf.size() + ","
							+ sampleBuf.size() + "\n");
				}

				if (eliminatedCount == Conf.current().getK() - 1) {
					resultLog.info(alts1.peek().getId() + "\n");
					return alts1.peek().getId();
				}
			}
		}
	}

	public double[][] getAlts(int demand) throws InterruptedException {
		double[][] retAlts = new double[demand][];
		for (int i = 0; i < demand; i++) {
			retAlts[i] = getAlt();
		}
		return retAlts;
	}

	@Override
	public double[] getAlt() throws InterruptedException {
		long start = System.currentTimeMillis(), count = 1;
		Alt alt = altBuf.take();
		// weak consistency, not critical
		while (!alt.isSurviving()) {
			count++;
			alt = altBuf.take();
		}
		getAltLog.trace((System.currentTimeMillis() - start) + "," + count
				+ "\n");
		return alt.argSnapshot();
	}

	public void putSamples(double[][] samplesArr) throws InterruptedException {
		for (double[] sampleArr : samplesArr) {
			sampleBuf.put(new Sample(sampleArr));
		}
	}

	@Override
	public void putSample(double[] sampleArr) throws InterruptedException {
		sampleBuf.put(new Sample(sampleArr));
	}

}
