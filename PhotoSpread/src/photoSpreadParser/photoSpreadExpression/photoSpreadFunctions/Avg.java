/**
 * 
 */
package photoSpreadParser.photoSpreadExpression.photoSpreadFunctions;


import photoSpread.PhotoSpreadException;
import photoSpread.PhotoSpreadException.FormulaError;
import photoSpreadObjects.PhotoSpreadDoubleObject;
import photoSpreadObjects.PhotoSpreadObject;
import photoSpreadParser.photoSpreadExpression.PhotoSpreadEvaluatable;
import photoSpreadParser.photoSpreadExpression.PhotoSpreadFormulaExpression;
import photoSpreadTable.PhotoSpreadCell;
import photoSpreadUtilities.PhotoSpreadObjIndexerFinder;
import photoSpreadUtilities.TreeSetRandomSubsetIterable;

/**
 * @author paepcke
 *
 */
public class Avg<A extends PhotoSpreadFormulaExpression> 
extends PhotoSpreadFunction
implements PhotoSpreadEvaluatable {
	
	public Avg () {
		this("Avg");
	}
	
	public Avg (String funcName) {
		super(funcName);
		_numOfTerms = 0;
	}
	
	public Double toDouble() throws FormulaError {
		return this.valueOf().toDouble();
	}

	public TreeSetRandomSubsetIterable<PhotoSpreadObject> evaluate(
			PhotoSpreadCell cell) throws FormulaError {
		
		_cell = cell;
		TreeSetRandomSubsetIterable<PhotoSpreadObject> res = 
			new TreeSetRandomSubsetIterable<PhotoSpreadObject>();
		res.setIndexer(new PhotoSpreadObjIndexerFinder());
		res.add(this.valueOf());
		return res;
	}

	@Override
	public PhotoSpreadDoubleObject valueOf() throws FormulaError {

		Double theSum = 0.0;
		AllArgEvalResults computedArgs;
		
		// Have all the arguments to this call to 'sum'
		// computed. We may get multiple results for each
		// argument. This conceptual ArrayList<ArrayList<PhotoSpreadDoubleConstant>>
		// is encapsulated in the ArgEvalResults class.
		// The valueOfArgs() method returns one of that
		// class' instances, filled with results:
		
		computedArgs = valueOfArgs();
		
		// Now just add everything together:
		
		AllArgEvalResults.FlattenedArgsIterator argResultsIt =
			computedArgs.flattenedArgsIterator();

		Double argValue = null;
		while (argResultsIt.hasNext()) {
			try {
				argValue = ((PhotoSpreadObject) argResultsIt.next()).toDouble();
				// We will get values of "null" from objects whose
				// attribute that is being processed here has never
				// been set:
				if (argValue == null) continue;
				theSum += argValue;
			
			} catch (ClassCastException e) {
				throw new PhotoSpreadException.FormulaError(
						"In function '" +
						getFunctionName() +
						"' the argument '" +
						argResultsIt.getMostRecentlyFedOut() +
						"' cannot be converted to a number.");
			}
			
			_numOfTerms++;
		}
		return new PhotoSpreadDoubleObject(_cell, theSum/(double)_numOfTerms);		
	}
}
