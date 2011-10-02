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

import edu.stanford.photoSpread.PhotoSpread;
import edu.stanford.photoSpread.PhotoSpreadException;
import edu.stanford.photoSpread.PhotoSpreadException.FormulaSyntaxError;
import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadObjects.PhotoSpreadStringObject;
import edu.stanford.photoSpreadParser.ExpressionParser;
import edu.stanford.photoSpreadParser.photoSpreadExpression.PhotoSpreadExpression;
import edu.stanford.photoSpreadUtilities.Const;
import edu.stanford.photoSpreadUtilities.Misc;
import edu.stanford.photoSpreadUtilities.Const.ObjMovements;
/**
 *
 * @author skandel
 */
public class PhotoSpreadTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	public static final int minDroppableColumn = 1;
	public static final int minDroppableRow = 0;

	static private String TABLE_ELEMENT_NAME = "table";
	static private String ROW_ELEMENT_NAME = "row";
	private ArrayList<String> columnNames;

	private Object _clipboard = null;
	@SuppressWarnings("unused")
	private java.io.Reader _reader;
	private java.io.StringReader _stringReader;
	private ArrayList<ArrayList<PhotoSpreadCell>> data;
	private PhotoSpreadTable _table;


	public PhotoSpreadTableModel() {
		columnNames = new ArrayList<String>();
		columnNames.add("");

		_stringReader = new java.io.StringReader( "" );
		_reader = new java.io.BufferedReader( _stringReader );
		// _parser = new ExpressionParser(_reader);
		initializeData();
		//loadTestData();
	}

	private void initializeData(){

		// The the trusted versions, b/c we did 
		// all checking at startup time: 
		int numRows = 0;
		int numCols = 0;
		numRows = PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.sheetNumRowsKey);
		numCols = PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.sheetNumColsKey);

		data = new ArrayList<ArrayList<PhotoSpreadCell>>();
		for(int col = 0; col < numCols; col++){
			columnNames.add(getColumnAsString(col+1));
			data.add(new ArrayList<PhotoSpreadCell>());
			PhotoSpreadCell cell = new PhotoSpreadCell(this, col, 0, "");
			cell.addObject(new PhotoSpreadStringObject(cell, ""+ (col+1)));
			data.get(col).add(cell);
			for(int row = 0; row < numRows; row++){
				data.get(col).add(new PhotoSpreadCell(this, col, row+1, ""));
			}
		}
	}

	/**
	 * Clear the table data, triggering cell-changed-events
	 * along the way.
	 */
	public void clear(){

		for(int row = 0; row < this.getRowCount(); row++){
			for(int col = 1; col < this.getColumnCount(); col++){
				this.getCell(row, col).clear(Const.DONT_EVAL, Const.DONT_REDRAW);
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
			for(int row = 0; row < data.size(); row++){
				for(int col = 1; col < data.get(row).size(); col++){
					try {
						data.get(row).get(col).evaluate(Const.DONT_REDRAW);
					} catch (Exception e) {
						Misc.showErrorMsgAndStackTrace(e, "");
						//e.printStackTrace();
					}
				}
			}

		for(int row = 0; row < data.size(); row++){
			for(int col = 1; col < data.get(row).size(); col++){
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

		xml.append("<" + TABLE_ELEMENT_NAME + ">" + System.getProperty("line.separator"));
		//xml.append(PhotoSpreadHelpers.getXMLElement(NUM_ROWS_ELEMENT_NAME, this.getRowCount()));
		//xml.append(PhotoSpreadHelpers.getXMLElement(NUM_COLS_ELEMENT_NAME, this.getColumnCount()));

		for(int i = 0; i < data.size(); i++){
			xml.append("<" + ROW_ELEMENT_NAME + ">" + System.getProperty("line.separator"));
			for(int j = 1; j < data.get(i).size(); j++){
				xml.append(data.get(i).get(j).toXML());
			}
			xml.append("</" + ROW_ELEMENT_NAME + ">" + System.getProperty("line.separator"));
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

	public Object getClipboard() {
		return _clipboard;
	}
	public void setClipboard(Object _clipboard) {

		this._clipboard = _clipboard;
	}

	public int getColumnCount() {

		return columnNames.size();
	}
	public int getRowCount() {
		return data.size();
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
			return data.get(row).get(col);
		} catch (IndexOutOfBoundsException e) {
		return null;
		}
	}

	public Class<?> getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	/**
	 * Return cell object at row/col, both with origin 0
	 * 
	 * @param colIndex
	 * @param rowIndex
	 * @return Cell object at row/col. 
	 */
	public PhotoSpreadCell getCell(int rowIndex, int colIndex){
		return data.get(rowIndex).get(colIndex);	
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
		return data.get(rowIndex-1).get(colIndex);
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
		data.get(row).set(col, (PhotoSpreadCell) value);
		//****triggerCellUpdate(row, col);
	}

	public void triggerCellUpdate(int row, int col){
		fireTableCellUpdated(row, col);
	}

	public void triggerTableUpdate () {
		fireTableDataChanged();
	}


	public void copyToClipboard(){
		/*
		for(int i = 0; i < this._table.getSelectedColumnCount(); i ++){
			System.out.println(this._table.getSelectedColumns()[i]);
		}
		for(int i = 0; i < this._table.getSelectedRowCount(); i ++){
			System.out.println(this._table.getSelectedRows()[i]);
		}
		*/
		PhotoSpreadCell cell = data.get(this.getTable().getSelectedRow()).get(this.getTable().getSelectedColumn());
		_clipboard = cell;

	}
	
	public void moveSelectedObjects(
			PhotoSpreadCell srcCell, 
			PhotoSpreadCell destCell) {
		moveOrCopySelectedObjects(srcCell, destCell,ObjMovements.MOVE);
	}

	public void copySelectedObjects(
			PhotoSpreadCell srcCell, 
			PhotoSpreadCell destCell) {
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
	 */
	
	public void moveOrCopySelectedObjects(
			PhotoSpreadCell srcCell, 
			PhotoSpreadCell destCell,
			ObjMovements moveOrCopy) {
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

		else if(destIsFormula){
			forceObjects(destCell, objects, Const.DO_EVAL, Const.DONT_REDRAW);
			return;
		}
		Misc.showErrorMsg("Cannot drag out of a formula cell.", PhotoSpread.getCurrentSheetWindow());
	}
	
	private static void forceObjects(
			PhotoSpreadCell destCell,
			ArrayList<PhotoSpreadObject> objects, 
			Boolean reEvaluateCell, 
			Boolean reDrawTable){

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
			Boolean reDrawTable){
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
