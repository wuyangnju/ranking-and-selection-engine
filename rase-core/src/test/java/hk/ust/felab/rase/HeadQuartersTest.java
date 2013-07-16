package hk.ust.felab.rase;

import hk.ust.felab.rase.master.MasterMain;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class HeadQuartersTest {
	// @Test
	// public void testRinottRas() throws Exception {
	// List<String> args = new LinkedList<String>();
	//
	// args.add("RinottRas"); // rasClass
	// args.add("0.05 1 10 68.2226"); // rasArgs
	// args.add(ClassLoader.getSystemResource("normal.5.alts").getPath()); //
	// alts
	// args.add("java");
	// args.add("hk.ust.felab.rase.sim.NormalSim"); // simClass
	// args.add(""); // simArgs
	// args.add("5"); // repeatTime
	// args.add("1");
	// args.add("tmp"); // logDir
	//
	// Headquarters.main(args.toArray(new String[0]));
	// System.out.println();
	// }
	//
	// @Test
	// public void testTableFillingRas() throws Exception {
	// List<String> args = new LinkedList<String>();
	//
	// args.add("TableFillingRas"); // rasClass
	// args.add("0.05 1 10"); // rasArgs
	// args.add(ClassLoader.getSystemResource("normal.5.alts").getPath()); //
	// alts
	// args.add("java");
	// args.add("hk.ust.felab.rase.sim.NormalSim"); // simClass
	// args.add(""); // simArgs
	// args.add("5"); // repeatTime
	// args.add("4");
	// args.add("tmp"); // logDir
	//
	// Headquarters.main(args.toArray(new String[0]));
	// System.out.println();
	// }
	//
	// @Test
	// public void testParallelSequentialRas() throws Exception {
	// List<String> args = new LinkedList<String>();
	//
	// args.add("ParallelSequentialRas"); // rasClass
	// args.add("0.05 1 10"); // rasArgs
	// args.add(ClassLoader.getSystemResource("normal.5.alts").getPath()); //
	// alts
	// args.add("java");
	// args.add("hk.ust.felab.rase.sim.NormalSim"); // simClass
	// args.add(""); // simArgs
	// args.add("5"); // repeatTime
	// args.add("4");
	// args.add("tmp"); // logDir
	//
	// Headquarters.main(args.toArray(new String[0]));
	// System.out.println();
	// }

	@Test
	public void testMatlabNormal() throws Exception {
		List<String> args = new LinkedList<String>();

		args.add("ParallelSequentialRas"); // rasClass
		args.add("0.05 1 10"); // rasArgs
		args.add(ClassLoader.getSystemResource("normal.5.alts").getPath()); // alts
		args.add("matlab");
		args.add("matlab.normal.NormalSim normalsim"); // simClass
		args.add(""); // simArgs
		args.add("5"); // repeatTime
		args.add("4");
		args.add("tmp"); // logDir

		MasterMain.main(args.toArray(new String[0]));
		System.out.println();
	}

	@Test
	public void testMatlabThreeStage() throws Exception {
		List<String> args = new LinkedList<String>();

		args.add("TableFillingRas"); // rasClass
		args.add("0.05 1 10"); // rasArgs
		args.add(ClassLoader.getSystemResource("three_stage.5.alts").getPath()); // alts
		args.add("matlab");
		args.add("matlab.three.stage.ThreeStage threestage"); // simClass
		args.add(""); // simArgs
		args.add("5"); // repeatTime
		args.add("4");
		args.add("tmp"); // logDir

		MasterMain.main(args.toArray(new String[0]));
		System.out.println();
	}

}
