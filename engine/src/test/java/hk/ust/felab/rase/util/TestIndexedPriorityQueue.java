package hk.ust.felab.rase.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class TestIndexedPriorityQueue {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		IndexedPriorityQueue<Element> q = new IndexedPriorityQueue<Element>(5,
				null, 0);
		Element[] l = new Element[100];

		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream("src/test/resources/myheap.log")));
		String line = null;
		while ((line = br.readLine()) != null) {
			String[] fields = line.split(",");
			String op = fields[0];
			double value = Double.parseDouble(fields[1]);
			int id = Integer.parseInt(fields[2]);
			if ("myOffer".equals(op)) {
				l[id - 1] = new Element(value);
				q.myOffer(l[id - 1]);
			} else if ("siftUp".equals(op)) {
				q.siftUp(l[id - 1]);
			} else if ("siftDown".equals(op)) {
				q.siftDown(l[id - 1]);
			} else if ("myRemove".equals(op)) {
				q.myRemove(l[id - 1]);
			} else {
				System.out.println(line);
			}
			q.size(); // otherwise can't set breakpoint here
		}
		System.out.println(q.check());
		br.close();
	}
}

class Element implements Indexed, Comparable<Element> {
	public double value;
	private int index;

	public Element(double value) {
		this.value = value;
	}

	@Override
	public int getIndex(int keyToIndex) {
		return index;
	}

	@Override
	public void setIndex(int keyToIndex, int index) {
		this.index = index;
	}

	@Override
	public int compareTo(Element o) {
		return value - o.value > 0 ? 1 : -1;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}
}
