/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.photoSpreadParser.photoSpreadExpression;

/**
 *
 * @author skandel
 */
abstract public class PhotoSpreadExpression implements PhotoSpreadEvaluatable, PhotoSpreadNormalizable {

    PhotoSpreadExpression  _expression = null;

    public PhotoSpreadExpression() {

    }

    public PhotoSpreadExpression getExpression () {
    	return _expression;
    }
   
    public String toString () {
    	return "<PhotoSpreadExpression '" + _expression + ">";
    }
    
      /**
 * copies an expression, adjusting row and column refernces as necessary
      * @param rowOffset number of rows to adjust row references 
       * @param colOffset number of columns to adjust column references
       * @return copied expresssion adjusted by row and column offset
 
 */
    public String copyExpression(int rowOffset, int colOffset){
        return this.toString();
    }
}
