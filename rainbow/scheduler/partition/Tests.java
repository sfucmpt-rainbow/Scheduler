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

	public static void testRequestPartition() {
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
		System.out.print("Failing block ");
		System.out.println(b[1]);
		p.notifyFailure(b[1]);
		System.out.print("Failing block ");
		System.out.println(b[3]);
		p.notifyFailure(b[3]);
		System.out.print("Failing block ");
		System.out.println(b[7]);
		p.notifyFailure(b[7]);
		System.out.println(String.format("There are %s Blocks being cached", p.getProcessing().size()));
		for (Partition block : p.getProcessing()) {
			System.out.println(block);
		}
		System.out.println("Requesting more blocks");
		for(int i=0;i<20;i++){
			System.out.println(p.requestPartition(30));
		}
	}
	public static void testRequestCache(){
		System.out.println("Begin test2");
		PartitionManager p = new PartitionManager(AlphabetGenerator.generateAlphabet(AlphabetGenerator.Types.LOWER_CASE), 8);
		Partition[] b = new Partition[10];
		System.out.println("Getting cache");
		for (int i = 0; i < 10; i++) {
			b[i] = p.requestCache(100);
			System.out.println(b[i]);
		}
		System.out.println("Completing cache");
		p.notifyCache(b[1]);
		System.out.println(b[1]);
		p.notifyCache(b[2]);
		System.out.println(b[2]);
		p.notifyCache(b[5]);
		System.out.println(b[5]);
		p.notifyCache(b[7]);
		System.out.println(b[7]);
		System.out.println("Releasing cache");
		p.releaseCache(b[3]);
		System.out.println(b[3]);
		p.releaseCache(b[4]);
		System.out.println(b[4]);
		p.releaseCache(b[5]);
		System.out.println(b[5]);
		System.out.println("Getting more cache");
		for (int i = 0; i < 10; i++) {
			b[i] = p.requestCache(80);
			System.out.println(b[i]);
		}
	}

	public static void main(String[] s) {
		//testRequestPartition();		
		testRequestCache();
	}
}
