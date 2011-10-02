/**
 * 
 */
package edu.stanford.photoSpread;

import java.io.File;
import java.security.InvalidParameterException;

import junit.framework.TestCase;
import edu.stanford.photoSpreadUtilities.CellCoordinates;
import edu.stanford.photoSpreadUtilities.Misc;

/**
 * @author paepcke
 *
 */
public class MiscTest extends TestCase {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link photoSpreadUtilities.Misc#Misc()}.
	 */
	public final void testMisc() {
		// fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link photoSpreadUtilities.Misc#makeDimensionFromPref(java.lang.String)}.
	 */
	public final void testMakeDimensionFromPref() {
		// fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link photoSpreadUtilities.Misc#getPrefInt(java.lang.String)}.
	 */
	public final void testGetPrefInt() {
		// fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link photoSpreadUtilities.Misc#ensureFileExtension(java.lang.String, java.lang.String)}.
	 */
	public void testEnsureFileExtension() {
		String desiredExt = "csv";
		String desiredExtWithDot = ".csv";
		String unwantedExt = "xml";
		String testNoExtension = "C:/foo/bar";
		String testWantedExtensionPresent = testNoExtension + "." + desiredExt;
		String testWantedExtensionWithDot = testNoExtension + desiredExtWithDot;
		String testUnWantedExtensionPresent = testNoExtension + "." + unwantedExt;
		String testDirectory = "/u/smith/";
		String correctResult = testNoExtension + "." + desiredExt;
		
		try {
			assertEquals(correctResult, Misc.ensureFileExtension(testNoExtension, desiredExt));
			assertEquals(correctResult, Misc.ensureFileExtension(testWantedExtensionPresent, desiredExt));
			assertEquals(correctResult, Misc.ensureFileExtension(testWantedExtensionWithDot, desiredExt));
			assertEquals(correctResult, Misc.ensureFileExtension(testUnWantedExtensionPresent, desiredExt));
			assertEquals(correctResult, Misc.ensureFileExtension(testNoExtension, "." + desiredExt));

			// The following duplicates some of the above tests.
		
			assertEquals("File with expected extension.", 
					"foo.ext",
					Misc.ensureFileExtension(new File("foo.ext").getPath(), "ext"));
			assertEquals("File with different extension.", 
					"foo.ext", Misc.ensureFileExtension(new File("foo.txt").getPath(), 
					"ext"));
			assertEquals("File without extension.", 
					"foo.ext", Misc.ensureFileExtension(new File("foo").getPath(), 
					"ext"));
			assertEquals("File with just a dot.", 
					"foo.ext", Misc.ensureFileExtension(new File("foo.").getPath(), "ext"));

			assertEquals("Absolut path with expected extension.", 
					"C:" + System.getProperty("file.separator") + 
					"users" + System.getProperty("file.separator") + 
					"foo.ext", 
					Misc.ensureFileExtension(new File("C:/users/foo.ext").getPath(), "ext"));
			assertEquals("Absolut path with different extension.", 
					"C:" + System.getProperty("file.separator") +
					"users" + System.getProperty("file.separator") +
					"foo.ext", 
					Misc.ensureFileExtension(new File("C:/users/foo.txt").getPath(), "ext"));
		} catch (Exception e) {
			fail("Unexpected exception");
		}
		
		try {
			Misc.ensureFileExtension(testDirectory, correctResult);
			fail("Expected ParseException for directory instead of filename");
		} catch (java.text.ParseException e) {
			// expected
		}
			
		try {
			assertEquals("A directory. Expect exception.",
						"Shouldn't see this.", 
						Misc.ensureFileExtension("C:/users/", "ext"));
			fail("Should have thrown exception.");
		} catch (java.text.ParseException e) {
			// Expected
		} catch (Exception e) {
			fail("Unexpected exception.");
		}
	}	
	
	/**
	 * 
	 */
	public void testIntToExcelName() {
		
		final int highestExcel = 256;
		final int toThirdDigit = 703;
		
		assertEquals("First column.", "A", Misc.intToExcelCol(1));
		assertEquals("Second column.", "B", Misc.intToExcelCol(2));
		assertEquals("25th column.", "Y", Misc.intToExcelCol(25));
		assertEquals("26th column.", "Z", Misc.intToExcelCol(26));
		assertEquals("27th column.", "AA", Misc.intToExcelCol(27));
		assertEquals("28th column.", "AB", Misc.intToExcelCol(28));
		assertEquals("Highest Excel minus 1.", "IU", Misc.intToExcelCol(highestExcel - 1));
		assertEquals("Highest Excel.", "IV", Misc.intToExcelCol(highestExcel));
		assertEquals("Highest Excel + 1.", "IW", Misc.intToExcelCol(highestExcel + 1));		
		
		assertEquals("Just before rollover to 3rd Excel digit.", "ZZ", Misc.intToExcelCol(toThirdDigit - 1));				
		assertEquals("Rollover to 3rd Excel digit.", "AAA", Misc.intToExcelCol(toThirdDigit));	
		
		try {
			assertEquals("Bad input: 0.", "Should fail", Misc.intToExcelCol(0));
			fail("Should have thrown InvalidParameterException for input '0'.");
		} catch (InvalidParameterException e) {
			// expected
		}
	}

	public void testExcelColToInt() {
		
		final int lastDoubleExcelDigInt = 702;
		
		assertEquals("First col.", 1, Misc.excelColToInt ("A"));
		assertEquals("First col.", 2, Misc.excelColToInt ("B"));
		assertEquals("Z", 26, Misc.excelColToInt ("Z"));
		assertEquals("Last legal Excel name.", 256, Misc.excelColToInt ("IV"));
		
		assertEquals("ZZ", lastDoubleExcelDigInt, Misc.excelColToInt ("ZZ"));
		
	}
	
	public void testGetCellAddress() {

		// The following checks are just to ensure that I have the ground truth right:
		assertEquals("Check value of 'AA1'", "AA1", Misc.getCellAddress(0, 27));
		assertEquals("Check value of 'BA1'", "BA1", Misc.getCellAddress(0, 53));

		//assertTrue("Simple case", (new CellCoordinates(1,1)).equals(Misc.getCellAddress("A1")));
		assertTrue("Simple case", (new CellCoordinates(0,1)).equals(Misc.getCellAddress("A1")));
		assertTrue("AA1", (new CellCoordinates(0,27)).equals(Misc.getCellAddress("AA1")));
		assertNull("Bad spec: '1A'", Misc.getCellAddress("1A"));
		assertNull("Bad spec: 'AB'", Misc.getCellAddress("AB"));
		assertNull("Bad spec: 'AB1C'", Misc.getCellAddress("AB1C"));
		assertNull("Bad spec: ''", Misc.getCellAddress(""));
		assertNull("Empty string", Misc.getCellAddress(""));
	}
	
	public void testGetExtension() {

		assertEquals("File with expected extension.", 
				"ext", Misc.getExtension(new File("foo.ext")));
		assertEquals("File without extension.", 
				null, Misc.getExtension(new File("foo")));
		assertEquals("File with just a dot.", 
				null, Misc.getExtension(new File("foo.")));
		assertEquals("Absolut path.",
				"ext", Misc.getExtension(new File("C:/users/foo.ext")));
		assertEquals("Absolut path with different extension.", 
				"txt", Misc.getExtension(new File("C:/users/foo.txt")));
		assertEquals("A directory. Expect null.",
					null, Misc.getExtension(new File("C:/users/")));

	} // end testEnsureFileExtension()
	
	public void testStringTrim() {
		
		// Single-char trash to trim from front and back:
		assertEquals("No dots", "foo", Misc.trim("foo", '.'));
		assertEquals("One dot front", "foo", Misc.trim(".foo", '.'));
		assertEquals("One dot back", "foo", Misc.trim("foo.", '.'));
		assertEquals("Two dots front", "foo", Misc.trim("..foo", '.'));
		assertEquals("Two dots back", "foo", Misc.trim("foo..", '.'));
		assertEquals("One dot front and back", "foo", Misc.trim(".foo.", '.'));
		assertEquals("Two dots front and back", "foo", Misc.trim("..foo..", '.'));
		
		// Multi-char trash to trim from front and back:
		assertEquals("Nothing to trim", "foo", Misc.trim("foo", "<trash>"));
		assertEquals("Nothing to trim with one embedded", "f<trash>oo", Misc.trim("f<trash>oo", "<trash>"));
		assertEquals("Trim one from front", "foo", Misc.trim("<trash>foo", "<trash>"));
		assertEquals("Trim two from front", "foo", Misc.trim("<trash><trash>foo", "<trash>"));
		assertEquals("Trim one from back", "foo", Misc.trim("foo<trash>", "<trash>"));
		assertEquals("Trim two from back", "foo", Misc.trim("foo<trash><trash>", "<trash>"));
		assertEquals("Trim two from front and back", "foo", Misc.trim("<trash><trash>foo<trash><trash>", "<trash>"));
		
	}
	
/*	public void testMakeUUID() {
		UUID testUUID1 = new UUID();
		UUID testUUID2 = new UUID();
		
		assertFalse(testUUID1.equals(testUUID2));
	}
*/
}
