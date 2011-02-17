/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package photoSpreadParser.photoSpreadExpression;

import photoSpread.PhotoSpreadException;
import photoSpreadObjects.PhotoSpreadObject;
import photoSpreadTable.PhotoSpreadCell;
import photoSpreadUtilities.TreeSetRandomSubsetIterable;

/**
 * Classes that implement <code>PhotoSpreadEvaluatable</code> can
 * all produce a set of <code>PhotoSpreadObject</code> instances
 * when their <code>evaluate()</code> method is called.
 * 
 * Note: the interfaces <code>PhotoSpreadNumericComputable</code>,
 * <code>PhotoSpreadStringComputable</code> are different from
 * <code>PhotoSpreadEvaluatable</code>. They produce not PhotoSpreadObjects,
 * but Java primitive values when their (required) valueOf() methods are called.
 * A single class may implement both Evaluatable and one of the other two.
 * 
 * @author skandel
 */
public interface PhotoSpreadEvaluatable {
    TreeSetRandomSubsetIterable<PhotoSpreadObject> evaluate(PhotoSpreadCell cell)
    throws PhotoSpreadException.FormulaError;
}
