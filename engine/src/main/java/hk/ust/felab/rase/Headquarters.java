package hk.ust.felab.rase;

import hk.ust.felab.rase.conf.Conf;
import hk.ust.felab.rase.master.Master;
import hk.ust.felab.rase.slave.Slave;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Headquarters {

	/**
	 * @param args
	 *            rasConf altsConf simThreadNum
	 * 
	 */
	public static void main(String[] args) throws Exception {
		Conf.loadFromCmdArgs(args);

		// available thread detection
		int simThreadNum = 6;

		Master master = new Master();

		ExecutorService executorService = Executors
				.newFixedThreadPool(simThreadNum + 2);
		executorService.execute(master.getAltProducer());
		for (int i = 0; i < simThreadNum; i++) {
			executorService.execute((new Slave(i, master)).getThread());
		}
		Future<Integer> future = executorService.submit(master
				.getSampleConsumer());
		System.out.println(future.get());
		executorService.shutdownNow();
		System.out.println("we are done");
	}

}