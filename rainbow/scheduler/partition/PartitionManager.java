package rainbow.scheduler.partition;

import java.util.TreeSet;

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

	private BlockRange nextAvailable;
	private String alphabet;
	// Keeps track of which blocks are being worked on
	private TreeSet<BlockRange> processing;
	// Keeps track of which blocks are being cached in rainbow tables
	private TreeSet<BlockRange> cached;
	// What the largest strings we allow are
	private int maxStringLength;

	public PartitionManager(String alphabet, int maxStringLength) {
		this.alphabet = alphabet;
		processing = new TreeSet<>();
		cached = new TreeSet<>();
		this.maxStringLength = maxStringLength;
		reset();
	}
	/*
	 * Resets the consumed blocks, maintains cached blocks
	 */

	public final void reset() {
		nextAvailable = new BlockRange(1, 0, 0);
		processing.clear();
	}

	/*
	 * Request a partition of the space, takes an size that gives that many
	 * blocks
	 *
	 * nextAvailable always has the first empty block counting from the bottom
	 *
	 */
	public BlockRange requestPartition(int size) {
		BlockRange block = null;
		long numberOfBlocks = PlaintextSpace.getNumberOfBlocks(alphabet, nextAvailable.stringLength);
		// Gets the next block that is larger than nextAvailable
		// Checks both the cache and processing block list
		BlockRange processingBlock = processing.ceiling(nextAvailable);
		BlockRange cachedBlock = cached.ceiling(nextAvailable);
		//No block found, we can choose the size
		if (processingBlock == null && cachedBlock == null) {
			/*
			 * Possible situations:
			 *
			 * if nextAvailable block number == numberOfBlocks: current
			 * stringLength is full, move to the next one
			 *
			 * elseif nextAvailable + size > numberOfBlocks: we reached the end
			 * of stringLength, assign as many blocks as possible
			 *
			 * else: normal assignment
			 */
			if (nextAvailable.startBlockNumber == numberOfBlocks) {
				// Increase the string length
				nextAvailable.stringLength++;
				// Reset the block number
				nextAvailable.startBlockNumber = 0;
				if (nextAvailable.stringLength > maxStringLength) {
					// TODO: handle this
				}
				// Return the new value, recursive function call
				return requestPartition(size);
			} else if (nextAvailable.startBlockNumber + size > numberOfBlocks) {
				block = nextAvailable.clone();
				block.startBlockNumber = nextAvailable.startBlockNumber;
				block.endBlockNumber = numberOfBlocks;
			} else {
				block = nextAvailable.clone();
				block.startBlockNumber = nextAvailable.startBlockNumber;
				block.endBlockNumber = block.startBlockNumber + size;
			}
		} else {
			// A block is found
			// Find the smallest of the values
			long ceiling = Math.min(Math.min(processingBlock.startBlockNumber, cachedBlock.endBlockNumber), nextAvailable.startBlockNumber + size);
			if (ceiling == nextAvailable.startBlockNumber) { // No room here
				block = findNextAvailableBlock(nextAvailable);
				block.endBlockNumber = block.startBlockNumber + size;
				nextAvailable.startBlockNumber = block.endBlockNumber;
			} else { // There is room, return the block
				block = new BlockRange(nextAvailable.stringLength, nextAvailable.startBlockNumber, ceiling);
				nextAvailable.startBlockNumber = ceiling;
			}
		}
		processing.add(block);
		nextAvailable.startBlockNumber = block.endBlockNumber;
		return block;
	}

	private BlockRange findNextAvailableBlock(BlockRange startPoint) {
		BlockRange tempPBlock = processing.ceiling(startPoint);
		BlockRange tempCBlock = cached.ceiling(startPoint);
		// End block is unused
		BlockRange temp = new BlockRange(startPoint.stringLength, Math.min(tempPBlock.endBlockNumber, tempCBlock.endBlockNumber), 0);
		while (true) {
			BlockRange pBlock = processing.higher(temp);
			BlockRange cBlock = cached.higher(temp);
			if (pBlock == null && cBlock == null) {
				break;
			} else if (pBlock.startBlockNumber == temp.endBlockNumber
					|| cBlock.startBlockNumber == temp.endBlockNumber) {
				temp.startBlockNumber = Math.max(pBlock.endBlockNumber,
						cBlock.endBlockNumber);
			} else {
				break;
			}
		}
		return temp;
	}
	/*
	 * Notify Complete, tells the partition manager that a block is complete
	 */

	public void notifyComplete(BlockRange b) {
		if (processing.contains(b)) {
			processing.remove(b);
		}
	}
	/*
	 * Request Cache, requests a block to be cached
	 *
	 * TODO: only a function stub
	 */

	public BlockRange requestCache(BlockRange b) {
		throw new UnsupportedOperationException();
	}
	/*
	 * Notify Cache, tells the partition manager that this block is being cached
	 * in a rainbowtable and should not be given as a job
	 *
	 * If this block was assigned it is automatically removed from the
	 * processing list
	 */

	public void notifyCache(BlockRange b) {
		if (!cached.contains(b)) {
			cached.add(b);
		}
		notifyComplete(b);
	}
	/*
	 * Release cache, tells the partition manager that this blockrange is no
	 * longer being cached TODO:function stub
	 */

	public void releaseCache(BlockRange b) {
		throw new UnsupportedOperationException();
	}
	/*
	 * For testing purposes only
	 */

	public TreeSet<BlockRange> getCached() {
		return cached;
	}

	public TreeSet<BlockRange> getProcessing() {
		return processing;
	}
}
