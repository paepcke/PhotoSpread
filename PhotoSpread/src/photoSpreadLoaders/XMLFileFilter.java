/**
 * 
 */
package photoSpreadLoaders;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import photoSpreadUtilities.Misc;



/**
 * @author paepcke
 *
 * CSV filter for file choosing dialog.
 */
public class XMLFileFilter  extends FileFilter {

	//Accept all directories and all .xml files:
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}
		
		String extension = Misc.getExtension(f);
		if (extension != null) {
			if (extension.equals(PhotoSpreadFileImporter.xml))
				return true;
			else
				return false;
		}

		return false;
	}

	//The description of this filter
	public String getDescription() {
		return "PhotoSpread XML files. Saved sheets. (.xml)";
	}
}	
