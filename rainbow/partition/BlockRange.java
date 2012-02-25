package rainbow.partition;

/*
 * Represents a single block Includes both string length and block number
 */
public class BlockRange implements Comparable<BlockRange> {

    /* Unused for now
    public enum BlockRangeStatus {

        INCOMPLETE,
        PROCESSING,
        CACHED,
        COMPLETE;
    }
    */
    public int stringLength;
    public long startBlockNumber;
    public long endBlockNumber;
    /* Unused for now
    //Only used by the scheduler to maintain the blocks current status
    public BlockRangeStatus status = BlockRangeStatus.INCOMPLETE;
    */

    public BlockRange(int stringLength, long startBlockNumber, long endBlockNumber) {
        this.stringLength = stringLength;
        this.startBlockNumber = startBlockNumber;
        this.endBlockNumber = endBlockNumber;
    }
    /*
     * Compares two blocks and orders them Assumes that blocks do not overlap
     * Compares by string length first then block number Only compares the
     * starting block number, works assuming blocks don't overlap
     */

    @Override
    public int compareTo(BlockRange o) {
        if (this.stringLength != o.stringLength) {
            return Integer.compare(this.stringLength, o.stringLength);
        } else {
            return Long.compare(this.startBlockNumber, o.startBlockNumber);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BlockRange == false) {
            return false;
        }
        BlockRange other = (BlockRange) obj;
        return this.stringLength == other.stringLength
                && this.startBlockNumber == other.startBlockNumber
                && this.endBlockNumber == other.endBlockNumber;
    }

    @Override
    public BlockRange clone() {
        return new BlockRange(stringLength, startBlockNumber, endBlockNumber);
    }

    @Override
    public String toString() {
        return String.format("Block{stringLength=%s, blocks(%s,%s)}", stringLength, startBlockNumber, endBlockNumber);
    }
}