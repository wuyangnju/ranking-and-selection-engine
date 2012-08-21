package hk.ust.felab.rase.util;

import java.util.LinkedList;
import java.util.List;

public class TestIndexedPriorityQueue {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		IndexedPriorityQueue<Element> q = new IndexedPriorityQueue<Element>(5,
				null, 0);
		List<Element> l = new LinkedList<Element>();
		l.add(new Element(5));
		l.add(new Element(1));
		l.add(new Element(6));
		l.add(new Element(8));
		l.add(new Element(0));
		for (Element e : l) {
			System.out.println(q.myOffer(e));
		}
		l.get(1).value = 7;
		System.out.println(q.siftDown(l.get(1)));
		System.out.println(q.myRemove(l.get(4)));
	}

}

class Element implements Indexed, Comparable<Element> {
	public int value;
	private int index;

	public Element(int value) {
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
		return value - o.value;
	}
}
