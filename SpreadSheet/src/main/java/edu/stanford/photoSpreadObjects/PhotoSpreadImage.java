/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.photoSpreadObjects;


import edu.stanford.inputOutput.ExifReader;
import edu.stanford.inputOutput.JfifReader;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import edu.stanford.photoSpread.PhotoSpreadException.BadUUIDStringError;
import edu.stanford.photoSpread.PhotoSpreadException.CannotLoadImage;
import edu.stanford.photoSpreadLoaders.PhotoSpreadImageLoader;
import edu.stanford.photoSpreadObjects.photoSpreadComponents.DraggableLabel;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;
import edu.stanford.photoSpreadUtilities.UUID;
import edu.stanford.photoSpreadUtilities.UUID.FileHashMethod;

/**
 *
 * @author skandel
 */
public class PhotoSpreadImage extends PhotoSpreadFileObject {

	private PhotoSpreadImageLoader _pil;


	/****************************************************
	 * Constructor(s)
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 *****************************************************/

	public PhotoSpreadImage(PhotoSpreadCell _cell, String _filePath) 
	throws FileNotFoundException, IOException {
		super(_cell, _filePath, new UUID(new File(_filePath), FileHashMethod.USE_FILE_SAMPLING));
		
		try {
			// Reads in Photospread user-defined metadata only
			ArrayList< ArrayList<String> > metadataSet = ExifReader.readMetaDataSet(_filePath);
			if (!metadataSet.isEmpty()) {
				for (int i = 0; i < metadataSet.size(); i++) {
					ArrayList<String> pair = metadataSet.get(i);
					super.setMetaData(pair.get(0), pair.get(1));
				}
			}
			
			// Reads in date/time in EXIF
			if (metadataSet.isEmpty() || (!containsKey(metadataSet, "Time") && !containsKey(metadataSet, "Date"))) {
				ArrayList< ArrayList<String> > dateTimeSet = ExifReader.readExifTimeDate(_filePath);
				if (!dateTimeSet.isEmpty()) {
					for (int i = 0; i < dateTimeSet.size(); i++) {
						ArrayList<String> dtPair = dateTimeSet.get(i);
						super.setMetaData(dtPair.get(0), dtPair.get(1));
					}
				}
			}
			
			// Reads in JFIF comment
			if (metadataSet.isEmpty() || !(containsKey(metadataSet, "HasBuckEyeData") || containsKey(metadataSet, "HasReconyxData"))) {
				ArrayList< ArrayList<String> > cameraTrapData = JfifReader.readMetaDataSet(_filePath);
				if (!cameraTrapData.isEmpty()) {
					for (int i = 0; i < cameraTrapData.size(); i++) {
						ArrayList<String> pair = cameraTrapData.get(i);
						super.setMetaData(pair.get(0), pair.get(1));
					}
				}
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		_pil = new PhotoSpreadImageLoader();
	}

	public PhotoSpreadImage(PhotoSpreadCell _cell, UUID _objectId, String _filePath) {
		super(_cell, _filePath, _objectId);

		_pil = new PhotoSpreadImageLoader();
	}
	
	/**
	 * This constructor is used when an XML saved-sheet
	 * file is loaded.
	 * @param _cell
	 * @param objectIdStr
	 * @param _filePath
	 * @throws BadUUIDStringError
	 */
	public PhotoSpreadImage(PhotoSpreadCell _cell, String objectIdStr, String _filePath) 
	throws BadUUIDStringError {
		super(_cell, _filePath, UUID.createFromUUIDString(objectIdStr));

		_pil = new PhotoSpreadImageLoader();
	}
	
	
	/****************************************************
	 * Methods
	 * @throws CannotLoadImage 
	 *****************************************************/

	public Component getObjectComponent( int height, int width) throws CannotLoadImage{
		return loadImageLabel(height, width);
	}

	public Component getWorkspaceComponent( int height, int width) throws CannotLoadImage{
		return _pil.getImageComponent(this, _filePath);
	}

	private DraggableLabel loadImageLabel( int height, int width) throws CannotLoadImage{
		return _pil.getImageComponent(this, _filePath, height, width);
	}
	
	private Boolean containsKey(ArrayList <ArrayList <String>> metadata, String key) {
		for (int i = 0; i < metadata.size(); i++) {
			ArrayList<String> pair = metadata.get(i);
			if (pair.get(0).equals(key)) return true;
			if (pair.get(1).equals(key)) return true;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public PhotoSpreadImage copyObject() {
		return new PhotoSpreadImage(_cell, getObjectID(), _filePath);
	}
	
}
