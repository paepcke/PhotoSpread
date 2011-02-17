/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package photoSpreadParser.photoSpreadExpression;

import photoSpread.PhotoSpread;
import photoSpreadObjects.PhotoSpreadObject;
import photoSpreadParser.photoSpreadNormalizedExpression.PhotoSpreadNormalizedExpression;
import photoSpreadTable.PhotoSpreadCell;
import photoSpreadUtilities.PhotoSpreadObjIndexerFinder;
import photoSpreadUtilities.TreeSetRandomSubsetIterable;

/**
 *
 * @author skandel
 */
public class PhotoSpreadConstantExpression extends PhotoSpreadFormulaExpression {
    
    
    TreeSetRandomSubsetIterable<PhotoSpreadConstant> _constants;
    
    public PhotoSpreadConstantExpression() {
        super();
        _constants = new TreeSetRandomSubsetIterable<PhotoSpreadConstant>();
        
    	PhotoSpread.trace("New " + this);
    }

     public PhotoSpreadConstantExpression(String constant, PhotoSpreadCell _cell) {
        super();
        _constants = new TreeSetRandomSubsetIterable<PhotoSpreadConstant>();
        //_constants.setIndexer(new PhotoSpreadObjIndexerFinder());
        _constants.add(new PhotoSpreadStringConstant( _cell, constant));
    	PhotoSpread.trace("New " + this);
    }

    public TreeSetRandomSubsetIterable<PhotoSpreadObject> evaluate(PhotoSpreadCell cell) {
       
        TreeSetRandomSubsetIterable<PhotoSpreadObject> res = 
        	new TreeSetRandomSubsetIterable<PhotoSpreadObject>();
        
        res.setIndexer(new PhotoSpreadObjIndexerFinder());
        
        //If expression is empty, do not add objects
        if(_constants.size()==1&&_constants.first().getObject().contentEquals("")) return res;
        
        for (PhotoSpreadConstant psConstant : _constants) {
        	res.addAll(psConstant.getObjects(), cell.getObjects());
        }

        return res;
    }

    @Override
    public String toString() {
        // return _constants.toString();
    	return "<PhotoSpreadConstantExpression '" + _constants + "'>";
    }

    public void addConstant(PhotoSpreadConstant object){
        
        _constants.add(object);
    	PhotoSpread.trace("Adding constant " + object + " to " + this);
    }

    public PhotoSpreadNormalizedExpression normalize(PhotoSpreadCell cell) {
        return new PhotoSpreadNormalizedExpression(cell);
    }
    
    

}
