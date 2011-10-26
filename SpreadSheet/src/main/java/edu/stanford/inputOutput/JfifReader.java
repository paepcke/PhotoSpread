package edu.stanford.inputOutput;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * @author evanshieh
 *
 * Custom JfifReader for PhotoSpread - reads attribute/value pairs from the "Comment"
 * tag field (Marker (Magic Number): 0xfffe) in the JFIF metadata associated with a file. This reader then
 * exports the metadata to PhotoSpread and stores them in the PhotoSpreadObjects.
 * 
 * Capability details:
 * - Reads and parses RECONYX metadata
 * - Reads and parses BUCKEYE metadata
 */
public class JfifReader {

	/**
	 * Private Unicode Constants
	 */
	private static final int COM_MARKER_FIRST_BYTE = 0xff;
	private static final int COM_MARKER_SECOND_BYTE = 0xfe;
	private static final byte DQT_MARKER_SECOND_BYTE = (byte) 0xdb;
	private static final byte RECONYX_SEPARATOR = (byte) 0x0d;
	private static final byte BUCKEYE_SEPARATOR = (byte) 0x0a;
	private static final byte RECONYX_KEYVAL = (byte) 0x3a; // ':'
	private static final byte BUCKEYE_KEYVAL = (byte) 0x3d; // '='

	/****************************************************
	 * Public Methods
	 *****************************************************/
	
	/**
	 * Reads JFIF Comment metadata from an image file,
	 * parses it according to Reconyx/Buckeye specifications
	 * and stores it in an ArrayList of ArrayLists. 
	 * @param file
	 */
	public static ArrayList< ArrayList<String> > readMetaDataSet(File file) {
		// Initializes a result ArrayList and an Array of bytes containing the metadata
		ArrayList< ArrayList<String> > result = new ArrayList< ArrayList<String> >();
		byte[] bData = getCommentMetadata(file);
		if (bData == null) return result;

		// Initializes local variables to allow the loop through the bytes
		int length = 0; // Length of the "word"
		int sPos = 0; // Starting position of a new "word"
		String key = "";
		String value = "";
		boolean isBuckeyePhoto = false;
		boolean isReconyxPhoto = false;
		boolean hasReadKey = false; 
		// Necessary in order to allow for the KEYVAL characters to appear multiple times (ex.) Dat: 2007-07-24 17:12:24)

		// Loops through the metadata, storing the appropriate words in the ArrayList< ArrayList<String> >
		for (int i = 0; i < bData.length; i++) {
			byte b = bData[i];
			if (b == RECONYX_SEPARATOR || b == BUCKEYE_SEPARATOR) {
				if (b == BUCKEYE_SEPARATOR) {
					value = makeWord(bData, sPos, length);
					isBuckeyePhoto = true; // ID's the photo according to the unique separator
				} else {
					value = makeWord(bData, sPos + 1, length - 1); // in order to skip over the extra space that reconyx adds
					isReconyxPhoto = true;
				}
				addWord(result, key, value);

				// Resets variables
				sPos = i + 1;
				length = 0;
				hasReadKey = false;
			} else if (b == RECONYX_KEYVAL || b == BUCKEYE_KEYVAL) {
				if (hasReadKey) {
					length++; // Signifies that this byte is not a separator, but a character
				} else {
					key = makeWord(bData, sPos, length);

					// Resets variables 
					sPos = i + 1;
					length = 0;
					hasReadKey = true; // Assumes that the first KEYVAL byte is in fact the bridge for the key/value pair
				}
			} else {
				length++;
			}
		}

		// Adds an 'ID' tag to the metadata set - this will prevent multiple reads, and thus multiple inheritance problems with EXIF data
		if (!result.isEmpty()) {
			if (isBuckeyePhoto) {
				addWord(result, "HasBuckEyeData", "true");
			} else if (isReconyxPhoto) {
				addWord(result, "HasReconyxData", "true");
			}
		}

		return result;
	}

	/**
	 * Reads JFIF Comment metadata from an image file,
	 * parses it according to Reconyx/Buckeye specifications
	 * and stores it in an ArrayList of ArrayLists. 
	 * @param filePath
	 */
	public static ArrayList< ArrayList<String> > readMetaDataSet(String filePath) {
		File file = openFile(filePath);
		return readMetaDataSet(file);
	}

	/**
	 * Reads JFIF Comment metadata from an image file,
	 * and stores it in a String.
	 * @param file
	 */
	public static String readMetadataToString(File file) {
		String data = "";
		byte[] bData = getCommentMetadata(file);
		if (bData != null) {
			try {
				data = new String(bData, "US-ASCII");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return data;
	}

	/**
	 * Reads the metadata in the JFIF comment field into
	 * an Array of bytes.
	 * @param file
	 */
	public static byte[] getCommentMetadata(File file) {
		byte[] metadata = null;
		try {
			FileInputStream fStream = new FileInputStream(file);
			int commentLength = getCommentFieldLength(fStream);
			if (!(commentLength < 0)) {
				metadata = new byte[commentLength];
				try {
					fStream.read(metadata);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return metadata;
	}

	/**
	 * Reads the length of the comment field for a JPG
	 * image. Assumes that the length of the comment field 
	 * is encoded by the two bytes following the 0xfffe marker
	 * @param file
	 */
	public static int getCommentFieldLength(FileInputStream fStream) {
		int result = -1;
		if (readUntilComment(fStream)) { // Reads the fStream up until the comment field length (is true if successful)
			try {
				result += 256*fStream.read(); // Reads first byte
				result += fStream.read(); 
				result -= 2; // Since the bytes in the file stream account for two extra bytes (the bytes indicating size)
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			}
		}
		return result;
	}

	/****************************************************
	 * Private Methods
	 *****************************************************/

	/*
	 * Opens the file
	 */
	private static File openFile (String filePath) {
		File result = null;
		try {
			result = new File (filePath);
		} catch (Exception e){
			System.err.println("Error: " + e.getMessage());
		}
		return result;
	}

	/*
	 * Reads the fStream up until the comment field length (returns true if successful)
	 */
	private static boolean readUntilComment(FileInputStream fStream) {
		int bInt = -1;
		while (true) {
			try {
				bInt = fStream.read();
				if (bInt == COM_MARKER_FIRST_BYTE) { // Signifies 'ff'
					int nextByte = fStream.read();
					if (nextByte == -1) return false;
					if (nextByte == COM_MARKER_SECOND_BYTE) return true;
					if (nextByte == DQT_MARKER_SECOND_BYTE) return false; 
					// Assumes that the DQT marker comes after COM marker (short circuit evaluation for efficiency)
				}
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			if (bInt == -1) return false; //Signifies unsuccessful read, or end of file
		}
	}

	/*
	 * Returns a new String "word" from an Array of bytes based on a specified starting position and length of the word.
	 */
	private static String makeWord (byte[] bArray, int sPos, int length) {
		byte[] bWord = new byte[length];
		for (int i = sPos; i < sPos + length; i++) {
			bWord[i-sPos] = bArray[i];
		}
		String word = new String(bWord);
		return word;
	}

	/*
	 * Adds a key/value pair to a 2-D ArrayList of the metadata
	 */
	private static void addWord (ArrayList< ArrayList<String> > metadata, String key, String value) {
		ArrayList<String> keyPair = new ArrayList<String>();
		keyPair.add(key);
		keyPair.add(value);
		metadata.add(keyPair);
	}
}
