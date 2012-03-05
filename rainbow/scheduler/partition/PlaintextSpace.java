package rainbow.scheduler.partition;

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
 * Warning: This class will not work if there are more than 2^63 plaintexts
 * since that will overflow a long i.e. upper-lower-alphanumeric length 11 is
 * enough to overflow
 *
 *
 * TODO: -Implement an iterator, will probably speed up the iteration if we
 * reuse the same character array and only reqire an increment and a check
 *
 * -Check to make sure we haven't past a block Need to check both to make sure
 * blockIndex < BLOCK_SIZE and that if its the last block then we should check
 * that limit also
 */
public class PlaintextSpace {

	public static final int BLOCK_SIZE = 100000000; // 100 million

	/*
	 * Calculates the number of blocks given a alphabet and the length of the
	 * plaintext
	 */
	public static long getNumberOfBlocks(String alphabet, int textLength) {
		long totalSize = 1;
		int alphabetLength = alphabet.length();
		for (int i = 0; i < textLength; i++) {
			totalSize *= alphabetLength;
		}
		// Not a very efficient way of doing it but it works
		if (totalSize % BLOCK_SIZE == 0) {
			return totalSize / BLOCK_SIZE;
		} else {
			return totalSize / BLOCK_SIZE + 1;
		}
	}
	String alphabet;
	int alphabetLength;
	long blockNumber;
	int textLength;

	public PlaintextSpace(String alphabet, long blockNumber, int textLength) {
		this.alphabet = alphabet;
		this.alphabetLength = alphabet.length();
		this.blockNumber = blockNumber;
		this.textLength = textLength;
		if (Math.pow(alphabetLength, textLength) > Math.pow(2, 63)) {
			throw new RuntimeException("Error, too many plaintexts. Possible overflow error");
		}
	}
	/*
	 * Shortcut to the static function
	 */

	public long getNumberOfBlocks() {
		return getNumberOfBlocks(alphabet, textLength);
	}
	/*
	 * Gets a text value for a certain index
	 */

	public String getText(int blockIndex) {
		char[] characterValues = new char[textLength];
		/*
		 * Calculate the index of this text Make sure one value is casted to
		 * long so we don't overflow int
		 */
		long index = (long) blockNumber * BLOCK_SIZE + blockIndex;
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
		System.out.println(String.format("There are %s blocks", ps1.getNumberOfBlocks()));
		// Since its only numbers should just be block_number * block_size
		// i.e. 3.2 billion with padded 0's
		if (!ps1.getText(0).equals("003200000000")) {
			throw new RuntimeException(String.format("Got %s and expected %s", ps1.getText(0), "003200000000"));
		}
		// All numbers from 000,000,000 to 999,999,999 = 1b combinations
		// Given blocks of 100m there should be 10 blocks
		if (getNumberOfBlocks(AlphabetGenerator.generateAlphabet(AlphabetGenerator.Types.NUMBERS), 9) != 10) {
			throw new RuntimeException();
		}
		// a-z = 26 letters, 5 character string = 26^5 = 11.8m combinations
		// blocks of 100m means there should be 1 blocks
		if (getNumberOfBlocks(AlphabetGenerator.generateAlphabet(AlphabetGenerator.Types.LOWER_CASE), 5) != 1) {
			throw new RuntimeException();
		}
	}

	public static void main(String[] s) {
		test();
	}
}