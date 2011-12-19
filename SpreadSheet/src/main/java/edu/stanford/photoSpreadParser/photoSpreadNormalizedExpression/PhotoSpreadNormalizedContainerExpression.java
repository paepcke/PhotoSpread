/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.photoSpreadParser.photoSpreadNormalizedExpression;

import java.util.ArrayList;
import java.util.Iterator;

import edu.stanford.photoSpread.PhotoSpreadException.IllegalArgumentException;
import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadParser.photoSpreadExpression.PhotoSpreadCondition;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;
import edu.stanford.photoSpreadUtilities.Const;

/**
 *
 * @author skandel
 */
public class PhotoSpreadNormalizedContainerExpression {
    private PhotoSpreadCell _cell;
    ArrayList<PhotoSpreadCondition> _conditions;
    
    public PhotoSpreadNormalizedContainerExpression(PhotoSpreadCell _cell){
        this._cell = _cell;
        _conditions = new ArrayList<PhotoSpreadCondition>();
    }
    
    
    public void addCondition(PhotoSpreadCondition condition){
        this._conditions.add(condition);
    }
    
    public void addConditions(ArrayList<PhotoSpreadCondition> conditions){
        this._conditions.addAll(conditions);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
       PhotoSpreadNormalizedContainerExpression ce = new PhotoSpreadNormalizedContainerExpression(this._cell); // super.clone();
       
        ce.addConditions(this._conditions);
        
        return ce;
    }

    @Override
    public String toString() {
    	String res = _cell.toString() + "[";
    	if (_conditions.isEmpty())
    		return res + "<no conditions>]";
    	Iterator<PhotoSpreadCondition> it = _conditions.iterator();
    	res += it.next().toString();
    	while (it.hasNext())
    		res += " & " + it.next().toString();
    	return res + "]";
    }

     public PhotoSpreadCell forceObject(PhotoSpreadObject object, Boolean reEvaluateCell, Boolean reDrawTable) 
    		 throws IllegalArgumentException{
        Iterator<PhotoSpreadCondition> it = _conditions.iterator();
        while(it.hasNext()){
            PhotoSpreadCondition condition = it.next();
            condition.forceObject(object);
        }
        
        PhotoSpreadCell oldContainer = object.getCell();
        oldContainer.removeObject(object);
        
        if (!_cell.isFormulaCell())
        	_cell.setFormula(
        			Const.OBJECTS_COLLECTION_INTERNAL_TOKEN, 
        			reEvaluateCell, 
        			reDrawTable);
        
        _cell.addObject(object);
        object.setCell(_cell);
        
        return _cell;
       
    }
    
    public boolean canForceObject(PhotoSpreadObject object) throws IllegalArgumentException{
        
        Iterator<PhotoSpreadCondition> it = _conditions.iterator();
        while(it.hasNext()){
            PhotoSpreadCondition condition = it.next();
            if(!condition.canForceObject(object)){
                return false;
            }
        }
        
        return true;
    }
}
