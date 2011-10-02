/**
 * 
 */
package edu.stanford.photoSpreadTable;

import java.util.HashMap;

import edu.stanford.photoSpreadUtilities.HashCodeUtil;

/**
 * @author paepcke
 *
 * Map to map row/column integer pairs to 
 * any desired object. The class is type
 * parameterized, so any objects can be
 * stored there, and retrieved. 
 * 
 * We could implement the Map interface, but its
 * keys are all individual objects. This would
 * force callers always to package the row/column
 * integers into some kind of data structure. 
 * 
 * We still provide all the methods of the Map
 * interface, but whenever a key is involved, we
 * take a row and a column int. These methods are
 * <pre>put(), get(), containsKey(),</pre> and
 * <pre>containsKey()</pre>
 * 
 *  This class is type-parameterized, so it can be used
 *  to hash objects of any type.
 *  
 * @param <T>
 */

public class TableCellObjectsMap<T> extends HashMap<Integer, T> {

	// private HashMap<RowColumnKey, T> theMap = new HashMap<RowColumnKey, T>(); 

	private static final long serialVersionUID = 1L;

	/****************************************************
	 * Constructor(s)
	 *****************************************************/

	public TableCellObjectsMap () {

	}

	/****************************************************
	 * Private (Inner) Classes
	 *****************************************************/

	protected final class RowColumnKey {

		private static final int _ROW = 0;
		private static final int _COL = 1;
		private int[] _key = {0, 0};

		RowColumnKey (int rowNum, int colNum) {
			_key[_ROW] = rowNum;
			_key[_COL] = colNum;
		}

		int row () {
			return _key[_ROW];
		}

		int col() {
			return _key[_COL];
		}

		public int hashCode(){
			
			int result = HashCodeUtil.SEED;
			//collect the contributions of various fields
			result = HashCodeUtil.hash(result, _key[_ROW]);
			result = HashCodeUtil.hash(result, _key[_COL]);
			return result;
		}		
	}

	/****************************************************
	 * Methods
	 *****************************************************/

	// Only four methods need to be shadowed. All use the
	// RowColumnKey class' hashCode() function to generate
	// hash keys:
	
	public boolean containsKey (int row, int col) {
		return super.containsKey(new RowColumnKey(row, col).hashCode());
	}

	public T get(int row, int col) {
		return super.get(new RowColumnKey(row, col).hashCode());
	}
	
	public T put (int row, int col, T obj) {
		return super.put(new RowColumnKey(row, col).hashCode(), obj);
	}
	
	public T remove(int row, int col) {
		return super.remove(new RowColumnKey(row, col).hashCode());
	}

}
