package hk.ust.felab.rase;

import hk.ust.felab.rase.agent.AgentController;

public class Branch {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		AgentController ac = new AgentController();
		ac.activateAgent(Integer.parseInt(args[0]), args[1],
				Integer.parseInt(args[2]), Integer.parseInt(args[3]),
				Integer.parseInt(args[4]), Integer.parseInt(args[5]),
				Integer.parseInt(args[6]), Integer.parseInt(args[7]), args[8],
				Integer.parseInt(args[9]));
	}

}
