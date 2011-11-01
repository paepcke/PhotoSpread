/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.photoSpreadObjects;

import edu.stanford.inputOutput.XMLProcessor;

import java.awt.Component;
import java.io.BufferedWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import edu.stanford.photoSpread.PhotoSpreadException;
import edu.stanford.photoSpread.PhotoSpreadException.BadUUIDStringError;
import edu.stanford.photoSpread.PhotoSpreadException.CannotLoadImage;
import edu.stanford.photoSpreadParser.photoSpreadExpression.PhotoSpreadComputable;
import edu.stanford.photoSpreadParser.photoSpreadExpression.photoSpreadFunctions.FunctionResultable;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;
import edu.stanford.photoSpreadUtilities.Const;
import edu.stanford.photoSpreadUtilities.Misc;
import edu.stanford.photoSpreadUtilities.PhotoSpreadHelpers;
import edu.stanford.photoSpreadUtilities.Misc.Pair;

import edu.stanford.photoSpreadUtilities.UUID;

/**
 *
 * @author skandel
 */
public abstract class PhotoSpreadObject implements Comparable<PhotoSpreadObject>,
FunctionResultable, PhotoSpreadComputable {

	private UUID _objectId;
	private TreeMap<String, String> _metadata;
	protected PhotoSpreadCell _cell;
	public static String FILEPATH_ARG = "_filePath";

	/****************************************************
	 * Constructor(s)
	 *****************************************************/

	/**
	 * returns an instance of a photo spread object, given
	 * the type of the object and the arguments to that type's
	 * constructor
	 * @param cell The cell that contains the object
	 * @param objectType the type of object
	 * @param args An array of arguments to pass to object constructor. 
	 * @return the object created
	 */


	/**
	 * Instantiate a PhotoSpread object from information
	 * in an XML file. Called from unmarshallObject(Node, PhotoSpreadCell).
	 * @param cell Cell where the object will reside
	 * @param objectType PhotoSpread class to instantiate
	 * @param args Arguments to the instance construction
	 * @return
	 */
	static public PhotoSpreadObject getInstance(PhotoSpreadCell cell, String objectType, ArrayList<String> args)
	throws BadUUIDStringError {

		PhotoSpreadObject obj = null;
		Constructor<?> cstr = null;

		try{
			Class<?> cl = Class.forName(objectType);

			switch (args.size()) {

			// Create constructor with 2 arguments:
			// a cell, and a string, which is the new object's 
			// file path:
			case 1: 
				cstr = cl.getConstructor(PhotoSpreadCell.class, String.class);
				obj = (PhotoSpreadObject) cstr.newInstance(cell, args.get(0));
				break;

				// Create constructor with 3 arguments:				
			case 2:
				// A cell, the string version of a UUID from which a UUID object
				// can be created, and the filename of the object:
				cstr = cl.getConstructor(PhotoSpreadCell.class, String.class, String.class);
				obj = (PhotoSpreadObject) cstr.newInstance(cell, args.get(0), args.get(1));
				break;

			default:
				throw new PhotoSpreadException.BadObjectInstantiationFromString(
						"Object instantiation for constructor with " +
						args.size() +
						" arguments: " + 
						args
				);
			}

			return obj;
		}
		catch(java.lang.NoSuchMethodException e){
			Misc.showErrorMsgAndStackTrace(e, "");
			//System.out.println(e.getMessage());
			//e.printStackTrace();
		}
		catch(java.lang.IllegalAccessException e){
			Misc.showErrorMsgAndStackTrace(e, "");
			//System.out.println(e.getMessage());
			//e.printStackTrace();
		}
		catch(java.lang.IllegalArgumentException e){
			Misc.showErrorMsgAndStackTrace(e, "");
			//System.out.println(e.getMessage());
			//e.printStackTrace();
		}
		catch(java.lang.InstantiationException e){
			Misc.showErrorMsgAndStackTrace(e, "");
			//System.out.println(e.getMessage());
			//e.printStackTrace();
		}
		catch(java.lang.Exception e){
			Misc.showErrorMsgAndStackTrace(e, "");
			//System.out.println(e.getMessage());
			//e.printStackTrace();
		}

		return null;
	}


	public PhotoSpreadObject(PhotoSpreadCell _cell, UUID _objectId){
		this._objectId = _objectId;
		_metadata = new TreeMap<String, String>();

		this._cell = _cell;
	}
	
	public PhotoSpreadObject(PhotoSpreadCell _cell, UUID _objectId, String fileName){
		// We throw the filename away (future use, maybe):
		this(_cell, _objectId);
	}

/*	public PhotoSpreadObject(PhotoSpreadCell _cell){
		this(_cell, new UUID());
	}
*/
	/****************************************************
	 * Methods
	 *****************************************************/

	public int compareTo (PhotoSpreadObject o) {
		return _objectId.compareTo(o.getObjectID());
	}

	/**
	 * Associative search ('equal by contents'). 
	 * Given a piece of 'content,' return true if
	 * this object contains the content, false otherwise.
	 * Subclasses of PhotoSpreadObject are expected to 
	 * provide appropriate implementations for this method.
	 * For example:
	 *      o PhotoSpreadStringObject: true if the string object
	 *        wraps a string that is '.equals(<the given content>)
	 *      o PhotoSpreadDoubleObject: true if the object's
	 *        wrapped number is '==' to the given content.
	 *      o PhotoSpreadFileObject: true if filenames are .equals.
	 *      o PhotoSpreadTableObject: true if uuid's are .equals.
	 * This class implements one version that returns true if 
	 * a given attribute/value pair is present in this object's
	 * metadata.
	 * 
	 * @param obj The piece of content against which to compare.
	 * @return True if this object contains the given content. Else false.
	 */

	public abstract <T extends Object> boolean contentEquals(T obj);

	/**
	 * Given an attribute/value pair, return true if this object's
	 * metadata contains that attribute/value pair (using equals()).
	 * 
	 * @param metaData A Pair whose first() is an attribute (String),
	 * and whose second() is a value (String). If the attribute part
	 * is null, this method returns true if any of the metadata entries
	 * strore the given value.
	 * 
	 * @return True if given pair is represented in this object's metadata,
	 * else False.
	 */
	public boolean contentEquals(Pair<String, String> metaData) {

		String attr  = metaData.first();
		String value = metaData.second();

		// If attr==null: check for presence of value in
		// *any* of the values:

		if (attr == null)
			return _metadata.containsValue(value);

		if (!_metadata.containsKey(attr))
			return false;

		String storedValue = _metadata.get(attr);
		return storedValue.equals(value);
	}

	public PhotoSpreadCell getCell() {
		return _cell;
	}

	public void setCell(PhotoSpreadCell _cell) {
		this._cell = _cell;
	}

	/**
	 * Returns a primitive object appropriate for the
	 * type. Examples: PhotoSpreadDoubleObject-->Double.
	 * PhotoSpreadFileObject->File (i.e. an instance of
	 * the Java File type) 
	 * @return Primitive object of appropriate type.
	 */
	public abstract Object valueOf();

	/**
	 * Return a Double if one can successfully cast.
	 * Else throw a ClassCastException.
	 * 
	 * @return a Double number that corresponds to the object.
	 */
	public abstract Double toDouble() throws ClassCastException;

	/**
	 * Return a String if one can successfully cast.
	 * Else throw a ClassCastException.
	 * 
	 * @return a String that corresponds to the object.
	 */
	public abstract String toString() throws ClassCastException;

	public UUID getObjectID() {
		return _objectId;
	}
	
	public void setObjectID(UUID theID) {
		_objectId = theID;
	}

	/**
	 * Sets the metadata of an object
	 * @param attr the attribute being set
	 * @param newValue the value being set

	 */

	public void setMetaData(String attr, String newValue){

		// In case this cell is indexed by metadata,
		// update that index by supplying the current and
		// new value:
		
		if (_cell.hasMetadataIndexer())
			_cell.invalidateMetadataIndexer(
					this,
					attr, 
					getMetaData(attr),
					newValue);
		_metadata.put(attr, newValue);
	}

	/**
	 * Returns the value for a given attribute of object metadata
	 * @param attr the attribute 
	 * @return the value for attr

	 */

	public String getMetaData(String attr){

		// The attribute might not be defined,
		// returning null for get(attr). We
		// want that null to be a string so that
		// formulas can compare against 

		String res = _metadata.get(attr);
		return (res == null) ? Const.NULL_VALUE_STRING : res;
	}

	/**
	 * Returns the attributes for all metadata in the object
	 * @return A set of all attributes 

	 */

	public Set<String> getMetaDataKeySet(){
		return _metadata.keySet();
	}

	/**
	 * Returns the metadata in the object
	 * @return An arraylist of metadata  

	 */


	public ArrayList<ArrayList<String>> getMetaDataSet(){
		Iterator<Entry<String, String>> it = _metadata.entrySet().iterator();
		ArrayList<ArrayList<String>> dataSet = new ArrayList<ArrayList<String>>();

		while(it.hasNext()){
			Entry<String, String> entry = it.next(); 
			ArrayList<String> set = new ArrayList<String>();
			set.add(entry.getKey());
			set.add(entry.getValue());
			dataSet.add(set);
		}
		return dataSet;
	}

	public void setMetaData(ArrayList<ArrayList<String>> data){
		clearMetadata();

		Iterator<ArrayList<String>> it = data.iterator();
		while(it.hasNext()){
			ArrayList<String> set = it.next();
			if(!set.get(0).equals("")){
				setMetaData(set.get(0), set.get(1));
			}
		}
	}

	abstract public <T extends PhotoSpreadObject> T copyObject ();
	
	private void clearMetadata(){
		_metadata.clear();
		_cell.clearMetadataIndexer();
	}

	/**
	 * returns the component representing the object
	 * @param height height of component
	 * @param width width of component
	 * @return the component representing object

	 */
	abstract public Component getObjectComponent(int height, int width)
	throws CannotLoadImage;

	/**
	 * returns the component representing the object
	 * @param height height of component
	 * @param width width of component
	 * @return the component representing object
	 * @throws CannotLoadImage 

	 */

	public Component getWorkspaceComponent(int height, int width) throws CannotLoadImage{

		return getObjectComponent(height, width);

	}

	/**
	 * returns string of object's xml representation
	 * @return String of xml representation

	 */

	public String toXML(){


		StringBuffer xml = new StringBuffer();
		xml.append(objectToXMLTag(this.getClass().getName())); 
		xml.append(PhotoSpreadHelpers.getXMLElement(XMLProcessor.OBJECT_CONSTRUCTOR_ARGUMENTS_ELEMENT, PhotoSpreadHelpers.TagType.startTag));
		xml.append(constructorArgsToXML());

		xml.append(PhotoSpreadHelpers.getXMLElement(XMLProcessor.OBJECT_CONSTRUCTOR_ARGUMENTS_ELEMENT, PhotoSpreadHelpers.TagType.endTag));
		xml.append(metaDataToXML());
		xml.append(PhotoSpreadHelpers.getXMLElement(XMLProcessor.OBJECT_ELEMENT, PhotoSpreadHelpers.TagType.endTag));
		return xml.toString();



	}

	/**
	 * writes the object to xml
	 * @param out the stream to write to

	 */

	public void toXML(BufferedWriter out){

		try{

			out.write(objectToXMLTag(this.getClass().getName())); 
			out.write(PhotoSpreadHelpers.getXMLElement(XMLProcessor.OBJECT_CONSTRUCTOR_ARGUMENTS_ELEMENT, PhotoSpreadHelpers.TagType.startTag));
			out.write(constructorArgsToXML());

			out.write(PhotoSpreadHelpers.getXMLElement(XMLProcessor.OBJECT_CONSTRUCTOR_ARGUMENTS_ELEMENT, PhotoSpreadHelpers.TagType.endTag));
			out.write(metaDataToXML());
			out.write(PhotoSpreadHelpers.getXMLElement(XMLProcessor.OBJECT_ELEMENT, PhotoSpreadHelpers.TagType.endTag));

		}
		catch(java.io.IOException e){
			System.out.println("to xml failed caught IOException");
		}


	}

	protected String metaDataToXML(){
		StringBuffer xml = new StringBuffer();
		
		Iterator<Entry<String, String>> it = _metadata.entrySet().iterator();
		if (it.hasNext()) {
			// Tags element that contains all the tags for this object:
			xml.append(PhotoSpreadHelpers.getXMLElement(XMLProcessor.TAGS_ELEMENT, PhotoSpreadHelpers.TagType.startTag));
		}
		while(it.hasNext()){
			Entry<String, String> entry = it.next(); 
			xml.append(PhotoSpreadHelpers.getXMLElement(XMLProcessor.TAG_ELEMENT, PhotoSpreadHelpers.TagType.startTag));
			xml.append(PhotoSpreadHelpers.getXMLElement(XMLProcessor.TAG_ATTRIBUTE_ELEMENT, entry.getKey()));
			xml.append(PhotoSpreadHelpers.getXMLElement(XMLProcessor.TAG_VALUE_ELEMENT, entry.getValue()));
			xml.append(PhotoSpreadHelpers.getXMLElement(XMLProcessor.TAG_ELEMENT, PhotoSpreadHelpers.TagType.endTag));
		}
		xml.append(PhotoSpreadHelpers.getXMLElement(XMLProcessor.TAGS_ELEMENT, PhotoSpreadHelpers.TagType.endTag));

		return xml.toString();
	}

	/**
	 * Generate XML entry for an object. This element has an XML attribute
	 * that specifies the PhotoSpread type of the object
	 * @param objectType
	 * @return
	 */
	protected String objectToXMLTag(String objectType){
		return PhotoSpreadHelpers.getXMLElement(XMLProcessor.OBJECT_ELEMENT +  " " + 
												XMLProcessor.OBJECT_TYPE_ELEMENT + "=" + "\"" + objectType + "\"", 
												PhotoSpreadHelpers.TagType.startTag);   
	}


	abstract public String constructorArgsToXML();
}
