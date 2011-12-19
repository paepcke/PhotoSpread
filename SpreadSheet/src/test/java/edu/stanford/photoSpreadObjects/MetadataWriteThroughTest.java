/**
 * 
 */
package edu.stanford.photoSpreadObjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.stanford.inputOutput.ExifReader;
import edu.stanford.inputOutput.ExifWriter;
import edu.stanford.inputOutput.InputOutput;
import edu.stanford.photoSpread.PhotoSpread;
import edu.stanford.photoSpread.PhotoSpread.DebugLevel;
import edu.stanford.photoSpreadObjects.photoSpreadComponents.ObjectsPanel;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;
import edu.stanford.photoSpreadTable.PhotoSpreadTableModel;

/**
 * @author paepcke
 *
 */

enum FileID {CAT, MAN};

public class MetadataWriteThroughTest {

	String emptyExifFileNameCat = "src/test/resources/TestCases/Photos/emptyExifCat.jpg";
	String tmpExifFileNameCat = "src/test/resources/TestCases/Photos/tmpExifCat.jpg";
	
	private String emptyExifFileNameMan = "src/test/resources/TestCases/Photos/emptyExifMan.jpg";
	private String tmpExifFileNameMan = "src/test/resources/TestCases/Photos/tmpExifMan.jpg";
	
	private File emptyExifFileCat;
	private File tmpExifFileCat;
	
	private File emptyExifFileMan;
	private File tmpExifFileMan;
	
	private PhotoSpreadTableModel tableModel;
	private PhotoSpreadCell cell_A1;
	
	static String currDir = System.getProperty("user.dir");
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		tmpExifFileCat   = new File(InputOutput.normalizePath(currDir + "/"+ tmpExifFileNameCat));
		emptyExifFileCat     = new File(InputOutput.normalizePath(currDir + "/"+ emptyExifFileNameCat));

		tmpExifFileMan   = new File(InputOutput.normalizePath(currDir + "/"+ tmpExifFileNameMan));
		emptyExifFileMan = new File(InputOutput.normalizePath(currDir + "/"+ emptyExifFileNameMan));
		
		
		// Make sure PhotoSpread does not pop up user information
		// messages:
		PhotoSpread.setDebutLevel(DebugLevel.AUTOMATIC_TESTING);
		
		// Create a table model with four rows and three columns,
		// which is the shape of our standard gold XML file:
		tableModel = new PhotoSpreadTableModel(4,3);
		
		cell_A1     =  tableModel.getCell(0,1);
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
	public void testWriteThrough() {
		
		// Get a fresh image file without Exif:
		getFreshImage(FileID.CAT);
		
		// First ensure that the tmp file really is empty 
		// of Exif PhotoSpread metadata:
		assertTrue("Temp file not clean at the outset.", tmpFileClean(FileID.CAT));
		
		// Load the empty tmp cat image:
		PhotoSpreadImage img = null;
		try {
			img = new PhotoSpreadImage(cell_A1, tmpExifFileNameCat);
		} catch (Exception e) {
			// Test point 1:
			fail("Could not load test image at test point 1:" + e.getMessage());
		}

		// Add a piece of metadata:
		img.setMetaData("Name", "value");
		// Now image file should no longer be clean:
		assertFalse("Write-through failed.", tmpFileClean(FileID.CAT));

		// Check the values themselves:
		String expected = "Name:value;";
		String res = metadataToStr(ExifReader.readMetaDataSet(tmpExifFileCat));
		assertEquals("Written-through metadata is incorrect.", res, expected);
		
		// Check clear Exif while we are here:
		ExifWriter.clearPhotoSpreadMetadata(tmpExifFileNameCat);
		assertTrue("Clearing Exif data did not clear it.", tmpFileClean(FileID.CAT));
	}

	@Test
	public void testTurnOffWriteThrough() {
		
		// Get a fresh image file without Exif:
		getFreshImage(FileID.CAT);
		
		// First ensure that the tmp file really is empty 
		// of Exif PhotoSpread metadata:
		assertTrue("Temp file not clean at the outset.", tmpFileClean(FileID.CAT));
		
		// Load the empty tmp cat image:
		PhotoSpreadImage img = null;
		try {
			img = new PhotoSpreadImage(cell_A1, tmpExifFileNameCat);
		} catch (Exception e) {
			// Test point 1:
			fail("Could not load test image at test point 1:" + e.getMessage());
		}

		// Turn off write-through:
		img.setWriteToExif(false);
		
		// Add a piece of metadata:
		img.setMetaData("Name", "value");
		
		// Now image file should still be clean now:
		assertTrue("Write-through happened even though it was turned off.", tmpFileClean(FileID.CAT));
	}
	
	public void testWriteAndClearWholeCell() {
		// Get a fresh image file without Exif:
		getFreshImage(FileID.CAT);
		getFreshImage(FileID.MAN);

		// First ensure that the tmp files really are both empty 
		// of Exif PhotoSpread metadata: (Test point 0)
		assertTrue("Temp file Cat not clean at the outset (test point 0).", tmpFileClean(FileID.CAT));
		assertTrue("Temp file Man not clean at the outset (test point 0).", tmpFileClean(FileID.MAN));
		
		// Load the empty tmp cat image into cell A1:
		PhotoSpreadImage imgCat = null;
		try {
			imgCat = new PhotoSpreadImage(cell_A1, tmpExifFileNameCat);
		} catch (Exception e) {
			// Test point 2:
			fail("Could not load test cat image at test point 2:" + e.getMessage());
		}

		// Same with man image:
		PhotoSpreadImage imgMan = null;
		try {
			imgMan = new PhotoSpreadImage(cell_A1, tmpExifFileNameMan);
		} catch (Exception e) {
			// Test point 3:
			fail("Could not load test man image at test point 3:" + e.getMessage());
		}
		
		// Add a piece of metadata:
		imgCat.setMetaData("Species", "cat");
		// Now image file should no longer be clean:
		assertFalse("Write-through failed.", tmpFileClean(FileID.CAT));

		// Add a piece of metadata:
		imgMan.setMetaData("Species", "man");
		// Now image file should no longer be clean:
		assertFalse("Write-through failed.", tmpFileClean(FileID.MAN));
		
		// Lock both images, and ensure that clearing Exif won't happen
		// when we ask for it:
		imgCat.setWriteToExif(false);
		imgMan.setWriteToExif(false);
		
		// Get the table's objectsPanel instance:
		ObjectsPanel objPanel = new ObjectsPanel(false); // No context menu
		objPanel.clearAllExifInCell(cell_A1);
		
		// Test point 3
		assertFalse("Temp file Cat cleared when locked (test point 3).", tmpFileClean(FileID.CAT));
		assertFalse("Temp file Man cleared when locked (test point 3).", tmpFileClean(FileID.MAN));
		
		// Unlock one image, clear all again, and check
		// that only the unlocked image is now clear:
		imgCat.setWriteToExif(true);
		objPanel.clearAllExifInCell(cell_A1);
		// Test point 4
		assertTrue("Temp file Cat not cleared when unlocked (test point 4).", tmpFileClean(FileID.CAT));
		assertFalse("Temp file Man cleared when locked (test point 4).", tmpFileClean(FileID.MAN));
		
		// Check that getting image Exif lock works too:
		// Test point 5
		assertTrue("Getting Exif write lock failed when unlocked (test point 5)", imgCat.getWriteToExif());
		assertFalse("Getting Exif write lock failed when locked (test point 5)", imgMan.getWriteToExif());
		
		// Add a non-image object to the A1 cell, and
		// ensure that unlocking and deleting Exif still
		// works (for just the images):
		
		@SuppressWarnings("unused")
		PhotoSpreadStringObject strObj = new PhotoSpreadStringObject(cell_A1, "one string");
		
		// Now clear the Exifs in the rest of the cell:
		objPanel.unlockAllExifAccessInCell();
		objPanel.clearAllExifInCell(cell_A1);
		// Test point 6
		assertTrue("Temp file Cat not cleared when unlocked (test point 6).", tmpFileClean(FileID.CAT));
		assertTrue("Temp file Man not cleared when unlocked (test point 6).", tmpFileClean(FileID.MAN));
		
		
	}
	
	
	//-----------------------    Utilities   ---------------------------
	
	private void getFreshImage(FileID whichFile) {
		try {
			if (whichFile == FileID.CAT)
				FileUtils.copyFile(emptyExifFileCat, tmpExifFileCat);
			else 
				FileUtils.copyFile(emptyExifFileMan, tmpExifFileMan);
		} catch (IOException e) {
			fail("Could not copy empty exif file to tmp file.");
		}
	}
	
	private void deleteTmpFile(FileID whichFile) {
		if (whichFile == FileID.CAT)
			FileUtils.deleteQuietly(tmpExifFileCat);
		else
			FileUtils.deleteQuietly(tmpExifFileMan);
	}

	private String metadataToStr(ArrayList<ArrayList<String>> md) {
		String res = "";
		for (ArrayList<String> pair : md) {
			res += pair.get(0) + ":" + pair.get(1) + ";";
		}
		return res;
	}
	
	private boolean tmpFileClean(FileID whichFile) {
		String res = null;
		if (whichFile == FileID.CAT)
			res = metadataToStr(ExifReader.readMetaDataSet(tmpExifFileCat));
		else
			res = metadataToStr(ExifReader.readMetaDataSet(tmpExifFileMan));
		return res.isEmpty();
	}
}
