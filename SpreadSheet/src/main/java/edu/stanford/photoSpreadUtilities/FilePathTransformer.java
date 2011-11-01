package edu.stanford.photoSpreadUtilities;

import java.io.File;

public class FilePathTransformer {
	
	File oldPathExample   = null;
	File newPathExample   = null;
	String oldPathToFiles = null;
	String newPathToFiles = null;

	File mostRecentUpdatedFullPath = null;

	public FilePathTransformer(String oldPathEx, String newPathEx) {
		oldPathExample = new File(oldPathEx);
		newPathExample = new File(newPathEx);
		prepareTransforms();

	}

	public String getUpdatedFilePath(String oldFullFilePath) {
		File newFileObj = getUpdatedFilePath(new File(oldFullFilePath));
		if (newFileObj == null)
			return null;
		else
			return newFileObj.getAbsolutePath();
	}
	
	public File getUpdatedFilePath(File oldFullPilePath) {
		String fileNameOnly = oldFullPilePath.getName();
		mostRecentUpdatedFullPath = new File(newPathToFiles, fileNameOnly);
		if (mostRecentUpdatedFullPath.exists())
			return mostRecentUpdatedFullPath;
		else
			return null;
	}
	
	// The following get methods are intended only for 
	// automated jUnit testing:
	
	public String getNewPathToFiles() {
		return newPathToFiles;
	}
	
	public File getMostRecentUpdatedFullPath() {
		return mostRecentUpdatedFullPath;
	}
	
	private void prepareTransforms() {
		oldPathToFiles = oldPathExample.getParent();
		if (oldPathToFiles == null)
			oldPathToFiles = "";
		
		newPathToFiles = newPathExample.getParent();
		if (newPathToFiles == null)
			newPathToFiles = "";
	}
}

