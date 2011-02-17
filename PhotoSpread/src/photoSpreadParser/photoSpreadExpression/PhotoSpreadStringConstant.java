package photoSpreadParser.photoSpreadExpression;

import photoSpreadObjects.PhotoSpreadObject;
import photoSpreadObjects.PhotoSpreadStringObject;
import photoSpreadTable.PhotoSpreadCell;
import photoSpreadUtilities.PhotoSpreadObjIndexerFinder;
import photoSpreadUtilities.TreeSetRandomSubsetIterable;

public class PhotoSpreadStringConstant extends PhotoSpreadConstant {
	
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
}
