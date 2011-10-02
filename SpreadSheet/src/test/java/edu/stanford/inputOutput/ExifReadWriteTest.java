package edu.stanford.inputOutput;

// TODO: turn this into a junit test.

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;

//import acm.program.ConsoleProgram;



public class ExifReadWriteTest {

	private static final long serialVersionUID = 1L;
	private static final byte RECONYX_SEPARATOR = (byte) 0x0d; // 0x0d
	private static final byte BUCKEYE_SEPARATOR = (byte) 0x0a; // 0x0a
	private static final byte RECONYX_KEYVAL = (byte) 0x3a; // 0x3a, ':'
	private static final byte BUCKEYE_KEYVAL = (byte) 0x3d; // 0x3d, '='

	/* Public Methods */

	/* 
	 * Method: init() 
	 */
	/**
	 * This method has the responsibility for initializing the 
	 * interactors in the application, and taking care of any other 
	 * initialization that needs to be performed.
	 */
	public void init() {

	}

	/* 
	 * Method: run() 
	 */
	/**
	 * This method dislays the initial start-up message.
	 */
	public void run() {
		while (true) {
			System.out.System.out.println("Please enter the filename (no path needed, RETURN to break): ");
			String fileName = System.in.readLine();
			if (fileName.equals("")) break;
			//int numBytes = readInt("Please enter the number of characters you wish to read: ");
			File testImageFile = getFile(fileName);
			
			//ExifWriter.clearDataField(testImageFile.getAbsolutePath(), TiffConstants.EXIF_TAG_USER_COMMENT);
			
			ArrayList< ArrayList<String> > metadata = JfifReader.readMetaDataSet(testImageFile);
			if (!(metadata.isEmpty())) {
				for (int i = 0; i < metadata.size(); i++) {
					ArrayList<String> keyPair = metadata.get(i);
					println(keyPair.get(0) + ", " + keyPair.get(1));
				}
			}
			
			
			
			/*
			String key = "";
			String value = "";
			byte[] bData = JfifReader.getCommentMetadata(testImageFile);
			//String sMetadata = new String(bData);
			int i = 0;
			while (true) {
				byte b = bData[i];
				println(b);
				if (b == 0x09) break;
				i++;
				if (i == bData.length) break;
			}
			*/
		
			/*
			String metadata = ExifReader.readMetadataToString(testImageFile, TiffConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
			println(metadata);
			
			println("");
			*/
			
			/*
			int length = 0;
			int sPos = 0;
			String key = "";
			String value = "";
			byte[] bData = JfifReader.getCommentMetadata(testImageFile);
			for (int i = 0; i < bData.length; i++) {
				byte b = bData[i];
				if (b == RECONYX_SEPARATOR || b == BUCKEYE_SEPARATOR || b == RECONYX_KEYVAL || b == BUCKEYE_KEYVAL) {
					if (b == RECONYX_SEPARATOR || b == BUCKEYE_SEPARATOR) {
						println(b + " Separator");
						value = makeWord(bData, sPos, length);
						println("Value: " + value);
					}
					if (b == RECONYX_KEYVAL || b == BUCKEYE_KEYVAL) {
						println(b + " Key/Value Pair");
						key = makeWord(bData, sPos, length);
						println("Key: " + key);
					}
					println("Length: " + length);
					println("Starting position: " + sPos);
					length = 0;
					sPos = i + 1;
				} else {
					println(b);
					length++;
				}
			}
			println("Length: " + length);
			*/
			
			/*
			ArrayList<String> metadataArray = ExifReader.readMetaDataKeyValue(testImageFile);
			println(metadataArray.toString());
			
		
			println("BEFORE update");
			readMetaData(testImageFile);
			
			String filePath = getFilePath(fileName);
			test(filePath);

			println("\nAFTER update");
			readMetaData(testImageFile);
			*/
		}
		println("The program has terminated");
	}

	private File getFile(String fileName){
		if (fileName.equals("")) return null;
		String filePath = "src/img/test/" + fileName + ".jpg";
		File result = null;
		result = new File(filePath);
		return result;			
	}
	
	private String getFilePath(String fileName) {
		if (fileName.equals("")) return null;
		String filePath = "src/img/test/" + fileName + ".jpg";
		return filePath;
	}
	
	@SuppressWarnings("unused")
	private void test(File testImageFile) {
		ArrayList< ArrayList<String> > tags = new ArrayList< ArrayList<String> >();
		String key = "";
		String value = "";
		
		while (true) {
			ArrayList<String> entry = new ArrayList<String>();
			key = readLine("Please enter the key: ");
			if (key.equals("")) break;
			value = readLine("Please enter the value: ");
			if (value.equals("")) break;
			entry.add(key);
			entry.add(value);
			tags.add(entry);
		}
		
		ExifWriter.write(testImageFile, tags);
	}
	
	private void test(String filePath) {
		ArrayList< ArrayList<String> > tags = new ArrayList< ArrayList<String> >();
		String key = "";
		String value = "";
		
		/*
		while (true) {
			ArrayList<String> entry = new ArrayList<String>();
			key = readLine("Please enter the key: ");
			if (key.equals("")) break;
			value = readLine("Please enter the value: ");
			if (value.equals("")) break;
			entry.add(key);
			entry.add(value);
			tags.add(entry);
		}
		
		ExifWriter.write(filePath, tags);
		*/
		
		key = readLine("Please enter the text to be written: ");
		ExifWriter.write(filePath, key);
	}

	/**
	 * Read metadata from image file and display it. 
	 * @param file
	 */
	@SuppressWarnings("rawtypes")
	private void readMetaData(File file) {
		IImageMetadata metadata = null;
		try {
			metadata = Sanselan.getMetadata(file);
		} catch (ImageReadException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (metadata instanceof JpegImageMetadata) {
			JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
			println("\nFile: " + file.getPath());
			printTagValue(jpegMetadata,
					TiffConstants.TIFF_TAG_XRESOLUTION);
			printTagValue(jpegMetadata,
					TiffConstants.TIFF_TAG_DATE_TIME);
			printTagValue(jpegMetadata,
					TiffConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
			printTagValue(jpegMetadata,
					TiffConstants.EXIF_TAG_CREATE_DATE);
			printTagValue(jpegMetadata,
					TiffConstants.EXIF_TAG_ISO);
			printTagValue(jpegMetadata,
					TiffConstants.EXIF_TAG_SHUTTER_SPEED_VALUE);
			printTagValue(jpegMetadata,
					TiffConstants.EXIF_TAG_APERTURE_VALUE);
			printTagValue(jpegMetadata,
					TiffConstants.EXIF_TAG_BRIGHTNESS_VALUE);

			// simple interface to GPS data
			TiffImageMetadata exifMetadata = jpegMetadata.getExif();
			if (exifMetadata != null) {
				try {
					TiffImageMetadata.GPSInfo gpsInfo = exifMetadata.getGPS();
					if (null != gpsInfo) {
						double longitude = gpsInfo.getLongitudeAsDegreesEast();
						double latitude = gpsInfo.getLatitudeAsDegreesNorth();
						println("    " +
								"GPS Description: " + gpsInfo);
						println("    " +
								"GPS Longitude (Degrees East): " +
								longitude);
						println("    " +
								"GPS Latitude (Degrees North): " +
								latitude);
					}
				} catch (ImageReadException e) {
					e.printStackTrace();
				}
			}

			println("EXIF items -");
			ArrayList items = jpegMetadata.getItems();
			for (int i = 0; i < items.size(); i++) {
				Object item = items.get(i);
				println("    " + "item: " +
						item);
			}
			println();
		}
	}

	private void printTagValue(JpegImageMetadata jpegMetadata, TagInfo tagInfo) {
		TiffField field = jpegMetadata.findEXIFValue(tagInfo);
		if (field == null) {
			println(tagInfo.name + ": " +
			"Not Found.");
		} else {
			println(tagInfo.name + ": " +
					field.getValueDescription());
		}
	}
	
	private static String makeWord (byte[] bArray, int sPos, int length) {
		byte[] bWord = new byte[length];
		for (int i = sPos; i < sPos + length; i++) {
			bWord[i-sPos] = bArray[i];
		}
		String word = new String(bWord);
		return word;
	}
	
	private static void addWord (ArrayList< ArrayList<String> > metadata, String key, String value) {
		ArrayList<String> keyPair = new ArrayList<String>();
		keyPair.add(key);
		keyPair.add(value);
		metadata.add(keyPair);
	}
	
	public static void main(String[] args) {
		ExifReadWriteTest test = new ExifReadWriteTest();
		test.run();
	}
}
