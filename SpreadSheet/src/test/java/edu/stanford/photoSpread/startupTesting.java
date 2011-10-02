package edu.stanford.photoSpread;
// package photoSpread;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import javax.naming.InvalidNameException;

import edu.stanford.inputOutput.InputOutput;
import edu.stanford.photoSpread.PhotoSpread;
import edu.stanford.photoSpread.PhotoSpreadException.IllegalArgumentException;

import junit.framework.TestCase;

public class startupTesting extends TestCase {

	String currDir = System.getProperty("user.dir");
	String testPrefsPathErrorFree;
	String testPrefsPathErrors;	
	String testNonExistingPath = "/Foo/bar/1234/";

	protected void setUp() throws Exception {

		super.setUp();
		
		testPrefsPathErrorFree = InputOutput.normalizePath(currDir +
				   "/TestCases/testPrefFileGood." +
				   PhotoSpread.prefsFileExtension);
		testPrefsPathErrors= InputOutput.normalizePath(currDir +
				   "/TestCases/testPrefFileBad." +
				   PhotoSpread.prefsFileExtension);
		
		FileWriter prefFileWriter= new FileWriter(testPrefsPathErrorFree);
		prefFileWriter.write("csvDelimiter = \",\"\n" +
								"lastDirWritten : \"" +
								currDir + "\"\n");
		prefFileWriter.close();

		prefFileWriter = new FileWriter(testPrefsPathErrors);
		prefFileWriter.write("csvDelimiter = '\\u1R000000'\n" +
								"lastDirWritten = \"" +
								currDir + "\"\n");
		prefFileWriter.close();
		
		System.setProperty(PhotoSpread.prefsFileKey, testPrefsPathErrorFree);
		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testInitPreferences() {
		
		// setUp() should have primed the system property
		// PhotSpread.prefsFileKey to point to a test
		// preferences file:
		assertEquals("System prefs file property.", 
				     testPrefsPathErrorFree,
				     System.getProperty(PhotoSpread.prefsFileKey));	
		
		// Run the method that's to be tested. Shouldn't
		// generate an error:
		try {
			PhotoSpread.initPreferences();
		} catch (InvalidNameException e) {
			fail("Unexpected InvalidNameException: " + e.getMessage());
		} catch (IOException e) {
			fail("Unexpected IOException: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			fail("Unexpected IllegalArgumentException: " + e.getMessage());
		}
		
		// initPreferences() should have set several 
		// properties. Check those in turn:
		assertEquals("Preference file property.", 
					 testPrefsPathErrorFree,
					 PhotoSpread.photoSpreadPrefs.getProperty(PhotoSpread.prefsFileKey));
		assertEquals("Preference csv delimiter.",
					 "\",\"",
					 PhotoSpread.photoSpreadPrefs.getProperty(PhotoSpread.csvFieldDelimiterKey));
		
		// Pretend user points to a grammatically incorrect pref file: 
		System.setProperty(PhotoSpread.prefsFileKey, testNonExistingPath);
		
		try {
			PhotoSpread.initPreferences();
			fail("Should have an IOException.");
		} catch (FileNotFoundException e) {
			// expected exception
		} catch (Exception e) {
			fail("Unexpected exception: " + e.getMessage() + "\nException class:" + e.getClass());
		}
		
		// Pretend user points to a grammatically incorrect pref file: 
		System.setProperty(PhotoSpread.prefsFileKey, testPrefsPathErrors);
		
		try {
			PhotoSpread.initPreferences();
			fail("Should have an IllegalArgumentException.");
		} catch (IllegalArgumentException e) {
			// expected exception
		} catch (Exception e) {
			fail("Unexpected exception: " + e.getMessage() + "\nException class:" + e.getClass());
		}

	}
	
	public void testProcessCommandLineArgs() {
		String[] args = {};
		
		// Check that empty args are OK:
		try {
			PhotoSpread.processCommandLineArgs(args);
			
		} catch (Exception e) {
			fail("Empty args argument kills processCommandLineArgs(): " +
				  e.getMessage() + 
				  "\nException class:" + e.getClass());
		}

		// Check catching an illegal option:
		String[] args1 = {"-Gfoodleguck"};
		
		try {
			PhotoSpread.processCommandLineArgs(args1);
			fail("Should have an IllegalArgumentException for the '-Gfoodleguck' option.");
			
		} catch (IllegalArgumentException e) {
			// expected
		} catch (Exception e) {
			fail("Unexpected exception: " + e.getMessage() + "\nException class:" + e.getClass());
		}
		
		// Check command line preferences being passed into
		// the System properties:
		
		String[] args2 = {"--foo", "bar"};

		try {
			PhotoSpread.processCommandLineArgs(args2);
			fail("Expected IllegalArgumentException for --foo bar");
			
		} catch (IllegalArgumentException e) {
			// expected
			
		} catch (Exception e) {
			fail("Unexpected exception: " + e.getMessage() + "\nException class:" + e.getClass());
		}
		
		// Two ints expected and properly provided:
		String[] args3 = {"--workspaceSize", "10", "30"};
		
		try {
			PhotoSpread.processCommandLineArgs(args3);
			assertEquals("Workspace size properly set. Should end up in System.properties.",
						  "10 30",
						  System.getProperty("workspaceSize"));

		} catch (Exception e) {
			fail("Unexpected exception: " + e.getMessage() + "\nException class:" + e.getClass());
		}
		
		
		// Attr/value pair without value:
		String[] args4 = {"--workspaceSize"};
		
		try {
			PhotoSpread.processCommandLineArgs(args4);
			fail("Should have an IllegalArgumentException for the missing value in attr/value.");

		} catch (IllegalArgumentException e) {
			// expected
			
		} catch (Exception e) {
			fail("Unexpected exception: " + e.getMessage() + "\nException class:" + e.getClass());
		}
		
		// Two ints expected, but not provided:
		String[] args5 = {"--workspaceSize", "\"a10", "30\""};
		
		try {
			PhotoSpread.processCommandLineArgs(args5);
			fail("Should have an IllegalArgumentException for the 'a10 30' non-integer");
			
		} catch (RuntimeException e) {
			// if (e.getCause().getClass() != PhotoSpreadException.IllegalPreferenceValueException.class)
				//fail("Expected runtime exception embedding IllegalPreferenceValueException");
			// expected
		} catch (Exception e) {
			fail("Unexpected exception: " + e.getMessage() + "\nException class:" + e.getClass());
		}
		
		
		
		
		// --pref prefsFile="E:\Users\Paepcke\dldev\src\PhotoSpreadTesting\TestCases\testPrefFileBad.psp" -rblue:green:gray --pref foo=bar

	}
	

}
