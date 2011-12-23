/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.stanford.photoSpreadTable;

import java.io.BufferedWriter;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.table.AbstractTableModel;

import edu.stanford.inputOutput.XMLProcessor;
import edu.stanford.photoSpread.PhotoSpread;
import edu.stanford.photoSpread.PhotoSpreadException;
import edu.stanford.photoSpread.PhotoSpreadException.FormulaError;
import edu.stanford.photoSpread.PhotoSpreadException.FormulaSyntaxError;
import edu.stanford.photoSpread.PhotoSpreadException.IllegalArgumentException;
import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadObjects.PhotoSpreadStringObject;
import edu.stanford.photoSpreadParser.ExpressionParser;
import edu.stanford.photoSpreadParser.photoSpreadExpression.PhotoSpreadExpression;
import edu.stanford.photoSpreadUtilities.Const;
import edu.stanford.photoSpreadUtilities.Const.ObjMovements;
import edu.stanford.photoSpreadUtilities.Misc;
/**
 *
 * @author skandel
 */
public class PhotoSpreadTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	public static final int minDroppableColumn = 1;
	public static final int minDroppableRow = 0;

	static private String TABLE_ELEMENT_NAME = "table";
	static private String COL_ELEMENT_NAME = "col";
	static private String COLNUM_ATTRIBUTE_NAME = "colNum";
	private ArrayList<String> columnNames;

	private Object _itemsClipboard = null;
	private PhotoSpreadCell _formulaClipboard = null;
	@SuppressWarnings("unused")
	private java.io.Reader _reader;
	private java.io.StringReader _stringReader;
	private ArrayList<ArrayList<PhotoSpreadCell>> data;
	private PhotoSpreadTable _table;
    // The the trusted versions, b/c we did 
    // all checking at startup time: 
	int numRows = 0;
	int numCols = 0;

	public PhotoSpreadTableModel() {
		this(PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.sheetNumRowsKey),
			 PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.sheetNumColsKey));
	}
	
	public PhotoSpreadTableModel(int theNumRows, int theNumCols) {
		
		numRows = theNumRows;
		numCols = theNumCols + 1;
		columnNames = new ArrayList<String>();
		columnNames.add("");

		_stringReader = new java.io.StringReader( "" );
		_reader = new java.io.BufferedReader( _stringReader );
		// _parser = new ExpressionParser(_reader);
		initializeData(numRows, numCols);
		//loadTestData();
	}

	private void initializeData(int numRows, int numCols){

		data = new ArrayList<ArrayList<PhotoSpreadCell>>();
		for(int col = 0; col < numCols; col++){
			columnNames.add(getColumnAsString(col+1));
			data.add(new ArrayList<PhotoSpreadCell>());
			PhotoSpreadCell cell = new PhotoSpreadCell(this, 0, col, "");
			data.get(col).add(cell);
			for(int row = 0; row < numRows; row++){
				data.get(col).add(new PhotoSpreadCell(this, row+1, col, ""));
			}
		}
		for (int row = 0; row < numRows; row++) {
			PhotoSpreadCell col0Cell = null;
			try {
				col0Cell = getCell(row, 0);
			} catch (IllegalArgumentException e) {};
			col0Cell.addObject(new PhotoSpreadStringObject(col0Cell, ""+(row+1)));
		}
	}

	/**
	 * Clear the table data, triggering cell-changed-events
	 * along the way.
	 */
	public void clear(){

		for(int row = 0; row < this.getRowCount(); row++){
			for(int col = 1; col < this.getColumnCount(); col++){
				this.getCellSafely(row, col).clear(Const.DONT_EVAL, Const.DONT_REDRAW);
			}
		}
		// Now that everything is cleared, eval each cell, just
		// to make *sure* that everything is initialized:
		for(int row = 0; row < this.getRowCount(); row++){
			for(int col = 1; col < this.getColumnCount(); col++){
				try {
					this.getCellSafely(row, col).evaluate(Const.DONT_REDRAW);
				} catch (FormulaError e) {
					// Should be an empty table, and should there not throw an error!
					e.printStackTrace();
				}
			}
		}
		fireTableDataChanged();
	}

	public PhotoSpreadTable getTable() {

		return _table;
	}

	public void updateAllCells(boolean doEval) {

		PhotoSpreadCellHandler cellHandler;

		if (doEval)
			for(int row = 0; row < getRowCount(); row++){
				for(int col = 1; col < getColumnCount(); col++){
					try {
						data.get(row).get(col).evaluate(Const.DONT_REDRAW);
					} catch (Exception e) {
						Misc.showErrorMsgAndStackTrace(e, "");
						//e.printStackTrace();
					}
				}
			}

		for(int row = 0; row < getRowCount(); row++){
			for(int col = 1; col < getColumnCount(); col++){
				cellHandler = _table.getCellEditorFor(row, col);
				try {
					cellHandler.redraw();
				} catch (NumberFormatException e) {
					Misc.showErrorMsgAndStackTrace(e, "");
					//e.printStackTrace();
				} catch (NotBoundException e) {
					Misc.showErrorMsgAndStackTrace(e, "");
					//e.printStackTrace();
				}
			}
		}

		triggerTableUpdate();
	}

	public void setTable(PhotoSpreadTable _table) {
		this._table = _table;
	}

	public String toXML(){

		StringBuffer xml = new StringBuffer();

		// Build <table numRows="..." numCols="...">
		// The '-1' subtracts the 0th column, which holds the
		// row numbers:
		xml.append("<" + TABLE_ELEMENT_NAME +
					" " + XMLProcessor.NUM_ROWS_ELEMENT + "=\"" + getRowCount() + "\"" +
					" " + XMLProcessor.NUM_COLS_ELEMENT + "=\"" + (getColumnCount() -1) + "\"" +
					">" + System.getProperty("line.separator"));
		//xml.append(PhotoSpreadHelpers.getXMLElement(NUM_ROWS_ELEMENT_NAME, this.getRowCount()));
		//xml.append(PhotoSpreadHelpers.getXMLElement(NUM_COLS_ELEMENT_NAME, this.getColumnCount()));

		for(int colNum = 1; colNum < this.getColumnCount(); colNum++){
			xml.append("<" + COL_ELEMENT_NAME + " " + 
							 COLNUM_ATTRIBUTE_NAME + "=\"" + colNum + "\"" + ">" + System.getProperty("line.separator"));
			for(int rowNum = 0; rowNum < this.getRowCount(); rowNum++){
				// Marshall one cell:
				xml.append(data.get(colNum).get(rowNum).toXML());
			}
			xml.append("</" + COL_ELEMENT_NAME + ">" + System.getProperty("line.separator"));
		}

		xml.append("</" + TABLE_ELEMENT_NAME + ">" + System.getProperty("line.separator"));
		return xml.toString();
	}

	public void toXML(BufferedWriter out){

		String xmlDump = toXML();
		try{
			out.write(xmlDump);
		}
		catch(java.io.IOException e){
			throw new RuntimeException(
					"PhotoSpread: could not write sheet to file. Error: " +
					e.getMessage());
		}
	}

	public PhotoSpreadExpression evaluate(String formula, PhotoSpreadCell cell) 
	throws FormulaSyntaxError {
		try{
			_stringReader = new java.io.StringReader( formula );
			_reader = new java.io.BufferedReader( _stringReader );
			ExpressionParser parser = new ExpressionParser(cell, formula); 
			return parser.Expression();
			
		// The first catch (TokenMgrError e) is required, because
		// it does not get caught via (Exception e). I guess the
		// automatically generated code does not inherit its exceptions
		// from Java's built-in Exception class:
			
		} catch (edu.stanford.photoSpreadParser.TokenMgrError e) {
			throw new PhotoSpreadException.FormulaSyntaxError("Invalid Formula: " + e.getMessage() + ".");
		} catch (Exception e) {
			throw new PhotoSpreadException.FormulaSyntaxError("Invalid Formula: " + e.getMessage() + ".");
		}
	}

	public void setFormulaClipboard(PhotoSpreadCell sourceCell) {
		_formulaClipboard = sourceCell;
	}
	
	public PhotoSpreadCell getFormulaClipboard() {
		return _formulaClipboard;
	}
	
	public boolean formulaClipboardEmpty() {
		return (_formulaClipboard == null);
	}
	
	public Object getClipboard() {
		return _itemsClipboard;
	}
	
	public void setClipboard(Object _clipboard) {
		this._itemsClipboard = _clipboard;
	}

	public boolean itemsClipboardEmpty() {
		return (_itemsClipboard == null);
	}
	
	
	public int getColumnCount() {
		return numCols;
		//return columnNames.size();
	}
	public int getRowCount() {
		return numRows;
	}
	
	public String getColumnName(int col) {
		return columnNames.get(col);
	}

	static public String getColumnAsString(int col){
		return Misc.intToExcelCol(col);
	}

	/**
	 * Return cell at given row/col, or null if table
	 * does not contain those cells.
	 */
	public Object getValueAt(int row, int col) {
		try {
			return data.get(col).get(row);
		} catch (IndexOutOfBoundsException e) {
		return null;
		}
	}

	public Class<?> getColumnClass(int c) {
		Object cell = getValueAt(0, c); 
		return cell.getClass();
	}

	/**
	 * Return cell object at row/col.
	 * 
	 * @param colIndex origin 1. 	 
	 * @param rowIndex origin 0.
	 * @return Cell object at row/col. 
	 */
	public PhotoSpreadCell getCell(int rowIndex, int colIndex)
	throws PhotoSpreadException.IllegalArgumentException {
		try {
			return data.get(colIndex).get(rowIndex);
		} catch (IndexOutOfBoundsException e) {
			if (rowIndex > data.size() - 1) {
				int uiRowNum = rowIndex + 1;
				throw new PhotoSpreadException.IllegalArgumentException("Row " + 
						uiRowNum + 
						" (in " + Misc.getCellAddress(rowIndex, colIndex) +
						") does not exist in this sheet.");
			} else {
				throw new PhotoSpreadException.IllegalArgumentException("Column in " + 
						 Misc.getCellAddress(rowIndex, colIndex) + " does not exist.");
			}
		}
	}
	
	/**
	 * Return cell object at rowIndex/coloIndex. Use this method
	 * when you are certain that there is no array-out-of-bounds
	 * problem with your row and column index.
	 * @param rowIndex
	 * @param colIndex
	 * @return
	 */
	public PhotoSpreadCell getCellSafely(int rowIndex, int colIndex) {
		return data.get(colIndex).get(rowIndex);
	}


	/**
	 * Return cell object at col/row. Row has origin 1; Col has origin 0
	 * This method is historic and deprecated. Use getCell() instead. 
	 * 
	 * @param colIndex
	 * @param rowIndex
	 * @return Cell object at row/col. 
	 */

	public PhotoSpreadCell getCellMixedOrigin(int colIndex, int rowIndex){
		//System.out.println("getting cell at " + colIndex + " " + rowIndex);
		return data.get(colIndex).get(rowIndex-1);
	}

	public static int getColumnFromName(String colName){
		return Misc.excelColToInt(colName);
	}

	/*
	 * Don't need to implement this method unless your table
	 * editable.
	 */
	public boolean isCellEditable(int row, int col) {
		//Note that the data/cell address is constant,
		//no matter where the cell appears onscreen.

		// Don't let user change the row number column:
		if (col < 1) {
			return false;
		} 
		return true;
	}
	/*
	 * Don't need to implement this method unless your table's
	 * data can change.
	 */
	public void setValueAt(Object value, int row, int col) {
		data.get(col).set(row, (PhotoSpreadCell) value);
		//****triggerCellUpdate(row, col);
	}

	public void triggerCellUpdate(int row, int col){
		fireTableCellUpdated(row, col);
	}

	public void triggerTableUpdate () {
		fireTableDataChanged();
	}


	public void copyToFormulaClipboard() {
		_formulaClipboard = this.getTable().getSelectedCell();
	}
	
	public void copyToClipboard(){
		_itemsClipboard = this.getTable().getSelectedCell();
;

	}
	
	public void moveSelectedObjects(
			PhotoSpreadCell srcCell, 
			PhotoSpreadCell destCell) throws IllegalArgumentException {
		moveOrCopySelectedObjects(srcCell, destCell,ObjMovements.MOVE);
	}

	public void copySelectedObjects(
			PhotoSpreadCell srcCell, 
			PhotoSpreadCell destCell) throws IllegalArgumentException {
		moveOrCopySelectedObjects(srcCell, destCell,ObjMovements.COPY);
	}
	
	/**
	 * For dnd moving of objects. The move from source cell
	 * to destination cell is NOT done if the source cell
	 * is a formula cell. You can't drag out of a formula cell.
	 * If destination is a formula cell, dropped objects are
	 * moved to the target cell, and their metadata is forced
	 * to satisfy the formula. If both source and destination
	 * are collections, the move is executed, and no metadata
	 * is changed.  
	 * NOTE this method does not update the UI.
	 * Caller must do that. 
	 * @param objects
	 * @throws IllegalArgumentException 
	 */
	
	public void moveOrCopySelectedObjects(
			PhotoSpreadCell srcCell, 
			PhotoSpreadCell destCell,
			ObjMovements moveOrCopy) throws IllegalArgumentException {
		if ((srcCell == null) ||
				(destCell == null))
			throw new RuntimeException(
					new PhotoSpreadException.DnDSourceOrDestNotSet(
							"Must set both source and destination cell before drag/drop execution."));

		boolean sourceIsFormula = srcCell.isFormulaCell();
		boolean destIsFormula = destCell.isFormulaCell();
		ArrayList<PhotoSpreadObject> objects = srcCell.getSelectedObjects();
		
		if(!sourceIsFormula && !destIsFormula){

			if (moveOrCopy == ObjMovements.MOVE)
				srcCell.removeObjects(objects);        
			destCell.addObjects(objects);

			Iterator<PhotoSpreadObject> it = objects.iterator();
			
			if (moveOrCopy == ObjMovements.MOVE)
				while(it.hasNext()){
					PhotoSpreadObject object = it.next();
					object.setCell(destCell);
				}

			try {
				srcCell.evaluate(Const.DONT_REDRAW);
			} catch (Exception e) {
				Misc.showErrorMsgAndStackTrace(e, "");
				//e.printStackTrace();
			}
			
			destCell.setFormula(Const.OBJECTS_COLLECTION_INTERNAL_TOKEN, 
					Const.DO_EVAL, 
					Const.DONT_REDRAW);
			return;
		}

		else if (sourceIsFormula && !destIsFormula) {
			// Don't move, just copy:
			destCell.addObjects(objects);
			//try {
			//	srcCell.evaluate(Const.DONT_REDRAW);
			//} catch (Exception e) {
			//	Misc.showErrorMsgAndStackTrace(e, "");
				//e.printStackTrace();
			//}
			
			destCell.setFormula(Const.OBJECTS_COLLECTION_INTERNAL_TOKEN, 
					Const.DO_EVAL, 
					Const.DONT_REDRAW);
			return;
			
		}
		else if(!sourceIsFormula && destIsFormula){
			
			// Don't move, just copy:
			destCell.addObjects(objects);
			forceObjects(destCell, objects, Const.DO_EVAL, Const.DONT_REDRAW);
			// Must re-eval the origin cell, so that its dependents get
			// updated. Else formula cells that were satisfied by the 
			// pre-forced value are not updated:
			forceObjects(srcCell, objects, Const.DO_EVAL, Const.DO_REDRAW);
			return;
		}
		
		else if(sourceIsFormula && destIsFormula){
			
			// Don't move, just copy:
			destCell.addObjects(objects);
			srcCell.removeObjects(objects);

			// Must re-eval the origin cell, so that its dependents get
			// updated. Else formula cells that were satisfied by the 
			// pre-forced value are not updated:
			
			forceObjects(srcCell, objects,  Const.DO_EVAL, Const.DO_REDRAW);

			forceObjects(destCell, objects, Const.DO_EVAL, Const.DONT_REDRAW);
			return;
		}
	}
	
	private static void forceObjects(
			PhotoSpreadCell destCell,
			ArrayList<PhotoSpreadObject> objects, 
			Boolean reEvaluateCell, 
			Boolean reDrawTable) throws IllegalArgumentException{

		if (destCell == null)
			throw new RuntimeException(
					new PhotoSpreadException.DnDSourceOrDestNotSet(
							"Must set both source and destination cell before drag/drop execution."));

		// Andreas Paepcke: Changed from ArrayList to Set. Else
		// when dragging many photos into a cell, that cell gets
		// re-evaluated as often as the number of photos being
		// added.

		// ArrayList<PhotoSpreadCell> cellsToUpdate = new ArrayList<PhotoSpreadCell>();
		// HashSet<PhotoSpreadCell> cellsToUpdate = (HashSet<PhotoSpreadCell>) Collections.synchronizedSet(new HashSet<PhotoSpreadCell>());    	
		HashSet<PhotoSpreadCell> cellsToUpdate = new HashSet<PhotoSpreadCell>();    	


		Iterator<PhotoSpreadObject> it = objects.iterator();

		while(it.hasNext()){

			PhotoSpreadObject object = it.next();
			PhotoSpreadCell updateCell = forceObject(
					object, 
					destCell,
					Const.DONT_EVAL,
					Const.DONT_REDRAW);
			if(updateCell != null){
				cellsToUpdate.add(updateCell);
			}
		}

		try {
			destCell.evaluate(reDrawTable);
		} catch (Exception e) {
			Misc.showErrorMsgAndStackTrace(e, "");
			//e.printStackTrace();
		}
		
		evaluateCells(cellsToUpdate);
	}
	
	private static PhotoSpreadCell forceObject(
			PhotoSpreadObject object, 
			PhotoSpreadCell destCell,
			Boolean reEvaluateCell, 
			Boolean reDrawTable) throws IllegalArgumentException{
		if (destCell == null)
			throw new RuntimeException(
					new PhotoSpreadException.DnDSourceOrDestNotSet(
							"Must set both source and destination cell before drag/drop execution."));
		return destCell.forceObject(object, reEvaluateCell, reDrawTable);
	}

	private static void evaluateCells(HashSet<PhotoSpreadCell> cells){
		Iterator<PhotoSpreadCell> it = cells.iterator();
		while(it.hasNext()){
			try {
				it.next().evaluate(Const.DONT_REDRAW);
			} catch (Exception e) {
				Misc.showErrorMsgAndStackTrace(e, "");
				//e.printStackTrace();
			}
		}
	}
	
}
