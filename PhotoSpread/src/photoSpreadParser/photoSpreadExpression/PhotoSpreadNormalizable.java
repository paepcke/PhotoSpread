/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package photoSpreadParser.photoSpreadExpression;

import photoSpreadParser.photoSpreadNormalizedExpression.PhotoSpreadNormalizedExpression;
import photoSpreadTable.PhotoSpreadCell;

/**
 *
 * @author skandel
 */
public interface PhotoSpreadNormalizable {
    
    PhotoSpreadNormalizedExpression normalize(PhotoSpreadCell cell);
    
}
