package hk.ust.felab.rase.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class TestLastEmptyLineOfFile {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream("src/main/op-scripts/conf/case1.ras")));
		String line = null;
		int lineCount = 0;
		while ((line = br.readLine()) != null) {
			System.out.println(line);
			lineCount++;
		}
		br.close();
		System.out.println(lineCount);
	}

}
