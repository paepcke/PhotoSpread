/**
 * 
 */
package edu.stanford.photoSpreadUtilities;

import java.io.File;

import edu.stanford.photoSpread.PhotoSpreadException;
import edu.stanford.photoSpread.PhotoSpreadException.FileIOException;

import junit.framework.TestCase;

/**
 * @author paepcke
 *
 */
public class ThumbprintTest extends TestCase {

	static String currDir = System.getProperty("user.dir");

	public void testThumbprint () {

		Thumbprint tp1 = null;
		Thumbprint tp2 = null;;
		Thumbprint tp1Again = null;
		@SuppressWarnings("unused")
		Thumbprint tpError = null;

		String f1 = currDir + "/src/test/resources/TestCases/Photos/crowdOneFaceClear.jpg";
		File f2 = new File (currDir + 
				    "/src/test/resources/TestCases/Photos/conventionCenterTwoWomen.jpg");

		try {
			tp1 = new Thumbprint(f1);
			tp2 = new Thumbprint(f2);
			tp1Again = new Thumbprint(f1);
		} catch (Exception e) {
			fail("Could not instantiate Thumbprint: " + e.getMessage());
		}

		assertEquals("Thumbprint with itself", true, tp1.equals(tp1));
		assertFalse("Thumbprints of unequal files should be unequal", tp1.equals(tp2));
		assertEquals("Thumbprint with new thumbprint of same file.", true, tp1.equals(tp1Again));

		try {
			tpError = new Thumbprint("foo/bar");
			fail("Did not catch file-not-found error");
		} catch (PhotoSpreadException.FileNotFoundException e) {
			// expected.
		} catch (FileIOException e) {
			fail("File IO exception: " + e.getMessage());
		}
	}
}
