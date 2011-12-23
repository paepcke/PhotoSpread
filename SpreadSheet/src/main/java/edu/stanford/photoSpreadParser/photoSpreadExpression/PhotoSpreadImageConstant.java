package edu.stanford.photoSpreadParser.photoSpreadExpression;

import edu.stanford.photoSpreadObjects.PhotoSpreadImage;
import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadParser.photoSpreadExpression.photoSpreadFunctions.FunctionResultable;
import edu.stanford.photoSpreadUtilities.PhotoSpreadObjIndexerFinder;
import edu.stanford.photoSpreadUtilities.TreeSetRandomSubsetIterable;

public class PhotoSpreadImageConstant extends PhotoSpreadConstant 
implements FunctionResultable {
	
	PhotoSpreadImage _imgObj = null;

	public PhotoSpreadImageConstant (PhotoSpreadImage imgObj) {
		_imgObj = imgObj;
	}
	
	
	@Override
	PhotoSpreadObject getObject() {
		return _imgObj;
	}

	@Override
	TreeSetRandomSubsetIterable<PhotoSpreadObject> getObjects() {
		TreeSetRandomSubsetIterable<PhotoSpreadObject> res =
			new TreeSetRandomSubsetIterable<PhotoSpreadObject>();
		res.setIndexer(new PhotoSpreadObjIndexerFinder());
		
		res.add(getObject());
		return res;
	}

	@Override
	public Object valueOf() {
		return _imgObj;
	}
	
	public String toFormula() {
		return ("<image>");
	}
}
