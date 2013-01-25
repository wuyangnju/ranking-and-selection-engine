package hk.ust.felab.rase;

import hk.ust.felab.rase.conf.Conf;
import hk.ust.felab.rase.master.Master;
import hk.ust.felab.rase.sim.SampleGen;
import hk.ust.felab.rase.slave.Slave;
import hk.ust.felab.rase.util.SampleGenClassLoader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;

public class Headquarters {

	/**
	 * TODO enhance cmdline args by common-cli
	 * 
	 * @param args
	 *            rasConf altsConf repeatTime logDir simThreadNum
	 */
	public static void main(String[] args) throws Exception {
		Conf.loadFromCmdArgs(args);

		int repeatTime = Integer.parseInt(args[2]);
		String logDir = args[3];
		// TODO available thread # detection
		int simThreadNum = Integer.parseInt(args[4]);

		String className = "hk.ust.felab.rase.sim.impl."
				+ Conf.current().getSampleGenerator();
		SampleGen[] sampleGens = new SampleGen[simThreadNum];

		for (int j = 1; j <= repeatTime; j++) {
			System.setProperty("log.dir", logDir + "/" + j);
			PropertyConfigurator.configure(ClassLoader
					.getSystemResourceAsStream("log4j.properties"));

			for (int i = 0; i < simThreadNum; i++) {
				SampleGenClassLoader classLoader = new SampleGenClassLoader();
				classLoader.loadClass(className);
				SampleGen sampleGen = (SampleGen) Class
						.forName(className, true, classLoader)
						.getConstructor(Integer.class).newInstance(i);
				sampleGens[i] = sampleGen;
			}

			Master master = new Master();
			Slave[] slaves = new Slave[simThreadNum];
			for (int i = 0; i < simThreadNum; i++) {
				slaves[i] = new Slave(i, sampleGens[i], master);
			}

			ExecutorService executorService = Executors
					.newFixedThreadPool(simThreadNum + 2);
			executorService.execute(master.getAltProducer());
			for (int i = 0; i < simThreadNum; i++) {
				executorService.execute(slaves[i].getThread());
			}
			Future<Integer> future = executorService.submit(master
					.getSampleConsumer());

			System.out.println(j + "," + future.get());
			executorService.shutdownNow();

			LogManager.shutdown();
		}
	}

}