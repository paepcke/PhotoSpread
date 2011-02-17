package photoSpreadParser.photoSpreadExpression;

import photoSpread.PhotoSpreadException.FormulaError;

/**
 * This interface is the parent to all PhotoSpread
 * functions. Each function extends the interface
 * as needed. For example: PhotoSpreadNumericComputable
 * is implemented by functions that generate a Double
 * when invoked.
 * 
 * @author paepcke
 *
 */
public abstract interface PhotoSpreadComputable {
	
	Object valueOf() throws FormulaError;
}
