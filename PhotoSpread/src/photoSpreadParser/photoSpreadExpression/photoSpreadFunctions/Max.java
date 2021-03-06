package photoSpreadParser.photoSpreadExpression.photoSpreadFunctions;

import photoSpread.PhotoSpreadException.FormulaError;
import photoSpreadObjects.PhotoSpreadDoubleObject;
import photoSpreadObjects.PhotoSpreadObject;
import photoSpreadParser.photoSpreadExpression.PhotoSpreadEvaluatable;
import photoSpreadParser.photoSpreadExpression.PhotoSpreadFormulaExpression;
import photoSpreadTable.PhotoSpreadCell;
import photoSpreadUtilities.PhotoSpreadObjIndexerFinder;
import photoSpreadUtilities.TreeSetRandomSubsetIterable;

public class Max<A extends PhotoSpreadFormulaExpression> 
extends PhotoSpreadFunction implements PhotoSpreadEvaluatable {

	public Max() {
		this("Max");
	}

	public Max(String _functionName) {
        super(_functionName);
    }
	
	@Override
	public TreeSetRandomSubsetIterable<PhotoSpreadObject> evaluate(
			PhotoSpreadCell cell) throws FormulaError {
		
		TreeSetRandomSubsetIterable<PhotoSpreadObject> res =
			new TreeSetRandomSubsetIterable<PhotoSpreadObject>();
		res.setIndexer(new PhotoSpreadObjIndexerFinder());
		res.add(this.valueOf());
		return res;
	}

	@Override
	public PhotoSpreadDoubleObject valueOf() throws FormulaError {
		
		AllArgEvalResults computedArgs;
		
		// Have all the arguments to this call to 'Max'
		// computed. We may get multiple results for each
		// argument. This conceptual ArrayList<ArrayList<PhotoSpreadxxx>>
		// is encapsulated in the ArgEvalResults class.
		// The valueOfArgs() method returns one of that
		// class' instances, filled with results:
		
		computedArgs = valueOfArgs();

		// Now find the Max:

		AllArgEvalResults.FlattenedArgsIterator argResultsIt =
			computedArgs.flattenedArgsIterator();
		
		PhotoSpreadObject obj = null;
		Double num = 0.0;
		Double currMax = Double.NEGATIVE_INFINITY;
		while (argResultsIt.hasNext()) {
			try {
				num = ((PhotoSpreadObject) argResultsIt.next()).toDouble();
			} catch (NumberFormatException e) {
				// Ignore non-numeric values, most
				// prominently: null:
				continue;
			} catch (ClassCastException e) {
				throwFormulaError(obj);
			}
			currMax = Math.max(currMax, num);
		}
		return new PhotoSpreadDoubleObject(_cell, currMax);
	}
	
	private void throwFormulaError(PhotoSpreadObject obj) throws FormulaError {
				throw new FormulaError(
						"Arguments to Max() function must be numbers. Problem: '" +
						obj +
						"'.");
	}
}
