package edu.stanford.photoSpreadUtilities;

// This test case uses JUnit 4, so test cases are
// identified with the @Test decorator.

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class FilePathTransformerTest {
	
	String oldFileName = "C:\\Users\\JohnDoe\\MyFiles\\oneFile.txt";
	// Just directory portion of new (correctly transformed) file:
	String newFileDir = File.separator + "home" + File.separator + 
						 "janedoe" + File.separator +
						 "HerFiles";
	
	// Transformed file name will use the local OS' file name sep char:
	String newFileName = newFileDir  + File.separator + "oneFile.txt";

	FilePathTransformer transformer = new FilePathTransformer(oldFileName, newFileName);
	
	@BeforeClass
	public static void prepForAllTests() throws Exception {
		
	}

	@AfterClass
	public static void cleanupForAllTests() throws Exception {
		// nothing to do. Just here for illustration of jUnit4
		// way of defining the old reserved-name tearDown() method.
	}
	
	@Test
	public void test() {
		assertEquals("Vanilla case, no odd conditions, but new file does not exist.", 
					 null, 
					 transformer.getUpdatedFilePath(oldFileName));
		
		// Now the semi-private mostRecentUpdatedFullPath instance variable 
		// in the transformer contains the new path that would have been returned if
		// the respected file had existed (which it didn't because
		// we are in testing.
		assertEquals("Vanilla case, new file non-existent; but did correct name get generated?",
					 newFileName,
					 transformer.getMostRecentUpdatedFullPath().toString());
	
		assertNull("Pass in just file name (which doesn't exist b/c we are testing.", 
				   transformer.getUpdatedFilePath("oneFile.txt"));
		
		assertEquals("Vanilla case, new file non-existent; but did correct name get generated?",
					 newFileName,
					 transformer.getMostRecentUpdatedFullPath().toString());

		assertNull("Pass in just file name (which doesn't exist b/c we are testing.", 
				   transformer.getUpdatedFilePath("oneFile.txt"));
	
		assertEquals("Vanilla case, new file non-existent; but did correct name get generated?",
					 newFileDir + File.separator + "oneFile.txt",
					 transformer.getMostRecentUpdatedFullPath().toString());
		
		assertNull("Pass empty string as file path to transform.", 
				   transformer.getUpdatedFilePath(""));
	
		assertEquals("Vanilla case, new file non-existent; but did correct name get generated?",
					 newFileDir,
					 transformer.getMostRecentUpdatedFullPath().toString());
	}
	
	public static void main(String[] args) {
		new FilePathTransformerTest().test();
	}

}
