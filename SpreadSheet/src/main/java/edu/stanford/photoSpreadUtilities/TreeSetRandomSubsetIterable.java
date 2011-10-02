/**
 * 
 */
package edu.stanford.photoSpreadUtilities;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import edu.stanford.photoSpread.PhotoSpreadException;
import edu.stanford.photoSpread.PhotoSpreadException.IllegalArgumentException;
import edu.stanford.photoSpreadObjects.ObjectIndexerFinder;
import edu.stanford.photoSpreadParser.photoSpreadExpression.PhotoSpreadComputable;
import edu.stanford.photoSpreadTable.PhotoSpreadTable;

/**
 * @author paepcke
 * 
 * Extends TreeSetRandomSubsetIterable to provide two iterator facilities.
 * First, an iterator that starts at the nth position
 * inclusive in the TreeSetRandomSubsetIterable and continues from there.
 * 
 * Second, an iterator that starts a the nth position,
 * inclusive, but only provides elements to the mth 
 * position, exclusive.
 * 
 * @param <E>: The type object the tree will hold.
 *
 */
public class TreeSetRandomSubsetIterable <E> 
extends TreeSet<E> implements Iterable<E> {

	private static final long serialVersionUID = 1L;

	private ObjectIndexerFinder<E> _indexer = null;

	/****************************************************
	 * Constructors
	 * @param addIndex TODO
	 *****************************************************/

	public TreeSetRandomSubsetIterable() {
		super();
	}

	public TreeSetRandomSubsetIterable(E singleInitialElement) {
		super();
		add(singleInitialElement);
	}

	public TreeSetRandomSubsetIterable(E[] initialElements) {
		super();
		for (E item : initialElements)
			add(item);
	}

	public TreeSetRandomSubsetIterable(Comparator<E> comp) {
		super(comp);
	}

	/****************************************************
	 * Methods
	 *****************************************************/

	public RandomSubsetIterator iterator (int startPosIncl) throws IllegalArgumentException {
		return new RandomSubsetIterator(startPosIncl, this);
	}

	public RandomSubsetIterator iterator (int startPosIncl, int endPosExcl) throws IllegalArgumentException {
		return new RandomSubsetIterator(startPosIncl, endPosExcl, this);
	}

	public void setIndexer(ObjectIndexerFinder<E> indexer) {
		this._indexer = indexer;
	}

	public ObjectIndexerFinder<?> getIndexer() {
		return _indexer;
	}
	
	public boolean hasIndexer () {
		return (_indexer != null);
	}

	@Override
	public boolean add(E obj) {

		if (_indexer != null) {
			try {
				_indexer.add((PhotoSpreadComputable)obj);
			} catch (IllegalArgumentException e) {
				Misc.showErrorMsgAndStackTrace(e, "");
				//e.printStackTrace();
			}
		}
		return super.add((E) obj);
	}

	/**
	 * Add the given object 'obj' to this collection.
	 * BUT: if the uniqueObjs contain an object that
	 * wraps the same item (e.g. string, double), then
	 * add that original object to this collection instead.
	 * @param obj
	 * @param uniqueObjs
	 * @return
	 */
	public E add(E obj, TreeSetRandomSubsetIterable<E> uniqueObjs) {
		
		E referenceObj = uniqueObjs.containsObject(obj);

		if (referenceObj == null) {
			add(obj);
			return obj;
		}
		else {
			add(referenceObj);
			return referenceObj;
		}
	}
	
/*	NOTE: We dont' need to override addAll(), because the superclass
 *        simply calls add() anyway. Indexing is done there! We don't
 *        want to do it twice.
 
    @Override
	public boolean addAll(Collection objs) {

		if (_indexer != null) {
			try {
				_indexer.addAll((Collection<PhotoSpreadComputable>)objs);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (ClassCastException e) {
				e.printStackTrace();
			}
		}
		return super.addAll(objs);
	}
*/
	
	public void addAll(Collection<E> objs, TreeSetRandomSubsetIterable<E> uniqueObjs) {
		
		for (E obj : objs) {
			add(obj, uniqueObjs);
		}
	}

	@Override
	public boolean remove (Object obj) {

		if (_indexer != null) {
			try {
				_indexer.remove((PhotoSpreadComputable)obj);
			} catch (IllegalArgumentException e) {
				Misc.showErrorMsgAndStackTrace(e, "");
				//e.printStackTrace();
			}
		}
		return super.remove(obj);
	}

	@Override
	public void clear () {
		if (_indexer != null)
			_indexer.clear();
		super.clear();
	}

	/**
	 * Given a string, find the object in this
	 * collection that wraps that string. 
	 * @param <T> is the type of the wrapping object that will be returned.
	 * @param str
	 * @return The object that wraps the given string.
	 */
	@SuppressWarnings("unchecked")
	public <T> T find (String str) {
		if (_indexer != null)
			return (T) _indexer.find(str);
		else
			return null;
	}

	/**
	 * Given a Java.IO.File object, find the object in this
	 * collection that wraps a file of the same pathname.
	 * @param <T> is the type of the wrapping object that will be returned.
	 * @param fileObj is the file object for which we want to find the wrapper.
	 * @return The <T> object that wraps an object with the same file path
	 *         as the given File object.
	 */
	@SuppressWarnings("unchecked")
	public <T> T find (File fileObj) {
		if (_indexer != null)
			return (T) _indexer.find(fileObj);
		else
			return null;
	}

	/**
	 * Given a Double, find the object in this
	 * collection that wraps that Double number.
	 * @param <T> is the type of the wrapping object that will be returned.
	 * @param num is the number whose wrapping object we want to find.
	 * @return The object that wraps the given Double.
	 */
	@SuppressWarnings("unchecked")
	public <T> T find (Double num) {
		if (_indexer != null)
			return (T) _indexer.find(num);
		else
			return null;
	}

	/**
	 * Given a Table object, find the object in this
	 * collection that wraps that Table object.
	 * @param <T> is the type of the wrapping object that will be returned.
	 * @param tableObj is the table whose wrapping object we want to find.
	 * @return The object that wraps the given table.
	 */
	@SuppressWarnings("unchecked")
	public <T> T find (PhotoSpreadTable tableObj) {
		if (_indexer != null)
			return (T) _indexer.find(tableObj);
		else
			return null;
	}

	public E containsObject (E objToCheckFor) {
		if (_indexer != null)
			return _indexer.containsValue(objToCheckFor);
		return null;
	}

	/****************************************************
	 * Inner) Classes
	 *****************************************************/

	class RandomSubsetIterator implements Iterator<E> {

		TreeSetRandomSubsetIterable<E> _TreeSetRandomSubsetIterableInstance;
		java.util.Iterator<E> _supersIterator;
		int _startPosIncl = -1;
		int _endPosExcl  = -1;
		int _currIndex = 0;

		/**
		 * Given a start position <i>n</i>, return an iterator
		 * whose first element is the TreeMap's <i>nth</i> element.
		 * 
		 *  We use the same iterator that TreeSetRandomSubsetIterable provides. No
		 *  copying of element.
		 *  
		 * @param startPos
		 * @return the Iterator
		 * @throws IllegalArgumentException 
		 */

		public RandomSubsetIterator (
				int startPosIncl, 
				TreeSetRandomSubsetIterable<E> TreeSetRandomSubsetIterableInstance) 
		throws IllegalArgumentException {

			if ((startPosIncl < 0) ||
					(startPosIncl >= TreeSetRandomSubsetIterableInstance.size()))

				throw new PhotoSpreadException.IllegalArgumentException(
						"Random access iterator requires positive start positions that " +
						"are less than the size of the collection (which is " +
						TreeSetRandomSubsetIterableInstance.size() +
						"). But " + 
						startPosIncl + " (start position) was passed in.");

			_startPosIncl = startPosIncl;
			_TreeSetRandomSubsetIterableInstance = TreeSetRandomSubsetIterableInstance;
			_supersIterator = (java.util.Iterator<E>) _TreeSetRandomSubsetIterableInstance.iterator();


			for (int i=0; i<_startPosIncl; i++) {
				if (_supersIterator.hasNext())
					_supersIterator.next();
			}
		}

		public RandomSubsetIterator (
				int startPosIncl, 
				int endPosExcl, 
				TreeSetRandomSubsetIterable<E> TreeSetRandomSubsetIterableInstance) 
		throws IllegalArgumentException {

			if ((startPosIncl < 0) ||
					(endPosExcl < 0) ||
					(startPosIncl >= TreeSetRandomSubsetIterableInstance.size()))

				throw new PhotoSpreadException.IllegalArgumentException(
						"Random access iterator requires positive start/end positions \n" +
						"that are less than the collection size (which is " +
						TreeSetRandomSubsetIterableInstance.size() +
						"). \nBut '" + 
						startPosIncl + "' (start position) and '" +
						endPosExcl + 
				"' (end position) were passed in.");

			_startPosIncl = startPosIncl;
			_endPosExcl = endPosExcl;
			_TreeSetRandomSubsetIterableInstance = TreeSetRandomSubsetIterableInstance;			
			_supersIterator = (java.util.Iterator<E>) _TreeSetRandomSubsetIterableInstance.iterator();

			for (int i=0; i<_startPosIncl; i++) {
				_supersIterator.next();
			}
			_currIndex = _startPosIncl;
		}

		/* 
		 * Version of hasNext() that honors the endPosExcl setting.
		 * 
		 * @see java.util.Iterator#hasNext()
		 */

		public boolean hasNext() {

			if (_endPosExcl == -1)
				// No end position. Iterator is to run to end
				// of the TreeSetRandomSubsetIterable's elements:
				return _supersIterator.hasNext();

			if (_currIndex >= _endPosExcl)
				return false;
			else
				return true;
		}


		/**
		 * Next() method that stops as instructed in the constructor.
		 * @return 
		 * @see java.util.Iterator#next()
		 */
		public E next()  {

			if (hasNext()) {
				_currIndex++;
				return _supersIterator.next();
			}
			else
				throw new java.util.NoSuchElementException(
						"Called next() on iterator with ceiling " +
						_endPosExcl +
						" (exclusive) and collection size of " +
						_TreeSetRandomSubsetIterableInstance.size() +
				".");
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			throw new UnsupportedOperationException("Remove is not implemented for this iterator.");

		}
	}
}
