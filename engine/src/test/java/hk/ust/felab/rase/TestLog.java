package hk.ust.felab.rase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

public class TestLog {
	private transient final Log log1 = LogFactory.getLog("test1");
	private transient final Log log2 = LogFactory.getLog("test2");
	private long count=0;

	@Test
	public void test() {
		for (int i = 0; i < 100; i++) {
			log1.trace(System.currentTimeMillis()+",");
			count++;
			log1.trace(System.currentTimeMillis()+"\n");;
//			log2.trace(" log2\n");
		}
	}

}
