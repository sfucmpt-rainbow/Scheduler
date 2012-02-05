
import java.math.BigInteger;
import java.util.Arrays;

/*
 * Methods to numerically index the plaintext space
 *
 * There are three values, a block number, a block index and a length
 *
 * A block number represents a range of the space(block) i.e. aaaaa-ccccc There
 * are lots of blocks so represented by a long(64-bit integer)
 *
 * A block index indexes the values in a block i.e. 123456 -> abcde
 *
 * A length represents the space that the block is in, i.e. length = 2 gives
 * aa,ab,ac,ad ...
 *
 */
class PlaintextSpace {

    public static final String UPPER_LOWER_ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqustuvwxyz0123456789";
    public static final int ALPHABET_LENGTH;

    static {
        ALPHABET_LENGTH = UPPER_LOWER_ALPHANUMERIC.length();
    }
    /*
     * Choosen so that it takes approximately 5 mins per block at a rate of 2M
     * hashes per second 5 * 60 * 2M = 600M
     */
    public static final int BLOCK_SIZE = 600000000; // 600M

    /*
     * Utility method used to quickly check values, if a single block is going
     * to be accessed many times or sequentially, instantiate the class intead
     *
     * Note, does not do bounds checking, instantiate the class for that
     */
    public static String getPlaintext(long blockIndex, int index, int stringLength) {
        int[] values = getBlockStart(blockIndex, stringLength);
        return getStringInBlock(values, index);
    }
    /*
     * Gets the first string in a block as an array of offsets into the alphabet
     * -1 represents no character
     */

    private static int[] getBlockStart(long blockIndex, int stringLength) {
        int[] values = new int[stringLength];

        BigInteger alphabetLength = BigInteger.valueOf(ALPHABET_LENGTH);
        BigInteger offset = BigInteger.valueOf(blockIndex).multiply(BigInteger.valueOf(BLOCK_SIZE));
        for (int i = values.length - 1; i >= 0; i--) {
            BigInteger[] quotientAndRemainder = offset.divideAndRemainder(alphabetLength);
            values[i] = quotientAndRemainder[1].intValue();
            offset = quotientAndRemainder[0];
            if (offset.signum() == 0) {
                break;
            }
        }
        return values;
    }
    /*
     * Takes a block's starting array and offset and converts it to a string
     */

    private static String getStringInBlock(int[] values, int offset) {
        int length = values.length;

        int[] blockStart = values.clone();
        for (int i = length - 1; i >= 0; i--) {
            int rem = offset % ALPHABET_LENGTH;
            offset = offset / ALPHABET_LENGTH;

            // Add it to the array and check if any values overflowed
            blockStart[i] += rem;
            for (int j = i; j >= 0; j--) {
                if (blockStart[i] >= ALPHABET_LENGTH) {
                    blockStart[i] -= ALPHABET_LENGTH;
                    if (i > 0) {
                        blockStart[i - 1]++;
                    }
                } else {
                    break;
                }
            }
            // Stop once there is nothing else to add
            if (offset == 0) {
                break;
            }
        }
        char[] characters = new char[length];
        for (int i = 0; i < length; i++) {
            characters[length - 1 - i] = UPPER_LOWER_ALPHANUMERIC.charAt(blockStart[length - 1 - i]);
        }
        return new String(characters);
    }

    /*
     * End Static Properties
     */
    int[] blockStart;
    long blockNumber;
    int blockIndex = 0;
    int length;

    /*
     * TODO: Check if a blocknumber is valid
     * i.e. there is no block 2 of length 1 strings
     */
    public PlaintextSpace(long blockNumber, int length) {
        blockStart = getBlockStart(blockNumber, length);
        this.blockNumber = blockNumber;
        this.length = length;
    }
    /*
     * Get the values a a point in the current block
     */

    public String getAtIndex(int index) {
        return getStringInBlock(blockStart, index);
    }

    /*
     * Used to iterate through the array
     */
    public String getNext() {
        return getStringInBlock(blockStart, blockIndex++);
    }

    /*
     * Check if the next value is valid
     */
    public boolean hasNext() {
        if (blockIndex >= BLOCK_SIZE) {
            return false;
        }
        int temp = blockIndex;
        for (int i = 0; i < length; i++) {
            temp /= ALPHABET_LENGTH;
        }
        if (temp > 0) {
            return false;
        }
        return true;
    }

    public static void main(String[] s) {
        // Methods to test out this class
        System.out.println(Arrays.toString(getBlockStart(0, 1)));
        System.out.println(Arrays.toString(getBlockStart(1, 3)));
        System.out.println(Arrays.toString(getBlockStart(2, 5)));


        //for (int i = 0; i < 200; i++) {
        //    System.out.println(getPlaintext(0, i, 1));
        //}



        //for (int i = 0; i < 200; i++) {
        //    System.out.println(getPlaintext(1, i, 5));
        //}

        // Example of how to interate an entire space
        PlaintextSpace pt = new PlaintextSpace(0, 3);
        while (pt.hasNext() && false) {
            System.out.println(pt.getNext());
        }
        
        // Warning this loop takes a long time
        PlaintextSpace pt2 = new PlaintextSpace(0, 5);
        int i = 0;
        while (pt2.hasNext()) {
            if (i % 100000 == 0) {
                System.out.println(i + ":" + pt2.getNext());
            } else {
                pt2.getNext();
            }
            i++;
        }
    }
}