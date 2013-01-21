package hk.ust.felab.rase.master;

import hk.ust.felab.rase.conf.ConfLoader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.concurrent.FutureTask;

public class MasterTest {
	static Master master;

	static class MockProduceSampleThread implements Runnable {
		@Override
		public void run() {
			String log = "src/test/resources/master_sample.csv";
			BufferedReader br;
			try {
				br = new BufferedReader(new InputStreamReader(
						new FileInputStream(log)));
				String line = null;
				while ((line = br.readLine()) != null) {
					String[] fields = line.split(",");
					double[] sample = new double[fields.length];
					for (int i = 0; i < fields.length; i++) {
						sample[i] = Double.parseDouble(fields[i]);
					}
					master.putSample(sample);
				}
				br.close();
			} catch (InterruptedException e) {
				System.out.println("mock interrupted");
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("mock finished");
		}
	}

	public static void main(String[] args) throws Exception {
		String rasConf = "src/main/op-scripts/conf/case1.ras";
		String altsConf = "src/main/op-scripts/conf/case1.100.alts";
		ConfLoader.loadConf(new String[] { rasConf, altsConf });
		master = new Master();

		Thread mock = new Thread(new MockProduceSampleThread());
		mock.start();
		FutureTask<Integer> future = new FutureTask<Integer>(
				master.getSampleConsumer());
		new Thread(future).start();
		System.out.println(future.get());
	}
}
