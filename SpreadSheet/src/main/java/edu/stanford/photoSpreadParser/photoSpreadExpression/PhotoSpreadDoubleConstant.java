package edu.stanford.photoSpreadParser.photoSpreadExpression;

import edu.stanford.photoSpreadObjects.PhotoSpreadDoubleObject;
import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadParser.photoSpreadExpression.PhotoSpreadConstant;
import edu.stanford.photoSpreadParser.photoSpreadExpression.photoSpreadFunctions.FunctionResultable;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;
import edu.stanford.photoSpreadUtilities.PhotoSpreadObjIndexerFinder;
import edu.stanford.photoSpreadUtilities.TreeSetRandomSubsetIterable;

public class PhotoSpreadDoubleConstant extends PhotoSpreadConstant 
implements FunctionResultable,
		   Comparable<PhotoSpreadDoubleConstant> {
	
	
	Double _number = 0.0;
	
	/****************************************************
	 * Constructors
	 *****************************************************/
	
	public PhotoSpreadDoubleConstant (PhotoSpreadCell cell, String numStr) {
		this(cell, Double.valueOf(numStr));
	}
	
	public PhotoSpreadDoubleConstant (PhotoSpreadCell cell, Double num) {
		_number = num;
		_cell = cell;
	}
	
	/****************************************************
	 * Methods
	 *****************************************************/

	
	public String toString () {
		return "<PhotoSpreadDoubleConstant (" + _number + ")>";
	}

	public PhotoSpreadObject getObject () {
		return new PhotoSpreadDoubleObject (_cell, _number);
	}
	
	public TreeSetRandomSubsetIterable<PhotoSpreadObject> getObjects() {
		TreeSetRandomSubsetIterable<PhotoSpreadObject> res =
			new TreeSetRandomSubsetIterable<PhotoSpreadObject>();
		res.setIndexer(new PhotoSpreadObjIndexerFinder());
		res.add(getObject());
		return res;
	}
	
	@Override
	public Double valueOf () {
		return _number;
	}
	
	public void setValue (Double value) {
		_number = value;
	}

	public Double toDouble() {
		return _number;
	}

	public int compareTo(PhotoSpreadDoubleConstant psDoubleConst) {
		return valueOf().compareTo(psDoubleConst.valueOf());
	}
}
