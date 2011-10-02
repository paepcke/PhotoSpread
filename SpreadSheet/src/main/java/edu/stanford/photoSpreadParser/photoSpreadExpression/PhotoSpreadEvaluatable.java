/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.photoSpreadParser.photoSpreadExpression;

import edu.stanford.photoSpread.PhotoSpreadException;
import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;
import edu.stanford.photoSpreadUtilities.TreeSetRandomSubsetIterable;

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
