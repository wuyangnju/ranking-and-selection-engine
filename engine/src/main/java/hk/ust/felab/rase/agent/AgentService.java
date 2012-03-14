package hk.ust.felab.rase.agent;

import hk.ust.felab.rase.common.Conf;
import hk.ust.felab.rase.slave.SlaveThread;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Service;

@Service
public class AgentService {

	private ArrayList<SlaveThread> slaveThreads = new ArrayList<SlaveThread>();

	public void activate() {
		Executor executor = Executors.newFixedThreadPool(Conf.localSlaveCount);
		for (int i = 0; i < Conf.localSlaveCount; i++) {
			slaveThreads.add(new SlaveThread(Conf.slaveIdOffset + i));
			executor.execute(slaveThreads.get(i));
		}
	}

}
