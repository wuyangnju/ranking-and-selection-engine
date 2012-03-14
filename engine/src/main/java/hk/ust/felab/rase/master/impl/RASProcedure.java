package hk.ust.felab.rase.master.impl;

import hk.ust.felab.rase.master.RasService;

import java.util.ArrayList;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

@Component
public class RASProcedure implements Runnable {

	private double[] rasArgs;

	@Resource
	private RasService rasService;

	public void init(double[] rasArgs) {
		this.rasArgs = rasArgs;
	}

	@Override
	public void run() {
		double[][] data = rasService.dataSnapshot();
		double alpha = rasArgs[0];
		double delta = rasArgs[1];
		double numOfSys = rasArgs[2];

		double a = (-1.0)
				/ delta
				* Math.log(2 - 2 * Math.pow((1 - alpha),
						(1.0 / (numOfSys - 1) * 1.0)));
		double b = delta / 2.0;
		double tau;
		double zpair;
		int lengofdata = data.length;
		int initnb = (int) rasArgs[3];
		ArrayList<Integer> toberev = new ArrayList<Integer>();

		long initSleep = (long) rasArgs[4];
		long midSleep = (long) rasArgs[5];
		try {
			Thread.sleep(initSleep * 1000);
			while (lengofdata > 1) {
				for (int i = 0; i < lengofdata; i++) {
					for (int j = 0; j < lengofdata; j++) {
						if (i != j && data[i][1] >= initnb
								&& data[j][1] >= initnb) {
							tau = Math.pow(
									(data[i][5] / data[i][1] - data[j][5]
											/ data[j][1]), (-1));
							zpair = tau * (data[i][4] - data[j][4]);
							if (zpair < Math.min(0, (-a + b * tau))) {
								toberev.add((int) data[i][0]);
								break;
							}
						}
					}
				}
				int[] toberemove = new int[toberev.size()];
				for (int i = 0; i < toberev.size(); i++) {
					toberemove[i] = toberev.get(i);
				}
				rasService.removeAlts(toberemove);
				
				Thread.sleep(midSleep* 1000);
				data = rasService.dataSnapshot();
				lengofdata = data.length;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}