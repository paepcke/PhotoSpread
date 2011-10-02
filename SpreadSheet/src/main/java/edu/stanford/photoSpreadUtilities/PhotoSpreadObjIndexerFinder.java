package edu.stanford.photoSpreadUtilities;

import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;

import edu.stanford.photoSpread.PhotoSpread;
import edu.stanford.photoSpread.PhotoSpreadException;
import edu.stanford.photoSpread.PhotoSpreadException.IllegalArgumentException;
import edu.stanford.photoSpreadObjects.ObjectIndexerFinder;
import edu.stanford.photoSpreadObjects.PhotoSpreadDoubleObject;
import edu.stanford.photoSpreadObjects.PhotoSpreadFileObject;
import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadObjects.PhotoSpreadStringObject;
import edu.stanford.photoSpreadObjects.PhotoSpreadTableObject;
import edu.stanford.photoSpreadParser.photoSpreadExpression.PhotoSpreadComputable;
import edu.stanford.photoSpreadTable.PhotoSpreadTable;

public class PhotoSpreadObjIndexerFinder implements ObjectIndexerFinder<PhotoSpreadObject> {

	protected static enum PhotoSpreadDispatch {
		STRING,
		DOUBLE,
		FILE,
		TABLE
	}

	private DispatchHashMap _dispatchTable = null;
	private HashMap<Object, PhotoSpreadObject> _contentIndex = 
		new HashMap<Object, PhotoSpreadObject>();

	// Reserved string to prepend to file names so that
	// we can distinguish them from regular strings: We
	// prepend the string "<cnt-E>File:" to the path: 
	private static char[] _reservedNameMarkerPrefix =  {KeyEvent.VK_5};
	private String _fileNameMarker = new String(_reservedNameMarkerPrefix) + "File:";

	private String _tableMarker = new String(_reservedNameMarkerPrefix) + "Table:";

	/****************************************************
	 * Constructors
	 *****************************************************/

	public PhotoSpreadObjIndexerFinder () {
		_dispatchTable = new DispatchHashMap();
	}

	/****************************************************
	 * Inner class DispatchHashMap
	 *****************************************************/

	public class DispatchHashMap extends HashMap<Class<?> , PhotoSpreadDispatch> { 

		private static final long serialVersionUID = 1L;

		public DispatchHashMap () {

			try {
				put(Class.forName("photoSpreadObjects.PhotoSpreadStringObject"), PhotoSpreadDispatch.STRING);
				put(Class.forName("photoSpreadObjects.PhotoSpreadDoubleObject"), PhotoSpreadDispatch.DOUBLE);
				put(Class.forName("photoSpreadObjects.PhotoSpreadTextFile"), PhotoSpreadDispatch.FILE);
				put(Class.forName("photoSpreadObjects.PhotoSpreadImage"), PhotoSpreadDispatch.FILE);
				put(Class.forName("photoSpreadObjects.PhotoSpreadTableObject"), PhotoSpreadDispatch.TABLE);
			} catch (ClassNotFoundException e) {
				// Use a runtime exception here, because class-not-found
				// will be obvious at debug time. This way we don't
				// have to drag handling of this exception around at runtime.
				throw new RuntimeException(
						"Class not found. Fix DispatchHashMap table initialization: " +
						e.getMessage());
			}
		}
	};

	/****************************************************
	 * Methods for ADDing objects to the index
	 *****************************************************/

	public boolean add(PhotoSpreadComputable obj) throws IllegalArgumentException {

		PhotoSpreadDispatch dispatch = _dispatchTable.get(obj.getClass()); 

		if (dispatch != null)
			switch (dispatch) {

			case STRING:
				return add((PhotoSpreadStringObject) obj);
			case DOUBLE:
				return add((PhotoSpreadDoubleObject) obj);
			case FILE:
				return add((PhotoSpreadFileObject) obj);
			case TABLE:
				return add((PhotoSpreadTableObject) obj);
			}

		throw new PhotoSpreadException.IllegalArgumentException(
				"Expecting a PhotoSpreadObject subtype to be indexed. Got: " + obj);
	}

	private boolean add(PhotoSpreadStringObject obj) {

		if (_contentIndex.put(getObjectIndexKey(obj), obj) == null) {
			PhotoSpread.trace("Indexed new PhotoSpreadStringObject " + obj);
			return true;
		}
		PhotoSpread.trace("PhotoSpreadStringObject replaced in index. Previous obj: " + obj);
		return false;
	}


	private boolean add(PhotoSpreadDoubleObject obj) {

		if (_contentIndex.put(getObjectIndexKey(obj), obj) == null) {
			PhotoSpread.trace("Indexed new PhotoSpreadDoubleObject " + obj);
			return true;
		}
		PhotoSpread.trace("PhotoSpreadDoubleObject replaced in index. Previous obj: " + obj);
		return false;
	}

	private boolean add(PhotoSpreadFileObject obj) {

		if (_contentIndex.put(getObjectIndexKey(obj), obj) == null) {
			PhotoSpread.trace("Indexed new PhotoSpreadFileObject " + obj);
			return true;
		}
		PhotoSpread.trace("PhotoSpreadFileObject replaced in index. Previous obj: " + obj);
		return false;
	}

	private boolean add(PhotoSpreadTableObject psTableObj) {

		if (_contentIndex.put(getObjectIndexKey(psTableObj), psTableObj) == null) {
			PhotoSpread.trace("Indexed new PhotoSpreadTableObject " + psTableObj);
			return true;
		}
		PhotoSpread.trace("PhotoSpreadTableObject replaced in index. Previous obj: " + psTableObj);
		return false;
	}

	public boolean addAll(Collection<PhotoSpreadComputable> objs)
	throws IllegalArgumentException {
		for (PhotoSpreadComputable item : objs) {
			add(item);
		}
		return true;
	}

	/****************************************************
	 * Methods for REMOVEing objects to the index
	 *****************************************************/

	public void clear() {
		_contentIndex.clear();
		PhotoSpread.trace("Cleared index");
	}

	public boolean remove(PhotoSpreadComputable obj)
	throws IllegalArgumentException {

		PhotoSpreadDispatch dispatch = _dispatchTable.get(obj.getClass()); 

		if (dispatch != null)
			switch (dispatch) {

			case STRING:
				return remove((PhotoSpreadStringObject) obj);
			case DOUBLE:
				return remove((PhotoSpreadDoubleObject) obj);
			case FILE:
				return remove((PhotoSpreadFileObject) obj);
			case TABLE:
				return remove((PhotoSpreadTableObject) obj);
			}

		throw new PhotoSpreadException.IllegalArgumentException(
				"Expecting a PhotoSpreadObject subtype to be indexed. Got: " + obj);
	}

	private boolean remove(PhotoSpreadStringObject obj) {
		_contentIndex.remove(getObjectIndexKey(obj));
		PhotoSpread.trace("Removed string object " + obj);
		return true;
	}

	private boolean remove(PhotoSpreadDoubleObject obj) {
		_contentIndex.remove(getObjectIndexKey(obj));
		PhotoSpread.trace("Removed double object " + obj);
		return true;
	}

	private boolean remove(PhotoSpreadFileObject obj) {
		_contentIndex.remove(getObjectIndexKey(obj));
		PhotoSpread.trace("Removed file object " + obj);
		return true;
	}

	private boolean remove(PhotoSpreadTableObject psTableObj) {
		_contentIndex.remove(getObjectIndexKey(psTableObj));
		PhotoSpread.trace("Removed table object " + psTableObj);
		return true;
	}

	/****************************************************
	 * Methods for FINDing objects in the index.
	 *****************************************************/

	public PhotoSpreadStringObject find (String strContent) {
		return (PhotoSpreadStringObject) _contentIndex.get(strContent);
	}

	public PhotoSpreadDoubleObject find (Double doubleContent) {
		return (PhotoSpreadDoubleObject) _contentIndex.get(doubleContent);
	}

	public PhotoSpreadFileObject find (File fileObj) {
		return (PhotoSpreadFileObject) _contentIndex.get(generateKeyFromFileObj(fileObj));
	}

	public PhotoSpreadTableObject find (PhotoSpreadTable psTableObj) {

		return (PhotoSpreadTableObject) _contentIndex.get(_tableMarker + psTableObj.toString());
	}

	/****************************************************
	 * Methods for maintenance
	 *****************************************************/

	public boolean isEmpty() {
		return _contentIndex.isEmpty();
	}

	public int size() {
		return _contentIndex.size();
	}

	/****************************************************
	 * Methods for generating index keys from objects that will be indexed
	 *****************************************************/

	private String getObjectIndexKey (PhotoSpreadStringObject obj) {
		return obj.valueOf();
	}

	private Double getObjectIndexKey (PhotoSpreadDoubleObject obj) {
		return obj.valueOf();
	}

	private String getObjectIndexKey (PhotoSpreadFileObject obj) {

		// We need to make an index key that we can reliably re-create
		// later from just the passed-in object's file name when
		// given a corresponding File object:

		File tmpFileWrapper = new File(obj.getFilePath());
		return generateKeyFromFileObj(tmpFileWrapper);
	}

	private String generateKeyFromFileObj (File theFile) {
		return _fileNameMarker + theFile.getAbsolutePath();
	}

	private String getObjectIndexKey (PhotoSpreadTableObject psTableObj) {

		// We need to make an index key that we can reliably re-create
		// later from just the passed-in table object, 
		// given a corresponding Table object:

		PhotoSpreadTable tmpTblObj = psTableObj.getTable();

		return generateKeyFromTableObj(tmpTblObj);
	}

	private String generateKeyFromTableObj (PhotoSpreadTable tbl) {

		return _tableMarker + tbl.toString();
	}

	/* 
	 * If this index contains a *value* that is the passed-in object,
	 * then return that object, else return null.
	 */
	public PhotoSpreadObject containsValue(PhotoSpreadObject obj) {
		return _contentIndex.get(obj.valueOf());
	}
}
