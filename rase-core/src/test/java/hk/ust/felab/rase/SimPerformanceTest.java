package hk.ust.felab.rase;

import hk.ust.felab.rase.sim.MatlabSim;
import hk.ust.felab.rase.sim.NormalSim;

import java.lang.reflect.Method;
import org.junit.Test;

public class SimPerformanceTest {

	@Test
	public void testNormal() throws Exception {
		Sim sim = new NormalSim();

		long start = System.currentTimeMillis();
		int repeat = 10000;
		for (int i = 0; i < repeat; i++) {
			sim.sim(new double[] { 1, 0, 1 }, null, new long[] { 1, 2, 3, 4, 5,
					6 });
		}
		System.out.println((System.currentTimeMillis() - start) + "/" + repeat);
	}

	@Test
	public void testMatlabNormal() throws Exception {
		String matlabClassStr = "matlab.normal.NormalSim";
		String matlabMethodStr = "normalsim";
		Method matlabMethod = Class.forName(matlabClassStr).getMethod(
				matlabMethodStr, int.class, Object[].class);
		Object matlabObject = Class.forName(matlabClassStr).newInstance();
		Sim sim = new MatlabSim(matlabObject, matlabMethod);

		long start = System.currentTimeMillis();
		int repeat = 10000;
		for (int i = 0; i < repeat; i++) {
			sim.sim(new double[] { 1, 0, 1 }, null, new long[] { 1 });
		}
		System.out.println((System.currentTimeMillis() - start) + "/" + repeat);
	}

	@Test
	public void testMatlabThreeStage() throws Exception {
		String matlabClassStr = "matlab.three.stage.ThreeStage";
		String matlabMethodStr = "threestage";
		Method matlabMethod = Class.forName(matlabClassStr).getMethod(
				matlabMethodStr, int.class, Object[].class);
		Object matlabObject = Class.forName(matlabClassStr).newInstance();
		Sim sim = new MatlabSim(matlabObject, matlabMethod);

		long start = System.currentTimeMillis();
		int repeat = 100;
		for (int i = 0; i < repeat; i++) {
			sim.sim(new double[] { 6, 7, 7, 12, 8 }, null, new long[] { 1 });
		}
		System.out.println(System.currentTimeMillis() - start + "/" + repeat);
	}

}
