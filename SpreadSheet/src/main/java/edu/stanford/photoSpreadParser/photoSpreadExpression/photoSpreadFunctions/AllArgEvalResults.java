/**
 * 
 */
package edu.stanford.photoSpreadParser.photoSpreadExpression.photoSpreadFunctions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author paepcke
 *
 */
public class AllArgEvalResults implements Iterable<ArgEvalResult<? extends Object>> {

	private ArrayList<ArgEvalResult<? extends Object>> _allArgResults =
		new ArrayList<ArgEvalResult<? extends Object>>();
	private ArgEvalResult<? extends Object> _oneArgResults;
	
	protected AllArgEvalResults () {
	}
	
	public Iterator<ArgEvalResult<? extends Object>> iterator () {
		return _allArgResults.iterator();
	}

	/****************************************************
	 * Inner-Inner Class FlattenedArgResultsIterator
	 *****************************************************/

	/**
	 * Given a AllArgEvalResults, return an iterator
	 * that feeds out a flat list of all results of
	 * evaluating all the arguments of a function.
	 * 
	 * @author paepcke
	 *
	 */
	public class FlattenedArgsIterator {
		
		ArgEvalResult<?> _currResultArray;
		Iterator<ArgEvalResult<?>> _argResultsArraysIterator;
		Iterator<?>  _argResultArrayIterator;
		AllArgEvalResults _allResults;
		
		Object _mostRecentFedOut = null;
		
		public FlattenedArgsIterator (AllArgEvalResults resultsStructure) {
			_allResults = resultsStructure;
			_argResultsArraysIterator = resultsStructure.iterator();
			switchToNextArgResults ();
		}

		/****************************************************
		 * Methods for FlattenedArgsIterator
		 *****************************************************/
		
		public Object getMostRecentlyFedOut () {
			return _mostRecentFedOut;
		}

		/**
		 * All results of one function argument have been fed out.
		 * Get the results of the next argument and feed it out.
		 * Note that the new result list that is popped off
		 * _argResultsArraysIterator may be empty, signifying 
		 * an empty set! So clients of this class, who receive
		 * this empty set later by calling next() on this instance,
		 * cannot simply call .next() on that result. They must
		 * check for null. 
		 * 
		 * @return True if any results are left to be fed out;
		 * false otherwise. 
		 */
		protected boolean switchToNextArgResults () {
			
			while(_argResultsArraysIterator.hasNext()) {
				_currResultArray = _argResultsArraysIterator.next();
				_argResultArrayIterator = _currResultArray.iterator();
				return true;
			}
			return false;
		}

		/**
		 * Return the next result from the flattened list
		 * of sub-results. A return of null means that we
		 * found a result set that was empty. That's a 
		 * legitimate result, and callers must be prepared
		 * to receive it.
		 * @return Object of type <T>, or null.
		 */
		@SuppressWarnings("unchecked")
		public <T> T next() {
			if (_argResultArrayIterator.hasNext()) {
				_mostRecentFedOut = _argResultArrayIterator.next();
				return (T) _mostRecentFedOut;
			}
			else {
				boolean haveMoreResults = switchToNextArgResults();
				if (haveMoreResults) {
					if (!_argResultArrayIterator.hasNext()) {
						// Empty-set result. Next call to this
						// next() method will move on to the next
						// result set in this 'else' clause. We
						// return null to communicate this empty set:
						return null;
					}
					_mostRecentFedOut = _argResultArrayIterator.next();
					return (T) _mostRecentFedOut;
				}
				// We should never get here:
				throw new NoSuchElementException("No more results available.");
			}
		}
		
		public boolean hasNext() {
			return (_argResultArrayIterator.hasNext() || _argResultsArraysIterator.hasNext());
					
		}
	}

	/****************************************************
	 * Methods for ArgEvalResults
	 *****************************************************/

/*	public void addOneArgResult (FunctionResultable res) {
		_oneArgResults.add(res);
	}
*/	
	public void addAllOneArgResults (ArgEvalResult<?> oneArgResults) {
		_allArgResults.add(oneArgResults);
	}
/*
	public void addAllOneArgResults (ArrayList<FunctionResultable> oneArgResults) {
		_oneArgResults.addAll(oneArgResults);
	}
*/
	@SuppressWarnings("unchecked")
	public <T> ArgEvalResult<T> newResultSet () {
		if ((_oneArgResults != null) && !_oneArgResults.isEmpty())
			_allArgResults.add(_oneArgResults);
		_oneArgResults = new ArgEvalResult<T>();
		return (ArgEvalResult<T>) _oneArgResults;
	}

	/**
	 * Iterator that feeds out one argument result set at 
	 * a time. That is, each fed item is a set of FunctionResultable
	 * packaged in an ArgEvalResult
	 * @return Iterator for feeding out one result set at a time.
	 */
	//public <T> Iterator<ArgEvalResult<T>> argsResultsIterator() {
	@SuppressWarnings("unchecked")
	public <T> Iterator<T> argsResultsIterator() {
		return (Iterator<T>) _allArgResults.iterator();
	}

	/**
	 * Iterator that feeds out a flat list of all results of
	 * evaluating all the arguments of a function. Use if
	 * all result sets from evaluating each of a function's
	 * arguments are to be lumped into one soup for further
	 * processing. (e.g. Union(arg1, arge2, arg3, ...)
	 *  
	 * @return Iterator that feeds out one FunctionResultable
	 * until all results of evaluating all of a function's 
	 * arguments have been provided.
	 */
	public FlattenedArgsIterator flattenedArgsIterator () {
		return new FlattenedArgsIterator(this);
	}
}

