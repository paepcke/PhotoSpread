/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.stanford.photoSpreadLoaders;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import edu.stanford.inputOutput.CsvReader;
import edu.stanford.photoSpread.PhotoSpread;
import edu.stanford.photoSpread.PhotoSpreadException.BadUUIDStringError;
import edu.stanford.photoSpreadObjects.PhotoSpreadImage;
import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadObjects.PhotoSpreadStringObject;
import edu.stanford.photoSpreadObjects.PhotoSpreadTextFile;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;
import edu.stanford.photoSpreadUtilities.Misc;
import edu.stanford.photoSpreadUtilities.UUID;
import edu.stanford.photoSpreadUtilities.UUID.FileHashMethod;

/**
 * 
 * @author skandel
 */
public class PhotoSpreadFileImporter {

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
							imgFile = resolveFile(csvFileDirStr, imgFile, UUID.createFromUUIDString(uuidStr));
							object = PhotoSpreadFileImporter.importFile(
									imgFile, cell, uuidStr);
						} else
							imgFile = resolveFile(csvFileDirStr, imgFile, null);
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
										JFileChooser.DIRECTORIES_ONLY);
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
	
	protected static File resolveFile(String startPath, File originalFile, UUID uuid) throws IOException{
		
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
	
	protected static int getMissingUUIDProblemAdvice(String savedFilePath, String candidateFilePath) {
		String[] incorrectUUIDRemedyOptions = 
			new String[] {
				"Accept File",          // Accept file
				"Keep Looking", };             // Keep looking
		int decision = JOptionPane.showOptionDialog(
				PhotoSpread.getCurrentSheetWindow(),  // Component to show dialog with
				"File '" + savedFilePath + "' was not found.  \n But we found this file: " + candidateFilePath + 
				" \n which has a different UUID.\n" +
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
				NEW_FOLDER_OPTION);
		return decision;
	}
}
