/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.photoSpreadParser.photoSpreadExpression;
import edu.stanford.photoSpread.PhotoSpread;
import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadParser.photoSpreadExpression.PhotoSpreadSpecialConstants.PhotoSpreadNullConstant;
import edu.stanford.photoSpreadParser.photoSpreadNormalizedExpression.PhotoSpreadNormalizedExpression;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;
import edu.stanford.photoSpreadUtilities.Misc;
import edu.stanford.photoSpreadUtilities.TreeSetRandomSubsetIterable;
/**
 *
 * @author skandel
 */
public class PhotoSpreadSimpleCondition extends PhotoSpreadCondition {
    
    // private String _rhs;

    public PhotoSpreadSimpleCondition(String lhs, String compOp, String rhs) {
    	
		// Strip the double quotes from string constants.
		// Else comparisons between the _lhs and a
		// metadata value of "foo" will compare "foo" with ""foo""
		// and fail:
    	
        super(Misc.trim(lhs, '"'), compOp);
        this._rhs = Misc.trim(rhs, '"');
        PhotoSpread.trace("New PhotoSpreadSimpleCondition: " + this);
    }

    public PhotoSpreadSimpleCondition(String lhs, String compOp, PhotoSpreadNullConstant rhs) {
    	super(lhs,compOp,rhs);
    	//this._rhs = Const.NULL_VALUE_STRING;
	}

	public String getLhs() {
        return _lhs;
    }

    public String getRhs() {
        return _rhs;
    }
    
    @Override
    public String toString() {
        // return _lhs + " " +  this._comparisionAsString + " " + _rhs;
    	return "<PhotoSpreadSimpleCondition: lhs:" + _lhs +  " CompOp:" + this._comparisionAsString + " rhs:" + _rhs + ">";
    }
        
    public boolean satisfiesCondition(PhotoSpreadObject object){
        
        String value = object.getMetaData(_lhs);
        return _compOp.satisfiesOperator(value, _rhs);
    }

    @Override
    public void forceObject(PhotoSpreadObject object) {
        _compOp.forceObject(object, _lhs, _rhs);
    }

	public TreeSetRandomSubsetIterable<PhotoSpreadObject> evaluate(PhotoSpreadCell cell) {
		throw new RuntimeException("Evaluate not implemented for PhotoSpreadSimpleCondition");
	}

	public PhotoSpreadNormalizedExpression normalize(PhotoSpreadCell cell) {
		throw new RuntimeException("Normalize not implemented for PhotoSpreadSimpleCondition");
	}
}
