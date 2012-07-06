package hk.ust.felab.rase.master;

import hk.ust.felab.rase.common.Conf;
import hk.ust.felab.rase.master.impl.Alt;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.stereotype.Service;

@Service
public class RasService {
	private Map<Integer, Alt> alts;
	private List<Alt> removedAlts;
	private ReentrantReadWriteLock rwl;

	public void init(double[][] altsArgs) {
		alts = new LinkedHashMap<Integer, Alt>();
		for (double[] altArgs : altsArgs) {
			alts.put((int) altArgs[0], new Alt(altArgs));
		}
		removedAlts = new LinkedList<Alt>();
		rwl = new ReentrantReadWriteLock(true);
	}

	public long addSample(int altId, double[] samples) {
		rwl.readLock().lock();
		try {
			if (alts.containsKey(altId)) {
				return alts.get(altId).addSample(samples);
			} else {
				return Conf.SYSTEM_NOT_FOUND;
			}
		} finally {
			rwl.readLock().unlock();
		}
	}

	public void removeAlts(int[] altsId) {
		rwl.writeLock().lock();
		try {
			for (int altId : altsId) {
				if (alts.containsKey(altId)) {
					removedAlts.add(alts.get(altId));
					alts.remove(altId);
				}
			}
		} finally {
			rwl.writeLock().unlock();
		}
	}

	public long surviveCount() {
		rwl.readLock().lock();
		try {
			return alts.size();
		} finally {
			rwl.readLock().unlock();
		}
	}

	public double[][] deletedSnapshot() {
		rwl.readLock().lock();
		try {
			double[][] snapshot = new double[removedAlts.size()][];
			int i = 0;
			for (Alt alt : removedAlts) {
				snapshot[i++] = alt.altSnapshot();
			}
			return snapshot;
		} finally {
			rwl.readLock().unlock();
		}
	}

	public double[][] argSnapshot() {
		rwl.readLock().lock();
		try {
			double[][] snapshot = new double[alts.size()][];
			int i = 0;
			for (Alt alt : alts.values()) {
				snapshot[i++] = alt.argSnapshot();
			}
			return snapshot;
		} finally {
			rwl.readLock().unlock();
		}
	}

	public double[][] dataSnapshot() {
		rwl.readLock().lock();
		try {
			double[][] snapshot = new double[alts.size()][];
			int i = 0;
			for (Alt alt : alts.values()) {
				snapshot[i++] = alt.dataSnapshot();
			}
			return snapshot;
		} finally {
			rwl.readLock().unlock();
		}
	}

	public double[][] altSnapshot() {
		rwl.readLock().lock();
		try {
			double[][] snapshot = new double[alts.size()][];
			int i = 0;
			for (Alt alt : alts.values()) {
				snapshot[i++] = alt.altSnapshot();
			}
			return snapshot;
		} finally {
			rwl.readLock().unlock();
		}
	}
}
