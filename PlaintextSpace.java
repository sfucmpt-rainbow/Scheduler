

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
 * Warning: This class will not work if there are more than 2^63 plaintexts since that will overflow a long
 * i.e. upper-lower-alphanumeric length 11 is enough to overflow
 *
 * 
 * TODO:
 *      -Implement an iterator, will probably speed up the iteration if we reuse
 *      the same character array and only reqire an increment and a check
 * 
 *      -Check to make sure we haven't past a block
 *      Need to check both to make sure blockIndex < BLOCK_SIZE and that if its
 *      the last block then we should check that limit also
 */
class PlaintextSpace {

    public static final int BLOCK_SIZE = 1000000; // 1 million
    String alphabet;
    int alphabetLength;
    int blockNumber;
    int textLength;

    public PlaintextSpace(String alphabet, int blockNumber, int textLength) {
        this.alphabet = alphabet;
        this.alphabetLength = alphabet.length();
        this.blockNumber = blockNumber;
        this.textLength = textLength;
        if (Math.pow(alphabetLength, textLength) > Math.pow(2, 63)) {
            throw new RuntimeException("Error, too many plaintexts. Possible overflow error");
        }
    }

    /*
     * Gets a text value for a certain index
     */
    public String getText(int blockIndex) {
        char[] characterValues = new char[textLength];
        long index = blockNumber * BLOCK_SIZE + blockIndex;
        for (int i = 0; i < textLength; i++) {
            int offset = (int) (index % alphabetLength);
            characterValues[textLength - 1 - i] = alphabet.charAt(offset);
            index /= alphabetLength;
        }
        return new String(characterValues);
    }
    /*
     * Some basic unit tests, should be moved elsewhere later
     */
    public static void test() {
        PlaintextSpace ps = new PlaintextSpace(
                AlphabetGenerator.generateAlphabet(
                AlphabetGenerator.Types.UPPER_CASE,
                AlphabetGenerator.Types.LOWER_CASE,
                AlphabetGenerator.Types.NUMBERS),
                0, // Block Number
                7); // Text length
        if (!ps.getText(0).equals("AAAAAAA")) {
            throw new RuntimeException();
        }
        for (int i = 0; i < 100; i++) {
            System.out.println(ps.getText(i));
        }
        PlaintextSpace ps1 = new PlaintextSpace(
                AlphabetGenerator.generateAlphabet(
                AlphabetGenerator.Types.NUMBERS),
                32, // Block Number
                12); // Text length
        
        // Since its only numbers should just be block_number * block_size
        // i.e. 32 million with padded 0's
        if (!ps1.getText(0).equals("000032000000")) {
            throw new RuntimeException();
        }
    }

    public static void main(String[] s) {
        test();
    }
}