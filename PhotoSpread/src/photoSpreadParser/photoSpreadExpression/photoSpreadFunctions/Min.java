package photoSpreadParser.photoSpreadExpression.photoSpreadFunctions;

import photoSpread.PhotoSpreadException.FormulaError;
import photoSpreadObjects.PhotoSpreadDoubleObject;
import photoSpreadObjects.PhotoSpreadObject;
import photoSpreadParser.photoSpreadExpression.PhotoSpreadEvaluatable;
import photoSpreadParser.photoSpreadExpression.PhotoSpreadFormulaExpression;
import photoSpreadTable.PhotoSpreadCell;
import photoSpreadUtilities.PhotoSpreadObjIndexerFinder;
import photoSpreadUtilities.TreeSetRandomSubsetIterable;

public class Min<A extends PhotoSpreadFormulaExpression> extends
		PhotoSpreadFunction implements PhotoSpreadEvaluatable {

	public Min() {
		this("Min");
	}

	public Min(String _functionName) {
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
		
		// Have all the arguments to this call to 'Min'
		// computed. We may get multiple results for each
		// argument. This conceptual ArrayList<ArrayList<PhotoSpreadxxx>>
		// is encapsulated in the ArgEvalResults class.
		// The valueOfArgs() method returns one of that
		// class' instances, filled with results:
		
		computedArgs = valueOfArgs();

		// Now find the Min:

		AllArgEvalResults.FlattenedArgsIterator argResultsIt =
			computedArgs.flattenedArgsIterator();
		
		PhotoSpreadObject obj = null;
		Double num = 0.0;
		Double currMin = Double.POSITIVE_INFINITY;
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
			currMin = Math.min(currMin, num);
		}
		return new PhotoSpreadDoubleObject(_cell, currMin);
	}
	
	private void throwFormulaError(PhotoSpreadObject obj) throws FormulaError {
				throw new FormulaError(
						"Arguments to Min() function must be numbers. Problem: '" +
						obj +
						"'.");
	}
}
