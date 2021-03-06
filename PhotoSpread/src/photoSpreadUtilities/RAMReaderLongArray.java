package photoSpreadUtilities;

import java.io.EOFException;
import java.io.IOException;

/*************************** RAMReaderLongArray *************** /**
 * 
 * @author paepcke
 * 
 *         Class: Given a long[], instances of this class satisfy the Java
 *         DataInput interface.
 */
public class RAMReaderLongArray extends RAMReader {

	long[] theInput = null;
	long currentPoppedNum = 0;

	public RAMReaderLongArray(long[] anInput) {
		theInput = anInput;
		numORightShiftsToDo = -1;
	}

	/**
	 * @return Length of the input array
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
		// Has the previously popped number been fed out?:
		if (numORightShiftsToDo < 0) {
			// Pop next number from the input array.
			if (arrayPointer >= theInput.length)
				throw new EOFException("Reached end of given RAM array");
			currentPoppedNum = theInput[arrayPointer++];
			numORightShiftsToDo = Const.NUM_BYTES_IN_ONE_LONG - 1;
		}
		currentByte = 0;
		long tmp = currentPoppedNum;
		for (int i=0; i<numORightShiftsToDo; i++)
			tmp = tmp >> Const.NUM_BITS_IN_ONE_BYTE;
		currentByte |= (byte) tmp;
		return currentByte;
	}

	@Override
	protected byte getOneByte() throws IOException {
		byte res = peek();
		// One less left-shift remaining for this popped
		// number. Method {@code peek()} will pop another number
		// if this decrement reached 0:
		numORightShiftsToDo--;
		return res;
	}
	
	/**
	 * Set input stream back to its beginning
	 */
	public void reset() {
		arrayPointer = 0;
		currentPoppedNum = 0;
		numORightShiftsToDo = -1;
	}
	
} // end class RAMReaderLongArray
