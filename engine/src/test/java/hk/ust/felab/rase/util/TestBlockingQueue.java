package hk.ust.felab.rase.util;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TestBlockingQueue {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// final BlockingQueue<Integer> bq = new LinkedBlockingQueue<Integer>();
		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		// try {
		// Thread.sleep(500);
		// bq.put(1);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// }
		// }).start();
		//
		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		// try {
		// System.out.println(bq.take());
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// }
		// }).start();

//		final BlockingQueue<Integer> bq = new LinkedBlockingQueue<Integer>(3);
//		bq.addAll(Arrays.asList(new Integer[] { 1, 2, 3, 4 }));
		System.out.println((-1)%5);
	}
}
