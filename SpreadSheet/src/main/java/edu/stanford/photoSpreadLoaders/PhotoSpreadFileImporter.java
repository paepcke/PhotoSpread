/*
 * TODO: Some of these methods ought to be static methods
 * 		 of PhotoSpreadImageLoader.
 */


package edu.stanford.photoSpreadLoaders;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import edu.stanford.inputOutput.CsvReader;
import edu.stanford.photoSpread.PhotoSpread;
import edu.stanford.photoSpread.PhotoSpread.DebugLevel;
import edu.stanford.photoSpread.PhotoSpreadException.BadUUIDStringError;
import edu.stanford.photoSpreadObjects.PhotoSpreadImage;
import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadObjects.PhotoSpreadStringObject;
import edu.stanford.photoSpreadObjects.PhotoSpreadTextFile;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;
import edu.stanford.photoSpreadUtilities.FilePathTransformer;
import edu.stanford.photoSpreadUtilities.Misc;
import edu.stanford.photoSpreadUtilities.UUID;
import edu.stanford.photoSpreadUtilities.UUID.FileHashMethod;

/**
 * 
 * @author skandel
 */
public class PhotoSpreadFileImporter {

	// For unit testing: a path to use instead
	// of one the user would input:
	public static File secretCandidatePath = null;
	// Again, for unit testing: the answer a user
	// would have given when asked to continue looking
	// for a file:
	public static boolean secretKeepLooking = true; 
	
	final static short SKIP_OPTION = 0;
	final static short NEW_FOLDER_OPTION = 1;
	final static short ABORT_OPTION = 2;

	final static short ACCEPT_FILE_OPTION = 0;
	final static short KEEP_LOOKING_OPTION = 1;
	
	
	
	public final static String jpeg = "jpeg";
	public final static String jpg = "jpg";
	public final static String gif = "gif";
	public final static String tiff = "tiff";
	public final static String tif = "tif";
	public final static String png = "png";

	public final static String csv = "csv";
	public final static String txt = "txt";
	public final static String xml = "xml";

	// List of file path transformers used to try the directories
	// of replacement files the user picked during unmarshalling of
	// PhotoSpread objects that are stored on the disk, but that are
	// not found on the current computer:
	private static ArrayList<FilePathTransformer> filePathTransformers = new ArrayList<FilePathTransformer>();
	
	// Does user want the offer to find a replacement
	// for files that are referenced in the XML file, but
	// are not available? We assume yes by default. Each
	// time user cancels out of such an offer we let them
	// say whether they want to stop getting the offers:
	static boolean wantStoredFileReplacement = true;
		
	public static PhotoSpreadObject importFile(File f, PhotoSpreadCell cell)
			throws BadUUIDStringError, FileNotFoundException, IOException {
		return importFile(f, cell, null);
	}

	/**
	 * Creates a photoSpread object from a file on disk. It uses the extension
	 * of file to create appropriate object Default is to create a simple string
	 * object from the file name if extension is not supported.
	 * 
	 * @param f
	 *            the file to be loaded
	 * @param cell
	 *            the cell in which the file is being loaded
	 * @param uuidStr
	 *            a string from which a UUID can be constructed
	 * @return the object created from the file
	 * @throws BadUUIDStringError
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static PhotoSpreadObject importFile(File f, PhotoSpreadCell cell,
			String uuidStr) throws BadUUIDStringError, FileNotFoundException,
			IOException {

		if (isImage(f)) {
			if (uuidStr != null)
				try {
					return new PhotoSpreadImage(cell, UUID
							.createFromUUIDString(uuidStr), f.getAbsolutePath());
				} catch (BadUUIDStringError e) {
					return new PhotoSpreadImage(cell, new UUID(f,
							FileHashMethod.USE_FILE_SAMPLING), f
							.getAbsolutePath());
				}
			else
				return new PhotoSpreadImage(cell, f.getAbsolutePath());
		} else if (isTextFile(f)) {
			if (uuidStr != null)
				try {
					return new PhotoSpreadTextFile(cell, f.getAbsolutePath(),
							uuidStr);
				} catch (BadUUIDStringError e) {
					return new PhotoSpreadTextFile(cell, f.getAbsolutePath(),
							new UUID(f, FileHashMethod.USE_WHOLE_FILE));
				}
			else
				return new PhotoSpreadTextFile(cell, f.getAbsolutePath());
		}

		return new PhotoSpreadStringObject(cell, f.getName());
	}

	public static boolean isImage(File f) {
		String fileName = f.getName().toLowerCase();
		if ((fileName.endsWith(".jpg")) || (fileName.endsWith(".jpeg"))
				|| (fileName.endsWith(".gif")) || (fileName.endsWith(".png"))) {
			return true;
		}
		return false;
	}

	public static boolean isTextFile(File f) {
		String fileName = f.getName();
		if (fileName.toLowerCase().endsWith(".txt")) {
			return true;
		}
		return false;
	}

	/**
	 * Given the File object for a PhotoSpread metadata csv file, and a cell
	 * object, import all the objects that are described in the csv file into
	 * that cell. If an image's file path is relative, then we take its path to
	 * be relative to the csv file. All the UUIDs are properly associated with
	 * their objects.
	 * 
	 * @param f File object for the csv file to be imported.
	 * @param cell  The PhotoSpread cell to import into.
	 * @return Number of records read from the CSV file. Note that 
	 * this may be much higher than the objects that were 
	 * created as part of the import. CSV rows with just 
	 * commas (from empty Excel rows) are records, but don't
	 * turn into objects.
	 * @throws BadUUIDStringError 
	 */
	public static int importCSV(File f,
			PhotoSpreadCell cell) throws BadUUIDStringError {

		int numRecordsLoaded = 0;
		int filePathIndex = -1;
		int uuidStrIndex = -1;
		String filename = "<unknown>";
		String uuidStr;
		File imgFile = null;
		String csvFileDirStr = f.getParent();
		boolean ignoreMissingFiles = false;

		try {
			CsvReader reader = new CsvReader(f.getPath());
			String[] headers;
			PhotoSpreadObject object;
			boolean reTrying = false;

			if (reader.readHeaders()) {

				headers = reader.getHeaders();
				for (int i = 0; i < headers.length; i++) {
					if (headers[i].equals(PhotoSpreadCell.FILEPATH))
						filePathIndex = i;
					if (headers[i].equals(PhotoSpreadCell.OBJECT_ID))
						uuidStrIndex = i;
				}
				if (filePathIndex < 0) {
					return numRecordsLoaded;
				}
				while (true) {
					try {
						if (!reTrying) {
							if (!reader.readRecord())
								return numRecordsLoaded;
							// Get file name of image:
							filename = reader.get(filePathIndex);
							imgFile = new File(filename);
						}
						else
							reTrying = false;
						
						// We always construct a new absolute path
						// from csvFileDirStr and the file's basename.
						// This enables users to interactively identify
						// a directory different from what's in the 
						// CSV file:
						
						
						// Make sure the image gets its original UUID back.
						// If we know where in the CSV file the stringified
						// UUID of the image resides, create a UUID from that
						// string:
						if (uuidStrIndex > -1) {
							uuidStr = reader.get(uuidStrIndex).replaceAll("[\'=]", "");
							imgFile = resolveFile(new File(csvFileDirStr), imgFile, UUID.createFromUUIDString(uuidStr));
							if (imgFile == null)
								// User wants to skip this image:
								continue;
							object = PhotoSpreadFileImporter.importFile(
									imgFile, cell, uuidStr);
						} else
							// No UUID was included in the csv file:
							imgFile = resolveFile(new File(csvFileDirStr), imgFile, null);
							if (imgFile == null)
								// User wants to skip this image:
								continue;
							object = PhotoSpreadFileImporter.importFile(
									imgFile, cell);

						for (int i = 0; i < headers.length; i++) {
							if ((i != filePathIndex) && (i != uuidStrIndex))
								object.setMetaData(headers[i], reader.get(i));
						}

						cell.addObject(object);
						numRecordsLoaded++;
					} catch (java.io.IOException e) {
						if (ignoreMissingFiles)
							continue;
						int userDecision = 
							getFileImportProblemAdvice(imgFile.getAbsolutePath());
						switch (userDecision) {
						case JOptionPane.CLOSED_OPTION:
						case ABORT_OPTION:
							return numRecordsLoaded;
						case SKIP_OPTION:
							ignoreMissingFiles = true;
							continue;
						case NEW_FOLDER_OPTION:
							File newDir = 
								Misc.getFileNameFromUser(
										null, // Let user see all files (no file filter) 
										JFileChooser.DIRECTORIES_ONLY,
										null); // use default text for dialog submit button.
							if (newDir == null) // User canceled.
								return numRecordsLoaded;
							csvFileDirStr = newDir.getPath();
							ignoreMissingFiles = false;
							reTrying = true;
							continue;
						default:
							continue;
						}
					} // end catch clause

				} // end while
			} // end if
		} // end outer try
		catch (java.io.IOException e) {
			Misc.showErrorMsg(
					"During CSV import: " + e.getMessage(),
							PhotoSpread.getCurrentSheetWindow());
		}
		return numRecordsLoaded;
	}
	
	protected static int getFileImportProblemAdvice(String filePath) {
		String[] missingFileRemedyOptions = 
			new String[] {
				"Skip Missing Files",          // SKIP_OPTION
				"Identify New File Folder",    // NEW_FOLDER_OPTION
				"Abort Import", };             // ABORT_OPTION
		int decision = JOptionPane.showOptionDialog(
				PhotoSpread.getCurrentSheetWindow(),  // Component to show dialog with
				"File '" + filePath + "' was not found.\n" +
				"Option '" +
				missingFileRemedyOptions[NEW_FOLDER_OPTION] +
				"' lets you pick any file as an alternative\n" +
				"folder, where the files referenced in the CSV file are now (at least mostly) located.\n" +
				"Choose one of:", 
				"Missing file reference in CSV file", // title in top of window frame.
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.QUESTION_MESSAGE, 
				null, missingFileRemedyOptions,
				NEW_FOLDER_OPTION);
		return decision;
	}
	
	/**
	 * Given a File instance containing the assumed path to an image,
	 * return a new File instance wrapping a (possibly corrected) path.
	 * The scenario for this method is this: In the past, an image was 
	 * tagged on a different machine <i>A</i>. The image information, including
	 * the original file path, and the file's UUID (content fingerprint),
	 * were exported to a <code>.csv</code> file. Now, on this machine <i>B</i>, 
	 * the <code>.csv</code> file is imported. A candidate file <i>C</i>
	 * might exist on this machine (<i>B</i>) at the same file system place as the 
	 * original file <i>O</i> on machine <i>A</i>. But <i>C</i> might
	 * have a different fingerprint than <i>O</i>, in which case the user
	 * must be involved in either deciding to accept <i>C</i>, or looking for
	 * an alternative.
	 * <p>
	 * Alternatively, no <i>C</i> might be found on <i>B</i>'s file system
	 * at <i>O</i>'s path. In that case the user must be asked to navigate
	 * to an alternate place, or to give up.
	 * <p>
	 * The final path is determined as follows:
	 * <ul>
	 * <li> If the given file <i>C</i> exists, and the original 
	 *      file's UUID is unknown, then <i>C</i> is returned.
	 * </li>
	 * <li> If the given file <i>C</i> exists, and the original file 
	 *      <i>O</i>'s UUID is  known, then a UUID is computed 
	 * 		using <i>C</i>'s content. If the UUID of <i>O</i> matches 
	 * 		the computed UUID, then <i>C</i> is returned.
	 * 		<p>
	 * 		If the UUIDs do not match, then the user is asked to either
	 * 		accept <i>C</i>, or to navigate to a new file, <i>C'</i>
	 * </li>
	 * <li> If the given file <i>C</i> does not exist, the instance variable
	 * 		<code>wantStoredFileReplacement</code> is checked, to determine
	 * 		whether the user wants an interactive replacement file search.
	 * 		If no such search is wanted, the method returns <code>null</code>
	 * 	    <p>
	 * 	    If <code>wantStoredFileReplacement</code> is <code>true</code>,
	 * 	    the user is asked to identify a replacement file on the user's
	 * 		machine. If that search yields a new file, the process in the 
	 * 		items above is performed.
	 *   
	 * @param searchStartDir Directory where guided search should start. If <code>null</code>,
	 * 		  guided search starts at current working directory.
	 * @param originalFile File instance that wraps the path to the original file.
	 * @param uuidOriginalFile UUID of the original file.
	 * @return File object representing the resolved path, or <code>null</code>
	 *         if no (replacement) file could be found.
	 * @throws IOException
	 */
	public static File resolveFile(File searchStartDir, File originalFile, UUID uuidOriginalFile) throws IOException{
		
		File candidate = null;
		UUID candidateUUID = null;
		
		if(originalFile.exists()) {
			// If the original UUID is unavailable,
			// then this is as good as we can make it:
			if (uuidOriginalFile == null)
				return originalFile;
			
			candidateUUID = new UUID(originalFile, FileHashMethod.USE_FILE_SAMPLING);
			candidate = originalFile;
			// If the passed-in's file's UUID matches the
			// original UUID, we are done:
			if (uuidOriginalFile.equals(candidateUUID))
				return candidate;
		} else {
			// Image file does not exist at the given (original) path:
			// perform guided search with user:
			candidate = correctPath(originalFile.getAbsolutePath());
			if (candidate == null)
				// User gave up:
				return null;
		}

		if (uuidOriginalFile == null)
			// No UUID is known of the original file. 
			// So, we return the new file as the One:
			return candidate;
					
		// At this point we have a new file path to an
		// image, and we know the UUID of the original file 
		// Compare UUIDs of original file and found file.
		// In loop, offer search for new file, if UUIDs disagree:
		while (true) {
			// Do UUIDs match?
			if( ! uuidOriginalFile.equals(candidateUUID)){
				// Nope, different UUIDs, therefore different file contents:
				// If user wants to keep looking, do that:
				if(getMismatchedUUIDProblemAdvice(originalFile.getPath(), candidate.getPath())) {
					// User wants to keep looking. Interact via dialogs:
					candidate = correctPath(originalFile.getAbsolutePath());
					if (candidate == null) {
						// User gave up after all:
						return null;
					}
					// Got a new file that might work. Compute its UUID:
					candidateUUID = new UUID(candidate, FileHashMethod.USE_FILE_SAMPLING);
				} else
					// User does not want to keep looking:
					return null;
			} else
				// UUIDs matched:
				return candidate;
		}
	}
	
/*	protected static File resolveFileOLD(String startPath, File originalFile, UUID uuid) throws IOException{
		
		if(originalFile.exists()) return originalFile;
		String path = originalFile.getPath();
		String[] parents = path.split(File.separator);
		StringBuffer relativePath = new StringBuffer("");
		for(int i = parents.length-1; i > 0; --i){
			relativePath.insert(0, File.separator + parents[i]);
			File candidate = new File(startPath + relativePath.toString());
			if(candidate.exists()){
				if(uuid==null){
					int decision = getMissingUUIDProblemAdvice(originalFile.getPath(), candidate.getPath());
					if(decision == KEEP_LOOKING_OPTION) continue;
				}
				else{
					UUID candidateUUID = new UUID(candidate, FileHashMethod.USE_FILE_SAMPLING);
					if(!uuid.equals(candidateUUID)){
						int decision = getMissingUUIDProblemAdvice(originalFile.getPath(), candidate.getPath());
						if(decision == KEEP_LOOKING_OPTION) continue;
					}
				}
				return candidate;
			}
		}
		throw new java.io.IOException("Could not resolve file path");
	}
*/	
	
	
	/**
	 * Request decision from user in case of two image files having different
	 * UUIDs.
	 * <p>
	 * Given an 'original' file path, and a new file path, display an
	 * option dialog that allows the user to decide whether to accept
	 * <code>candidateFilePath</code> as a correct image.
	 * 
	 * @param originalFilePath Path to the image file that generated the authoritative UUID 
	 * @param candidateFilePath Path to a new image file whose UUID differs from the file
	 * at <code>originalFilePath</code>.
	 * @return Boolean to indicate whether user wants to accept the candidate file, or not.
	 */
	protected static boolean getMismatchedUUIDProblemAdvice(String originalFilePath, String candidateFilePath) {

		// For unit testing:
		if (PhotoSpread.getDebutLevel() == DebugLevel.AUTOMATIC_TESTING)
			// The unit test suite can set this
			// response ahead of testing:
			return secretKeepLooking;
		
		String[] incorrectUUIDRemedyOptions = 
			new String[] {
				"Accept File",
				"Keep Looking", };
		
		int decision = JOptionPane.showOptionDialog(
				PhotoSpread.getCurrentSheetWindow(),  // Component to show dialog with
				"Image content discrepancy:\n" +
				"The original file '" + originalFilePath + "',\nand the file '" + candidateFilePath + 
				"'\n on this machine seem to have different contents (they have different content fingerprints).\n" +
				"Option '" +
				incorrectUUIDRemedyOptions[ACCEPT_FILE_OPTION] +
				"' accepts this candidate file\n" +
				"If this is not the right file, select option '" +
				incorrectUUIDRemedyOptions[KEEP_LOOKING_OPTION] +
				"' and we will keep loooking", 
				"Mismatched UUID", // title in top of window frame.
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.QUESTION_MESSAGE, 
				null, incorrectUUIDRemedyOptions,
				KEEP_LOOKING_OPTION); // Default
		
		// Even though there are only two options now,
		// check for either, in case we add more options
		// later:
		if (decision == ACCEPT_FILE_OPTION)
			return true;
		else if (decision == KEEP_LOOKING_OPTION)
			return false;
		else
			return false;
	}
	
	public static File correctPath(String dysfunctionalOldPath) {

		if (PhotoSpread.getDebutLevel() == DebugLevel.AUTOMATIC_TESTING)
			// A test suite can set this variable 
			// ahead of testing:
			return secretCandidatePath;
		
		// First, check whether any of the replacement
		// directories that the user provided for previous
		// PhotoSpread objects that were not found on the
		// disk work for this path:

		String newPathStr;
		for (FilePathTransformer xformer : PhotoSpreadFileImporter.filePathTransformers) {
			newPathStr = xformer.getUpdatedFilePath(dysfunctionalOldPath);
			if (newPathStr != null)
				return new File(newPathStr);
		}

		// If user opted out of replacement offers,
		// just return null.
		if (!wantStoredFileReplacement)
			return null;

		// Offer replacement for the file
		File newFilePath = Misc.getFileReplacementFromUser(dysfunctionalOldPath);
		if (newFilePath != null) {
			// Remember the path to the directory the user
			// navigated to, so that we can check future problem
			// objects against all those directories before asking
			// the user again for help:
			filePathTransformers.add(new FilePathTransformer(dysfunctionalOldPath, newFilePath.toString()));
			return newFilePath;
		}

		// User cancelled out of the offer to replace.
		// Ask whether they want to opt out of these offers
		// for this load operation:

		wantStoredFileReplacement = 
				Misc.showConfirmMsg("Wish continuance of offers for file replacements?", 
						PhotoSpread.getCurrentSheetWindow());
		return null;
	}
}
