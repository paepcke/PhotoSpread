package edu.stanford.inputOutput;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;

/**
 * @author evanshieh
 *
 * Custom ExifReader for PhotoSpread - reads attribute/value pairs from the "UserComment"
 * tag field (Tag ID: 0x9286) in the EXIF metadata associated with a file. This reader then
 * exports the metadata to PhotoSpread and stores them in the PhotoSpreadObjects.
 * 
 * Additional read capabilities:
 * - Reads date/time field in the format 
 */
public class ExifReader {

	private static final long serialVersionUID = 1L;
	
	/****************************************************
	 * Public Methods
	 *****************************************************/
	
	/**
	 * Reads UserComment metadata from an image file and 
	 * stores it in an ArrayList of ArrayLists.
	 * @param file
	 */
	public static ArrayList< ArrayList<String> > readMetaDataSet(File file) {
		String data = readMetadataToString(file, TiffConstants.EXIF_TAG_USER_COMMENT);
		//String notPsData = getPrecedingUserCommentData(file);
		//data = data.substring(notPsData.length());
		data.trim();
		ArrayList< ArrayList<String> > result = parseToArrayList(data);
		return result;
	}
	
	/**
	 * Reads UserComment metadata from an image file location and 
	 * stores it in an ArrayList of ArrayLists.
	 * @param filePath
	 */
	public static ArrayList< ArrayList<String> > readMetaDataSet(String filePath) {
		File file = openFile(filePath);
		return readMetaDataSet(file);
	}

	/**
	 * Reads UserComment metadata from an image file location and 
	 * stores it in a String.
	 * @param file
	 * @param tagLocation
	 */
	public static String readMetadataToString(File file, TagInfo tagLocation) {
		IImageMetadata metadata = null;
		try {
			metadata = Sanselan.getMetadata(file);
		} catch (ImageReadException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String data = "";
		JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
		if (jpegMetadata != null) {
			TiffField field = jpegMetadata.findEXIFValue(tagLocation);
			if (field != null) data = field.getValueDescription();
		}

		return data;
	}
	
	/**
	 * Reads Time/Date metadata from an image file location and 
	 * stores it in an ArrayList of ArrayLists.
	 * @param filePath
	 */
	public static ArrayList< ArrayList<String> > readExifTimeDate(String filePath) {
		File file = openFile(filePath);
		String data = readMetadataToString(file, TiffConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
		data.trim();
		StringTokenizer lb = new StringTokenizer(data, " '");
		
		ArrayList< ArrayList<String> > result = new ArrayList< ArrayList<String> >();
		
		if (lb.hasMoreTokens()) {
			// Add date
			String date = lb.nextToken();
			ArrayList<String> datePair = new ArrayList<String>();
			date.trim();
			datePair.add("Date");
			datePair.add(date);
			result.add(datePair);
			
			// Add time
			String time = lb.nextToken();
			ArrayList<String> timePair = new ArrayList<String>();
			time.trim();
			timePair.add("Time");
			timePair.add(time);
			result.add(timePair);
		}
		return result;
	}
	
	/**
	 * Reads UserComment metadata from an image file location, with
	 * the intention of storing and preserving all data that wasn't 
	 * written by PhotoSpread. Thus, this method will return any data
	 * (as a string) written externally up until the point where 
	 * PhotoSpread data begins.
	 * 
	 * @param file
	 */
	public static String getPrecedingUserCommentData(File file) {
		String result = "";
		String metadata = readMetadataToString(file, TiffConstants.EXIF_TAG_USER_COMMENT);
		
		// Accounts for the single quotation marks added to the beginning/end of any new metadata by EXIF standards
		if (metadata.charAt(0) == '\'' && metadata.charAt(metadata.length() - 1) == '\'') { 
			metadata = metadata.substring(1, metadata.length()-1);
		}
		
		// Reads metadata up until user-defined metadata
		int index = 0;
		while (true) {
			if (index >= metadata.length()) break;
			char ch = metadata.charAt(index);
			if (ch == '[') break; // Assumes preceding comment doesn't have '[' character
			result += ch;
			index++;
		}
		return result;
	}
	
	/****************************************************
	 * Private Methods
	 *****************************************************/
	
	/*
	 * Opens a file
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
	 * Parses a string to an Arraylist defined by PhotoSpread specifications (UserComment field only)
	 */
	private static ArrayList< ArrayList<String> > parseToArrayList(String data) {
		// Tokenizer for parsing according to line breaks (lb)
		data.trim();
		StringTokenizer lb = new StringTokenizer(data, "\n" + "'"); // Decodes metadata (stored with a single quotation before/after)
		ArrayList< ArrayList<String> > result = new ArrayList< ArrayList<String> >();
		
		while (lb.hasMoreTokens()) {
			String line = lb.nextToken();
			line.trim();
			if (line.charAt(0) == '[' && line.charAt(line.length() - 1) == ']') {
				ArrayList<String> pair = addPair(line);
				result.add(pair);
			} 
		}
		return result;
	}

	/*
	 * Reads in a line of PhotoSpread metadata, then parses it accordingly
	 * and stores it in an ArrayList of length 2 (a pair)
	 */
	private static ArrayList<String> addPair(String line) {
		ArrayList<String> result = new ArrayList<String>();
		boolean isKey = true;
		String key = "";
		String value = "";
		
		for (int i = 1; i < line.length() - 1; i++) { // In order to avoid brackets
			char ch = line.charAt(i);
			if (ch == '\t') {
				isKey = false;
			} else if (isKey) {
				key += ch;
			} else {
				value += ch;
			}
		}
		
		key.trim();
		value.trim();
		result.add(key);
		result.add(value);
		
		return result;
	}
}