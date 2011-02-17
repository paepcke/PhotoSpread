package photoSpreadUtilities;

import java.io.EOFException;
import java.io.IOException;

/**************************** RAMReaderShortArray *************** 
 * 
 * @author paepcke
 * 
 *         Class: Given a short[], instances of this class satisfy the Java
 *         DataInput interface.
 */
public class RAMReaderCharArray extends RAMReader {

	char[] theInput = null;

	public RAMReaderCharArray(char[] anInput) {
		theInput = anInput;
	}

	/**
	 * @return Length of the input array:
	 */
	@Override
	protected int length() {
		return theInput.length;
	}

	/**
	 * Return next byte from the input array, without advancing the input
	 * array read pointer. If needed, pops one number from the input array.
	 * Chops that number into bytes, and returns the head byte to callers.
	 * When the whole popped number has been retrieved by callers to {@code
	 * getOneByte()}, this method pops another number from the input array
	 * when called the next time.
	 * 
	 * @return One byte, read from the input array
	 * @throws IOException
	 */
	@Override
	protected byte peek() throws IOException {
		if (arrayPointer >= theInput.length)
			throw new EOFException("Reached end of given RAM array");
		char resChar = theInput[arrayPointer];
		return (byte) resChar; 
	}

	@Override
	protected byte getOneByte() throws IOException {
		byte res = peek();
		arrayPointer++;
		return res;
	}

	/**
	 * Set input stream back to its beginning
	 */
	public void reset() {
		arrayPointer = 0;
	}
	
} // end class RAMReaderBooleanArray


