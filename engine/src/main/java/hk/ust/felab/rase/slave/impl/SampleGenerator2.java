package hk.ust.felab.rase.slave.impl;

import hk.ust.felab.rase.slave.SampleGenerator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import umontreal.iro.lecuyer.randvar.UniformGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

public class SampleGenerator2 implements SampleGenerator {
	private MRG32k3a serviceTimeMrg32k3a;
	private MRG32k3a waitingTimeMrg32k3a;

	private static List<HisData> hisDatas = new LinkedList<HisData>();

	public static void main(String[] args) throws ParseException {
		SampleGenerator2 sampleGenerator2 = new SampleGenerator2(0);
		double[] alt = { 0, 25, 16, 410, 463, 27, 21, 30, 16 };
		System.out.println(sampleGenerator2.generate(alt)[0]);
		double[] alt1 = { 0, 21, 15, 392, 487, 30, 20, 29, 14 };
		System.out.println(sampleGenerator2.generate(alt1)[0]);
		double[] alt2 = { 0, 25, 15, 393, 480, 28, 21, 32, 14 };
		System.out.println(sampleGenerator2.generate(alt2)[0]);
		double[] alt3 = { 0, 22, 17, 394, 481, 26, 23, 32, 13 };
		System.out.println(sampleGenerator2.generate(alt3)[0]);
		double[] alt4 = { 0, 24, 15, 401, 475, 30, 20, 30, 13 };
		System.out.println(sampleGenerator2.generate(alt4)[0]);
		double[] alt5 = { 0, 25, 17, 410, 467, 25, 22, 29, 13 };
		System.out.println(sampleGenerator2.generate(alt5)[0]);

	}

	private static Double parse(String in) {
		if (in == null) {
			return null;
		}
		if (in.equals("NA")) {
			return null;
		} else {
			try {
				return Double.parseDouble(in);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	static {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					SampleGenerator2.class.getClassLoader()
							.getResourceAsStream("data.csv")));
			String line = br.readLine();
			while ((line = br.readLine()) != null) {
				String[] items = line.split(",");
				HisData hisData = new HisData();

				// for (String item : items) {
				// System.out.print(item + " ");
				// }
				// System.out.println();

				hisData.Svc = items[0];
				hisData.Vsl = items[1];
				hisData.Voy = Integer.parseInt(items[2]);
				hisData.Dir = items[3];
				hisData.Facility = items[4];
				// 2008-12-31 20:00:00

				// hisData.Berth_LTA = items[5];
				// hisData.LTD = items[6];
				// hisData.Berth_ETA_ATA = items[7];
				// hisData.ETD_ATD = items[8];
				// hisData.Arr_Diff = Double.parseDouble(items[9]);
				// hisData.Dep_Diff = Double.parseDouble(items[10]);
				hisData.portTime = parse(items[12]);
				hisData.LportTime = parse(items[13]);
				hisData.arrDiff = parse(items[14]);
				hisData.depDiff = parse(items[15]);
				hisData.SeaT = parse(items[16]);
				hisData.PSeaT = parse(items[17]);

				hisDatas.add(hisData);
			}
			br.close();

			// System.out.println("xxxxxxxxxx:" + hisDatas.size());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public SampleGenerator2(int slaveId) {
		serviceTimeMrg32k3a = new MRG32k3a();
		waitingTimeMrg32k3a = new MRG32k3a();
		for (int i = 0; i < slaveId; i++) {
			serviceTimeMrg32k3a.resetNextSubstream();
			waitingTimeMrg32k3a.resetNextSubstream();
		}
	}

	public double[] generate(double[] alt) {
		// data[0]: id
		// data[1-8]: valid combination of times
		double[] samples = new double[1];
		samples[0] = 0;

		String[] ports = { "\"YAT\"", "\"SKZ\"", "\"HKG\"", "\"LGB\"",
				"\"KHH\"", "\"FUQ\"", "\"ZIA\"", "\"HKG\"", "\"YAT\"" };
		int k = 8;
		double[] dist = { 64, 26, 6345, 6126, 240, 183, 274, 38 };
		double vl = 16;
		double vh = 24;
		double percent = 0.9;
		double[] delayP = { 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2 };
		double[] delayTL = { 20, 20, 20, 40, 15, 20, 20, 20 };
		double[] delayPC = { 0.3, 0.3, 0.3, 0.3, 0.3, 0.3, 0.3, 0.3 };

		double[] mean = { 0, 0, 0, 0, 0, 0, 0, 0 };

		for (int i = 0; i < k; i++) {
			double sum = 0;
			double count = 0;
			for (HisData hisData : hisDatas) {
				if (hisData.Facility.equals(ports[i])) {
					if (hisData.portTime != null) {
						sum += hisData.portTime;
						count++;
					}
				}
			}
			mean[i] = sum / count;
		}

		double[] quant = { 0, 0, 0, 0, 0, 0, 0, 0 };

		for (int i = 0; i < k; i++) {
			ArrayList<Double> portTimes = new ArrayList<Double>();
			for (HisData hisData : hisDatas) {
				if (hisData.Facility.equals(ports[i])) {
					if (hisData.portTime != null) {
						portTimes.add(hisData.portTime);
					}
				}
			}
			Collections.sort(portTimes);
			quant[i] = portTimes.get((int) Math.round(portTimes.size()
					* percent));
		}

		int[] Tmin = new int[k];
		int[] Tmax = new int[k];
		for (int i = 0; i < k; i++) {
			Tmin[i] = (int) (dist[i] / vh + mean[i]);
			Tmax[i] = (int) (dist[i] / vl + quant[i]);
		}

		int Total = 42 * 24;
		double[] fuelE = new double[k];
		// int bigM = 1000000;
		// int fuelM = bigM;
		double fuelM = 0;

		int flag = 0;
		int delayV = 0;
		int N = 100;

		int[] tps = new int[k];
		for (int i = 0; i < k; i++) {
			tps[i] = (int) alt[i + 1];
		}

		Double[] delay = new Double[k];
		Double[] earlyE = new Double[k];
		Double[] waitingT = new Double[k];
		Double serviceT;
		Double[] speed = new Double[k];
		Double[] fuel = new Double[k];
		for (int i = 0; i < k; i++) {
			delay[i] = new Double(0);
			earlyE[i] = new Double(0);
			waitingT[i] = new Double(0);
			speed[i] = new Double(0);
			fuel[i] = new Double(0);
		}

		for (int i = 0; i < k; i++) {
			Double[] xK = new Double[hisDatas.size()];
			Double[] yK = new Double[hisDatas.size()];
			for (int j = 0; j < xK.length; j++) {
				xK[j] = null;
				yK[j] = null;
			}
			int j = 1;
			for (int kk = 0; kk < hisDatas.size() - 1; kk++) {
				if ((hisDatas.get(kk).Facility.equals(ports[i]))
						&& (hisDatas.get(kk + 1).Facility.equals(ports[i + 1]))
						&& (hisDatas.get(kk).Vsl
								.equals(hisDatas.get(kk + 1).Vsl))) {
					HisData hisData = hisDatas.get(kk);
					if ((hisData.portTime != null)
							&& (hisData.LportTime != null)
							&& (hisData.arrDiff != null)
							&& (hisData.SeaT != null)
							&& (hisData.PSeaT != null)) {
						xK[j] = hisDatas.get(kk + 1).arrDiff;
						yK[j] = hisDatas.get(kk).arrDiff
								+ hisDatas.get(kk).portTime
								- hisDatas.get(kk).LportTime + dist[i] / vh
								- hisDatas.get(kk).PSeaT;
						j++;
					}
				}
			}

			for (int t = 0; t < xK.length; t++) {
				if (((yK[t] != null) && xK[t] != null) && (xK[t] > 0)
						&& (yK[t] - xK[t] <= 6) && (yK[t] - xK[t] >= -6)) {
					xK[t] = null;
					yK[t] = null;
				}
			}

			List<Double> xKclean = new ArrayList<Double>();

			for (Double xKd : xK) {
				if (xKd != null) {
					xKclean.add(xKd);
				}
			}
			int index = (int) UniformGen.nextDouble(waitingTimeMrg32k3a, 0,
					xKclean.size());
			waitingT[i] = xKclean.get(index);

			List<Double> tempData = new ArrayList<Double>();
			for (HisData hisData : hisDatas) {
				if (hisData.Facility.equals(ports[i])) {
					tempData.add(hisData.portTime);
				}
			}

			List<Double> tempClean = new ArrayList<Double>();
			for (Double tempDataD : tempData) {
				if (tempDataD != null) {
					tempClean.add(tempDataD);
				}
			}

			index = (int) UniformGen.nextDouble(serviceTimeMrg32k3a, 0,
					tempClean.size());
			serviceT = tempClean.get(index);

			if (i == 0) {
				earlyE[i] = serviceT + dist[i] / vh - tps[i];
			}
			if (i != 0) {
				earlyE[i] = delay[i - 1] + serviceT + dist[i] / vh - tps[i];
			}

			delay[i] = Math.max(waitingT[i], earlyE[i]);

			if (i == 0) {
				speed[i] = dist[i] / (tps[i] + delay[i] + serviceT) * 1.0;
			}
			if (i != 0) {
				speed[i] = dist[i]
						/ (tps[i] + delay[i] - delay[i - 1] + serviceT) * 1.0;
			}

			if (speed[i] <= vl) {
				speed[i] = vl;
			} else if (speed[i] >= vh) {
				speed[i] = vh;
			}

			fuel[i] = dist[i]
					* (6.709347 * speed[i] - 0.408568 * Math.pow(speed[i], 2)
							+ 0.008309 * Math.pow(speed[i], 3) - 36.567518);

			fuelE[i] = fuel[i];
		}

		fuelM = 0;
		for (int i = 0; i < k; i++) {
			fuelM += fuelE[i];
		}

		samples[0] = fuelM;

		return samples;
	}

	static class HisData {
		String Svc;
		String Vsl;
		Integer Voy;
		String Dir;
		String Facility;
		Date Berth_LTA;
		Date LTD;
		Date Berth_ETA_ATA;
		Date ETD_ATD;
		Double Arr_Diff;
		Double Dep_Diff;
		Double portTime;
		Double LportTime;
		Double arrDiff;
		Double depDiff;
		Double SeaT;
		Double PSeaT;
	}
}
