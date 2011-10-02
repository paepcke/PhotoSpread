/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.photoSpreadTable;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;

import edu.stanford.photoSpread.PhotoSpread;
import edu.stanford.photoSpreadObjects.photoSpreadComponents.ObjectsPanel;
import edu.stanford.photoSpreadObjects.photoSpreadComponents.Workspace;
import edu.stanford.photoSpreadTable.DnDSupport.StillLabel;
import edu.stanford.photoSpreadTable.photoSpreadFormulaEditor.PhotoSpreadFormulaEditor;
import edu.stanford.photoSpreadUtilities.CellCoordinates;
import edu.stanford.photoSpreadUtilities.Const;
import edu.stanford.photoSpreadUtilities.Misc;
import edu.stanford.photoSpreadUtilities.Misc.ShowHelpAction;

/**
 *
 * @author skandel
 */
public class PhotoSpreadTable extends JTable {

	private static final long serialVersionUID = 1L;

	private PhotoSpreadFormulaEditor _formulaEditor;
	private PhotoSpreadTableModel _tableModel;
	private Workspace _workspace;

	// Place to remember which/whether we hover-highlighted 
	// any cell during a drag/drop operation. This way we
	// can be sure to unHoverHighlight the cell when the 
	// drag/drop is done:

	public PhotoSpreadCellHandler _mostRecentDropTarget = null;
	private DnDSupport _dndViz = null;

	/****************************************************
	 * Constructor(s)
	 *****************************************************/


	public PhotoSpreadTable(PhotoSpreadTableModel model, JFrame enclosingWindow) {

		super(model);

		_tableModel = model;
		_tableModel.setTable(this);
		_formulaEditor = new PhotoSpreadFormulaEditor(this);

		_workspace = new Workspace(enclosingWindow);
		PhotoSpread.setCurrentWorkspaceWindow(_workspace);
		_workspace.setMinimumSize(new Dimension(500,550));
		// Put the workspace next to this sheet window:
		_workspace.setLocationRelativeTo(null);
		Point wsLocation = _workspace.getLocation();
		_workspace.setLocation(wsLocation.x, wsLocation.y - 50);
		_workspace.setVisible(true);

		// Inits for drag/drop support:
		_dndViz = (StillLabel) DnDSupport.TableDnDVizFactory(
				enclosingWindow,
				this,
				DnDSupport.DnDVizStyle.STILL_LABEL);

		this.setRowHeight(PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.sheetRowHeightMinKey));
		this.setIntercellSpacing(new Dimension(Const.SPACE_BETWEEN_TABLE_CELLS_HOR, Const.SPACE_BETWEEN_TABLE_CELLS_VER));

		this.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		this.setSelectionBackground(Const.activeCellBackgroundColor);
		this.setBackground(Const.inactiveCellBackgroundColor);

		this.setAutoResizeMode(AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		//this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		// Ask to be notified of changes in the model:
		_tableModel.addTableModelListener(new PhotoSpreadTableModelListener(this));

		// When starting to type, give focus to the 
		// cell that has the cursor over it. User doesn't
		// have to click the cell (default is false):

		// this.setSurrendersFocusOnKeystroke(true);

		setDefaultRenderer(
				Object.class,   // All columns whose content type is Object (the default)
				// will use this following class for rendering
				new PhotoSpreadCellEditorAndRenderer());

		setDefaultEditor(
				Object.class,   // All columns whose content type is Object (the default)
				// will use this following class for cell editing
				new PhotoSpreadCellEditorAndRenderer());

		RowHeightRegulationListener mouseHandler = new RowHeightRegulationListener(this);
		this.addMouseListener(mouseHandler);
		this.addMouseMotionListener(mouseHandler);


		// Request a cell editor for the 
		// upper left cell to initialize
		// selection- and dnd machinery:
		
		getDefaultEditor().getTableCellEditorComponent(
				this,
				getCell(0, 1),
				Const.IS_SELECTED,
				0, // row
				1); // column
		
		Misc.bindKey(this, "F1", new ShowHelpAction(
				"To do in Sheet Window", 
				"HelpFiles/sheetHelp.html", 
				PhotoSpread.getCurrentSheetWindow()));
	}

	/****************************************************
	 * Listeners
	 *****************************************************/

	private class RowHeightRegulationListener extends MouseInputAdapter {

		PhotoSpreadTable _table;
		int _rowBeingResized = -1;
		int _cursorYWas = 0;
		private Cursor _resizeCursor = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
		private Cursor _savedCursor = null;

		public RowHeightRegulationListener (PhotoSpreadTable table) {
			_table = table;
		}

		public void mousePressed (MouseEvent e) {

			CellCoordinates cellCoords = getCellAddressUnderCursor();
			if (cellCoords.column() == 0) {
				_rowBeingResized = cellCoords.row();
				_cursorYWas = e.getYOnScreen();
				// If we keep dragging enabled the system gets confused:
				_table.setDragEnabled(false);
				if (_savedCursor == null)
					_savedCursor = _table.getCursor();
				_table.setCursor(_resizeCursor);
			}
		}

		public void mouseReleased (MouseEvent e) {
			_rowBeingResized = -1;
			_table.setDragEnabled(true);
			_table.setCursor(_savedCursor);
		}

		public void mouseClicked (MouseEvent e) {
			if (e.getClickCount() == 2) {
				// Double clicked on column 0? If yes,
				// reset current row to default height:
				CellCoordinates cellCoords = getCellAddressUnderCursor();
				if (cellCoords.column() == 0) {
					int defaultRowHeight = PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.sheetRowHeightMinKey);
					setRowHeight(cellCoords.row(), defaultRowHeight);
				}
			}
		}

		public void mouseDragged (MouseEvent e) {

			if (_rowBeingResized >= 0) {
				adjustRowHeight(
						_rowBeingResized, 
						_cursorYWas, 
						e.getYOnScreen(),
						Const.motionSensitivity);
				_cursorYWas = e.getYOnScreen();
			}
		}
	}


	private class PhotoSpreadTableModelListener implements TableModelListener {

		PhotoSpreadTable _table = null;

		public PhotoSpreadTableModelListener (PhotoSpreadTable table) {
			_table = table;
		}
		/* (non-Javadoc)
		 * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
		 */
		public void tableChanged(TableModelEvent changeEvent) {

			int affectedCol = changeEvent.getColumn();
			int firstAffectedRow = changeEvent.getFirstRow();
			int lastAffectedRow = changeEvent.getLastRow();
			int changeType = changeEvent.getType();
			PhotoSpreadCell currCell = _table.getSelectedCell();

			if ((affectedCol < 0) || 
					(firstAffectedRow < 0) ||
					(lastAffectedRow < 0))
				return;

			PhotoSpreadCellEditorAndRenderer cellEditorFinder = _table.getDefaultEditor();
			PhotoSpreadCellHandler cellHandler = null;

			switch (changeType) {

			case TableModelEvent.UPDATE:

				// One or more rows in a single column were updated.
				// Cause each of them to repaint:
				for (int row = firstAffectedRow; row <= lastAffectedRow; row++) {
					// If the modifification was in the currently selected cell,
					// then we need to call the 'big' version of getTableCellEditorComponent().
					// That version (re)initializes the formula bar and the Workspace: 
					if ((row == currCell.getRow()) && (affectedCol == currCell.getColumn()))
						cellHandler = (PhotoSpreadCellHandler) cellEditorFinder.getTableCellEditorComponent(
								_table,
								null,     // no initial value to show. Cell has own value
								Const.IS_SELECTED,
								row,
								affectedCol);	

					else
						cellHandler = cellEditorFinder.getTableCellEditorComponent(_table, row, affectedCol);
					cellHandler.stopCellEditing();
				}
				break;
			case TableModelEvent.INSERT:

				// Inserting rows in the middle of a sheet isn't implemented yet:

				if (firstAffectedRow < _table.getRowCount())
					// throw new PhotoSpreadException.NotImplementedException("Inserting rows in middle of PhotoSpread sheet is not yet implemented.");
					System.out.println("Inserting rows in middle of PhotoSpread sheet is not yet implemented.");

				// NOTE: NOT SURE THIS IS RIGHT. NO TIME TO THINK RIGHT NOW:
				// Append rows is manageable: just ask for the cell editor
				// in each of the new rows.

				for (int row = firstAffectedRow; row < lastAffectedRow; row++) {
					for (int col = 0; col < _table.getColumnCount(); col++)
						cellHandler = cellEditorFinder.getTableCellEditorComponent(_table, row, col);
				}
				break;

			case TableModelEvent.DELETE:

				// throw new PhotoSpreadException.NotImplementedException("Deletion in PhotoSpread sheet is not yet implemented.");
				System.out.println("Deletion in PhotoSpread sheet is not yet implemented.");

				break;

			default:
				// ignore any other change types (there shouldn't be any).
			}
		}
	}

	/****************************************************
	 * Getter/Setter(s)
	 *****************************************************/

	public PhotoSpreadFormulaEditor getFormulaEditor() {
		return _formulaEditor;
	}

	public Workspace getWorkspace () {
		return _workspace;
	}
	
	public PhotoSpreadTableModel getTableModel() {
		return _tableModel;
	}

	public DnDSupport getDndViz() {
		return _dndViz;
	}

	public PhotoSpreadTableModel getPhotoSpreadModel(){
		return this._tableModel;
	}

	public void setCellFormula(int row, int col , String formula){

		PhotoSpreadCell cell = (PhotoSpreadCell) _tableModel.getValueAt(row, col);
		cell.setFormula(formula, Const.DO_EVAL, Const.DO_REDRAW);
	}

	public void setSelectedCellFormula(String formula){
		int row = this.getSelectedRow();
		int col = this.getSelectedColumn();

		setCellFormula(row, col, formula);
	}

	/**
	 * Find PhotoSpreadCell object at (row, column)
	 * @param row
	 * @param col
	 * @return PhotoSpreadCell object held in given table cell.
	 */
	public PhotoSpreadCell getCell(int row, int col) {
		return (PhotoSpreadCell) _tableModel.getValueAt(row, col); 
	}

	/**
	 * Find PhotoSpreadCell object by coordinates
	 * @param coords 
	 * @return PhotoSpreadCell object held in given table cell.
	 */
	public PhotoSpreadCell getCell(CellCoordinates coords) {
		return (PhotoSpreadCell) _tableModel.getValueAt(coords.row(), coords.column()); 
	}

	
	/**
	 * Identify this table's cell coordinates (row, column) under the current cursor location.
	 * @return CellCoordinate object. Returns -1 for row and/or column, 
	 * if cursor is not located over a row/column.
	 */
	public CellCoordinates getCellAddressUnderCursor () {
		return new CellCoordinates (getRowUnderCursor(), getColumnUnderCursor());
	}

	/**
	 * Find the number of the column under the current cursor position.
	 * @return This table's column under the cursor, or -1 if cursor is not 
	 * over a column
	 */
	public int getColumnUnderCursor() {

		Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
		SwingUtilities.convertPointFromScreen(mouseLoc, this);
		return this.columnAtPoint(mouseLoc);
	}

	/**
	 * Find the number of the row under the current cursor position.
	 * @return This table's row under the cursor, or -1 if cursor is not 
	 * over a row.
	 */
	public int getRowUnderCursor() {

		Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
		SwingUtilities.convertPointFromScreen(mouseLoc, this);
		return this.rowAtPoint(mouseLoc);
	}

	/**
	 * Return cell coordinates given a Point in table coordinates.
	 * @param loc Location in table coordinates
	 * @return Coordinates of cell underneath that location. Like (1,5).
	 */
	public CellCoordinates getCellAddressAt (Point loc) {
		return new CellCoordinates (getRowAt (loc), getColumnAt(loc));
	}

	/**
	 * Return row, given a Point in table coordinates.
	 * @param loc Location in table coordinates
	 * @return The row number (zero origin)
	 */
	public int getRowAt (Point loc) {
		return this.rowAtPoint(loc);
	}

	/**
	 * Return column, given a Point in table coordinates.
	 * @param loc Location in table coordinates
	 * @return The column number (zero origin)
	 */
	public int getColumnAt (Point loc) {
		return this.columnAtPoint(loc);
	}

	public PhotoSpreadCell getSelectedCell() {
		int row = this.getSelectedRow();
		int col = this.getSelectedColumn();
		return getCell(row, col);
	}

	/****************************************************
	 * Methods
	 *****************************************************/
    // The following two methods unfortunately don't work.
	// I'd love to have them.
/*	public void activateCell (CellCoordinates coords) {
		activateCell(coords.row(), coords.column());
	}
	
	public void activateCell (int row, int col) {
		getDefaultEditor().getTableCellEditorComponent(
				this,
				null,
				true, 
				row, 
				col);
	}
*/	
	@Override
	public int getSelectedRow() {

		PhotoSpreadCell selectedCellObj = 
			((PhotoSpreadCellEditorAndRenderer) getDefaultEditor(Object.class)).getSelectedCellObject();		
		return selectedCellObj.getRow();
	}

	/**
	 * Return the index of the first selected column, or -1 if none selected.
	 * @see javax.swing.JTable#getSelectedColumn()
	 */
	@Override
	public int getSelectedColumn() {
		PhotoSpreadCell selectedCellObj = 
			((PhotoSpreadCellEditorAndRenderer) getDefaultEditor(Object.class)).getSelectedCellObject();
		if (selectedCellObj == null)
			return -1;

		return selectedCellObj.getColumn();
	}

	/**
	 * Provides the TableCellEditor for PhotoSpreadTable instances.
	 * This object is primarily useful to provide CellEditor instances,
	 * that is PhotoSpreadCellHandler instances, for given row/column
	 * pairs.
	 * @return
	 */
	public PhotoSpreadCellEditorAndRenderer getDefaultEditor () {

		TableCellEditor jTableObjectTypedCellEditor = getDefaultEditor (Object.class);
		return  (PhotoSpreadCellEditorAndRenderer) jTableObjectTypedCellEditor;
	}

	public PhotoSpreadCellHandler getActiveCellEditor(){

		return getCellEditorFor (new CellCoordinates (getSelectedRow(), getSelectedColumn()));
	}
	
	public PhotoSpreadCellHandler getCellEditorFor (int row, int col) {

		PhotoSpreadCellHandler cellEditor = null;
		PhotoSpreadCellEditorAndRenderer tableCellEditor = getDefaultEditor();

		cellEditor = tableCellEditor.getTableCellEditorComponent(this, row, col);
		return cellEditor;
	}
	
	public PhotoSpreadCellHandler getCellEditorFor (CellCoordinates coords) {
		return getCellEditorFor(coords.row(), coords.column());
	}

	public void clearCell(int row, int col) {
		((PhotoSpreadCell) _tableModel.getValueAt(row, col)).clear();
	}

	protected void adjustRowHeight (
			int rowToResize, 
			int cursorYWas, 
			int cursorYIs,
			int motionSensitivity) {

		int relativeMove =  motionSensitivity * (cursorYIs - cursorYWas);
		// int currRowHeight = getRowHeight(_rowBeingResized);
		int newSize  = getRowHeight(rowToResize) + relativeMove;
		setRowHeight(
				rowToResize, 
				Math.max (PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.sheetRowHeightMinKey), 
						newSize));
	}

	public void redraw(){
		this._workspace.redraw();
	}

	public boolean validEditingCell(int row, int col){

		// return (row >= 0 && col >= 0 && row < this.getRowCount() && col < this.getRowCount());
		return (row >= 0 && col >= 1 && row < this.getRowCount() && col < this.getColumnCount());
	}

	public boolean isCellEditable(int row, int column) {

		if (column > 0)
			return true;
		else
			return false;
	}

	/**
	 * Convenience method to fire a table cell change
	 * event just knowing the table, row, and col, and 
	 * without having to dig out the table model. 
	 * @param row
	 * @param col
	 */
	public void fireTableCellUpdated(int row, int col) {
		_tableModel.fireTableCellUpdated(row, col);
	}

	public static boolean loadFiles (PhotoSpreadCell cell, ArrayList<File> files) {
		return ObjectsPanel.loadFiles(cell, files);
	}

	public static boolean loadFiles (PhotoSpreadCell cell, File[] files) {
		return ObjectsPanel.loadFiles(cell, files);
	}

} 

