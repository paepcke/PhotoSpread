package edu.stanford.inputOutput;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import junit.framework.TestCase;

public class CsvReadWriteTest extends TestCase {

	static String csvOutTestFile;
	static CsvWriter csvWriter;
	static String currDir = System.getProperty("user.dir");
	static String testCSVErrorFree;
	static String testCSVErrors;	
	static String testNonExistingPath = "/Foo/bar/1234/";

	protected void setUp() throws Exception {
		super.setUp();

		csvOutTestFile = InputOutput.normalizePath(currDir + 
				"/src/test/resources/TestCases/testCSVTest-Written." + "csv");

		testCSVErrorFree = InputOutput.normalizePath(currDir +
				"/src/test/resources/TestCases/testCSVFileGood." + "csv");
		testCSVErrors= InputOutput.normalizePath(currDir +
				"/src/test/resources/TestCases/testCSVFileBad." + "csv");

		FileWriter csvFileWriter= new FileWriter(testCSVErrorFree);
		csvFileWriter.write("Filename,Creator\n" +
				"E:/Users/Paepcke/foo.jpg,Eric\n" +
		"C:\\Eric\",\" the bio student\",\" LOVES odd and long filenames.,Andreas\n");
		csvFileWriter.close();

		csvFileWriter = new FileWriter(testCSVErrors);
		csvFileWriter.write("Filename,Creator\n" +
		"C:\\Eric, the bio student, LOVES odd and long filenames.,Andreas\n");
		csvFileWriter.close();

	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public final void testControlSettings() {

		try {
			csvWriter = new CsvWriter(csvOutTestFile);

		} catch (Exception e) {
			fail("Unexpected exception: " + e.getMessage() + "\nException class:" + e.getClass());
		}
		
/*		System.out.println(csvWriter.getTextQualifier());     // default: "
		System.out.println(csvWriter.getUseTextQualifier());  // default: true
		System.out.println(csvWriter.getEscapeMode());        // default: 1
		System.out.println(csvWriter.getComment());           // default: #
		System.out.println(csvWriter.getForceQualifier());    // default: false
*/		
		csvWriter.setTextQualifier('@');
		assertEquals("Text qualifier.", '@', csvWriter.getTextQualifier());

		csvWriter.setTextQualifier('@');
		assertEquals("Text qualifier char.", '@', csvWriter.getTextQualifier());

		csvWriter.setUseTextQualifier(false);
		assertEquals("Use text qualifier.", false, csvWriter.getUseTextQualifier());

		csvWriter.setEscapeMode(1);
		assertEquals("Escape mode.", 1, csvWriter.getEscapeMode());

		csvWriter.setComment('#');
		assertEquals("Comment char.", '#', csvWriter.getComment());

		csvWriter.setForceQualifier(false);
		assertEquals("Use force qualifier.", false, csvWriter.getForceQualifier());

		csvWriter.close();
	}

	public final void testCsvWriterString() {
		
		try {
			csvWriter = new CsvWriter(null);
		fail ("Expected IllegalArgumentException");
		}
		
		catch (IllegalArgumentException e) {
			// Expected
		} catch (Exception e) {
			fail("Unexpected exception: " + e.getMessage() + "\nException class:" + e.getClass());
		}
		
		try {
			csvWriter = new CsvWriter(csvOutTestFile);

		} catch (Exception e) {
			fail("Unexpected exception: " + e.getMessage() + "\nException class:" + e.getClass());
		}
	}

	public final void testWriteString() {
		String testEntry = "Filename,Species,Poisonous\n" +
							"/foo/bar/fum.jpg,Frog,no\n" +
							"E:\\fish\\oh my, a \"poisonCritter\".jpg,Fishy,yes\n";

		StringBuilder strBuilder = new StringBuilder();
		
		try {
			// Don't have each line quoted:
			csvWriter.setUseTextQualifier(false);
			
			csvWriter.write(testEntry);
			csvWriter.close();
			
		} catch (IOException e) {
			fail("Unexpected IOException while writing to csv file.");
		}
		
		try {
			Scanner fileReader = new Scanner (new File(csvOutTestFile));
			while (fileReader.hasNextLine()) {
				strBuilder.append(fileReader.nextLine() + "\n");
			}
			
		} catch (Exception e) {
			fail("Unexpected exception: " + e.getMessage() + "\nException class:" + e.getClass());
		}

		assertEquals("Comparing written-out csv with what's been read back.",
					 testEntry, strBuilder.toString());
	}
	
	public final void testEnsureExcelCompatibility() {
		
		ArrayList<String> testStrings = new ArrayList<String>();
		ArrayList<String> testResults = new ArrayList<String>();
		
		// Test 1:
		testStrings.add("foo");
		testResults.add("foo");
		
		// Test 2:
		testStrings.add("foo,bar");
		testResults.add("\"foo\",\"bar\"");
		
		// Test 3:
		testStrings.add("foo\"bar\"");
		testResults.add("\"foo\"\"\"bar\"\"\"\"");
		
		// Test 4:
		testStrings.add("\"foobar\"");
		testResults.add("\"\"\"\"foobar\"\"\"\"");
		
		// Test 5:
		Iterator<String> testIt = testStrings.iterator();
		Iterator<String> resultIt = testResults.iterator();
		
		int i = 1;
		while (testIt.hasNext()) {
			assertEquals("Excel compatibility test " + i++, resultIt.next(), csvWriter.ensureExcelCompatibility(testIt.next()));
		}
	}

	public final void testReplace() {
		// fail("Not yet implemented"); // TODO
	}

}
