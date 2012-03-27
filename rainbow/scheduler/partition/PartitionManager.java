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
	
	private Partition nextAvailable;
	private String alphabet;
	// Keeps track of which blocks are being worked on
	private TreeSet<Partition> processing;
	// Keeps track of which blocks are being cached in rainbow tables
	private TreeSet<Partition> cached;
	// Keeps track of which blocks are having their cache built but is not ready yet
	private TreeSet<Partition> caching;
	// What the largest strings we allow are
	private int maxStringLength;
	
	public PartitionManager(String alphabet, int maxStringLength) {
		this.alphabet = alphabet;
		processing = new TreeSet<Partition>();
		cached = new TreeSet<Partition>();
		caching = new TreeSet<Partition>();
		this.maxStringLength = maxStringLength;
		reset();
	}
	/*
	 * Resets the consumed blocks, maintains cached blocks
	 */
	
	public final void reset() {
		nextAvailable = new Partition(1, 0, 0);
		processing.clear();
	}

	/*
	 * Request a partition of the space, takes an size that gives that many
	 * blocks
	 *
	 * nextAvailable always has the first empty block counting from the bottom
	 *
	 */
	public Partition requestPartition(int size) {
		if (nextAvailable.stringLength > maxStringLength) {
			// No more blocks found, space has been exausted
			return null;
		}
		Partition result = nextAvailable.clone();
		// Find the next start block
		result = findPartitionStartBlock(result, size);
		if (result.stringLength < nextAvailable.stringLength) {
			// We need to move up to the next string length
			return requestPartition(size);
		}
		// We found a good start point, now find the end point
		result = findPartitionEndBlock(result, size);
		result.setStatus(Partition.Status.PROCESSING);
		if (result.endBlockNumber > nextAvailable.startBlockNumber) {
			nextAvailable.startBlockNumber = result.endBlockNumber;
		}
		processing.add(result);
		return result;
	}
	
	private Partition findPartitionStartBlock(Partition result, int size) {
		long numberOfBlocks = PlaintextSpace.getNumberOfBlocks(alphabet, result.stringLength);
		boolean blockIsGood;
		do {
			blockIsGood = true;
			// First check for processing blocks
			Partition pBlock = processing.ceiling(result);
			if (pBlock != null && pBlock.startBlockEquals(result)) {
				result.startBlockNumber = pBlock.endBlockNumber;
				blockIsGood = false;
			}
			// Now check for cached blocks
			Partition cBlock = cached.ceiling(result);
			if (cBlock != null && cBlock.startBlockEquals(result)) {
				result.startBlockNumber = cBlock.endBlockNumber;
				blockIsGood = false;
			}
			// Check if we hit the end of the number of blocks
			if (result.startBlockNumber >= numberOfBlocks) {
				// Reset, requestCache will notice that this has changed
				nextAvailable.stringLength++;
				nextAvailable.startBlockNumber = 0;
				return result;
			}
			// If all of the above pass, check the flag
		} while (!blockIsGood);
		return result;
	}
	
	private Partition findPartitionEndBlock(Partition result, int size) {
		long numberOfBlocks = PlaintextSpace.getNumberOfBlocks(alphabet, result.stringLength);
		// Set a max value and decrease from there
		result.endBlockNumber = Long.MAX_VALUE;
		Partition pBlock = processing.ceiling(result);
		if (pBlock != null
				&& result.stringLength == pBlock.stringLength
				&& result.endBlockNumber > pBlock.startBlockNumber) {
			result.endBlockNumber = pBlock.startBlockNumber;
		}
		Partition cBlock = cached.ceiling(result);
		if (cBlock != null
				&& result.stringLength == cBlock.stringLength
				&& result.endBlockNumber > cBlock.startBlockNumber) {
			result.endBlockNumber = cBlock.startBlockNumber;
		}
		// Check if we exceeded the size given
		if (result.startBlockNumber + size < result.endBlockNumber) {
			result.endBlockNumber = result.startBlockNumber + size;
		}
		// Check if we exceeded the number of blocks
		if (result.endBlockNumber > numberOfBlocks) {
			result.endBlockNumber = numberOfBlocks;
		}
		if (result.startBlockNumber >= result.endBlockNumber) {
			throw new RuntimeException("Something went horribly wrong with the algorithm(probably a bug)");
		}
		return result;
	}

	/*
	 * Notify Complete, tells the partition manager that a block is complete
	 */
	public void notifyComplete(Partition b) {
		if (processing.contains(b)) {
			processing.ceiling(b).setStatus(Partition.Status.COMPLETE);
		} else {
			throw new RuntimeException("Trying to complete block that was not requested");
		}
	}
	/*
	 * Notify Failure, tells the partition manager that a block has failed
	 * Generally caused by a client dcing without notice
	 */
	
	public void notifyFailure(Partition b) {
		if (processing.contains(b)) {
			processing.remove(b);
		}
		if (b.compareTo(nextAvailable) < 0) {
			nextAvailable = b;
		}
	}
	/*
	 * Request Cache, requests a block to be cached
	 *
	 * For now it only gives blocks in maxStringLength range
	 *
	 * Note: Almost a duplicate of requestPartition
	 */
	
	public Partition requestCache(int size) {
		int stringLength = maxStringLength;
		Partition result = new Partition(stringLength, 0, 0);
		// Find the next start block
		result = findCacheStartBlock(result, size);
		if (result == null) {
			return result;
		}
		// We found a good start point, now find the end point
		result = findCacheEndBlock(result, size);
		result.setStatus(Partition.Status.CACHING);
		caching.add(result);
		return result;
	}
	
	private Partition findCacheStartBlock(Partition result, int size) {
		long numberOfBlocks = PlaintextSpace.getNumberOfBlocks(alphabet, result.stringLength);
		
		boolean blockIsGood;
		do {
			blockIsGood = true;
			// First check for caching blocks
			Partition c1Block = caching.ceiling(result);
			if (c1Block != null && c1Block.startBlockEquals(result)) {
				result.startBlockNumber = c1Block.endBlockNumber;
				blockIsGood = false;
			}
			// Now check for cached blocks
			Partition c2Block = cached.ceiling(result);
			if (c2Block != null && c2Block.startBlockEquals(result)) {
				result.startBlockNumber = c2Block.endBlockNumber;
				blockIsGood = false;
			}
			// Check if we hit the end of the number of blocks
			if (result.startBlockNumber >= numberOfBlocks) {
				// Fail as in the method description
				return null;
			}
			// If all of the above pass, check the flag
		} while (!blockIsGood);
		return result;
	}
	
	private Partition findCacheEndBlock(Partition result, int size) {
		long numberOfBlocks = PlaintextSpace.getNumberOfBlocks(alphabet, result.stringLength);
		// Set a max value and decrease from there
		result.endBlockNumber = Long.MAX_VALUE;
		Partition c1Block = caching.ceiling(result);
		if (c1Block != null
				&& result.stringLength == c1Block.stringLength
				&& result.endBlockNumber > c1Block.startBlockNumber) {
			result.endBlockNumber = c1Block.startBlockNumber;
		}
		Partition c2Block = cached.ceiling(result);
		if (c2Block != null
				&& result.stringLength == c2Block.stringLength
				&& result.endBlockNumber > c2Block.startBlockNumber) {
			result.endBlockNumber = c2Block.startBlockNumber;
		}
		// Check if we exceeded the size given
		if (result.startBlockNumber + size < result.endBlockNumber) {
			result.endBlockNumber = result.startBlockNumber + size;
		}
		// Check if we exceeded the number of blocks
		if (result.endBlockNumber > numberOfBlocks) {
			result.endBlockNumber = numberOfBlocks;
		}
		if (result.startBlockNumber >= result.endBlockNumber) {
			throw new RuntimeException("Something went horribly wrong with the algorithm(probably a bug)");
		}
		return result;
	}
	/*
	 * Notify Cache, tells the partition manager that this block is being cached
	 * in a rainbowtable and should not be given as a job
	 *
	 */
	
	public void notifyCache(Partition b) {
		if (caching.contains(b)) {
			caching.remove(b);
		}
		if (!cached.contains(b)) {
			b.setStatus(Partition.Status.CACHED);
			cached.add(b);
		}
		// A cache may be building as a block is being brute forced
		// Let the brute force finish the check
		// notifyComplete(b);
	}
	/*
	 * Release cache, tells the partition manager that this blockrange is no
	 * longer being cached
	 */
	
	public void releaseCache(Partition b) {
		if (cached.contains(b)) {
			cached.remove(b);
		}
		if (caching.contains(b)) {
			caching.remove(b);
		}
	}
	/*
	 * For testing purposes only
	 */
	
	public TreeSet<Partition> getCached() {
		return cached;
	}
	
	public TreeSet<Partition> getCaching() {
		return caching;
	}
	
	public TreeSet<Partition> getProcessing() {
		return processing;
	}
}
