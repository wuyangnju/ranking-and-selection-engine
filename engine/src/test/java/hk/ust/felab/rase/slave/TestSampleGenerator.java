package hk.ust.felab.rase.slave;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.junit.Test;

import umontreal.iro.lecuyer.rng.MRG32k3a;

public class TestSampleGenerator {
	@Test
	public void test() {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter("test.csv");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		MRG32k3a mrg32k3a = new MRG32k3a();
		for (int i = 0; i < 100000; i++) {
			for (int j = 0; j < 3; j++) {
				if (j != 0) {
					pw.print(",");
				}
				pw.print(mrg32k3a.nextDouble());
			}
			pw.println();
			mrg32k3a.resetNextSubstream();
		}
		pw.close();

	}
}
