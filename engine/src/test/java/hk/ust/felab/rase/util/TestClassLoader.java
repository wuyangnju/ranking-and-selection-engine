package hk.ust.felab.rase.util;

public class TestClassLoader {
	public static void main(String[] args) throws Exception {
		System.out.println(Class.forName("umontreal.iro.lecuyer.simevents.Sim")
				.getClassLoader());
		ClassLoader cl1 = new SampleGenClassLoader();
		cl1.loadClass("umontreal.iro.lecuyer.simevents.Sim");
		ClassLoader cl2 = new SampleGenClassLoader();
		cl2.loadClass("umontreal.iro.lecuyer.simevents.Sim");
		System.out.println(Class.forName("umontreal.iro.lecuyer.simevents.Sim",
				true, cl1).getClassLoader());
		System.out.println(Class.forName("umontreal.iro.lecuyer.simevents.Sim",
				true, cl2).getClassLoader());
	}
}
