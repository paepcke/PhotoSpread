package edu.stanford.photoSpreadUtilities;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;

/**
 * 
 */

/**
 * @author paepcke
 * 
 */

public abstract class RAMReader implements DataInput {

	// Make constructors for long[], byte[], int[], and String.
	// Make all methods below work for all.

	protected int arrayPointer = 0;
	protected short numORightShiftsToDo = 0;
	protected byte currentByte = 0;

	// ******************************** Methods to Override ********************

	protected byte getOneByte() throws IOException {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	protected byte peek() throws IOException {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	protected int length() {
		throw new UnsupportedOperationException("Not yet implemented");

	}

	// ******** Methods to that implement the DataInput Interface
	// ********************

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readUTF() NOT Implemented!!!
	 */
	public String readUTF() throws IOException {
		throw new IOException("RAMReader does not support UTF.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readFully(byte[]) Fill given byte array to the
	 * hilt.
	 * 
	 * We don't make this method final, b/c for some input array types this
	 * method may be done more efficiently:
	 */
	public void readFully(byte[] targetArr) throws IOException {
		if (targetArr == null)
			throw new NullPointerException(
					"Passed NULL as copy target to RAMReader readFully() procedure");
		// Very inefficient for now:
		for (int i = 0; i < targetArr.length; i++)
			targetArr[i] = getOneByte();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readFully(byte[], int, int)
	 * 
	 * We don't make this method final, b/c for some input array types this
	 * method may be done more efficiently:
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
			targetArr[i + off] = getOneByte();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readByte()
	 */
	public final byte readByte() throws IOException {
		return (getOneByte());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readChar() Note: the return char is Unicode (i.e.
	 * two bytes).
	 */
	public final char readChar() throws IOException {
		byte firstByte = getOneByte();
		// Is it US-Ascii? (leading bit=0)
		if ((firstByte & 0x80) == 0)
			return (char) firstByte;
		// Two-byte char:
		byte secondByte = getOneByte();
		return (char) ((firstByte << 8) | (secondByte & 0xff));
	}

	@Override
	public short readShort() throws IOException {
		byte firstByte = getOneByte();
		byte secondByte = getOneByte();
		return (short) ((firstByte << 8) | (secondByte & 0xff));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readUnsignedShort()
	 */
	public final int readUnsignedShort() throws IOException {
		byte firstByte = getOneByte();
		byte secondByte = getOneByte();

		return (((firstByte & 0xff) << 8) | (secondByte & 0xff));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readInt()
	 */
	public final int readInt() throws IOException {
		byte[] theBytes = new byte[Const.NUM_BYTES_IN_ONE_INT];
		for (int i = 0; i < Const.NUM_BYTES_IN_ONE_INT; i++)
			theBytes[i] = getOneByte();
		int res = 0;
		res = (theBytes[0] << 24) | (theBytes[1] << 16) | (theBytes[2] << 8) | theBytes[3];
		return res;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readLong()
	 */
	public final long readLong() throws IOException {
		byte[] theBytes = new byte[Const.NUM_BYTES_IN_ONE_LONG];
		for (int i = 0; i < Const.NUM_BYTES_IN_ONE_LONG; i++)
			theBytes[i] = getOneByte();

		return (((long) (theBytes[0] & 0xff) << 56)
				| ((long) (theBytes[1] & 0xff) << 48)
				| ((long) (theBytes[2] & 0xff) << 40)
				| ((long) (theBytes[3] & 0xff) << 32)
				| ((long) (theBytes[4] & 0xff) << 24)
				| ((long) (theBytes[5] & 0xff) << 16)
				| ((long) (theBytes[6] & 0xff) << 8) | ((long) (theBytes[7] & 0xff)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readFloat()
	 */
	public final float readFloat() throws IOException {
		int baseInt = readInt();
		return Float.intBitsToFloat(baseInt);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readDouble()
	 */
	public final double readDouble() throws IOException {
		long baseLong = readLong();
		return Double.longBitsToDouble(baseLong);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readBoolean()
	 */
	public final boolean readBoolean() throws IOException {
		if (getOneByte() == 0)
			return false;
		else
			return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readUnsignedByte()
	 */
	public final int readUnsignedByte() throws IOException {
		return (getOneByte() & 0xff);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readLine()
	 */
	public final String readLine() throws IOException {
		String resStr = "";
		char oneChar;

		do {
			oneChar = readChar();
			if (oneChar == '\n')
				return resStr;
			if (oneChar == '\r') {
				char nextChar = (char) peek();
				if (nextChar == '\n')
					// Throw away the \n that follows the \r
					readChar();
				return resStr;
			} else
				resStr += oneChar;
		} while (true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#skipBytes(int)
	 */
	public final int skipBytes(int numToSkip) throws IOException {
		int numBytesSkipped = 0;
		try {
			for (numBytesSkipped = 0; numBytesSkipped < numToSkip; numBytesSkipped++)
				getOneByte();
		} catch (EOFException e) {
			// just ignore it, as called for by the Interface specification
		}
		return numBytesSkipped;
	}

	public static void main(String[] args) {
		// Test RAMReader

		//-------------- Byte Array --------------------
		
		System.out.println("Begin Test RAMReaderByteArray");
		byte[] byteArray = new byte[] { 1, 2, 3, 4 };
		RAMReaderByteArray byteArrayReader = new RAMReaderByteArray(byteArray);
		printAll_Bytes(byteArray, byteArrayReader);
		
		byteArray[0] = 0;
		byteArray[1] = 0;
		byteArray[2] = 0;
		byteArray[3] = 22;
		
		byteArrayReader = new RAMReaderByteArray(byteArray);
		printOne_Int(byteArrayReader);
		System.out.println("End Test RAMReaderByteArray");
		//-------------------------------------------------
		System.out.println("Begin Test RAMReaderCharArray");
		char [] charArray = new char[] {'a', 'b', 'c', 'd'};
		RAMReaderCharArray charArrayReader = new RAMReaderCharArray(charArray);
		printAll_Chars(charArray, charArrayReader);
		charArrayReader.reset();
		printOne_Char(charArrayReader);
		System.out.println("End Test RAMReaderCharArray");
		//-------------------------------------------------
		System.out.println("Begin Test RAMReaderShortArray");
		short[] shortArray = new short[] {1,2,3,4};
		RAMReaderShortArray shortArrayReader = new RAMReaderShortArray(shortArray);
		printAll_Shorts(shortArray, shortArrayReader);
		shortArrayReader.reset();
		printOne_Short(shortArrayReader);
		System.out.println("End Test RAMReaderShortArray");
		//-------------------------------------------------
		System.out.println("Begin Test RAMReaderIntArray");
		int[] intArray = new int[] {1,2,3,4};
		RAMReaderIntArray intArrayReader = new RAMReaderIntArray(intArray);
		printAll_Ints(intArray, intArrayReader);
		intArrayReader.reset();
		printOne_Int(intArrayReader);
		System.out.println("End Test RAMReaderIntArray");
		//-------------------------------------------------
		System.out.println("Begin Test RAMReaderLongArray");
		long[] longArray = new long[] {1,2,3,4};
		RAMReaderLongArray longArrayReader = new RAMReaderLongArray(longArray);
		printAll_Longs(longArray, longArrayReader);
		longArrayReader.reset();
		printOne_Long(longArrayReader);
		System.out.println("End Test RAMReaderLongArray");
		//-------------------------------------------------
		System.out.println("Begin Test RAMReaderDoubleArray");
		double[] doubleArray = new double[] {1,2,3,4};
		RAMReaderDoubleArray doubleArrayReader = new RAMReaderDoubleArray(doubleArray);
		printAll_Doubles(doubleArray, doubleArrayReader);
		doubleArrayReader.reset();
		printOne_Double(doubleArrayReader);
		System.out.println("End Test RAMReaderDoubleArray");
		
		
		
	}

	private static void printAll_Bytes(byte[] byteArray,	RAMReader arrayReader) {
		for (int i = 0; i < Const.NUM_BYTES_IN_ONE_BYTE * byteArray.length; i++)
			try {
				System.out.println("One byte: " + arrayReader.readByte());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	private static void printAll_Shorts(short[] shortArray, RAMReader arrayReader) {
		for (int i = 0; i < Const.NUM_BYTES_IN_ONE_SHORT *  shortArray.length; i++)
			try {
				System.out.println("One byte from short: " + arrayReader.readByte());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	private static void printAll_Ints(int[] intArray, RAMReader arrayReader) {
		for (int i = 0; i < Const.NUM_BYTES_IN_ONE_INT *  intArray.length; i++)
			try {
				System.out.println("One byte from int: " + arrayReader.readByte());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	private static void printAll_Longs(long[] longArray, RAMReader arrayReader) {
		for (int i = 0; i < Const.NUM_BYTES_IN_ONE_LONG *  longArray.length; i++)
			try {
				System.out.println("One byte from long: " + arrayReader.readByte());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	private static void printAll_Doubles(double[] doubleArray, RAMReader arrayReader) {
		for (int i = 0; i < Const.NUM_BYTES_IN_ONE_DOUBLE *  doubleArray.length; i++)
			try {
				System.out.println("One byte from double: " + arrayReader.readByte());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	/*
	private static void printAll_Booleans(boolean[] booleanArray, RAMReader arrayReader) {
		for (int i = 0; i < Const.NUM_BYTES_IN_ONE_BOOLEAN * booleanArray.length; i++)
			try {
				System.out.println("One byte from boolean: " + arrayReader.readByte());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	*/
	
	private static void printAll_Chars(char[] booleanArray, RAMReader arrayReader) {
		for (int i = 0; i < Const.NUM_BYTES_IN_ONE_CHAR *  booleanArray.length; i++)
			try {
				System.out.println("One byte from char: " + arrayReader.readByte());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	private static void printOne_Short(RAMReader arrayReader) {
			try {
				System.out.println("One short: " + arrayReader.readShort());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	private static void printOne_Int(RAMReader arrayReader) {
			try {
//				for (int i=0; i<4; i++)
					System.out.println("One int: " + arrayReader.readInt());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	private static void printOne_Long(RAMReader arrayReader) {
			try {
				System.out.println("One long: " + arrayReader.readLong());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	private static void printOne_Double(RAMReader arrayReader) {
			try {
				System.out.println("One double: " + arrayReader.readDouble());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	private static void printOne_Char(RAMReader arrayReader) {
			try {
				System.out.println("One char: " + arrayReader.readChar());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	/*
	private static void printOne_Boolean(RAMReader arrayReader) {
			try {
				System.out.println("One boolean: " + arrayReader.readBoolean());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	*/
	
	
	
} // end class RAMReader

