/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package photoSpreadObjects;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JTextArea;

import photoSpread.PhotoSpreadException.BadUUIDStringError;
import photoSpreadObjects.photoSpreadComponents.DraggableLabel;
import photoSpreadTable.PhotoSpreadCell;
import photoSpreadUtilities.UUID;
import photoSpreadUtilities.UUID.FileHashMethod;

/**
 *
 * @author skandel
 */
public class PhotoSpreadTextFile extends PhotoSpreadFileObject {

	/****************************************************
	 * Constructor(s)
	 *****************************************************/
	
	public PhotoSpreadTextFile(PhotoSpreadCell _cell, String _filePath, UUID _objectId) {
		super( _cell, _filePath, _objectId);
	}
	
	public PhotoSpreadTextFile(PhotoSpreadCell _cell, String _filePath) throws FileNotFoundException, IOException {
		// Force use of entire file content for UUID creation. Text files 
		// are too likely to be similar to each other to risk use of (the faster)
		// content sampling:
		super( _cell, _filePath, new UUID(new File(_filePath), FileHashMethod.USE_WHOLE_FILE));
	}

	public PhotoSpreadTextFile(PhotoSpreadCell _cell, String _filePath, String uuidString) 
	throws FileNotFoundException, IOException, BadUUIDStringError {
		// Force use of entire file content for UUID creation. Text files 
		// are too likely to be similar to each other to risk use of (the faster)
		// content sampling:
		super( _cell, _filePath, UUID.createFromUUIDString(uuidString));
	}
	
	/****************************************************
	 * Methods
	 *****************************************************/
	
	public Component getObjectComponent( int height, int width){

		DraggableLabel label = new DraggableLabel(this);
		label.setText(_filePath);
		return label;
	}

	public Component getWorkspaceComponent(int height, int width){

		System.out.println("get worksapce component");
		JTextArea label = new JTextArea();
		label.setSize(height, width);

		//...checks on aFile are elided
		StringBuffer contents = new StringBuffer();

		try {
			//use buffering, reading one line at a time
			//FileReader always assumes default encoding is OK!
			BufferedReader input =  new BufferedReader(new FileReader(new File(_filePath)));
			try {
				String line = null; //not declared within while loop
				/*
				 * readLine is a bit quirky :
				 * it returns the content of a line MINUS the newline.
				 * it returns null only for the END of the stream.
				 * it returns an empty String if two newlines appear in a row.
				 */
				while (( line = input.readLine()) != null){
					contents.append(line);
					contents.append(System.getProperty("line.separator"));
					if(contents.length() > 1000){
						break;
					}
				}
				System.out.println(contents.toString());
				label.setText(contents.toString());

				return label;
			}
			finally {
				input.close();
			}
		}
		catch (IOException ex){
			label.setText("File Not Read");
			ex.printStackTrace();

			return label;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public PhotoSpreadTextFile copyObject() {
		return new PhotoSpreadTextFile(_cell, _filePath, getObjectID());
	}
	
}
