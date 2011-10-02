/**
 * 
 */
package edu.stanford.photoSpreadLoaders;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import edu.stanford.photoSpreadUtilities.Misc;



/**
 * @author paepcke
 *
 * CSV filter for file choosing dialog.
 */
public class CSVFileFilter  extends FileFilter {

	//Accept all directories and all csv/txt files:
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}
		
		String extension = Misc.getExtension(f);
		if (extension != null) {
			if (extension.equals(PhotoSpreadFileImporter.csv) ||
					extension.equals(PhotoSpreadFileImporter.txt))
				return true;
			else
				return false;
		}

		return false;
	}

	//The description of this filter
	public String getDescription() {
		return "Comma-separated value files (.csv)";
	}
}	
