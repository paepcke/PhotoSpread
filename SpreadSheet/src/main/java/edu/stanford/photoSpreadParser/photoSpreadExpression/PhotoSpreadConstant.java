package edu.stanford.photoSpreadParser.photoSpreadExpression;

import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadParser.photoSpreadExpression.photoSpreadFunctions.FunctionResultable;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;
import edu.stanford.photoSpreadUtilities.TreeSetRandomSubsetIterable;


public abstract class PhotoSpreadConstant extends PhotoSpreadConstantExpression
implements FunctionResultable, PhotoSpreadComputable {
	
	protected PhotoSpreadCell _cell;

	public PhotoSpreadConstant() {
		super();
	}
	
	/**
	 * @return PhotoSpreadObject that wraps this real-world constant, which might be a 
	 * Double, a String, etc.
	 */
	abstract PhotoSpreadObject getObject();
	
	/**
	 * @return Set of PhotoSpreadObject that wrap real-world constants. If
	 * the callee only contains a single such object (e.g. PhotoSpreadStringConstant)
	 * then the set will contain only that single object.
	 */
	abstract TreeSetRandomSubsetIterable<PhotoSpreadObject> getObjects();
	
	/**
	 * @return the real-world Java quantity that is wrapped by this PhotoSpreadConstant grammar entity.
	 */
	public abstract Object valueOf();
	
	/**
	 * Return an object that can be used as the return
	 * result of a PhotoSpread function. All subclasses
	 * of PhotoSpreadConstant have that capability without 
	 * doing any work.
	 * 
	 * @return This very instance.
	 */
	public PhotoSpreadConstant getAsConstant() {
		return this;
	}
}
