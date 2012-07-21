package hk.ust.felab.rase.slave;
import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

public class TestSampleGenerator {
	public static void main(String[] args) {
		MRG32k3a mrg32k3a = new MRG32k3a();
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				System.out.println(NormalGen.nextDouble(mrg32k3a, 0, 1));
			}
			System.out.println();
			mrg32k3a.resetNextSubstream();
		}
	}
}
