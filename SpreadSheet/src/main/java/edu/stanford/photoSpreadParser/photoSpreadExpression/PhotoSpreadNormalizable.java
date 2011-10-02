/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.photoSpreadParser.photoSpreadExpression;

import edu.stanford.photoSpreadParser.photoSpreadNormalizedExpression.PhotoSpreadNormalizedExpression;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;

/**
 *
 * @author skandel
 */
public interface PhotoSpreadNormalizable {
    
    PhotoSpreadNormalizedExpression normalize(PhotoSpreadCell cell);
    
}
