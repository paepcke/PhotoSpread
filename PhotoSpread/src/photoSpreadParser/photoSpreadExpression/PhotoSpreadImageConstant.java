package photoSpreadParser.photoSpreadExpression;

import photoSpreadObjects.PhotoSpreadImage;
import photoSpreadObjects.PhotoSpreadObject;
import photoSpreadParser.photoSpreadExpression.photoSpreadFunctions.FunctionResultable;
import photoSpreadUtilities.PhotoSpreadObjIndexerFinder;
import photoSpreadUtilities.TreeSetRandomSubsetIterable;

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
}
