/**
 * 
 */
package edu.stanford.photoSpreadUtilities;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import edu.stanford.photoSpread.PhotoSpreadException;
import edu.stanford.photoSpreadUtilities.Misc.Pair;

/**
 * @author paepcke
 *
 */
public class PhotoSpreadProperties<KeyType, ValueType>  {
	
	private static final int defaultExpectedNumEntries = 20;
	private Hashtable<KeyType, ValueType> _theHashTable;
	private PhotoSpreadProperties<KeyType, ValueType> _theDefaults;
	
	
	/****************************************************
	 * Constructor(s)
	 *****************************************************/
	
	public PhotoSpreadProperties(PhotoSpreadProperties<KeyType, ValueType> defaults, int expectedNumEntries) {

		_theHashTable = new Hashtable<KeyType, ValueType>((int) (expectedNumEntries / .75));
		_theDefaults = defaults;
	}
	
	public PhotoSpreadProperties(PhotoSpreadProperties<KeyType, ValueType> defaults) {

		this(defaults, defaultExpectedNumEntries);
	}

	public PhotoSpreadProperties(int expectedNumEntries) {

		this(null, expectedNumEntries);
	}
	
	public PhotoSpreadProperties() {

		this(null, defaultExpectedNumEntries);
	}
	
	

	/****************************************************
	 * Methods
	 *****************************************************/
	
	public void put (KeyType key, ValueType value) {
		_theHashTable.put(key, value);
	}

	public ValueType get (KeyType key) {
		
		ValueType res = _theHashTable.get(key); 
		if (res != null)
			return res;
		else
			if (_theDefaults != null)
				return _theDefaults.get(key);
			else
				return null;
	}
	
	public void setProperty (KeyType key, ValueType value) {
		put (key, value);
	}

	public ValueType getProperty (KeyType key) {
		return get(key);
	}
	
	public Enumeration<KeyType> keys() {
		return _theHashTable.keys();
	}
	
	/**
	 * Given a user preferences key whose value is
	 * expected to be an int, return that int.
	 * Key existence and integer type checking is done.
	 * 
	 * @param userPrefsKey
	 * @return User preference (an integer)
	 */
	public Integer getInt(KeyType userPrefsKey) {
		
		int res;
		String thePrefIfAString;
		int thePrefIfAnInt;
		
		try {
			
			thePrefIfAString = ((String) get(userPrefsKey));
			if (thePrefIfAString == null)
				return null;
			
		} catch (ClassCastException eNotAString) {
			
			try {
				
				thePrefIfAnInt = ((Integer) get(userPrefsKey));
				return thePrefIfAnInt;
				
			} catch (ClassCastException eNotAnInt) {
			
			throw new RuntimeException(new PhotoSpreadException.IllegalPreferenceValueException (
					"Preference value '" +
					userPrefsKey +
					"' is not a string or integer."));
			}
		}

		//if (thePrefIfAString == null) {
		//	return null;
		//}

		try {
			res = Integer.parseInt(thePrefIfAString.trim());
			
		} catch (NumberFormatException e) {
			throw new RuntimeException(new PhotoSpreadException.IllegalPreferenceValueException(
					"Expected an integer for user preference '" +
					userPrefsKey +
					"'. Was given '" + 
					thePrefIfAString + 
			"' instead."));
		}

		return res;
	}

	
	/**
	 * Given a key into the user preferences, expect that the 
	 * entry contains two integers that correspond to some width and
	 * height. We return a respective Dimension object.
	 * Exception NotBoundException if property not found.
	 * Exception NumberFormatException if property value is not 
	 * a string of two, space-separated integers.
	 * 
	 * @param userPrefsKey
	 * @return 
	 */

	public Dimension getDimension(KeyType userPrefsKey) {

		Pair<Integer, Integer> widthHeight = null;
		Dimension valIsDimension = null;
		
		// Get the requested user preference value:
		try {
			valIsDimension = (Dimension) get(userPrefsKey);
			return valIsDimension;

		} catch (ClassCastException eNotAString) {
			
			try {
				
				ValueType pref = get(userPrefsKey);
				
				if (pref == null)
					return null;
				
				widthHeight = twoIntsFromString (pref);
				return new Dimension((int) widthHeight.first(), (int) widthHeight.second());
				
			} catch (ClassCastException eNotAnInt) {
			
				throw new RuntimeException(new PhotoSpreadException.IllegalPreferenceValueException (
						"Preference value '" +
						userPrefsKey +
						"' is not a string or Dimension instance."));
			}
		}
	}
	
	/**
	 * Given a string with two integers, produce a Pair object
	 * with the two integers. Error checking is performed on
	 * the string.
	 * 
	 * @param strOfTwoSpaceSeparatedInts
	 * @return Pair object containing two ints.
	 */
	
	public Pair<Integer, Integer> twoIntsFromString (ValueType twoIntsStr) {
	
		String strOfTwoSpaceSeparatedInts = ((String) twoIntsStr);
		
		// Partition the string by whitespace. The 3 tells the
		// split to return at most 3 elements: the first two
		// (hopefully) numbers, and any rest of the string. The
		// latter we ignore:

		String[] intStrArray = strOfTwoSpaceSeparatedInts.split("[ \t\n\f\r]", 3);
		if (intStrArray.length < 2) {
			throw new NumberFormatException("Expected a string with two integers. Was given '" + 
					strOfTwoSpaceSeparatedInts + "'.");
		}

		int first, second = 0;

		try {
			first = Integer.parseInt(intStrArray[0]);
			second = Integer.parseInt(intStrArray[1]);

		} catch (NumberFormatException e) {
			throw new RuntimeException(new NumberFormatException("Expected a string with two integers. Was given '" + 
					strOfTwoSpaceSeparatedInts + "'."));
		}
		
		return new Misc().new Pair<Integer, Integer>(first, second);
	}

	
	@SuppressWarnings("unchecked")
	public void load (BufferedReader inStream)
	throws IOException {
		
		String propName = "";
		
		// Kludge to save me work:
		Properties loadPropsCrutch = new Properties();
		
		loadPropsCrutch.load(inStream);
		Enumeration<String> propNames = (Enumeration<String>)loadPropsCrutch.propertyNames();
		
		while (propNames.hasMoreElements()) {
			
			propName = (String) propNames.nextElement();
			put((KeyType) propName, (ValueType) loadPropsCrutch.getProperty(propName));
		}
	}

	public void store (FileWriter outStream, String comments)
	throws IOException {
		
		KeyType propName = null;
		Enumeration<KeyType> hashKeys = _theHashTable.keys();
		
		// Kludge to save me work:
		Properties savePropsCrutch = new Properties();
		
		while (hashKeys.hasMoreElements()) {
			propName = (KeyType) hashKeys.nextElement();
			savePropsCrutch.put(propName,  get(propName));
		}
		
		savePropsCrutch.store(outStream, comments);
	}

	@SuppressWarnings("unchecked")
	public void loadFromXML (InputStream inStream)
	throws IOException {
		
		String propName = "";
		
		// Kludge to save me work:
		Properties loadPropsCrutch = new Properties();
		
		loadPropsCrutch.loadFromXML(inStream);
		
		Enumeration<String> propNames = (Enumeration<String>) loadPropsCrutch.propertyNames();
		
		while (propNames.hasMoreElements()) {
			
			propName = (String) propNames.nextElement();
			put((KeyType) propName, (ValueType) loadPropsCrutch.getProperty(propName));
		}
	}

	public void storeToXML (OutputStream outStream, String comments)
	throws IOException {
		
		KeyType propName = null;
		Enumeration<KeyType> hashKeys = _theHashTable.keys();
		
		// Kludge to save me work:
		Properties savePropsCrutch = new Properties();
		
		while (hashKeys.hasMoreElements()) {
			propName = (KeyType) hashKeys.nextElement();
			savePropsCrutch.put(propName,  get(propName));
		}
		
		savePropsCrutch.storeToXML(outStream, comments);
	}
}
