package edu.stanford.inputOutput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.stanford.photoSpread.PhotoSpread;
import edu.stanford.photoSpread.PhotoSpread.DebugLevel;
import edu.stanford.photoSpread.PhotoSpreadException;
import edu.stanford.photoSpread.PhotoSpreadException.IllegalArgumentException;
import edu.stanford.photoSpreadObjects.PhotoSpreadImage;
import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;
import edu.stanford.photoSpreadTable.PhotoSpreadTableModel;
import edu.stanford.photoSpreadUtilities.Const;
import edu.stanford.photoSpreadUtilities.TreeSetRandomSubsetIterable;

/**
 * We load a known XML test file. The table is 3 rows by 4 columns,
 * and is set up as follows:
 *    A1: Collection of two images from ...test/resources/Photos
 *    B1: formula: =A1   // i.e. this is an automatic copy of A1
 *    A2: formula: =A1[Species=foo]  // This cell is empty, since neither photo in A1
 *    								 // satisfies the condition.
 *    B2: value: "Testing"
 *    other cells empty.
 *    
 * @author paepcke
 *
 */
public class XMLSheetSaveLoadTest {

	
	// Equality result for File1.compareTo(File2):
	static final int EQUAL = 0; 
	
	static String goldExampleFileName = "";
	static String badTableDimensionsXMLFileName = "";
	static String missingCloseTagXMLFileName = "";
	static String tooFewCellsXMLFileName = ""; 
	
	static String newXMLSavedSheetFileName = ""; 
	
	static String currDir = System.getProperty("user.dir");
	static PhotoSpreadCell _cell_A1_photoSet = null;
	static PhotoSpreadCell _cell_B1_refToA1  = null;
	static PhotoSpreadCell _cell_A2_Formula  = null;
	static PhotoSpreadCell _cell_B2_Text     = null;
    static PhotoSpreadCell _cell_C4_Empty    = null;
    
	static PhotoSpreadTableModel _tableModel;
	
	@BeforeClass
	public static void prepForAllTests() throws Exception {

		// All test cases are in the resources/TestCases
		// subtree below the code root:
		goldExampleFileName = InputOutput.normalizePath(currDir + 
				"/src/test/resources/TestCases/testXMLSaveSheetReference.xml");
		badTableDimensionsXMLFileName = InputOutput.normalizePath(currDir +
				"/src/test/resources/TestCases/testXMLBadTableDimensions.xml");
		missingCloseTagXMLFileName = InputOutput.normalizePath(currDir +
				"/src/test/resources/TestCases/testXMLCorruptedSaveSheetReference.xml");
		tooFewCellsXMLFileName = InputOutput.normalizePath(currDir +
				"/src/test/resources/TestCases/testXMLTooFewCellsSavedSheet.xml"); 
		
		newXMLSavedSheetFileName = InputOutput.normalizePath(currDir + 
				"/src/test/resources/TestCases/tmpXMLFile.xml");
		
		// Make sure PhotoSpread does not pop up user information
		// messages:
		PhotoSpread.setDebutLevel(DebugLevel.AUTOMATIC_TESTING);
		
		// Create a table model with four rows and three columns,
		// which is the shape of our standard gold XML file:
		_tableModel = new PhotoSpreadTableModel(4,3);
		
		_cell_A1_photoSet =  _tableModel.getCell(0,1);
		_cell_B1_refToA1  =  _tableModel.getCell(0,2);
		_cell_A2_Formula  =  _tableModel.getCell(1,1);
		_cell_B2_Text     =  _tableModel.getCell(1,2);
		_cell_C4_Empty    =  _tableModel.getCell(3,3);
		
	}
	
	@AfterClass
	public static void cleanupForAllTests() throws Exception {
		// Remove the temporary test image without complaint
		// about the File instance still being null, or the
		// file not being there. Just do our best:
		FileUtils.deleteQuietly(new File(newXMLSavedSheetFileName));
		
	}
	
	@Test
	public final void testReadCorrectXMLFile() {
			
		
		try {
			assertFalse("Try loading a non-existing XML file.", InputOutput.loadTable(new File("foo/bar"), _tableModel));
			assertTrue("Load the gold XML file example.", InputOutput.loadTable(new File(goldExampleFileName), _tableModel));
		} catch (HeadlessException e) { } 
		  catch (IllegalArgumentException e) { };
	

			
		assertTrue("Cell A1 should know it is an item collection.", _cell_A1_photoSet.isItemCollection());
		assertEquals("Cell A1's formula should be the 'object collection' string constant.",
				_cell_A1_photoSet.getFormula(), Const.OBJECTS_COLLECTION_INTERNAL_TOKEN);
		// PhotoSpreadNormalizedExpression ne = _cell_A1_photoSet.getNormalizedExpression();
		assertEquals("Cell A1's normalized expression slot.",
				_cell_A1_photoSet.getNormalizedExpression().toString(), 
				"[<Cell A1 with formula '_/Objects/_'>[<no conditions>]]"
				);
		
		// Get the two photo objects from A1:
		TreeSetRandomSubsetIterable<PhotoSpreadObject> objects = _cell_A1_photoSet.getObjects();
		assertEquals("Number of photo objs in A1 should be 2.",2,objects.size());
		
		Iterator<PhotoSpreadObject> _cell_A1_photoSetIt = objects.iterator();
		
		PhotoSpreadImage photo1 = (PhotoSpreadImage) _cell_A1_photoSetIt.next();
		
		assertEquals("Cell A1's first photo ID number.", 
				"-4971708435189753829", photo1.getObjectID().toString());
		assertEquals("First photo's @ID attribute.",
					 "-4971708435189753829",
					 photo1.getMetaData("@ID"));
		assertEquals("Cell A1's first photo file path.", 
				"C:\\Users\\paepcke\\dldev\\EclipseWorkspaces\\PhotoSpread\\SpreadSheet\\src\\test\\resources\\TestCases\\Photos\\crowdOneFaceClear.jpg",
				photo1.getFilePath());
		
		PhotoSpreadImage photo2 = (PhotoSpreadImage) _cell_A1_photoSetIt.next();
		assertEquals("Cell A1's second photo ID number.", 
				"-1854442517828450558", photo2.getObjectID().toString());
		assertEquals("Cell A1's second photo file path.", 
				"C:\\Users\\paepcke\\dldev\\EclipseWorkspaces\\PhotoSpread\\SpreadSheet\\src\\test\\resources\\TestCases\\Photos\\conventionCenterTwoWomen.jpg",
				photo2.getFilePath());
		
		assertEquals("Cell B1 formula.",
				     "=A1",
				     _cell_B1_refToA1.getFormula());
		
		assertEquals("Cell A2 formula.",
					 "=A1[Species=foo]",
					 _cell_A2_Formula.getFormula());
		
		assertEquals("Cell B2 text.",
					 "Testing",
					 _cell_B2_Text.getFormula());
		
		assertEquals("Cell C4 empty cell.",
					 "",
					 _cell_C4_Empty.getFormula());
	}
	
	@Test
	public final void testMismatchedTableDimensions() {
		try {
			assertFalse("Load XML file of 4x4 table into 4x3 table model.", 
					InputOutput.loadTable(new File(badTableDimensionsXMLFileName), _tableModel));
		} catch (HeadlessException e) { } catch (IllegalArgumentException e) {};
	}
	
	@Test
	public final void testReadCorruptedXMLFile() {
		
		try {
			assertFalse("Load XML file with missing tag.", 
					InputOutput.loadTable(new File(missingCloseTagXMLFileName), _tableModel));
		} catch (HeadlessException e) { }
		  catch (IllegalArgumentException e) { }
	}
	
	@Test
	public final void testTooFewCells() {
		try {
			assertFalse("Load XML file with one missing cell element.", 
					InputOutput.loadTable(new File(tooFewCellsXMLFileName), _tableModel));
		} catch (HeadlessException e) { }
		  catch (IllegalArgumentException e) { }
	}
	
	@Test
	public final void testSaveXMLFile() {
		// Load the healthy gold file:
		File inFile = new File(goldExampleFileName);
		try {
			assertTrue("Load the gold XML file example.", InputOutput.loadTable(inFile, _tableModel));
		} catch (HeadlessException e) { }
		  catch (IllegalArgumentException e) { }
			
		// Now save it out into a temp file:
		File outFile = new File(newXMLSavedSheetFileName);
		try {
			assertTrue("Save good table to XML file.", 
					InputOutput.saveTable(outFile, _tableModel));
		} catch (HeadlessException e) {
			fail(e.getMessage());
		} catch (PhotoSpreadException e) {
			fail(e.getMessage());
		}
		try {
			assertTrue("Compare saved file to gold file.", FileUtils.contentEquals(inFile, outFile));
		} catch (IOException e) {
			fail("Failed to compare saved XML file to gold file.");
		}
	}	
}
