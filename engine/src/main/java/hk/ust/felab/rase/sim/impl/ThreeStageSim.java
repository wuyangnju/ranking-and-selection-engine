package hk.ust.felab.rase.sim.impl;

import hk.ust.felab.rase.sim.SampleGen;
import umontreal.iro.lecuyer.randvar.ExponentialGen;
import umontreal.iro.lecuyer.randvar.RandomVariateGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.simevents.Event;
import umontreal.iro.lecuyer.simevents.Sim;

import java.util.LinkedList;

public class ThreeStageSim implements SampleGen {

	MRG32k3a genArrMrg32k3a;
	MRG32k3a[] genServMrg32k3as;

	class ThreeStage {

		double warmupTime;
		double horizonTime;
		boolean warmupDone;
		boolean[] block;
		LinkedList<Customer> wait1List = new LinkedList<Customer>();
		LinkedList<Customer> wait2List = new LinkedList<Customer>();
		LinkedList<Customer> wait3List = new LinkedList<Customer>();
		LinkedList<Customer> serv1List = new LinkedList<Customer>();
		LinkedList<Customer> serv2List = new LinkedList<Customer>();
		LinkedList<Customer> serv3List = new LinkedList<Customer>();

		double[] bufferSize;
		double[] mu;
		double lambda;
		RandomVariateGen genArr;
		RandomVariateGen[] genServ;

		double tStart, tTerminal;
		int nbDepart = 0;

		// public Tally statSojourn = new Tally ("total sojourn time ");

		class Customer {
			double arrivTime, serv1Time, serv2Time, serv3Time;
		}

		public ThreeStage(double[] x) {
			this.mu = new double[3];
			this.bufferSize = new double[2];

			mu[0] = x[1];
			mu[1] = x[2];
			mu[2] = x[3];
			bufferSize[0] = x[4];
			bufferSize[1] = x[5];
			warmupTime = 500.0;
			horizonTime = 1000.0;
			lambda = 100.0;

			block = new boolean[2];
			block[0] = false;
			block[1] = false;

			genArr = new ExponentialGen(genArrMrg32k3a, lambda);
			genServ = new RandomVariateGen[3];
			for (int i = 0; i < 3; i++) {
				genServ[i] = new ExponentialGen(genServMrg32k3as[i], mu[i]);
			}
		}

		class Arrival extends Event {
			public void actions() {
				new Arrival().schedule(genArr.nextDouble()); // Next arrival.
				Customer cust = new Customer(); // Cust just arrived.
				cust.arrivTime = Sim.time();
				cust.serv1Time = genServ[0].nextDouble();
				cust.serv2Time = genServ[1].nextDouble();
				cust.serv3Time = genServ[2].nextDouble();

				if (serv1List.size() > 0) { // Must join the queue.

					wait1List.addLast(cust);
				} else { // Starts service.
					serv1List.addLast(cust);
					new Departure1().schedule(cust.serv1Time);
				}
			}
		}

		class Departure1 extends Event {
			public void actions() {
				if (wait2List.size() == bufferSize[0]) {
					block[0] = true;
				} else {
					block[0] = false;
					Customer cust = serv1List.removeFirst();
					if (serv2List.size() > 0) {
						wait2List.addLast(cust);
					} else {
						serv2List.addLast(cust);
						new Departure2().schedule(cust.serv2Time);
					}
					if (wait1List.size() > 0) {
						Customer cust1 = wait1List.removeFirst();
						serv1List.addLast(cust1);
						new Departure1().schedule(cust1.serv1Time);
					}
				}
			}
		}

		class Departure2 extends Event {
			public void actions() {
				if (wait3List.size() == bufferSize[1]) {
					block[1] = true;
				} else {
					block[1] = false;
					Customer cust = serv2List.removeFirst();
					if (serv3List.size() > 0) {
						wait3List.addLast(cust);
					} else {
						serv3List.addLast(cust);
						new Departure3().schedule(cust.serv3Time);
					}
					if (wait2List.size() > 0) {
						Customer cust1 = wait2List.removeFirst();
						serv2List.addLast(cust1);
						new Departure2().schedule(cust1.serv2Time);
						if (block[0] == true) {
							Customer cust2 = serv1List.removeFirst();
							wait2List.addLast(cust2);
							block[0] = false;
							if (wait1List.size() > 0) {
								Customer cust3 = wait1List.removeFirst();
								serv1List.addLast(cust3);
								new Departure1().schedule(cust3.serv1Time);
							}
						}
					}
				}
			}
		}

		class Departure3 extends Event {
			public void actions() {
				serv3List.removeFirst();
				if (warmupDone) {
					// statSojourn.add(Sim.time()-cust.arrivTime);
					nbDepart++;
				}
				if (wait3List.size() > 0) {
					Customer cust1 = wait3List.removeFirst();
					serv3List.addLast(cust1);
					new Departure3().schedule(cust1.serv3Time);
					if (block[1] == true) {
						Customer cust2 = serv2List.removeFirst();
						wait3List.addLast(cust2);
						if (wait2List.size() > 0) {
							Customer cust5 = wait2List.removeFirst();
							serv2List.add(cust5);
							new Departure2().schedule(cust5.serv2Time);
						}
						block[1] = false;
						if (block[0] == true) {
							Customer cust3 = serv1List.removeFirst();
							wait2List.addLast(cust3);
							block[0] = false;
							if (wait1List.size() > 0) {
								Customer cust4 = wait1List.removeFirst();
								serv1List.addLast(cust4);
								new Departure1().schedule(cust4.serv1Time);
							}
						}
					}

				}
			}
		}

		class endWarmup extends Event {
			public void actions() {
				tStart = Sim.time();
				warmupDone = true;
			}
		};

		class endOfSim extends Event {
			public void actions() {
				Sim.stop();
				tTerminal = Sim.time();
			}
		};

		public void simulateOneRun() {
			Sim.init();
			new endOfSim().schedule(horizonTime);
			new endWarmup().schedule(warmupTime);
			warmupDone = false;

			new Arrival().schedule(genArr.nextDouble());

			Sim.start();
		}
	}

	public ThreeStageSim(Integer subStreamId) {
		genArrMrg32k3a = new MRG32k3a();
		genServMrg32k3as = new MRG32k3a[3];
		for (int i = 0; i < 3; i++) {
			genServMrg32k3as[i] = new MRG32k3a();
		}

		for (int i = 0; i < subStreamId; i++) {
			genArrMrg32k3a.resetNextSubstream();
			for (int j = 0; j < 3; j++) {
				genServMrg32k3as[j].resetNextSubstream();
			}
		}
	}

	@Override
	public double[] generate(double[] alt) {
		ThreeStage throughput = new ThreeStage(alt);

		throughput.simulateOneRun();

		return new double[] {
				throughput.nbDepart
						/ (throughput.tTerminal - throughput.tStart), 1 };
	}
}