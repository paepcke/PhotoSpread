package edu.stanford.photoSpreadUtilities;

import java.io.EOFException;
import java.io.IOException;

/********************* RAMReaderByteArray *************** /**
 * 
 * @author paepcke
 * 
 *         Class: Given a byte[], instances of this class satisfy the Java
 *         DataInput interface.
 */
public class RAMReaderByteArray extends RAMReader {

	byte[] theInput = null;

	public RAMReaderByteArray(byte[] anInput) {
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
		return theInput[arrayPointer];
	}

	@Override
	protected byte getOneByte() throws IOException {
		byte res = peek();
		arrayPointer++;
		return res;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readFully(byte[]) Fill given byte array to the
	 * hilt.
	 * 
	 * Array copy from byte[] to byte[]:
	 */
	public void readFully(byte[] targetArr) throws IOException {
		if (targetArr == null)
			throw new NullPointerException(
					"Passed NULL as copy target to RAMReader readFully() procedure");

		int targetArrayLen = targetArr.length;
		// Target array larger than num of
		// bytes left in the byte array?
		int bytesLeft = theInput.length - arrayPointer - 1;
		if (bytesLeft == 0) return;
		if (targetArrayLen > bytesLeft)
			targetArrayLen = bytesLeft;
		
		for (int i = 0; i < targetArrayLen; i++)
			targetArr[i] = theInput[i+arrayPointer];
		arrayPointer += targetArrayLen;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readFully(byte[], int, int)
	 * 
	 * Partial array copy from byte[] to byte[]:
	 */
	public void readFully(byte[] targetArr, int off, int len)
			throws IOException {
		if (len == 0)
			return;
		if (targetArr == null)
			throw new NullPointerException(
					"Passed NULL as copy target to RAMReader readFully() procedure");
		if (off < 0)
			throw new IndexOutOfBoundsException(
					"Specified negative offset to RAMReader readFully() procedure");
		if (len < 0)
			throw new IndexOutOfBoundsException(
					"Specified negative length to RAMReader readFully() procedure");
		if (off + len > targetArr.length)
			throw new IndexOutOfBoundsException(
					"Length plus offset exceed byte array length in RAMReader's readFully() procedure");
		for (int i = 0; i < len; i++)
			targetArr[i + off] = theInput[i + arrayPointer];
		arrayPointer += len;
	}

	/**
	 * Set input stream back to its beginning
	 */
	public void reset() {
		arrayPointer = 0;
	}
	
	
} // end class RAMReaderByteArray
