
import java.util.ArrayList;
import java.util.PriorityQueue;

/*
 * Manages all the partitions of the plaintext space Use this to request a
 * partition to be assign to a client
 *
 * Does not keep an array of all blocks since it could waste a few GB of memory
 * for no reason
 *
 * Instead it stores a sorted list of blocks being worked on and cached blocks
 * It also stores a pointer to the next available block
 *
 */
public class PartitionManager {

    private Block nextAvailable;
    private String alphabet;
    // Keeps track of which blocks are being worked on
    private PriorityQueue<Block> processing;
    // Keeps track of which blocks are being cached in rainbow tables
    private PriorityQueue<Block> cached;

    public PartitionManager(String alphabet, int maxStringLength) {
        this.alphabet = alphabet;
        processing = new PriorityQueue<>();
        cached = new PriorityQueue<>();
        reset();
    }
    /*
     * Resets the consumed blocks, maintains cached blocks
     */

    public final void reset() {
        nextAvailable = new Block(1, 0);
        processing.clear();
    }

    /*
     * Request a partition of the space, takes an size that gives that many
     * blocks
     *
     * Algorithm - just starts from the bottom and grabs every available block
     *
     */
    public Block[] requestPartition(int size) {
        Block[] blocks = new Block[size];
        int blocksIndex = 0;
        long numberOfBlocks = PlaintextSpace.getNumberOfBlocks(alphabet, nextAvailable.stringLength);
        while (blocksIndex < size) {
            Block selected = nextAvailable.clone();
            // Ensure that the block is not cached
            if (!cached.contains(selected)) {
                // Add to return list and processing list
                processing.add(selected);
                blocks[blocksIndex++] = selected;
            }
            // Modify nextAvailable
            nextAvailable.blockNumber++;
            if (nextAvailable.blockNumber >= numberOfBlocks) {
                nextAvailable.blockNumber = 0;
                nextAvailable.stringLength++;
                numberOfBlocks = PlaintextSpace.getNumberOfBlocks(alphabet, nextAvailable.stringLength);
            }
        }
        return blocks;
    }
    /*
     * Notify Complete, tells the partition manager that a block is complete
     */

    public void notifyComplete(Block b) {
        if (processing.contains(b)) {
            processing.remove(b);
        }
    }
    /*
     * Notify Cache, tells the partition manager that this block is being cached
     * in a rainbowtable and should not be given as a job
     *
     * If this block was assigned it is automatically removed from the
     * processing list
     */

    public void notifyCache(Block b) {
        if (!cached.contains(b)) {
            cached.add(b);
        }
        notifyComplete(b);
    }

    public PriorityQueue<Block> getCached() {
        return cached;
    }

    public PriorityQueue<Block> getProcessing() {
        return processing;
    }
}
