/**
 * 
 */
package edu.stanford.photoSpreadUtilities;

import java.io.File;

import edu.stanford.photoSpreadTable.PhotoSpreadTable;

/**
 * @author paepcke
 * 
 * Implementing classes contain a set objects
 * that are intended to be unique. Instances of
 * those classes provide search methods that 
 * return null if they do not contain an object
 * that can be retrieved by the a given key.
 * Else those calls return the object.
 * 
 * The intent is for callers to always use the
 * reference objects that are held in the instance
 * of this class. The class PhotoSpreadCell functions
 * as an ObjectUniquenessReference. When, for instance,
 * the Double 11.0 is to be added to a cell that already
 * contains an instance of the PhotoSpreadDoubleObject
 * that corresponds to 11.0, the parsing methods will
 * reuse the existing object. 
 *
 */
public interface ObjectUniquenessReference<T> {
	
	T find(String str);
	T find(Double dbl);
	T find(File fileObj);
	T find(PhotoSpreadTable fileObj);
}
