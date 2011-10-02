/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.photoSpreadParser.photoSpreadNormalizedExpression;

import java.util.ArrayList;
import java.util.Iterator;
import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadParser.photoSpreadExpression.PhotoSpreadCondition;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;

/**
 *
 * @author skandel
 */
public class PhotoSpreadNormalizedExpression {
    
    ArrayList<PhotoSpreadNormalizedContainerExpression> _containerExpressions;
    
    public PhotoSpreadNormalizedExpression(){
        _containerExpressions = new ArrayList<PhotoSpreadNormalizedContainerExpression>();
    }
    
    public PhotoSpreadNormalizedExpression(PhotoSpreadCell cell){
        this();
       
        PhotoSpreadNormalizedContainerExpression ce = new PhotoSpreadNormalizedContainerExpression(cell);
        
        _containerExpressions.add(ce);
    }
    
    public void union(PhotoSpreadNormalizedExpression other){
        this._containerExpressions.addAll(other._containerExpressions);
        
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        PhotoSpreadNormalizedExpression ne = new PhotoSpreadNormalizedExpression();
        ///ne._containerExpressions = new ArrayList<PhotoSpreadNormalizedContainerExpession>();
        Iterator<PhotoSpreadNormalizedContainerExpression> it = _containerExpressions.iterator();
        while(it.hasNext()){
            ne.addContainerExpression((PhotoSpreadNormalizedContainerExpression) it.next().clone());
        }
        
        return ne;
    }

    @Override
    public String toString() {
        return _containerExpressions.toString();
    }
     
    public void addContainerExpression(PhotoSpreadNormalizedContainerExpression ce){
        _containerExpressions.add(ce);
    }
    
    public void addCondition(PhotoSpreadCondition condition){
        Iterator<PhotoSpreadNormalizedContainerExpression> it = _containerExpressions.iterator();
        while(it.hasNext()){
            it.next().addCondition(condition);
        }
    }
    
    public void addConditions(ArrayList<PhotoSpreadCondition> conditions){
        Iterator<PhotoSpreadNormalizedContainerExpression> it = _containerExpressions.iterator();
        while(it.hasNext()){
            it.next().addConditions(conditions);
        }
    }
    
    public PhotoSpreadCell forceObject(PhotoSpreadObject object, Boolean reEvaluateCell, Boolean reDrawTable){
        if(_containerExpressions.size() == 1){
            PhotoSpreadNormalizedContainerExpression ce = _containerExpressions.get(0);
            if(ce.canForceObject(object)){
                return ce.forceObject(object, reEvaluateCell, reDrawTable);
            }
        }
        
        return null;
    }
}
