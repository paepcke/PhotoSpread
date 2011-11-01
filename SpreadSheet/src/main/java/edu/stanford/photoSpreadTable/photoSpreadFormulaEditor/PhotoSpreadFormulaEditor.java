/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.photoSpreadTable.photoSpreadFormulaEditor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import edu.stanford.photoSpread.PhotoSpread;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;
import edu.stanford.photoSpreadTable.PhotoSpreadTable;
import edu.stanford.photoSpreadUtilities.Const;
import edu.stanford.photoSpreadUtilities.Misc;

/**
 *
 * @author skandel
 */
public class PhotoSpreadFormulaEditor extends JTextField {
    
	private static final long serialVersionUID = 1L;
	
	PhotoSpreadTable _parentTable;
	String _initialValue = "";

    public PhotoSpreadFormulaEditor(final PhotoSpreadTable _parentTable) {
    	
    	ActionMap _myActionMap = this.getActionMap();
    	InputMap  _myInputMap  = this.getInputMap();
        
    	this._parentTable = _parentTable;
    	
    	// Make ENTER key submit the formula to the cell:
        _myInputMap.put(
        		KeyStroke.getKeyStroke(
        		KeyEvent.VK_ENTER, 0),
        "SubmitAction");

    	// Make ESC key abort edit and revert to initial value:
        _myInputMap.put(
        		KeyStroke.getKeyStroke(
        		KeyEvent.VK_ESCAPE, 0),
        "AbortAction");
        
        
        _myActionMap.put("SubmitAction", new AbstractAction() {
        	
        	private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				// The user wants to submit this formula, 
				// (as opposed to aborting): so submit it to the cell:
				PhotoSpreadCell theCell = _parentTable.getSelectedCell(); 
				if (theCell.isObjectCollection() &&
					getText().isEmpty()) {
					// User is clearing a cell by erasing the
					// special formula entry for non-formula cells
					// that are simple containers. We'll allow this,
					// but we warn:
					if ((theCell.getObjects().size() > 0) &&
							(Misc.showConfirmMsg(
							"Erasing this 'formula' will clear cell " +
							theCell.getCellAddress() +
							" of all objects. Continue?"))) {
						theCell.clear();
						return;
					}
					else {
						PhotoSpreadFormulaEditor.this.revertToInitialValue();
						return;
					}
				}
				PhotoSpreadFormulaEditor.this.submitToCell();
			}
        });

        _myActionMap.put("AbortAction", new AbstractAction() {
        	
        	private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				// User aborted editing the formula:
				PhotoSpreadFormulaEditor.this.revertToInitialValue();
			}
        });
    }
    
    private void submitToCell(){
    	
		// If this cell contains a collection of loaded
		// objects, refuse to enter the purely informational
		// "(Item Collection)" as a formula. Unless the formula
		// is being set to the empty string, which happens when
		// the user runs clearCell() from the context menu:
		
		if (getText().equals(Const.OBJECTS_COLLECTION_PUBLIC_TOKEN)) {
			Misc.showInfoMsg("This cell contains a collection of items that was not computed. Please right-click->Clear Cell before changing the cell formula.",
					PhotoSpread.getCurrentSheetWindow());
			return;
		}
		
        _parentTable.setSelectedCellFormula(this.getText());
        
        // Make the editor window lose focus
        // to make clear that the input was
        // accepted:
        
        this.setFocusable(false);
        this.setFocusable(true);
        _parentTable.getActiveCellEditor().stopCellEditing();
        _parentTable.getActiveCellEditor().activeHighlight();
    }
    
    private void showFormula (String formulaStr) {
    	if (formulaStr.equals(Const.OBJECTS_COLLECTION_INTERNAL_TOKEN))
    		setText(Const.OBJECTS_COLLECTION_PUBLIC_TOKEN);
    	else
    		setText(formulaStr);
    }
    
    private void revertToInitialValue () {
    	showFormula(_initialValue);
    }
    
    public void setInitialValue (String initVal) {
    	_initialValue = initVal;
    	showFormula(initVal);
    }
}
