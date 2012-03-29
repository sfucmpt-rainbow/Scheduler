package rainbow.scheduler.partition;

/*
 * Represents a single block Includes both string length and block number
 */
public class Partition implements Comparable<Partition> {

	public enum Status {

		INCOMPLETE,
		PROCESSING,
		CACHING,
		CACHED,
		COMPLETE;
	}
	public int stringLength;
	public long startBlockNumber;
	public long endBlockNumber;
	//Only used by the scheduler to maintain the blocks current status
	private Status status = Status.INCOMPLETE;

	/*
	 * Represents a range from startBlockNumber to endBlockNumber includes
	 * startblockNumber but excludes endBlockNumber so that for(long
	 * i=startBlockNumber;i<endBlockNumber;i++) works as expected
	 */
	public Partition(int stringLength, long startBlockNumber, long endBlockNumber) {
		this.stringLength = stringLength;
		this.startBlockNumber = startBlockNumber;
		this.endBlockNumber = endBlockNumber;
	}
	/*
	 * Compares two blocks and orders them Assumes that blocks do not overlap
	 * Compares by string length first then block number Only compares the
	 * starting block number, works assuming blocks don't overlap
	 */

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		if (status == Status.INCOMPLETE) {
			// Incomplete should never occur
			// If a block does not exist then it is incomplete
			throw new RuntimeException("Error, assigning incomplete status");
		}
		this.status = status;
	}

	@Override
	public int compareTo(Partition o) {
		if (this.stringLength != o.stringLength) {
			return this.stringLength - o.stringLength;
		} else {
			return (int) (this.startBlockNumber - o.startBlockNumber);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Partition == false) {
			return false;
		}
		Partition other = (Partition) obj;
		return this.stringLength == other.stringLength
				&& this.startBlockNumber == other.startBlockNumber
				&& this.endBlockNumber == other.endBlockNumber;
	}

	public boolean startBlockEquals(Partition p) {
		return this.stringLength == p.stringLength
				&& this.startBlockNumber == p.startBlockNumber;
	}

	@Override
	public Partition clone() {
		return new Partition(stringLength, startBlockNumber, endBlockNumber);
	}

	@Override
	public String toString() {
		return String.format("Block{stringLength=%s, blocks(%s,%s), status=%s}", stringLength, startBlockNumber, endBlockNumber, status.toString());
	}
}
