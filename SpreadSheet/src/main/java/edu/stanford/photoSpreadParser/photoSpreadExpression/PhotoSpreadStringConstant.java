package edu.stanford.photoSpreadParser.photoSpreadExpression;

import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadObjects.PhotoSpreadStringObject;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;
import edu.stanford.photoSpreadUtilities.PhotoSpreadObjIndexerFinder;
import edu.stanford.photoSpreadUtilities.TreeSetRandomSubsetIterable;

public class PhotoSpreadStringConstant extends PhotoSpreadConstant
	implements Comparable<PhotoSpreadStringConstant> {
	
	String _str = "";
	
	public PhotoSpreadStringConstant (PhotoSpreadCell cell, String str) {
		_str = str;
		_cell = cell;
	}
	
	public String toString () {
		return "<PhotoSpreadStringConstant '" + _str + "'>";
	}
	
	public PhotoSpreadObject getObject () {
		return new PhotoSpreadStringObject (_cell, _str);
	}
	
	public TreeSetRandomSubsetIterable<PhotoSpreadObject> getObjects() {
		TreeSetRandomSubsetIterable<PhotoSpreadObject> res =
			new TreeSetRandomSubsetIterable<PhotoSpreadObject>();
		res.setIndexer(new PhotoSpreadObjIndexerFinder());
		res.add(getObject());
		return res;
	}

	@Override
	public String valueOf () {
		return _str;
	}

	public int compareTo(PhotoSpreadStringConstant psStrConst) {
		return _str.compareTo(psStrConst.valueOf());
	}
}
