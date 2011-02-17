/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package photoSpreadParser.photoSpreadExpression;

import java.util.ArrayList;

import photoSpreadObjects.PhotoSpreadObject;
import photoSpreadParser.photoSpreadNormalizedExpression.PhotoSpreadNormalizedExpression;
import photoSpreadTable.PhotoSpreadCell;
import photoSpreadTable.PhotoSpreadTableModel;
import photoSpreadUtilities.Misc;
import photoSpreadUtilities.PhotoSpreadObjIndexerFinder;
import photoSpreadUtilities.TreeSetRandomSubsetIterable;

/**
 *
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
		System.out.println(cellRange.substring(1, 2));
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

	public ArrayList<PhotoSpreadCell> getCells(PhotoSpreadCell cell){
		ArrayList<PhotoSpreadCell> cells = new ArrayList<PhotoSpreadCell>();
		PhotoSpreadTableModel table = cell.getTableModel();

		for(int col = this._startColIndex; col <= this._endColIndex; col++){
			for(int row = this._startRowIndex; row <= this._endRowIndex; row++){
				cells.add(table.getCellMixedOrigin(col, row));

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
				PhotoSpreadCell nextCell = table.getCellMixedOrigin(col, row);
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

			cellRange += PhotoSpreadTableModel.getColumnAsString(this._startColIndex+colOffset);
		}
		if(_startRowFixed){
			cellRange += PhotoSpreadCellRange.DOLLAR + this._startRowIndex;
		}
		else{
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

	public TreeSetRandomSubsetIterable<PhotoSpreadObject> evaluate(PhotoSpreadCell cell) {

		TreeSetRandomSubsetIterable<PhotoSpreadObject> objects = 
			new TreeSetRandomSubsetIterable<PhotoSpreadObject>(
					photoSpreadUtilities.
					PhotoSpreadComparatorFactory.
					createPSMetadataComparator());
		
		objects.setIndexer(new PhotoSpreadObjIndexerFinder());
		
		PhotoSpreadTableModel table;
		
		table = cell.getTableModel();
		PhotoSpreadCell c;

		for(int col = this._startColIndex; col <= this._endColIndex; col++){
			for(int row = this._startRowIndex; row <= this._endRowIndex; row++){

				c = table.getCellMixedOrigin(col, row);
				c.addDependent(cell);
				cell.addReference(c);
				objects.addAll(c.getObjects());
			}
		}
		return objects;
	}
}
