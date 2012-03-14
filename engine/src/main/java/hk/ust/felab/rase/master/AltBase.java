package hk.ust.felab.rase.master;

import java.util.concurrent.locks.ReentrantLock;

public abstract class AltBase {

	abstract protected int getId();

	abstract protected double[] getArgs();

	abstract protected double[] getData();

	private final ReentrantLock lock = new ReentrantLock(true);

	abstract protected long doAddSample(double[] sample);

	public long addSample(double[] sample) {
		lock.lock();
		try {
			return doAddSample(sample);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * @return {id, args[0],args[1]...}
	 */
	public double[] argSnapshot() {
		lock.lock();
		try {
			double[] snapshot = new double[getArgs().length + 1];
			snapshot[0] = getId();
			System.arraycopy(getArgs(), 0, snapshot, 1, getArgs().length);
			return snapshot;
		} finally {
			lock.unlock();
		}

	}

	/**
	 * @return {id, data[0],data[1]...}
	 */
	public double[] dataSnapshot() {
		lock.lock();
		try {
			double[] snapshot = new double[getData().length + 1];
			snapshot[0] = getId();
			System.arraycopy(getData(), 0, snapshot, 1, getData().length);
			return snapshot;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * @return {id, args[0],args[1]...data[0],data[1]...}
	 */
	public double[] altSnapshot() {
		lock.lock();
		try {
			double[] snapshot = new double[getArgs().length + getData().length
					+ 1];
			snapshot[0] = getId();
			System.arraycopy(getArgs(), 0, snapshot, 1, getArgs().length);
			System.arraycopy(getData(), 0, snapshot, 1 + getArgs().length,
					getData().length);
			return snapshot;
		} finally {
			lock.unlock();
		}
	}
}
