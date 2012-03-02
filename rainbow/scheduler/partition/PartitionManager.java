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
	// Keeps track of which blocks are having their cache built but is not ready yet
	private TreeSet<BlockRange> caching;
	// What the largest strings we allow are
	private int maxStringLength;

	public PartitionManager(String alphabet, int maxStringLength) {
		this.alphabet = alphabet;
		processing = new TreeSet<>();
		cached = new TreeSet<>();
		caching = new TreeSet<>();
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
		if (nextAvailable.stringLength > maxStringLength) {
			// No more blocks found, space has been exausted
			return null;
		}
		BlockRange result = nextAvailable.clone();
		long numberOfBlocks = PlaintextSpace.getNumberOfBlocks(alphabet, nextAvailable.stringLength);
		while (true) {
			boolean blockIsGood = true;
			// First check for processing blocks
			BlockRange pBlock = processing.ceiling(result);
			if (pBlock != null && pBlock.startBlockNumber == result.startBlockNumber) {
				result.startBlockNumber = pBlock.endBlockNumber;
				blockIsGood = false;
			}
			// Now check for cached blocks
			BlockRange cBlock = cached.ceiling(result);
			if (cBlock != null && cBlock.startBlockNumber == result.startBlockNumber) {
				result.startBlockNumber = cBlock.endBlockNumber;
				blockIsGood = false;
			}
			// Check if we hit the end of the number of blocks
			if (result.startBlockNumber >= numberOfBlocks) {
				// Reset
				nextAvailable.stringLength++;
				nextAvailable.startBlockNumber = 0;
				if (nextAvailable.stringLength > maxStringLength) {
					// No more blocks found, space has been exausted
					return null;
				}
				// Tail recursion(hopefully)
				return requestPartition(size);
			}
			// If all of the above pass, check the flag
			if (blockIsGood) {
				break;
			}
		}
		// We found a good start point, now find the end point
		// Set a max value and decrease from there
		result.endBlockNumber = Long.MAX_VALUE;
		BlockRange pBlock = processing.ceiling(result);
		if (pBlock != null && result.endBlockNumber > pBlock.startBlockNumber) {
			result.endBlockNumber = pBlock.startBlockNumber;
		}
		BlockRange cBlock = cached.ceiling(result);
		if (cBlock != null && result.endBlockNumber > cBlock.startBlockNumber) {
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
		result.setStatus(BlockRange.Status.PROCESSING);
		processing.add(result);
		return result;
	}

	/*
	 * Notify Complete, tells the partition manager that a block is complete
	 */
	public void notifyComplete(BlockRange b) {
		if (processing.contains(b)) {
			processing.ceiling(b).setStatus(BlockRange.Status.COMPLETE);
		} else {
			throw new RuntimeException("Trying to complete block that was not requested");
		}
	}
	/*
	 * Notify Failure, tells the partition manager that a block has failed
	 * Generally caused by a client dcing without notice
	 */

	public void notifyFailure(BlockRange b) {
		if (processing.contains(b)) {
			processing.remove(b);
		}
		if (b.startBlockNumber < nextAvailable.startBlockNumber) {
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

	public BlockRange requestCache(int size) {
		int stringLength = maxStringLength;
		BlockRange result = new BlockRange(stringLength, 0, 0);
		long numberOfBlocks = PlaintextSpace.getNumberOfBlocks(alphabet, nextAvailable.stringLength);
		while (true) {
			boolean blockIsGood = true;
			// First check for caching blocks
			BlockRange c1Block = caching.ceiling(result);
			if (c1Block != null && c1Block.startBlockNumber == result.startBlockNumber) {
				result.startBlockNumber = c1Block.endBlockNumber;
				blockIsGood = false;
			}
			// Now check for cached blocks
			BlockRange c2Block = cached.ceiling(result);
			if (c2Block != null && c2Block.startBlockNumber == result.startBlockNumber) {
				result.startBlockNumber = c2Block.endBlockNumber;
				blockIsGood = false;
			}
			// Check if we hit the end of the number of blocks
			if (result.startBlockNumber >= numberOfBlocks) {
				// Fail as in the method description
				return null;
			}
			// If all of the above pass, check the flag
			if (blockIsGood) {
				break;
			}
		}
		// We found a good start point, now find the end point
		// Set a max value and decrease from there
		result.endBlockNumber = Long.MAX_VALUE;
		BlockRange c1Block = caching.ceiling(result);
		if (c1Block != null && result.endBlockNumber > c1Block.startBlockNumber) {
			result.endBlockNumber = c1Block.startBlockNumber;
		}
		BlockRange c2Block = cached.ceiling(result);
		if (c2Block != null && result.endBlockNumber > c2Block.startBlockNumber) {
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
		result.setStatus(BlockRange.Status.CACHING);
		caching.add(result);
		return result;
	}
	/*
	 * Notify Cache, tells the partition manager that this block is being cached
	 * in a rainbowtable and should not be given as a job
	 *
	 */

	public void notifyCache(BlockRange b) {
		if (caching.contains(b)) {
			caching.remove(b);
		}
		if (!cached.contains(b)) {
			b.setStatus(BlockRange.Status.CACHED);
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

	public void releaseCache(BlockRange b) {
		cached.remove(b);
	}
	/*
	 * For testing purposes only
	 */

	public TreeSet<BlockRange> getCached() {
		return cached;
	}

	public TreeSet<BlockRange> getCaching() {
		return caching;
	}

	public TreeSet<BlockRange> getProcessing() {
		return processing;
	}
}
