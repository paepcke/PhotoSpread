/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package photoSpreadObjects.photoSpreadComponents;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import photoSpreadObjects.PhotoSpreadObject;
import photoSpreadTable.PhotoSpreadCell;
import photoSpreadUtilities.ComputableDimension;
import photoSpreadUtilities.Const;

/**
 *
 * @author skandel
 */
public class DraggablePanel extends JPanel implements PhotoSpreadAddable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	PhotoSpreadObject _parentObject;
    PhotoSpreadCell _cell;
    
    
    public DraggablePanel(PhotoSpreadObject _parentObject) {
        this._parentObject = _parentObject;
        this._cell = null;
        
    }
    
    public PhotoSpreadCell getCell() {
        return _cell;
    }

    public JComponent getComponent() {
        return this;
    }

    public PhotoSpreadObject getParentObject() {
        return _parentObject;
    }

   
    public void setCell(PhotoSpreadCell cell) {
        _cell = cell;
    }

     public void highlight(){
    	 
        this.setBorder(new LineBorder(
        		Const.labelHighlightBorderColor, 
        		Const.labelSelectionBorderWidth));
    }
    
    public void unhighlight(){
        this.setBorder(null);
    }
    
    public ComputableDimension getNaturalSize() {
    	return (ComputableDimension) this.getSize(); 	
    }
    
	 public int compareTo(PhotoSpreadAddable thatAddable) {
		 return this.getNaturalSize().compareTo(thatAddable.getNaturalSize());
	 }
    
    
}
