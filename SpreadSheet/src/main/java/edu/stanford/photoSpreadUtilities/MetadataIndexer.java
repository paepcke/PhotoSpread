/**
 * 
 */
package edu.stanford.photoSpreadUtilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import edu.stanford.photoSpreadObjects.PhotoSpreadObject;

/**
 * @author paepcke
 *
 * Indexer over metadata. Each indexer indexes given PhotoSpreadObjects
 * by a pre-defined set of metadata keys. Example:
 * 
 * Let an indexer be initialized to the key set "Name" and "Height".
 * When a PhotoSpreadObject is passed to the add() method, that method
 * will retrieve the values of the metadata keys "Name" and "Height"
 * from the given object. The method will then add the given object
 * under both the value of "Name", and under the value of "Height".
 * So, beginning with a metadata value we can later get all the
 * objects that have a given value for "Name", or another given
 * value for "Height".
 * 
 * The get(<metadataKey>, <metadataValue>) method returns a HashSet 
 * with all the PhotoSpreadObjects that contain <metadataValue> as 
 * the value for <metadataKey>.
 * 
 * Assume an indexer is set for indexing 
 * on metadata keys key_1 through key_n.
 * Then the mapping from metadata values
 * (mdv) to PhotoSpreadObject sets looks
 * like this:
 *             key_1: {...}
 *
 *   mdv_1      ...
 *
 *             key_n: {...}
 *
 *
 *
 *
 *             key_1: {...}
 *
 *   mdv_2      ...
 *
 *             key_n: {...}
 * 
 *   The data structure for all this is like this:
 *   HashMap<metadataValues, HashMap<Key_x, HashSet<PhotoSpreadObject>>> 
 *   
 *   We call the outer HashMap the 'ValueMap.' We call each
 *   of the inner HashMaps 'KeyMap's. We call each HashSet of
 *   PhotoSpreadObjects a 'Bucket'. Respective inner classes
 *   are defined for clarity in the code.
 */

public class MetadataIndexer {

	ArrayList<String> _keysToIndex = new ArrayList<String>();
	ValueMap _valueMap = new ValueMap();

	/****************************************************
	 * Constructor
	 *****************************************************/


	public MetadataIndexer (ArrayList<String> metadataKeys) {

		_keysToIndex = metadataKeys;
	}

	/****************************************************
	 * Inner Classes Bucket, ValueMap, and KeyMap
	 *****************************************************/

	class ValueMap extends HashMap<String, KeyMap> {
		private static final long serialVersionUID = 1L;
	}

	class KeyMap extends HashMap<String, Bucket> {
		private static final long serialVersionUID = 1L;
	}

	class Bucket extends HashSet<PhotoSpreadObject> {
		private static final long serialVersionUID = 1L;
	}

	/****************************************************
	 * Methods
	 *****************************************************/

	public ArrayList<String> getIndexedMetadataKeys () {
		return _keysToIndex;
	}
	
	public ValueMap getValueMap () {
		return _valueMap;
	}

	/**
	 * Index all the given object by all of the
	 * metadata keys that this index indexes.
	 * @param objToIndex
	 */
	public void add (PhotoSpreadObject objToIndex) {
		
		for (String keyToIndex : _keysToIndex) {
			String metadataValue = objToIndex.getMetaData(keyToIndex);
			add(objToIndex, keyToIndex, metadataValue);
		}
	}

	public void add (
			PhotoSpreadObject objToIndex, 
			String metadataKey, 
			String metadataValue) {

		Bucket bucket = null;
		KeyMap keyMap = null;

		if (!_keysToIndex.contains(metadataKey))
			return;

		// Get the KeyMap for the metadata value we just retrieved:
		keyMap = _valueMap.get(metadataValue);

		// If we never saw this value before,
		// we need to create a new map that maps
		// metadata keys to buckets:

		if (keyMap == null) {
			keyMap = installNewKeymap(metadataValue);
		}

		// In the resulting keymap: put the object into the
		// keyToIndex bucket:

		bucket = keyMap.get(metadataKey);
		bucket.add(objToIndex);
	}

	public void addAll (TreeSetRandomSubsetIterable<PhotoSpreadObject> objs) {
		for (PhotoSpreadObject obj : objs)
			add(obj);
	}

	/**
	 * @param metadataKey
	 * @param metadataValue
	 * @return All objects that contain the given metadataValue for
	 * the given metadataKey. Null if value not found.
	 */
	public Bucket get (String metadataKey, String metadataValue) {

		// See whether the given metadata value 
		// leads to a KeyMap:
		KeyMap keyMap = _valueMap.get(metadataValue);
		if (keyMap == null)
			return null;

		// Got a KeyMap for the given value. Get the bucket
		// for the given metadata key (for the value):
		return keyMap.get(metadataKey);
	}

	/**
	 * Remove the given object's index entry for 
	 * the given metadata value/key pair.
	 * @param obj
	 * @param metadataKey
	 * @param metadataValue
	 * @return Bucket with the object removed.
	 */
	public Bucket remove (
			PhotoSpreadObject obj, String metadataKey, String metadataValue) {

		// Need to act only if this indexer
		// indexes the given key:
		if (! _keysToIndex.contains(metadataKey))
			return null;

		Bucket bucket = get(metadataKey, metadataValue);
		bucket.remove(obj);
		return bucket;
	}

	/**
	 * Update a given object's entry in this index.
	 * This means removing the entry for the given
	 * metadata key's old value, and adding a new
	 * entry for the new key.
	 * @param obj
	 * @param metadataKey
	 * @param oldValue
	 * @param newValue
	 */
	public void updateIndex (
			PhotoSpreadObject obj, 
			String metadataKey,
			String oldValue,
			String newValue) {

		// If this index isnt' set to index this
		// metadataKey, do nothing:
		
		if (!_keysToIndex.contains(metadataKey))
			return;
		
		// If oldValue was ever set, remove that value's
		// entry from the index:

		if (!oldValue.equals(Const.NULL_VALUE_STRING)){
			
			Bucket bucket = remove(obj, metadataKey, oldValue);

			// If no bucket existed that contained the 
			// given object for the old value, then something
			// went wrong:

			if (bucket == null)
				throw new RuntimeException(
						"Metadata index corrupt: should contain object '" +
						obj +
						"' as having metadata '" +
						metadataKey +
						" = " +
						oldValue +
						"'. \nDetected while updating index to '" +
						metadataKey +
						" = " +
						newValue +
				"'.");
		}
		add(obj, metadataKey, newValue);
	}

	/**
	 * Empty the entire index.
	 */
	public void clear() {

		// Clearing the whole ValueMap will
		// properly orphan all KeyMaps and their
		// buckets:

		_valueMap.clear();
	}

	/**
	 * Add a new metadata key to the list of keys that
	 * this indexer indexes. The method accepts a collection
	 * of objects that were only indexed with the old metadata
	 * key set. These objects will be indexed additionally
	 * by the new key. This method will do nothing if the
	 * given 'new' metadata key is already part of the indexed
	 * set.
	 * @param newKey
	 * @param objsToIndex
	 */
	public void addMetadataKeyToIndex (
			String newKey,
			TreeSetRandomSubsetIterable<PhotoSpreadObject> objsToIndex) {

		// If the 'new' key is already being indexed by
		// this indexer, then no work is to be done:

		if (_keysToIndex.contains(newKey))
			return;

		_keysToIndex.add(newKey);

		if (objsToIndex == null)
			return;

		// Run through every object in objsToIndex, get
		// their metadata for the new key, and index
		// the object for that new key

		for (PhotoSpreadObject obj : objsToIndex) {
			add(obj, newKey, obj.getMetaData(newKey));
		}
	}

	public void addMetadataKeyToIndex (String newKey) {
		addMetadataKeyToIndex(newKey, null);
	}

	public void addMetadataKeysToIndex (
			ArrayList<String> newKeys,
			TreeSetRandomSubsetIterable<PhotoSpreadObject> objsToIndex) {
		for (String key : newKeys)
			addMetadataKeyToIndex(key, objsToIndex);
	}

	public void addMetadataKeysToIndex (ArrayList<String> newKeys) {
		for (String key : newKeys)
			addMetadataKeyToIndex(key, null);
	}

	/**
	 * Clear the index. Then run over all the given objects
	 * and index them for all of this indexer's metadata keys.
	 * @param objs
	 */
	public void rebuildIndex(TreeSetRandomSubsetIterable<PhotoSpreadObject> objs) {
		clear();
		for (PhotoSpreadObject obj : objs)
			add(obj);
	}

	/**
	 * Service method: make a new keymap and initialize it
	 * with all the metadata keys that this indexer indexes.
	 * Install the new keymap as the target of the given
	 * metadataValue in the ValueMap. If a KeyMap already
	 * exists in this indexer for the given metadata value,
	 * this method does nothing.
	 * @param metadataValue
	 * @return The newly created KeyMap.
	 */
	private KeyMap installNewKeymap (String metadataValue) {
		KeyMap keyMap = null;

		keyMap = _valueMap.get(metadataValue);

		// If ValueMap already contains a KeyMap for
		// the given value, do nothing:
		if (keyMap != null)
			return keyMap;

		keyMap = new KeyMap();
		for (String mdKey : _keysToIndex)
			keyMap.put(mdKey, new Bucket());
		_valueMap.put(metadataValue, keyMap);
		return keyMap;
	}
	
	// TODO: EDITING (eshieh)
	public HashMap<PhotoSpreadObject, HashMap<String,String>> getInverseMap() {
		HashMap<PhotoSpreadObject, HashMap<String,String>> result = 
			new HashMap<PhotoSpreadObject, HashMap<String, String>>();
		
		Iterator<String> itValue =_valueMap.keySet().iterator();
		Iterator<String> itKey = null;
		Iterator<PhotoSpreadObject> itPsObject = null;
		
		String value = "";
		String key = "";
		PhotoSpreadObject psObject = null;
		
		KeyMap keyMap = null;
		Bucket bucket = null;
		
		while (itValue.hasNext()) {
			value = itValue.next();
			keyMap = _valueMap.get(value);
			itKey =_valueMap.keySet().iterator();
			while (itKey.hasNext()) {
				key = itKey.next();
				bucket = keyMap.get(value);
				itPsObject = bucket.iterator();
				while (itPsObject.hasNext()) {
					psObject = itPsObject.next();
					updateInverseMap(result, psObject, key, value);
				}
			}
		}
		return result;
	}

	private void updateInverseMap(HashMap <PhotoSpreadObject, HashMap<String,String>> result, 
			PhotoSpreadObject psObject,
			String key, String value) {
		
		HashMap<String,String> nMap = new HashMap<String,String>();
		if (result.containsKey(psObject)) {
			nMap = result.get(psObject);
		}
		nMap.put(key, value); // Overwrites previous values of the same key, if existent
		result.put(psObject, nMap);
	}
}
