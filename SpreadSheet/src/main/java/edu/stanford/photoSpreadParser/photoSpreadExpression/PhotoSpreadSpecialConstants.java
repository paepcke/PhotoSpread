package edu.stanford.photoSpreadParser.photoSpreadExpression;

import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;
import edu.stanford.photoSpreadUtilities.Const;
import edu.stanford.photoSpreadUtilities.TreeSetRandomSubsetIterable;

abstract public class PhotoSpreadSpecialConstants extends PhotoSpreadConstant {

	
	@Override
	PhotoSpreadObject getObject() {
		return null;
	}

	@Override
	public Object valueOf() {
		return null;
	}
	
	/****************************************************
	 * Inner Class ObjectsCollection
	 *****************************************************/

	public static class ObjectsCollectionConstant extends PhotoSpreadSpecialConstants {
		
		public ObjectsCollectionConstant (PhotoSpreadCell cell) {
			super();
			_cell = cell;
		}
		
		@Override
		public TreeSetRandomSubsetIterable<PhotoSpreadObject> evaluate(PhotoSpreadCell cell) {
			return cell.getObjects();
		}
		
		public String toString() {
			return Const.OBJECTS_COLLECTION_PUBLIC_TOKEN;
		}
		
		public TreeSetRandomSubsetIterable<PhotoSpreadObject> getObjects() {
			return _cell.getObjects();
		}
		
		public Object valueOf() {
			return _cell.getObjects();
		}
	}
	
	public final static class PhotoSpreadNullConstant extends PhotoSpreadSpecialConstants {

		private static PhotoSpreadNullConstant instance = null;
		
		private PhotoSpreadNullConstant() {
			// Exists only to defeat instantiation.
		}
		
		public static PhotoSpreadNullConstant getInstance() {
			if(instance == null) {
				instance = new PhotoSpreadNullConstant();
			}
			return instance;
		}
		
		@Override
		TreeSetRandomSubsetIterable<PhotoSpreadObject> getObjects() {
			return null;
		}
		
		@Override
		public Object valueOf() {
			return this;
		}
		
		public String toString() {
			return "null";
		}
		
		/**
		 * We use polymorphism for the implementation.
		 * @param arg
		 * @return true if the given object is a null constant.
		 */
		public boolean equals(PhotoSpreadNullConstant arg) {
			return true;
		}
		
		public boolean equals(Object arg) {
			return false;
		}
	}
}
