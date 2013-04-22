package hk.ust.felab.rase;

import org.junit.Test;

public class HeadQuartersTest {
	@Test
	public void testRinottRas() throws Exception {
		String[] args = new String[7];

		int i = 0;
		args[i++] = ClassLoader.getSystemResource("normal.5.alts").getPath(); // alts
		args[i++] = ClassLoader.getSystemResource("RinottRas.class").getPath();
		args[i++] = "0.05 1 10 68.2226"; // rasArgs
		args[i++] = ClassLoader.getSystemResource("NormalSim.class").getPath();
		args[i++] = ""; // simArgs
		args[i++] = "5"; // repeatTime
		args[i++] = "tmp"; // logDir

		Headquarters.main(args);
	}

	@Test
	public void testTableFillingRas() throws Exception {
		String[] args = new String[7];

		int i = 0;
		args[i++] = ClassLoader.getSystemResource("normal.5.alts").getPath(); // alts
		args[i++] = ClassLoader.getSystemResource("TableFillingRas.class")
				.getPath();
		args[i++] = "0.05 1 10 68.2226"; // rasArgs
		args[i++] = ClassLoader.getSystemResource("NormalSim.class").getPath();
		args[i++] = ""; // simArgs
		args[i++] = "5"; // repeatTime
		args[i++] = "tmp"; // logDir

		Headquarters.main(args);
	}

	@Test
	public void testParallelSequentialRas() throws Exception {
		String[] args = new String[7];

		int i = 0;
		args[i++] = ClassLoader.getSystemResource("normal.5.alts").getPath(); // alts
		args[i++] = ClassLoader
				.getSystemResource("ParallelSequentialRas.class").getPath();
		args[i++] = "0.05 1 10 68.2226"; // rasArgs
		args[i++] = ClassLoader.getSystemResource("NormalSim.class").getPath();
		args[i++] = ""; // simArgs
		args[i++] = "5"; // repeatTime
		args[i++] = "tmp"; // logDir

		Headquarters.main(args);
	}
}
