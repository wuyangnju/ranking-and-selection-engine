package hk.ust.felab.rase.master;

import hk.ust.felab.rase.master.impl.RASProcedure;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

@Service
public class MasterService {

	private int slaveCount;

	@Resource
	private RasService rasService;

	@Resource
	private RASProcedure rasProcedure;

	public void activate(int slaveCount, double[] rasArgs, double[][] altsArgs) {
		this.slaveCount = slaveCount;
		rasService.init(altsArgs);
		rasProcedure.init(rasArgs);
		new Thread(rasProcedure).start();
	}

	public double[][] requestTask(int slaveId) {
		double[][] altSystems = rasService.argSnapshot();
		int fromIndex = (int) Math.round(altSystems.length * slaveId
				/ (slaveCount * 1.0));
		int toIndex = (int) Math.round(altSystems.length * (slaveId + 1)
				/ (slaveCount * 1.0));
		double[][] tasks = new double[toIndex - fromIndex][];
		System.arraycopy(altSystems, fromIndex, tasks, 0, toIndex - fromIndex);
		return tasks;
	}

	public long submitSample(double[][] sample) {
		double[] singleSample = new double[sample[0].length - 1];
		System.arraycopy(sample[0], 1, singleSample, 0, sample[0].length - 1);
		return rasService.addSample((int) sample[0][0], singleSample);
	}

	public long survivalCount() {
		return rasService.surviveCount();
	}

	public double[][] altSnapshot() {
		return rasService.altSnapshot();
	}

}