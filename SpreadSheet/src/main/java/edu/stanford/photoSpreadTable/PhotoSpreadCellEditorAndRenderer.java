/**
 * 
 */
package edu.stanford.photoSpreadTable;

import java.awt.Component;
import java.rmi.NotBoundException;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import edu.stanford.photoSpread.PhotoSpread;
import edu.stanford.photoSpreadUtilities.ComputableDimension;
import edu.stanford.photoSpreadUtilities.Misc;

/**
 * @author paepcke
 * 
 * Implements both the TableCellEditor and TableCellRenderer interfaces
 * for JTable. Both rendering and editing is provided by a combination
 * of PhotoSpreadCellHandler objects (subclasses of ObjectsPanel),
 * and the formula editor. Only one formula editor instance exists,
 * but each cell has its own associated PhotoSpreadCellHandler.
 * 
 * We maintain an association between row/column pairs and their associated
 * PhotoSpreadCellHandler instance. When asked for a Component, both
 * getTableCellRendererComponent() and getTableCellEditorComponent() therefore
 * return a PhotoSpreadCellHandler instance, which is either created,
 * if one doesn't exist for the respective row/column pair, or is retrieved
 * from a specialized HashMap.
 * 
 */

public class PhotoSpreadCellEditorAndRenderer extends AbstractCellEditor 
implements TableCellEditor, TableCellRenderer {

	
	private static final long serialVersionUID = 1L;

	// Place to remember each row/column's (i.e. visual cell's) PhotoSpreadCellHandler:
	
	static TableCellObjectsMap<PhotoSpreadCellHandler> _tableCellHandlers = new TableCellObjectsMap<PhotoSpreadCellHandler>();
	
	PhotoSpreadCell _currentEditedCell = null;
	PhotoSpreadCellHandler _currentCellHandler = null; 
	
	PhotoSpreadCellHandler _prevDragCellHandler = null; 

	/****************************************************
	 * Constructor(s)
	 *****************************************************/
	
	/**
	 * 
	 */
	public PhotoSpreadCellEditorAndRenderer() {
		super();
	}

	/****************************************************
	 * Methods to Satisfy Interface TableCellRenderer
	 *****************************************************/
	
	/**
	 * The one method required by the TableCellRenderer
	 * interface. We find or create the PhotoSpreadCellHandler
	 * instance that provides both rendering and display,
	 * and return it.
	 *  
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(
			JTable table,
			Object currCellValue,
			boolean isSelected,     // I *believe* this is true when 
									// cell is in same row with the
									// active cell
			boolean hasFocus,       // Called for each cell that's hovered
									// over, except for the active cell.
			int row,
			int column) {

		PhotoSpreadCellHandler cellHandler = _tableCellHandlers.get(row, column); 

        if (cellHandler == null) {
        	cellHandler = createCellHandler ((PhotoSpreadTable) table, row, column);
        }
        
        // Need the following unHighlight() because
        // JTable really does use cell editor/renderer
        // components as 'rubber stamps.' This means
        // that if the mechanism gets the selected cell
        // into its throat, it will use its opacity 
        // on other cells one click on. Strange...:
        
        cellHandler.activeUnHighlight();
       
        return cellHandler;
	}
	
	/****************************************************
	 * Getter/Setter(s)
	 *****************************************************/
	
	public PhotoSpreadCell getSelectedCellObject() {
		return _currentEditedCell;
	}
	
	/****************************************************
	 * Methods to Satisfy Interface TableCellEditor
	 *****************************************************/

	/*
	 * The one method required by the TableCellEditor
	 * interface. We find or create the PhotoSpreadCellHandler
	 * instance that provides both rendering and display,
	 * and return it.
	 * 
	 * Before we do, we need to get that handler ready
     *
	 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
	 */
	public Component getTableCellEditorComponent(
			JTable table, 
			Object initialValueForEditorToShow,
			boolean isSelected, 
			int row, 
			int column) {

		PhotoSpreadCellHandler cellHandler = _tableCellHandlers.get(row, column); 
		
		if (_currentCellHandler != null)
			_currentCellHandler.activeUnHighlight();
			
        _currentEditedCell = (PhotoSpreadCell) ((PhotoSpreadTable) table).getValueAt(row, column);
        
        if (cellHandler == null) {
        	cellHandler = createCellHandler (
        			(PhotoSpreadTable)table, 
        			_currentEditedCell,
        			row,
        			column);
        }

        _currentCellHandler = cellHandler;
        cellHandler.activeHighlight();
        try {
        	
			((PhotoSpreadTable) table).getWorkspace().setDisplayedCell(_currentEditedCell);
			
		} catch (NumberFormatException e) {
			Misc.showErrorMsgAndStackTrace(e, "");
			// e.printStackTrace();
		} catch (NotBoundException e) {
			Misc.showErrorMsgAndStackTrace(e, "");
			//e.printStackTrace();
		}
        
        updateFormulaEditor((PhotoSpreadTable) table, row, column);
        ((PhotoSpreadTable) table).getFormulaEditor().requestFocus();

		return cellHandler;
	}

	/**
	 * Simplified method for obtaining the cell editor
	 * of the currently selected cell.
	 * <b>NOTE</b> This version of the method will
	 * not initialize the returned handler. It just
	 * retrieves the handler. You <b>must</b> call 
	 * the long version to get all the initialization
	 * done that is required to start editing.
	 * 
	 * @param table Table that holds the cell whose editor is requested
	 * @param row
	 * @param column
	 * @return PhotoSpreadCellHandler that renders and edits the currently
	 * selected cell.
	 */
	public PhotoSpreadCellHandler getTableCellEditorComponent(
			PhotoSpreadTable table, 
			int row, 
			int column) {

		PhotoSpreadCellHandler cellHandler = _tableCellHandlers.get(row, column); 
        
		if (_currentCellHandler != null) {
			_currentCellHandler.activeUnHighlight();
			_currentCellHandler.stopCellEditing();
		}
		
        if (cellHandler == null) {
        	cellHandler = createCellHandler (
        			(PhotoSpreadTable)table, 
        			_currentEditedCell,
        			row,
        			column);
        }

        return cellHandler;
	}

	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	public Object getCellEditorValue() {
		return _currentEditedCell;
	}

	/****************************************************
	 * Support Methods for Both TableCellRenderer and TableCellEditor
	 *****************************************************/
	
	private PhotoSpreadCellHandler createCellHandler (PhotoSpreadTable table, int row, int column) {
		return createCellHandler (
				table,
				(PhotoSpreadCell) ((PhotoSpreadTable) table).getValueAt(row, column),
				row,
				column);
	}
	
	/**
	 * Create a new CellHandler that will be the 
	 * visual in one cell.
	 * @param table
	 * @param row
	 * @param column
	 * @return
	 * @throws NotBoundException
	 */
	private PhotoSpreadCellHandler createCellHandler (
			PhotoSpreadTable table,
			PhotoSpreadCell cellObj,
			int row,
			int column) {
		
		PhotoSpreadCellHandler cellHandler;

		// Create a handler for this cell. 
		if (column == 0)
			cellHandler = new PhotoSpreadColumnZeroCellHandler (table, row, cellObj);
		else
			cellHandler = new PhotoSpreadCellHandler(table, cellObj);
		
		// Make this new cell ObjectsPanel findable by row/col:
		_tableCellHandlers.put(row, column, cellHandler);
		
		// Set minimum cell size as specified in the user preference
		// file or the built-in defaults:

		cellHandler.setMinimumSize(new ComputableDimension(
				PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.sheetColWidthMinKey),
				PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.sheetRowHeightMinKey)));
		
		// Set current and preferred sizes also to user/default sizes:
		cellHandler.setSize(cellHandler.getMinimumSize());;
		cellHandler.setPreferredSize(cellHandler.getMinimumSize());
		
		cellHandler.setDisplayedCell((PhotoSpreadCell) table.getValueAt(row, column));
		
		// Prepare panel to use the table's background as its
		// normal color.
		
		cellHandler.setBackground(null);
		cellHandler.setOpaque(true);

		return cellHandler;
	}
	
	private void updateFormulaEditor(PhotoSpreadTable table, int row, int column) {
		PhotoSpreadCell currCell = (PhotoSpreadCell) table.getValueAt(row, column);
		table.getFormulaEditor().setInitialValue(currCell.getFormula());
	}
}
