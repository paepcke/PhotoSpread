/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.photoSpreadParser.photoSpreadExpression;

import java.util.Iterator;

import edu.stanford.photoSpread.PhotoSpreadException.IllegalArgumentException;
import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadParser.photoSpreadExpression.PhotoSpreadSpecialConstants.PhotoSpreadNullConstant;
import edu.stanford.photoSpreadUtilities.Const;
import edu.stanford.photoSpreadUtilities.TreeSetRandomSubsetIterable;

/**
 * 
 * @author skandel
 */
abstract public class PhotoSpreadCondition extends PhotoSpreadFormulaComponent {

	protected String _lhs;
	protected String _rhs;
	protected ComparisonOperator _compOp;
	protected String _comparisionAsString;
	// Whether formula explicitly includes
	// 'null'. When this var is false, objects
	// will be ignored in solving the formula
	// if they do not have any value for a metadata
	// key that is mentioned in the formula:
	protected boolean _nullExplicit = false;
	static private String EQUALS = "=";
	static private String NOT_EQUALS = "!=";
	static private String LESS_THAN = "<";
	static private String GREATER_THAN = ">";
	static private String LESS_THAN_EQUALS = "<=";
	static private String GREATER_THAN_EQUALS = ">=";

	/****************************************************
	 * Constructor(s)
	 *****************************************************/

	public PhotoSpreadCondition(String lhs, String compOp) {

		_lhs = lhs;

		if (compOp.equals(EQUALS)) {
			this._compOp = new EqualsOperator();
		} else if (compOp.equals(NOT_EQUALS)) {
			this._compOp = new NotEqualsOperator();
		} else if (compOp.equals(LESS_THAN)) {
			this._compOp = new LessThanOperator();
		} else if (compOp.equals(GREATER_THAN)) {
			this._compOp = new GreaterThanOperator();
		} else if (compOp.equals(LESS_THAN_EQUALS)) {
			this._compOp = new LessThanEqualsOperator();
		} else if (compOp.equals(GREATER_THAN_EQUALS)) {
			this._compOp = new GreaterThanEqualsOperator();
		}

		this._comparisionAsString = compOp;
	}

	public PhotoSpreadCondition(String lhs, String compOp, PhotoSpreadNullConstant rhs) {
		// Represent the null constant as a string, so that
		// all the string comparisons will do the right
		// thing. But remember that this formula explicitly
		// asked for comparisons to null:

		this(lhs, compOp);
		_rhs = Const.NULL_VALUE_STRING;
		_nullExplicit = true;
	}

	/****************************************************
	 * Methods
	 *****************************************************/

	public String getLhs() {
		return _lhs;
	}

	public String toString() {
		return "<PhotoSpreadCondition: _lhs=" + _lhs + "; CompOp="
				+ _comparisionAsString + ">";
	}

	public String toFormula() {
		String lhs = (_lhs.equals(Const.NULL_VALUE_STRING)) ? "null" : _lhs;
		return lhs + _comparisionAsString;
	}
	
	public String copyCondition(int rowOffset, int colOffset) {
		return toFormula();
	}

	/**
	 * determines whether object satisfies this condition
	 * 
	 * @param object
	 *            the object being tested for satisfaction
	 * @return true if object satisfies this condition/false otherwise
	 * @throws IllegalArgumentException 
	 */

	abstract public boolean satisfiesCondition(PhotoSpreadObject object) throws IllegalArgumentException;

	/**
	 * determines whether object can be forced into a condition
	 * 
	 * @param object
	 *            the object being tested
	 * @return true if object can be forced/false otherwise
	 * @throws IllegalArgumentException 
	 */

	public boolean canForceObject(PhotoSpreadObject object) throws IllegalArgumentException {
		return satisfiesCondition(object)
				|| (_compOp instanceof EqualsOperator)
				|| (_compOp instanceof NotEqualsOperator);
	}

	/**
	 * determines forces object into a condition
	 * 
	 * @param object
	 *            the object being forced
	 * @throws IllegalArgumentException 
	 */

	abstract public void forceObject(PhotoSpreadObject object) throws IllegalArgumentException;

	/****************************************************
	 * ComparisonOperator Abstract Class
	 *****************************************************/

	abstract protected class ComparisonOperator {

		String _op = Const.NULL_VALUE_STRING;

		abstract public boolean satisfiesOperator(int comparision);

		abstract public boolean satisfiesOperator(String lhs, String rhs);

		abstract public boolean satisfiesOperator(String lhs,
				TreeSetRandomSubsetIterable<PhotoSpreadObject> rhs);

		public void forceObject(PhotoSpreadObject object, String lhs, String rhs) {

		}

		public void forceObject(PhotoSpreadObject object, String lhs,
				TreeSetRandomSubsetIterable<PhotoSpreadObject> rhs) {

		}

		protected String adjustCaseSensitivity(String str) {
			return str.toLowerCase();
		}

		public String toString() {
			return "<ComparisonOperator '" + _op + "'>";
		}

		/**
		 * When a formula does not explicitly mention 'null' then we want all
		 * objects that are eligible to participate in the formula's solution to
		 * have values for the metadata keys that are mentioned in the formula.
		 * If the values for those metadata keys have never been set, then we
		 * want to exclude those objects. Ex.:
		 * 
		 * =A1[Age != 10]
		 * 
		 * This formula would retrieve all objects in A1 that do not have an Age
		 * metadata. That's not what we likely want. In contrast, we *do* want
		 * to include such objects in the following case:
		 * 
		 * =A1[Age=null]
		 * 
		 * Such a formula would be used, for instance, to find all the objects
		 * whose Age hasn't been recorded yet.
		 * 
		 * @param lhs  Left hand side of the formula
		 * @param rhs  Right hand side fo the formula
		 * @return true if an object whose metadata
		 * produced one of the given values should be ignored. 
		 */
		protected boolean excludeObjFromEval(String lhs, String rhs) {
			if (_nullExplicit)
				// If formula explicitly mentioned null,
				// then all objects need to be included:
				return false;
			// Null was not mentioned in the formula. So,
			// if either of the given values is the special
			// null string, we should ignore the object:
			if ((lhs.equals(Const.NULL_VALUE_STRING))
					|| (rhs.equals(Const.NULL_VALUE_STRING)))
				return true;
			else
				return false;
		}

		/**
		 * See comment in excludeObjFromEval(String,String) above.
		 * @param value The value that is either the special null string, or not. 
		 * @return true if an object whose metadata
		 * produced one of the given values should be ignored. 
		 */
		protected boolean excludeObjFromEval(String value) {
			if (_nullExplicit)
				return false;
			if (value.equals(Const.NULL_VALUE_STRING))
				return true;
			return false;
		}
	}

	/****************************************************
	 * EqualsOperator Extends the Abstract ComparisonOperator
	 *****************************************************/

	protected class EqualsOperator extends ComparisonOperator {

		public EqualsOperator() {
			_op = "=";
		}

		public boolean satisfiesOperator(int comparison) {
			return (comparison == 0);
		}

		@Override
		public boolean satisfiesOperator(String lhs, String rhs) {
			if (excludeObjFromEval(lhs, rhs))
				return false;

			return adjustCaseSensitivity(lhs)
					.equals(adjustCaseSensitivity(rhs));
		}

		@Override
		public void forceObject(PhotoSpreadObject object, String lhs, String rhs) {
			object.setMetaData(lhs, rhs);
		}

		@Override
		public void forceObject(PhotoSpreadObject object, String lhs,
				TreeSetRandomSubsetIterable<PhotoSpreadObject> rhs) {
			if (rhs.size() == 1) {
				object.setMetaData(lhs, rhs.first().toString());
			}
		}

		@Override
		public boolean satisfiesOperator(String lhs,
				TreeSetRandomSubsetIterable<PhotoSpreadObject> rhs) {
			Iterator<PhotoSpreadObject> it = rhs.iterator();
			while (it.hasNext()) {
				PhotoSpreadObject object = it.next();
				String objAsString = object.toString();
				if (excludeObjFromEval(lhs, objAsString))
					return false;

				if (adjustCaseSensitivity(lhs).equals(
						adjustCaseSensitivity(objAsString))) {
					return true;
				}
			}
			return false;
		}
	}

	/****************************************************
	 * NotEqualsOperator Extends the Abstract ComparisonOperator
	 *****************************************************/

	protected class NotEqualsOperator extends ComparisonOperator {

		public NotEqualsOperator() {
			_op = "!=";
		}

		public boolean satisfiesOperator(int comparison) {
			return (comparison != 0);
		}

		@Override
		public boolean satisfiesOperator(String lhs, String rhs) {

			if (excludeObjFromEval(lhs, rhs))
				return false;
			return !adjustCaseSensitivity(lhs).equals(
					adjustCaseSensitivity(rhs));
		}

		@Override
		public boolean satisfiesOperator(String lhs,
				TreeSetRandomSubsetIterable<PhotoSpreadObject> rhs) {
			if (excludeObjFromEval(lhs))
				return false;
			
			return (!new EqualsOperator().satisfiesOperator(lhs, rhs));
		}
	}

	/****************************************************
	 * LessThanOperator Extends the Abstract ComparisonOperator
	 *****************************************************/

	protected class LessThanOperator extends ComparisonOperator {

		public LessThanOperator() {
			_op = "<";
		}

		public boolean satisfiesOperator(int comparison) {
			return (comparison < 0);
		}

		@Override
		public boolean satisfiesOperator(String lhs, String rhs) {

			if (excludeObjFromEval(lhs, rhs))
				return false;
			
			try {
				Double lhsDouble = Double.parseDouble(lhs);
				Double rhsDouble = Double.parseDouble(rhs);
				return lhsDouble < rhsDouble;
			} catch (NumberFormatException e) {
				return lhs.compareTo(rhs) < 0;
			}
		}

		@Override
		public boolean satisfiesOperator(String lhs,
				TreeSetRandomSubsetIterable<PhotoSpreadObject> rhs) {
			
			if (excludeObjFromEval(lhs))
				return false;

			Iterator<PhotoSpreadObject> it = rhs.iterator();
			while (it.hasNext()) {
				PhotoSpreadObject object = it.next();
				String objAsString = object.toString();
				
				if (satisfiesOperator(lhs, objAsString)) {
					return true;
				}
			}
			return false;
		}
	}

	/****************************************************
	 * GreaterThanOperator Extends the Abstract ComparisonOperator
	 *****************************************************/

	protected class GreaterThanOperator extends ComparisonOperator {

		public GreaterThanOperator() {
			_op = ">";
		}

		public boolean satisfiesOperator(int comparison) {
			return (comparison > 0);
		}

		@Override
		public boolean satisfiesOperator(String lhs, String rhs) {
			
			if (excludeObjFromEval(lhs, rhs))
				return false;

			return !(new LessThanOperator().satisfiesOperator(lhs, rhs) || new EqualsOperator()
					.satisfiesOperator(lhs, rhs));

		}

		@Override
		public boolean satisfiesOperator(String lhs,
				TreeSetRandomSubsetIterable<PhotoSpreadObject> rhs) {
			if (excludeObjFromEval(lhs))
				return false;
			boolean lessOrEqual = (new LessThanOperator().satisfiesOperator(lhs, rhs) || new EqualsOperator()
					.satisfiesOperator(lhs, rhs));
			return !lessOrEqual;

		}
	}

	/****************************************************
	 * LessThanEqualsOperator Extends the Abstract ComparisonOperator
	 *****************************************************/

	protected class LessThanEqualsOperator extends ComparisonOperator {

		public LessThanEqualsOperator() {
			_op = "<=";
		}

		public boolean satisfiesOperator(int comparison) {
			return (comparison <= 0);
		}

		@Override
		public boolean satisfiesOperator(String lhs, String rhs) {

			return new LessThanOperator().satisfiesOperator(lhs, rhs)
					|| new EqualsOperator().satisfiesOperator(lhs, rhs);
		}

		@Override
		public boolean satisfiesOperator(String lhs,
				TreeSetRandomSubsetIterable<PhotoSpreadObject> rhs) {
			if (excludeObjFromEval(lhs))
				return false;
			
			return new LessThanOperator().satisfiesOperator(lhs, rhs)
					|| new EqualsOperator().satisfiesOperator(lhs, rhs);
		}
	}

	/****************************************************
	 * GreaterThanEqualsOperator Extends the Abstract ComparisonOperator
	 *****************************************************/

	protected class GreaterThanEqualsOperator extends ComparisonOperator {

		public GreaterThanEqualsOperator() {
			_op = ">=";
		}

		public boolean satisfiesOperator(int comparison) {
			return (comparison >= 0);
		}

		@Override
		public boolean satisfiesOperator(String lhs, String rhs) {
			return new GreaterThanOperator().satisfiesOperator(lhs, rhs)
					|| new EqualsOperator().satisfiesOperator(lhs, rhs);
		}

		@Override
		public boolean satisfiesOperator(String lhs,
				TreeSetRandomSubsetIterable<PhotoSpreadObject> rhs) {
			
			if (excludeObjFromEval(lhs))
				return false;
			
			return new LessThanOperator().satisfiesOperator(lhs, rhs)
					|| new EqualsOperator().satisfiesOperator(lhs, rhs);
		}

	}
}
