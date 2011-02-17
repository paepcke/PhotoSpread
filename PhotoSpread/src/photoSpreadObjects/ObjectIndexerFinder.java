/**
 * 
 */
package photoSpreadObjects;

import java.io.File;
import java.util.Collection;

import photoSpread.PhotoSpreadException.IllegalArgumentException;
import photoSpreadParser.photoSpreadExpression.PhotoSpreadComputable;
import photoSpreadTable.PhotoSpreadTable;

/**
 * @author paepcke
 *
 */
public interface ObjectIndexerFinder<T> {

	public boolean add(PhotoSpreadComputable obj) throws IllegalArgumentException;
	public boolean addAll(Collection<PhotoSpreadComputable> obj) throws IllegalArgumentException;
	public boolean remove (PhotoSpreadComputable obj) throws IllegalArgumentException;
	public void	   clear ();
	
	public PhotoSpreadStringObject find (String strContent);
	public PhotoSpreadDoubleObject find (Double doubleContent);
	public PhotoSpreadFileObject find (File fileObj);
	public PhotoSpreadTableObject find (PhotoSpreadTable psTableObj);
	
	public boolean isEmpty();
	public int size();
	
	public T containsValue(T obj);
}
