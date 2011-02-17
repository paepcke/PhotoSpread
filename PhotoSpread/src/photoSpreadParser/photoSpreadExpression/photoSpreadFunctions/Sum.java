package photoSpreadParser.photoSpreadExpression.photoSpreadFunctions;


import photoSpread.PhotoSpreadException;
import photoSpread.PhotoSpreadException.FormulaError;
import photoSpreadObjects.PhotoSpreadDoubleObject;
import photoSpreadObjects.PhotoSpreadObject;
import photoSpreadParser.photoSpreadExpression.PhotoSpreadEvaluatable;
import photoSpreadTable.PhotoSpreadCell;
import photoSpreadUtilities.PhotoSpreadObjIndexerFinder;
import photoSpreadUtilities.TreeSetRandomSubsetIterable;

public class Sum extends PhotoSpreadFunction 
implements PhotoSpreadEvaluatable {
	
	public Sum() {
		this("Sum");
	}
	
	public Sum (String funcName) {
		super(funcName);
	}
	
	public Double toDouble() throws FormulaError {
		return this.valueOf().toDouble();
	}

	public TreeSetRandomSubsetIterable<PhotoSpreadObject> evaluate(
			PhotoSpreadCell cell) throws FormulaError {
		
		TreeSetRandomSubsetIterable<PhotoSpreadObject> res;

		_cell = cell;
		
		res = new TreeSetRandomSubsetIterable<PhotoSpreadObject>();
		res.setIndexer(new PhotoSpreadObjIndexerFinder());
		res.add(this.valueOf());
		return res;
	}

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

		while (argResultsIt.hasNext()) {
			try {
				PhotoSpreadObject oneRes = ((PhotoSpreadObject) argResultsIt.next());
				// Argument results may be empty sets, in
				// which case next() returns null. Just skip
				// over such results:
				if (oneRes == null) continue;
				theSum += oneRes.toDouble();
			} catch (ClassCastException e) {
				throw new PhotoSpreadException.FormulaError(
						"In function '" +
						getFunctionName() +
						"' the argument '" +
						argResultsIt.getMostRecentlyFedOut() +
						"' cannot be converted to a number.");
			}
		}
		
		return new PhotoSpreadDoubleObject(_cell, theSum);
	}
}
