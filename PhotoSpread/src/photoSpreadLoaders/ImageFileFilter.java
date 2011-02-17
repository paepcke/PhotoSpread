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
 * Images filter for file choosing dialog.
 */
public class ImageFileFilter  extends FileFilter {

	//Accept all directories and all gif, jpg, tiff, or png files.
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}

		String extension = Misc.getExtension(f);
		if (extension != null) {
			if (extension.equals(PhotoSpreadFileImporter.jpeg) ||
				extension.equals(PhotoSpreadFileImporter.jpg)  ||
					extension.equals(PhotoSpreadFileImporter.gif) ||
					extension.equals(PhotoSpreadFileImporter.png)) 
				 /* extension.equals(PhotoSpreadFileImporter.tiff) ||{
				 */
				return true;
			else
				return false;
		}
		return false;
	}

	//The description of this filter
	public String getDescription() {
		return "All image files (.jpeg, .jpg, .gif, .png)";   // ,.tif(f), .gif, .png";
	}
}	

