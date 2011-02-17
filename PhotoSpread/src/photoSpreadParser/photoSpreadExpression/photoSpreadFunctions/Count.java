package photoSpreadParser.photoSpreadExpression.photoSpreadFunctions;

import java.util.NoSuchElementException;

import photoSpread.PhotoSpreadException.FormulaError;
import photoSpreadObjects.PhotoSpreadDoubleObject;
import photoSpreadObjects.PhotoSpreadObject;
import photoSpreadParser.photoSpreadExpression.PhotoSpreadEvaluatable;
import photoSpreadParser.photoSpreadExpression.PhotoSpreadFormulaExpression;
import photoSpreadTable.PhotoSpreadCell;
import photoSpreadUtilities.PhotoSpreadObjIndexerFinder;
import photoSpreadUtilities.TreeSetRandomSubsetIterable;

public class Count<A extends PhotoSpreadFormulaExpression> extends
		PhotoSpreadFunction implements PhotoSpreadEvaluatable {
	
	public Count() {
		this("Count");
	}

	public Count(String _functionName) {
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
		
		// Have all the arguments to this call to 'Count'
		// computed. We may get multiple results for each
		// argument. This conceptual ArrayList<ArrayList<PhotoSpreadxxx>>
		// is encapsulated in the ArgEvalResults class.
		// The valueOfArgs() method returns one of that
		// class' instances, filled with results:
		
		computedArgs = valueOfArgs();

		// Now count the number of arguments:

		AllArgEvalResults.FlattenedArgsIterator argResultsIt =
			computedArgs.flattenedArgsIterator();
		
		// We need to pull the args out from the above iterator
		// one by one and count them, b/c the iterator does the
		// flattening on the fly:
		double numObjs = 0;
		while (true) {
			try {
				argResultsIt.next();
				numObjs++;
			} catch (NoSuchElementException e) {
				break;
			}
		}
		return new PhotoSpreadDoubleObject(_cell, numObjs);
	}
}
