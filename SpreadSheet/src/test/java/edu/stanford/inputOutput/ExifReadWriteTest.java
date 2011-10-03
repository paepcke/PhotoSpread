package edu.stanford.inputOutput;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;

/**
 * jUnit test for reading and writing Exif to jpg images.
 * 
 * @author paepcke
 *
 */
public class ExifReadWriteTest extends TestCase {

	static String currDir = System.getProperty("user.dir");

	// Test image that's always present in the test branch's
	// resources subtree:
	static String exifEmptyMetadataImgFileName;	
	static File exifEmptyMetadataImgFile;
	
	// We'll keep copying the above test image
	// whenever we need a fresh image to test on:
	static String exifTempMetadataImgFileName;
	static File exifTempMetadataImgFile = null;
	
	// Test image that contains camera Exif entries:
	static String imgWithCameraExifFileName;
	static File imgWithCameraExifFile = null;
	
	// For results returned by ExifReader():
	static ArrayList< ArrayList<String> > arrayRes = null;
	
	protected void setUp() throws Exception {
		super.setUp();

		// All test images are in the resources/TestCases/Photos
		// subtree below the code root:
		exifEmptyMetadataImgFileName = InputOutput.normalizePath(currDir + 
				"/src/test/resources/TestCases/Photos/ambulance.jpg");
		exifEmptyMetadataImgFile = new File(exifEmptyMetadataImgFileName);
		
		exifTempMetadataImgFileName = InputOutput.normalizePath(currDir + 
				"/src/test/resources/TestCases/Photos/ambulanceTemp.jpg");
		
		imgWithCameraExifFileName = InputOutput.normalizePath(currDir + 
				"/src/test/resources/TestCases/Photos/imgWithCameraExif.jpg");
		imgWithCameraExifFile = new File(imgWithCameraExifFileName);		
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		// Remove the temporary test image without complaint
		// about the File instance still being null, or the
		// file not being there. Just do our best:
		FileUtils.deleteQuietly(exifTempMetadataImgFile);
	}
	
	public final void testReadEmptyMetadata() {
		arrayRes = ExifReader.readMetaDataSet(exifEmptyMetadataImgFileName);
		assertTrue("Reading from virgin image: expecting empty metadata", 
					arrayRes.isEmpty());
	}
	
	public final void testWriteReadOneKey() throws IOException {

		// Build [["key1", "value1"]]:
		ArrayList< ArrayList<String> > oneKeyMetadataArray = 
			new ArrayList<ArrayList<String>> ();
		
		ArrayList<String> singleKeyArray =
			new ArrayList<String>();
		singleKeyArray.add("key1");
		singleKeyArray.add("value1");
		
		oneKeyMetadataArray.add(singleKeyArray);
		
		// Get a fresh copy of the no-metadata reference file:
		createNoMetadataImgFile();
		
		// Write the metadata:
		ExifWriter.write(exifTempMetadataImgFileName, oneKeyMetadataArray);
		
		// ... and read it back:
		arrayRes = ExifReader.readMetaDataSet(exifTempMetadataImgFileName);
		
		// ArrayLists test equality be checking all the individual elements:
		assertEquals("Read back single-key metadata written from array.",
			oneKeyMetadataArray,
			arrayRes);
	}
	
	public final void testWriteReadKeysFromHashtable() throws IOException {
		
		// Build the hashmap that holds the metadata to write:
		HashMap<String, String> keyValuePairHashMap = new HashMap<String, String>();
		keyValuePairHashMap.put("key2", "value2");
		keyValuePairHashMap.put("key3", "value3");
		
		// Get a fresh copy of the no-metadata reference file:
		createNoMetadataImgFile();
		
		ExifWriter.write(exifTempMetadataImgFileName, keyValuePairHashMap);
		
		arrayRes = ExifReader.readMetaDataSet(exifTempMetadataImgFileName);
		
		// Turn the result nested ArrayList into a HashMap
		// on the fly, and compare with the HashMap we put in.
		// HashMaps know how to compare for equality:
		assertEquals("Read back metadata written from a HashMap.",
			keyValuePairHashMap,
			ExifReader.metadataArrayToHashmap(arrayRes));
	}
	
	public final void testWriteReadKeysFromMetadataIndexer() {
		// TODO: write test case for exif writing from MetadataIndexer.
	}
	
	public final void testReadExifDateTime() {
		// Build HashMap of expected result, using our test image:
		HashMap<String,String> hashMapRes = new HashMap<String,String>();
		hashMapRes.put("Date", "2003:03:18");
		hashMapRes.put("Time", "07:52:20");
		
		// Expecting: [Date, 2003:03:18], [Time, 07:52:20]]
		arrayRes = ExifReader.readExifTimeDate(imgWithCameraExifFileName);

		assertEquals("Reading exif data and time.",
					 hashMapRes,
					 ExifReader.metadataArrayToHashmap(arrayRes));
	}
	
	public final void testClearPhotoSpreadMetadata () throws IOException {

		// Build the hashmap that holds the metadata to write:
		HashMap<String, String> keyValuePairHashMap = new HashMap<String, String>();
		keyValuePairHashMap.put("key4", "value4");
		keyValuePairHashMap.put("key5", "value5");
		
		// Get a fresh copy of the no-metadata reference file:
		createNoMetadataImgFile();
		
		// Make sure we have metadata to clear:
		ExifWriter.write(exifTempMetadataImgFileName, keyValuePairHashMap);
		
		// Read back what we wrote:
		arrayRes = ExifReader.readMetaDataSet(exifTempMetadataImgFileName);
		
		assertEquals("Read back metadata in preparation of clearing it.",
			keyValuePairHashMap,
			ExifReader.metadataArrayToHashmap(arrayRes));
		
		// Now clear, re-read, and ensure that metadata
		// is now empty:
		ExifWriter.clearPhotoSpreadMetadata(exifTempMetadataImgFileName);
		arrayRes = ExifReader.readMetaDataSet(exifTempMetadataImgFileName);
		
		assertTrue("Metadata after clearing.", arrayRes.isEmpty());
	}

	
	// --------------------   Private Methods --------------------
	
	
	/**
	 * Copy our clean test file to a temporary file:
	 * @throws IOException
	 */
	private final void createNoMetadataImgFile() throws IOException {
		FileUtils.copyFile(exifEmptyMetadataImgFile, new File(exifTempMetadataImgFileName));
	}
}