/**
 * 
 */
package edu.stanford.photoSpreadLoaders;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.stanford.inputOutput.ExifWriter;
import edu.stanford.inputOutput.InputOutput;
import edu.stanford.photoSpread.PhotoSpread;
import edu.stanford.photoSpread.PhotoSpread.DebugLevel;
import edu.stanford.photoSpreadUtilities.UUID;
import edu.stanford.photoSpreadUtilities.UUID.FileHashMethod;

/**
 * @author paepcke
 *
 */
public class PhotoSpreadFileLoaderTest {

	enum FileID {CAT, MAN};
	
	String pathToTestPhotos = "src/test/resources/TestCases/Photos";
	File   pathToTestPhotosFile = new File(pathToTestPhotos);
	
	String fileNameCat = pathToTestPhotos + "/emptyExifCat.jpg";
	String fileNameCatTmp = pathToTestPhotos + "/tmpExifCat.jpg";

	private String fileNameMan = pathToTestPhotos + "/emptyExifMan.jpg";
	private String fileNameManTmp = pathToTestPhotos + "/tmpExifMan.jpg";
	
	private File catFile;
	private File catFileTmp;
	
	private File manFile;
	private File manFileTmp;
	
	static String currDir = System.getProperty("user.dir");
	
	static UUID catUUID = null;
	static UUID manUUID = null;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
		// Tell PhotoSpreadFileImporter that we are
		// testing, and it should not show UI dialogs:
		PhotoSpread.setDebutLevel(DebugLevel.AUTOMATIC_TESTING);
		
		catFileTmp   = new File(InputOutput.normalizePath(currDir + "/"+ fileNameCatTmp));
		catFile     = new File(InputOutput.normalizePath(currDir + "/"+ fileNameCat));

		manFileTmp   = new File(InputOutput.normalizePath(currDir + "/"+ fileNameManTmp));
		manFile = new File(InputOutput.normalizePath(currDir + "/"+ fileNameMan));
		
		catUUID = new UUID(catFile, FileHashMethod.USE_FILE_SAMPLING);
		manUUID = new UUID(manFile, FileHashMethod.USE_FILE_SAMPLING);		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		deleteTmpFile(FileID.CAT);
		deleteTmpFile(FileID.MAN);
	}

	@Test
	public void testFindFileIdenticalFiles() {
		// Get a precise copy of an image:
		getFreshImage(FileID.CAT);
		// Find whether the paths are accepted as the same:
		File finalFile = null;
		try {
			finalFile = PhotoSpreadFileImporter.resolveFile(pathToTestPhotosFile, 
															catFileTmp, 
															catUUID);
		} catch (IOException e) {
			fail("Test point 1: " + e.getMessage());
		}
		// Final file should be the tmp file, whose computed UUID should
		// be identical to its original, the cat file:
		assert(finalFile.equals(catFileTmp));
	}
	
	@Test
	public void testFindFileDifferentFiles() {
		// Get a precise copy of an image:
		getFreshImage(FileID.CAT);
		
		// Change something in the tmp file to make
		// its UUID different:
		ExifWriter.write(catFileTmp, "Species", "cat");
		
		// Pretend the user rejects the tmp file. This will be modified
		// by the code under test, because debuglevel is set to testing:
		PhotoSpreadFileImporter.secretKeepLooking = UuidMismatchDecision.KEEP_LOOKING;
		// Pretend the user navigated to the (modified) tmp file:
		PhotoSpreadFileImporter.secretCandidatePath = catFileTmp;
		
		File finalFile = null;
		try {
			finalFile = PhotoSpreadFileImporter.resolveFile(pathToTestPhotosFile, 
															catFileTmp, 
															catUUID);
		} catch (IOException e) {
			fail("Test point 2: " + e.getMessage());
		}
		// Since the user punted, we should get null as the
		// result:
		assertTrue("Failed to return null when supposed to.", finalFile == null);
		
		// Now pretend the user navigated to the 'correct' file
		// after the mismatch was disovered:
		PhotoSpreadFileImporter.secretCandidatePath = catFile;
		PhotoSpreadFileImporter.secretKeepLooking = UuidMismatchDecision.ACCEPT_LOCAL_FILE;
		// Now that file should get accepted:
		try {
			finalFile = PhotoSpreadFileImporter.resolveFile(pathToTestPhotosFile, 
															catFileTmp, 
															catUUID);
		} catch (IOException e) {
			fail("Test point 3: " + e.getMessage());
		}
		assertTrue("Failed to accept a corrected path.", finalFile.equals(catFileTmp));
	}
	
	@Test
	public void testFindFileNoOriginalUUID() {
		// Get a precise copy of an image:
		getFreshImage(FileID.CAT);
		File finalFile = null;
		
		// Test case where given file <i>C</i> exists, and the original
		// file <i>O</i>'s UUID is unknown:
		try {
			finalFile = PhotoSpreadFileImporter.resolveFile(pathToTestPhotosFile, 
															catFileTmp, 
															null);
		} catch (IOException e) {
			fail("Test point 4: " + e.getMessage());
		}
		assertTrue("Failed to accept an original path when original UUID is unknown.", 
					finalFile.equals(catFileTmp));
		
		// Test case where given file <i>C</i> does not exist, and the original
		// file <i>O</i>'s UUID is unknown, but the user navigates to a new
		// file:
		
		File nonExistingFile = new File("/foo/bar/fum.txt");
		PhotoSpreadFileImporter.secretCandidatePath = catFile;
		PhotoSpreadFileImporter.secretKeepLooking = UuidMismatchDecision.KEEP_LOOKING;
		try {
			finalFile = PhotoSpreadFileImporter.resolveFile(pathToTestPhotosFile, 
															nonExistingFile, 
															null);
		} catch (IOException e) {
			fail("Test point 5: " + e.getMessage());
		}
		
		// Since user navigated to a known file (catFile), and the original UUID is
		// unknown, the known file should be returned:
		assertTrue("Failed to returned known path that user nagivated to, when original UUID is unknown.",
				finalFile.equals(catFile));

	}
	
	//-----------------------    Utilities   ---------------------------
	
	private void getFreshImage(FileID whichFile) {
		try {
			if (whichFile == FileID.CAT)
				FileUtils.copyFile(catFile, catFileTmp);
			else 
				FileUtils.copyFile(manFile, manFileTmp);
		} catch (IOException e) {
			fail("Could not copy empty exif file to tmp file.");
		}
	}
	
	private void deleteTmpFile(FileID whichFile) {
		if (whichFile == FileID.CAT)
			FileUtils.deleteQuietly(catFileTmp);
		else
			FileUtils.deleteQuietly(manFileTmp);
	}
}
