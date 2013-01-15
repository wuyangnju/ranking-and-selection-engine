package hk.ust.felab.rase.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class TestBlockingQueue {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final BlockingQueue<Double> bq = new LinkedBlockingQueue<Double>(512);
		Executor executor = Executors.newFixedThreadPool(48);
		executor.execute(new Runnable() {
			@Override
			public void run() {
				bq.offer(Math.random());
			}
		});

		for (int i = 0; i < 47; i++) {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					bq.poll();
				}
			});
		}
	}
}
