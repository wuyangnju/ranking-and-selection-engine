package hk.ust.felab.rase.agent;

import hk.ust.felab.rase.common.Conf;
import hk.ust.felab.rase.slave.SlaveThread;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Service;

@Service
public class AgentService {

	private ArrayList<SlaveThread> slaveThreads = new ArrayList<SlaveThread>();

	public void activate(String sampleGenerator, int sampleCountStep)
			throws IllegalArgumentException, SecurityException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException,
			ClassNotFoundException {
		Executor executor = Executors.newFixedThreadPool(Conf.localSlaveCount);
		for (int i = 0; i < Conf.localSlaveCount; i++) {
			slaveThreads.add(new SlaveThread(Conf.slaveIdOffset + i,
					sampleGenerator, sampleCountStep, this));
			executor.execute(slaveThreads.get(i));
		}
	}

	public double[] requestTask() {
		// TODO:
		return null;
	}

	public void submitSample(long altId, double sample) {
		// TODO:
	}

}
