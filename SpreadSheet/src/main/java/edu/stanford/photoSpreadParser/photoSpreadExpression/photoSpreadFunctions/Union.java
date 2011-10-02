/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.photoSpreadParser.photoSpreadExpression.photoSpreadFunctions;


import edu.stanford.photoSpread.PhotoSpreadException;
import edu.stanford.photoSpread.PhotoSpreadException.FormulaError;
import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadParser.photoSpreadExpression.PhotoSpreadEvaluatable;
import edu.stanford.photoSpreadParser.photoSpreadExpression.PhotoSpreadFormulaExpression;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;
import edu.stanford.photoSpreadUtilities.PhotoSpreadObjIndexerFinder;
import edu.stanford.photoSpreadUtilities.TreeSetRandomSubsetIterable;

/**
 *
 * @author skandel
 */
public class Union<A extends PhotoSpreadFormulaExpression> extends PhotoSpreadFunction
implements PhotoSpreadEvaluatable {

	public Union() {
		this("Union");
	}

	public Union(String _functionName) {
        super(_functionName);
    }
    
	public TreeSetRandomSubsetIterable<PhotoSpreadObject> valueOf() 
	throws FormulaError {
		
		AllArgEvalResults computedArgs;
		TreeSetRandomSubsetIterable<PhotoSpreadObject> res =
			new TreeSetRandomSubsetIterable<PhotoSpreadObject>();
			res.setIndexer(new PhotoSpreadObjIndexerFinder());

		
		// Have all the arguments to this call to 'union'
		// computed. We may get multiple results for each
		// argument. This conceptual ArrayList<ArrayList<PhotoSpreadxxx>>
		// is encapsulated in the ArgEvalResults class.
		// The valueOfArgs() method returns one of that
		// class' instances, filled with results:
		
		computedArgs = valueOfArgs();

		// Now just Union everything together:

		AllArgEvalResults.FlattenedArgsIterator argResultsIt =
			computedArgs.flattenedArgsIterator();
		
		while (argResultsIt.hasNext()) {
			try {
				// Get another result:
				PhotoSpreadObject argRes = argResultsIt.next();
				// Arg results are allowed to be empty sets,
				// in which case AllArgEvalResults.FlattenedArgsIterator
				// returns a null. Skip such empty results:
				if (argRes == null) continue;
				res.add(argRes);

			} catch (ClassCastException e) {
				throw new PhotoSpreadException.FormulaError(
						"In function '" +
						getFunctionName() +
						"' the argument '" +
						argResultsIt.getMostRecentlyFedOut() +
						"' cannot be converted to an object.");
			}
		}
		return res;
	}

	@Override
	public TreeSetRandomSubsetIterable<PhotoSpreadObject> evaluate(
			PhotoSpreadCell cell) throws FormulaError {
		return valueOf();
	}
}
