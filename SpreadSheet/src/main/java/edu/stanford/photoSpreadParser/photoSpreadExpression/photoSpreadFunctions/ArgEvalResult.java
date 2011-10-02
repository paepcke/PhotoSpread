package edu.stanford.photoSpreadParser.photoSpreadExpression.photoSpreadFunctions;

import java.util.ArrayList;
import java.util.Iterator;

import edu.stanford.photoSpreadUtilities.TreeSetRandomSubsetIterable;

public class ArgEvalResult<T> {
	
	public ArrayList<T> _argResultArray = new ArrayList<T>();

	/****************************************************
	 * Constructors for ArgEvalResult
	 *****************************************************/

	public ArgEvalResult () {
	}

	public ArgEvalResult (T initialValue) {
		_argResultArray.add(initialValue);
	}

	public ArgEvalResult (ArrayList<T> initialValues) {
		_argResultArray.addAll(initialValues);
	}

	public ArgEvalResult (TreeSetRandomSubsetIterable<T> initialValues) {
		addAll(initialValues);
	}
	
	public ArgEvalResult (T[] initialValues) {
		addAll(initialValues);
	}
	
	/****************************************************
	 * Methods for ArgEvalResult
	 *****************************************************/


	public void add (T newResult) {
		_argResultArray.add(newResult);
	}
	
	public void addAll (ArrayList<T> results) {
		_argResultArray.addAll(results);
	}
	
	@SuppressWarnings("unchecked")
	public void addAll (TreeSetRandomSubsetIterable<T> results) {
		for (T obj : ((T[]) results.toArray()))
			_argResultArray.add(obj);			
	}
	
	public void addAll (T[] results) {
		for (T obj : results)
			_argResultArray.add(obj);
	}
	
	public void addAll (ArgEvalResult<T> results) {
		// Recursive: The passed-in results is a different
		// object than this instance:
		_argResultArray.addAll(results._argResultArray);
	}
	
	public boolean isEmpty() {
		return _argResultArray.isEmpty();
	}
	
	public ArrayList<T> getAll() {
		return _argResultArray;
	}
	
	@SuppressWarnings({ "unchecked", "hiding" })
	public <T> Iterator<T> iterator() {
		return ((ArrayList<T>) _argResultArray).iterator();
	}
}
