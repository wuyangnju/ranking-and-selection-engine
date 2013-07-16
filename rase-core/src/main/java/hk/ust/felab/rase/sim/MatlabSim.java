package hk.ust.felab.rase.sim;

import hk.ust.felab.rase.Sim;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.mathworks.toolbox.javabuilder.MWNumericArray;

public class MatlabSim implements Sim {

	private Object simObject;
	private Method simMethod;

	public MatlabSim(Object simObject, Method simMethod) {
		this.simObject = simObject;
		this.simMethod = simMethod;
	}

	@Override
	public double[] sim(double[] alt, double[] args, long[] seed) {
		Object matlabResult = null;
		try {
			matlabResult = simMethod.invoke(simObject, 1, new Object[] { alt,
					seed });
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		MWNumericArray nwArray = (MWNumericArray) ((Object[]) matlabResult)[0];
		double[][] a = (double[][]) nwArray.toDoubleArray();
		MWNumericArray.disposeArray(nwArray);
		return a[0];
	}
}
