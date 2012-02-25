package rainbow.partition;


import java.util.ArrayList;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
/**
 *
 * @author WesleyLuk
 */
public class ApplicationRunner {
    /*
     * Unit tests, tests adding and removing blocks
     *
     */

    public static void test1() {
        System.out.println("Beginning test1");
        PartitionManager p = new PartitionManager(AlphabetGenerator.generateAlphabet(AlphabetGenerator.Types.LOWER_CASE), 8);
        Block[] blocks = p.requestPartition(100);
        System.out.println(String.format("There are %s blocks being processed", p.getProcessing().size()));
        System.out.println(p.getProcessing());
        for (int i = 0; i < blocks.length; i += 3) {
            p.notifyComplete(blocks[i]);
        }
        System.out.println(String.format("There are %s blocks being processed", p.getProcessing().size()));
        System.out.println(p.getProcessing());
    }
    /*
     * Tests adding blocks to the cache and resetting
     */

    public static void test2() {
        System.out.println("Beginning test2");
        PartitionManager p = new PartitionManager(AlphabetGenerator.generateAlphabet(AlphabetGenerator.Types.LOWER_CASE), 8);
        Block[] blocks = p.requestPartition(100);
        ArrayList<Block> cachedBlocks = new ArrayList<>();;
        for (int i = blocks.length / 4; i < blocks.length / 2; i++) {
            p.notifyCache(blocks[i]);
            cachedBlocks.add(blocks[i]);
        }
        System.out.println(String.format("There are %s blocks being processed", p.getProcessing().size()));
        System.out.println(p.getProcessing());
        p.reset();
        Block[] blocks2 = p.requestPartition(100);
        System.out.println(String.format("There are %s blocks being processed", p.getProcessing().size()));
        System.out.println(p.getProcessing());
        // Ensure there are no reassigned blocks
        for (int i = 0; i < blocks2.length; i++) {
            if (cachedBlocks.contains(blocks2[i])) {
                throw new RuntimeException("Cached block was reassigned");
            }
        }
        System.out.println("No duplicates found");
    }

    public static void main(String[] s) {
        test1();
        test2();
    }
}
