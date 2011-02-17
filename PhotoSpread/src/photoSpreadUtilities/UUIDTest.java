package photoSpreadUtilities;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;

import javax.swing.ImageIcon;

public class UUIDTest {


	private Hashtable<String, String> fileHashes = new Hashtable<String, String>();

	String filePath = null;
	File fileObj = null;
	String fileContent = null;
	Date fileLoadTime = null;
	Date pixelUUIDTime = null;

	private UUID theUUIDFromPixels;
	private UUID theUUIDFromSamples;
	private UUID theUUIDFromEntireFile;

	public UUIDTest() {
	}
	
	/*
	public Date getFileLoadTime(String theFilePath) {

		byte[] imgBytes = null;

		long startTime = System.currentTimeMillis();
		try {
			imgBytes = readBigFile(theFilePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
		fileLoadTime = new Date(endTime - startTime);
		return fileLoadTime;
	}
	*/

	public Date getFileSampleUUIDTime(String theFilePath) {

		long startTime = System.currentTimeMillis();
		
		try {
			theUUIDFromSamples = new UUID(new File(theFilePath), UUID.FileHashMethod.AUTOMATIC);
		} catch (FileNotFoundException e) {
			Misc.showErrorMsgAndStackTrace(e, "");
			//e.printStackTrace();
		} catch (IOException e) {
			Misc.showErrorMsgAndStackTrace(e, "");
			//e.printStackTrace();
		}
		
		long endTime = System.currentTimeMillis();
		fileLoadTime = new Date(endTime - startTime);
		String uuidAsStr = theUUIDFromSamples.toString();
		if (fileHashes.get(uuidAsStr) != null) {
			System.out.println(
					"Non-unique UUID for " +
					theFilePath +
					" and\n" +
					"                    " +
					fileHashes.get(uuidAsStr) +
					". UUID: " + theUUIDFromSamples);
			return null;
		}
		fileHashes.put(uuidAsStr, theFilePath);
		return fileLoadTime;
	}
	
	public Date getPixelsFromImageIconUUIDTime(String path) {

		ImageIcon iIcon = new ImageIcon(path);
		// The file name is only passed to improve error messages
		// in case loading fails:
		long startTime = System.currentTimeMillis();
		try {
			theUUIDFromPixels = new UUID(iIcon, path);
		} catch (photoSpread.PhotoSpreadException.CannotLoadImage e) {
			Misc.showErrorMsgAndStackTrace(e, "");			
			//e.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
		pixelUUIDTime = new Date(endTime - startTime);
		return pixelUUIDTime;
	}
	
	public Date getEntireFileUUIDTime(String path) {

		long startTime = System.currentTimeMillis();
		try {
			theUUIDFromEntireFile = new UUID(new File(path), UUID.FileHashMethod.USE_WHOLE_FILE);;
		} catch (IOException e) {
			Misc.showErrorMsgAndStackTrace(e, "");
			//e.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
		//pixelUUIDTime = new Date(endTime - startTime);
		//return pixelUUIDTime;
		return new Date(endTime - startTime);
	}

	public static void main(String[] theArgs) {
		
		Date uuidComputationTime = null;
		String[] args = new String[] {
				"C:/Users/paepcke/dldev/Testing/oneKFile.txt",
				"C:/Users/paepcke/dldev/Testing/gunman1.txt",
				"C:/Users/paepcke/dldev/Testing/gunman2.txt",
				"c:/Users/paepcke/Pictures/PhotoSpreadTestPictures/Photos/airportTarmacHelicoptersWomanGreenShirt.jpg",
				"c:/Users/paepcke/Pictures/PhotoSpreadTestPictures/Photos/",
				"C:/Users/paepcke/Pictures/PhotoSpreadTestPictures/Reconyx-Jun27-2007-TO-Jul7-2007Partial/"
				};
		final String testImagePath = 
			"c:/Users/paepcke/Pictures/PhotoSpreadTestPictures/Photos/airportTarmacHelicoptersWomanGreenShirt.jpg";
		
		UUIDTest tester = new UUIDTest();

		for (String fileName : args) {
			File fileObj = new File(fileName);
			if (!fileObj.exists()) {
				System.out.println("File/directory '" + fileName
						+ "' does not exist.");
				continue;
			}
			if (fileObj.isFile()) {
				uuidComputationTime = tester.getFileSampleUUIDTime(fileName);
				// If time is null, a message of collision was put out:
				if (uuidComputationTime == null) continue;
				System.out.println("*****File load time: "
						+ uuidComputationTime.getTime() + " msecs" + "; file size: " + fileObj.length());
			} else {
				String[] allFiles = fileObj.list();
				long totalTime = 0;
				long totalBytes = 0;
				for (String fileNameInDir : allFiles) {
					String absolutePath = fileName + fileNameInDir;
					File thisFile = new File(absolutePath);
					uuidComputationTime = tester.getFileSampleUUIDTime(absolutePath);
					// If time is null, a message of collision was put out:					
					if (uuidComputationTime == null) continue;
					totalTime += uuidComputationTime.getTime();
					totalBytes += thisFile.length();
				}
				int numFiles = allFiles.length;
				float avgFileSize = (float) totalBytes / numFiles;
				float avgLoadTime = (float) totalTime / numFiles;
				System.out.println("Loaded " + numFiles + " files.\n"
						+ "Total bytes: " + totalBytes
						+ "\nAverage file size: " + avgFileSize + " ("
						+ (float) avgFileSize / 1000 + " KB)"
						+ "\nTotal time: " + totalTime + " msec\n"
						+ "*****Average load time: " + avgLoadTime + " msecs");
			}
		}
		// Make sure collisions get detected:
		System.out.println("Re-fingerprinting a file; should cause collision warning:");
		uuidComputationTime = tester.getFileSampleUUIDTime(args[0]);
		if (uuidComputationTime == null)
			System.out.println("Passed collision test.");
		else
			System.out.println("Failed collision test.");
		
		// Getting UUID from an entire file, instead of just a sample
		// (Will cause collision warning, which is fine.)
		System.out.println("UUID based on entire file worked: " + tester.theUUIDFromEntireFile);
		
		// UUIDs from ImageIcons should come out the same as UUIDs from whole-file samples:
		Date totalTimeImgFromIcon = tester.getPixelsFromImageIconUUIDTime(testImagePath);
		Date totalTimeImgFromWholeFile = tester.getEntireFileUUIDTime(testImagePath);
		//Date totalTimeImgFromFileSample = tester.getFileSampleUUIDTime(testImagePath);
		System.out.println("Testing time and UUID equality from both file and in-memory ImageIcon:");
		System.out.println("     Time from File: " + totalTimeImgFromWholeFile.getTime() + " msecs");
		System.out.println("     Time from Icon: " + totalTimeImgFromIcon.getTime() + " msecs");
		System.out.println("UUID from pixels:" + tester.theUUIDFromPixels);
		System.out.println("UUID from entire file:" + tester.theUUIDFromEntireFile);

		System.out.println("File name 1: " + new UUID("C:/Users/paepcke/Pictures/PhotoSpreadTestPictures/Reconyx-Jun27-2007-TO-Jul7-2007Partial/M0000406.JPG"));
		System.out.println("File name 2: " + new UUID("C:/Users/paepcke/Pictures/PhotoSpreadTestPictures/Reconyx-Jun27-2007-TO-Jul7-2007Partial/M0000103.JPG"));
		System.out.println("Finished.");
	}
}