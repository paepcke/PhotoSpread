/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.photoSpreadObjects.photoSpreadComponents;

import javax.swing.JComponent;

import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;
import edu.stanford.photoSpreadUtilities.ComputableDimension;

/**
 *
 * @author skandel
 */
public interface PhotoSpreadAddable extends Comparable<PhotoSpreadAddable> {
	
	public void setCell(PhotoSpreadCell cell);
	public PhotoSpreadCell getCell();
	public PhotoSpreadObject getParentObject();
	public JComponent getComponent();
	public void highlight();
	public void unhighlight();
	public int compareTo(PhotoSpreadAddable thatAddable);
	public ComputableDimension getNaturalSize();
}
