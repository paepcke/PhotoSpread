/**
 * 
 */
package edu.stanford.photoSpreadUtilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import edu.stanford.photoSpreadObjects.PhotoSpreadObject;

/**
 * @author paepcke
 *
 * Static class for generating PhotoSpread object
 * comparators. Comparators are classes that implement
 * interface Comparator for a particular type. Their
 * main method is compare(o1, o2). An important use
 * for this mechanism is to sort TreeSetRandomSubsetIterable and TreeMap
 * structures. 
 * 
 * The code below consists of a static outer factory class,
 * which offers createXXX() methods to return Comparator 
 * instances for sorting PhotoSpread objects by various
 * criteria: UUID, and metadata content. Others could be
 * added, such as image-based comparators.
 */
public class PhotoSpreadComparatorFactory {
	
	public static Comparator<PhotoSpreadObject> createPSUUIDComparator() {
		return new PhotoSpreadComparatorFactory().new UUIDComparator();
	}

	public static PhotoSpreadComparatorFactory.MetadataComparator createPSMetadataComparator() {
		return new PhotoSpreadComparatorFactory().new MetadataComparator();
	}
	
	public static PhotoSpreadComparatorFactory.MetadataComparator createPSMetadataComparator(String metadataSortField) {
		return new PhotoSpreadComparatorFactory().new MetadataComparator(metadataSortField);
	}
	
	/****************************************************
	 * Inner Class: Comparison by UUID 
	 *****************************************************/

	/**
	 * @author paepcke
	 *
	 * Comparator class for sorting PhotoSpreadObjects by UUID
	 *
	 */
	public class UUIDComparator implements Comparator<PhotoSpreadObject> {

		/**
		 * Compares two PhotoSpreadObject's by 
		 * their universally unique identifier (UUID). 
		 */
		
		public UUIDComparator() {
		}
		
		/**
		 * Comparison operator for use by sorting. Compares the
		 * two passed-in objects by their UUID in alpha order.
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */

		public int compare(PhotoSpreadObject o1, PhotoSpreadObject o2) {
			return o1.getObjectID().compareTo(o2.getObjectID());
		}
	}
	
	/****************************************************
	 * Inner Class: Comparison by metadata 
	 *****************************************************/

	/**
	 * @author paepcke
	 * 
	 * Comparator class for sorting PhotoSpreadObjects by metadata. 
	 * Up to three levels of sorting are possible.
	 *
	 */
	public class MetadataComparator  implements Comparator<PhotoSpreadObject> {
		
		Comparator<String> _stringComparatorCaseInsensitive = String.CASE_INSENSITIVE_ORDER;
		
		ArrayList<String> _sortFldKeys = new ArrayList<String>();
		
		public MetadataComparator() {
			// For this comparator to compare based on metadata
			// comparisons, user later needs to call addMetadataSortKey().
		}
		
		public MetadataComparator(String sortFld1Key) {
			_sortFldKeys.add(sortFld1Key);
		}
		
		/****************************************************
		 * Methods inner class MetadataComparator
		 *****************************************************/
		
		
		/** Adds new metadata search key at bottom of search key hierarchy
		 * @param newKey
		 */
		
		public void addMetadataSortKey (String newKey) {
			_sortFldKeys.add(newKey);
		}


		/** Adds multiple sorted keys in equals() order to bottom of search key hierarchy
		 * @param newKeys
		 */
		
		public void addMetadataSortKey (Collection<String> newKeys) {
			_sortFldKeys.addAll(newKeys);
		}
		
		/** Replaces given oldKey with newKey in the metadata key search hierarchy.
		 * If oldKey is not present in the existing sort hierarchy keys, just
		 * add oldKey to the bottom of the hierarchy.
		 * 
		 * @param oldKey
		 * @param newKey
		 */
		
		public void replaceMetadataSortKey (String oldKey, String newKey) {
			
			int place = _sortFldKeys.indexOf(oldKey);
			
			if (place == Const.NOT_FOUND)
				_sortFldKeys.add(newKey);
			else
				_sortFldKeys.set(place, newKey);
		}
		
		public void clearMetadataSortKeys () {
			_sortFldKeys.clear();
		}
		
		/** Remove a metadata sort key from the sort hierarchy 
		 * @param keyToDiscard
		 */
		
		public void removeMetadataSortKey (String keyToDiscard) {

			int place = _sortFldKeys.indexOf(keyToDiscard);
			
			if (place == Const.NOT_FOUND)
				return;
			else
				_sortFldKeys.remove(place);
		}
		
		/**
		 * Workhorse: compare two PhotoSpreadObject instances
		 * (or subclasses of it). First comparison is by 
		 * the metadata value of _sortFld1Key. If those values
		 * are equal, values for _sortFld2Key are compared, etc.
		 * down one more level. 
		 * 
		 * @param PhotoSpreadObject o1
		 * @param PhotoSpreadObject o2
		 * @return Negative number for o1 < o2; 0 for equal, positive number for o1 > o2
		 */
		public int compare(PhotoSpreadObject o1, PhotoSpreadObject o2) {

			if (_sortFldKeys.isEmpty())
				return o1.getObjectID().compareTo(o2.getObjectID());
			
			int res;
			String searchKey;
			String metadataO1;
			String metadataO2;
			
			Iterator<String> it = _sortFldKeys.iterator();
			
			while (it.hasNext()) {
				
				searchKey = it.next();
				metadataO1 = o1.getMetaData(searchKey);
				metadataO2 = o2.getMetaData(searchKey);

				// Compare the objs by this metadata field. Note
				// that getMetaData() above returns the string 
				// Const.NULL_VALUE_STRING. 
				// if a requested metadata field is absent. We use the
				// String class' built-in case insensitive comparator:
				
				if (metadataO1.equalsIgnoreCase(Const.NULL_VALUE_STRING))
					
					if (metadataO2.equalsIgnoreCase(Const.NULL_VALUE_STRING))
						
						// Neither O1 nor O2 has a value for the metadata attribute:
						return o1.getObjectID().compareTo(o2.getObjectID());
					else
						
						// O1 does not have a value for the metadata attribute,
						// but O2 does. We 'prefer' O2: O1 > O2, i.e. O2 orders
						// earlier than O1:
						return Const.BIGGER;
				else
					
					if (metadataO2.equalsIgnoreCase(Const.NULL_VALUE_STRING))
						
						// O1 has a value for the metadata attribute, but O2
						// does not. We prefer O1: O1 < O2, i.e. O2 orders later
						// than O1:
						
						return Const.SMALLER;
				
					else
					
						// Both O1 and O2 have values for the metadata
						// attributes. Compare the two values:
						
						res = _stringComparatorCaseInsensitive.compare(metadataO1, metadataO2);
				
				// Normalize return vals: -1 for less-than, +1 for greater-than.
				// This way caller can use Const.BIGGER and Const.SMALLER on the
				// return. (I dropped that nicety to gain speed during big sorts):
				
				// if (res < 0) res = -1;
				// if (res > 0) res = 1;
				
				if (res != Const.EQUAL) return res;
				
				// Objects are equal by this current metadata key's value. Loop
			}
				
			// Objects are equal by all of the search keys. So they are EQUAL:
			
			return Const.EQUAL;
		}
	}
}
