package hk.ust.felab.rase;

import hk.ust.felab.rase.conf.ClusterConf;
import hk.ust.felab.rase.conf.ConfLoader;
import hk.ust.felab.rase.conf.RasConf;
import hk.ust.felab.rase.master.Master;
import hk.ust.felab.rase.slave.Slave;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Headquarters {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		ConfLoader.loadConf(args);

		Master master = new Master();
		List<Slave> slaves = new ArrayList<Slave>(
				ClusterConf.get().slaveLocalCount);
		for (int i = ClusterConf.get().slaveIdOffset; i < ClusterConf.get().slaveLocalCount; i++) {
			slaves.add(new Slave(i, RasConf.get().sampleGenerator, master));
		}

		ExecutorService executorService = Executors
				.newFixedThreadPool(ClusterConf.get().slaveLocalCount + 2);
		executorService.execute(master.getAltProducer());
		for (int i = 0; i < ClusterConf.get().slaveLocalCount; i++) {
			executorService.execute(slaves.get(i).getThread());
		}
		Future<Integer> future = executorService.submit(master
				.getSampleConsumer());
		System.out.println(future.get());
		executorService.shutdownNow();
		System.out.println("we are done");
	}

}
