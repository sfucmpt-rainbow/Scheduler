package rainbow.scheduler.partition;

import java.util.ArrayList;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
/**
 *
 * @author WesleyLuk
 */
public class Tests {
	/*
	 * Unit tests, tests adding and removing blocks
	 *
	 */

	public static void test1() {
		System.out.println("Beginning test1");
		PartitionManager p = new PartitionManager(AlphabetGenerator.generateAlphabet(AlphabetGenerator.Types.LOWER_CASE), 8);
		Partition[] b = new Partition[10];
		for (int i = 0; i < 10; i++) {
			b[i] = p.requestPartition(100);
			System.out.println(b[i]);
		}
		System.out.println(String.format("There are %s Blocks being cached", p.getProcessing().size()));
		p.notifyComplete(b[2]);
		p.notifyComplete(b[4]);
		p.notifyComplete(b[6]);
		p.notifyComplete(b[8]);
		p.notifyFailure(b[7]);
		System.out.println(String.format("There are %s Blocks being cached", p.getProcessing().size()));
		for (Partition block : p.getProcessing()) {
			System.out.println(block);
		}
	}

	public static void main(String[] s) {
		test1();
		//test2();
	}
}
