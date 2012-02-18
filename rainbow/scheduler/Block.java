package rainbow.scheduler;

/*
 * Represents a single block
 * Includes both string length and block number
 */

public class Block implements Comparable<Block> {

    public int stringLength;
    public long blockNumber;

    public Block(int stringLength, long blockNumber) {
        this.stringLength = stringLength;
        this.blockNumber = blockNumber;
    }

    @Override
    public int compareTo(Block o) {
        if (this.stringLength != o.stringLength) {
            return Integer.compare(this.stringLength, o.stringLength);
        } else {
            return Long.compare(this.blockNumber, o.blockNumber);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Block == false) {
            return false;
        }
        Block other = (Block) obj;
        return this.stringLength == other.stringLength && this.blockNumber == other.blockNumber;
    }

    @Override
    public Block clone(){
        return new Block(stringLength, blockNumber);
    }

    @Override
    public String toString() {
        return "Block{" + "stringLength=" + stringLength + ", blockNumber=" + blockNumber + '}';
    }
}