/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

//

package edu.stanford.photoSpreadParser.photoSpreadExpression;

import java.security.InvalidParameterException;
import java.util.ArrayList;

import edu.stanford.photoSpread.PhotoSpreadException.IllegalArgumentException;
import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadParser.photoSpreadNormalizedExpression.PhotoSpreadNormalizedExpression;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;
import edu.stanford.photoSpreadTable.PhotoSpreadTableModel;
import edu.stanford.photoSpreadUtilities.Misc;
import edu.stanford.photoSpreadUtilities.PhotoSpreadObjIndexerFinder;
import edu.stanford.photoSpreadUtilities.TreeSetRandomSubsetIterable;

/**
 * Dec 22, 2011: Fixed incorrect parameter order in calls to 
 *                PhotoSpreadTableModel.getCellSafely(row, col).
 *                These calls affected getCells() and normalize();
 *                Andreas Paepcke
 * @author skandel
 */
public class PhotoSpreadCellRange implements PhotoSpreadNormalizable {

	String _cellRange;
	int _startRowIndex;
	int _startColIndex;
	int _endRowIndex;
	int _endColIndex;

	boolean _startColFixed;
	boolean _startRowFixed;
	boolean _endColFixed;
	boolean _endRowFixed;

	static public String DOLLAR = "$";

	/****************************************************
	 * Constructors
	 *****************************************************/
	
	public PhotoSpreadCellRange(
			boolean _startColFixed, 
			String _startCol, 
			boolean _startRowFixed, 
			int _startRow, 
			boolean _endColFixed, 
			String _endCol, 
			boolean _endRowFixed, 
			int _endRow) {

		this._startColFixed = _startColFixed;
		this._endColFixed = _endColFixed;
		this._startRowFixed = _startRowFixed;
		this._endRowFixed = _endRowFixed;

		this._startRowIndex = _startRow;
		this._startColIndex = PhotoSpreadTableModel.getColumnFromName(_startCol);
		this._endRowIndex = _endRow;
		this._endColIndex = PhotoSpreadTableModel.getColumnFromName(_endCol);

		_cellRange = _startCol + _startRow + ":" + _endCol + _endRow;


	}

	public PhotoSpreadCellRange( 
			String _startCol,
			int _startRow,  
			String _endCol, 
			int _endRow) {

		this(false, _startCol, false, _startRow, false, _endCol, false, _endRow);
	}

	public PhotoSpreadCellRange(
			boolean _startColFixed, 
			String _startCol, 
			boolean _startRowFixed, 
			int _startRow) {
		
		this._startColFixed = _startColFixed;
		this._startRowFixed = _startRowFixed;
		this._endColFixed = _startColFixed;
		this._endRowFixed = _startRowFixed;


		this._startRowIndex = _startRow;
		this._startColIndex = PhotoSpreadTableModel.getColumnFromName(_startCol);
		this._endRowIndex = this._startRowIndex;
		this._endColIndex = this._startColIndex;

		_cellRange = _startCol + _startRow;


	}

	public PhotoSpreadCellRange(String _startCol, int _startRow) {
		this._startRowIndex = _startRow;
		this._startColIndex = PhotoSpreadTableModel.getColumnFromName(_startCol);
		this._endRowIndex = this._startRowIndex;
		this._endColIndex = this._startColIndex;

		_cellRange = _startCol + _startRow;
	}

	public PhotoSpreadCellRange(String cellRange) {
		this._startRowIndex = Integer.parseInt(cellRange.substring(1,2));
		this._startColIndex = PhotoSpreadTableModel.getColumnFromName(cellRange.substring(0,1));

		if(cellRange.length() > 2){

			this._endRowIndex = Integer.parseInt(cellRange.substring(4,5));
			this._endColIndex = PhotoSpreadTableModel.getColumnFromName(cellRange.substring(3,4));
		}
		else{

			this._endRowIndex = this._startRowIndex;
			this._endColIndex = this._startColIndex;   
		}
		_cellRange = cellRange;
	}

	/****************************************************
	 * Methods
	 *****************************************************/

	
	@Override
	public String toString() {

		// return _cellRange;
		return "<PhotoSpreadCellRange " + 
				Misc.getCellAddress(_startRowIndex - 1, _startColIndex) + 
				":" + 
				Misc.getCellAddress(_endRowIndex - 1, _endColIndex) + 
				">";
	}

	public String toFormula() {
		
		String res = _startColFixed ? "$" : "";
		res += Misc.intToExcelCol(_startColIndex);
		res += _startRowFixed ? "$" : "";
		res += _startRowIndex;
		
		if ((_endRowIndex == _startRowIndex) &&
			(_endColIndex == _startColIndex))
			return res;
		
		res += ":";
		res += _endColFixed ? "$" : "";
		res += Misc.intToExcelCol(_endColIndex);
		res += _endRowFixed ? "$" : "";
		res += _endRowIndex;
		
		return res;
	}
	
	public ArrayList<PhotoSpreadCell> getCells(PhotoSpreadCell cell){
		ArrayList<PhotoSpreadCell> cells = new ArrayList<PhotoSpreadCell>();
		PhotoSpreadTableModel table = cell.getTableModel();

		for(int col = this._startColIndex; col <= this._endColIndex; col++){
			for(int row = this._startRowIndex; row <= this._endRowIndex; row++){
				cells.add(table.getCellSafely(row-1, col));

			}
		}
		return cells;
	}

	public PhotoSpreadNormalizedExpression normalize(PhotoSpreadCell cell) {

		PhotoSpreadNormalizedExpression normalizedExpression = 
			new PhotoSpreadNormalizedExpression();


		PhotoSpreadTableModel table = cell.getTableModel();

		for(int col = this._startColIndex; col <= this._endColIndex; col++){
			for(int row = this._startRowIndex; row <= this._endRowIndex; row++){
				PhotoSpreadCell nextCell = table.getCellSafely(row-1, col);
				PhotoSpreadNormalizedExpression ne = nextCell.getNormalizedExpression();
				try{
					normalizedExpression.union( (PhotoSpreadNormalizedExpression) ne.clone());
				}
				catch(CloneNotSupportedException e){
					System.out.println("Cloning failed in PhotoSpreadCellRange.java in normalize");
				}
			}
		}

		return normalizedExpression;
	}

	public String copyCellRange(int rowOffset, int colOffset){
		String cellRange = "";

		if(_startColFixed){
			cellRange += PhotoSpreadCellRange.DOLLAR + PhotoSpreadTableModel.getColumnAsString(this._startColIndex);

		}else{
			int newCol = this._startColIndex+colOffset;
			if (newCol < 1) {
				throw new InvalidParameterException("Cell column offset improperly places cell 'to the left of' column 'A'.");
			}
			cellRange += PhotoSpreadTableModel.getColumnAsString(this._startColIndex+colOffset);
		}
		if(_startRowFixed){
			cellRange += PhotoSpreadCellRange.DOLLAR + this._startRowIndex;
		}
		else{
			int newRow = this._startRowIndex + rowOffset;
			if (newRow < 1) {
				throw new InvalidParameterException("Cell row offset improperly places cell below row zero.");
			}
			cellRange += this._startRowIndex + rowOffset;
		}
		if(_cellRange.contains(":")){
			if(_endColFixed){
				cellRange += PhotoSpreadCellRange.DOLLAR + PhotoSpreadTableModel.getColumnAsString(this._endColIndex);
			}
			else{
				cellRange += PhotoSpreadTableModel.getColumnAsString(this._endColIndex+colOffset);
			}
			if(_endRowFixed){
				cellRange += PhotoSpreadCellRange.DOLLAR + this._endRowIndex;
			}
			else{
				cellRange += this._endRowIndex + rowOffset;
			}
		}

		return cellRange;
	}

	public TreeSetRandomSubsetIterable<PhotoSpreadObject> evaluate(PhotoSpreadCell cell) throws IllegalArgumentException {

		TreeSetRandomSubsetIterable<PhotoSpreadObject> objects = 
			new TreeSetRandomSubsetIterable<PhotoSpreadObject>(
					edu.stanford.photoSpreadUtilities.
					PhotoSpreadComparatorFactory.
					createPSMetadataComparator());
		
		objects.setIndexer(new PhotoSpreadObjIndexerFinder());
		
		PhotoSpreadTableModel table;
		
		table = cell.getTableModel();
		PhotoSpreadCell c;

		for(int col = this._startColIndex; col <= this._endColIndex; col++){
			for(int row = this._startRowIndex; row <= this._endRowIndex; row++){

				c = table.getCell(row-1, col);
				c.addDependent(cell);
				cell.addReference(c);
				objects.addAll(c.getObjects());
			}
		}
		return objects;
	}
}
