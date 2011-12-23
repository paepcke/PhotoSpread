/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.photoSpreadParser.photoSpreadExpression;

import java.util.ArrayList;
import java.util.HashMap;

import edu.stanford.photoSpread.PhotoSpread;
import edu.stanford.photoSpread.PhotoSpreadException;
import edu.stanford.photoSpread.PhotoSpreadException.IllegalArgumentException;
import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadObjects.PhotoSpreadStringObject;
import edu.stanford.photoSpreadParser.photoSpreadExpression.photoSpreadFunctions.PhotoSpreadFunction;
import edu.stanford.photoSpreadParser.photoSpreadNormalizedExpression.PhotoSpreadNormalizedExpression;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;
import edu.stanford.photoSpreadUtilities.Const;
import edu.stanford.photoSpreadUtilities.PhotoSpreadObjIndexerFinder;
import edu.stanford.photoSpreadUtilities.TreeSetRandomSubsetIterable;

/**
 * 
 * @author skandel
 */
abstract public class PhotoSpreadFormulaExpression extends
		PhotoSpreadExpression implements PhotoSpreadEvaluatable {

	// Strings that the parser delivers when calling
	// addCondition(). These constants indicate whether
	// the condition is to be ANDed or ORed with the
	// previous condition, or whether it is to be
	// Negated:
	private final String PARSED_AND_CONDITION = "AND";
	private final String PARSED_OR_CONDITION = "OR";

	// For niceness and speed in the code below,
	// we use an enum in place of the clumsy
	// strings above:
	private enum ConditionBoolConnector {
		NO_BOOLEAN, AND, OR
	}

	private HashMap<PhotoSpreadCondition, ConditionBoolConnector> conditionsBoolConnectors = new HashMap<PhotoSpreadCondition, ConditionBoolConnector>();

	String _info = Const.NULL_VALUE_STRING;
	PhotoSpreadFunction _func = null;
	ArrayList<PhotoSpreadCondition> _conditions = new ArrayList<PhotoSpreadCondition>();
	String _selection = null;

	public PhotoSpreadFormulaExpression() {
		this("", null);
	}

	public PhotoSpreadFormulaExpression(String info,
			PhotoSpreadFunction function) {
		super();
		_info = info;
		_func = function;
	}

	public String toString() {

		String res = "";

		res += "<PhotoSpreadFormulaExpression (" + _info + "); " + _func;
		if (_conditions.size() > 0) {
			res += "[";
			for (PhotoSpreadCondition cond : _conditions) {
				if (cond != _conditions.get(0))
					res += " & ";
				res += cond;
			}
			res += "]";
		}
		if (_selection.length() > 0)
			res += "." + _selection;
		return res;
	}
	
	public String toFormula() {
		String res = _func.toFormula();
		if (_conditions.size() > 0) {
			res += "[";
			for (PhotoSpreadCondition cond : _conditions) {
				if (cond != _conditions.get(0))
					res += " & ";
				res += cond;
			}
			res += "]";
		}
		if (_selection.length() > 0)
			res += "." + _selection;
		return res;
	}

	protected String conditionsAndSelectionToString(int rowOffset, int colOffset) {
		String str = "";
		if (_conditions != null) {
			if (_conditions.size() > 0) {
				str += "[";
				str += _conditions.get(0).copyCondition(rowOffset, colOffset);
				for (int i = 1; i < _conditions.size(); i++) {
					str += " & "
							+ _conditions.get(i).copyCondition(rowOffset,
									colOffset);
				}
				str += "]";
			}

		}
		if (_selection != null) {
			str += "." + _selection;
		}

		return str;
	}

	abstract public PhotoSpreadNormalizedExpression normalize(
			PhotoSpreadCell cell);

	abstract public TreeSetRandomSubsetIterable<PhotoSpreadObject> evaluate(
			PhotoSpreadCell cell) throws PhotoSpreadException.FormulaError, IllegalArgumentException;

	/**
	 * Add a dot expression selection to the expression: A1.age
	 * 
	 * @param selection
	 *            to be added to the expression
	 */
	public void addSelection(String selection) {
		this._selection = selection;
		PhotoSpread.trace("Adding selection " + selection + " to" + this);
	}

	/**
	 * Add a bracket condition to an expression: A1[age>20] This method is
	 * called from the auto-generated parser, which is driven by the file
	 * PhotoSpreadParser.jj.
	 * 
	 * The condition is added to a list of conditions. Through a HashMap, the
	 * boolean connective, if any, is associated with this condition (AND, OR,
	 * NOT, or NO_BOOLEAN).
	 * 
	 * This information is used in expression evaluation later on
	 * (applyConditionsAndSelection()).
	 * 
	 * @param condition
	 *            condition to be added to the expression
	 * @param whether
	 *            to AND, OR, or Negate this condition. Values are one of
	 *            PARSED_AND_CONDITION, PARSED_OR_CONDITION,
	 *            PARSED_NOT_CONDITION, or PARSED_NO_BOOLEAN_CONDITION
	 * */

	public void addCondition(PhotoSpreadCondition condition,
			String parsedConnective) {
		this._conditions.add(condition);

		ConditionBoolConnector theConnective;
		if (parsedConnective.equals(PARSED_AND_CONDITION))
			theConnective = ConditionBoolConnector.AND;
		else if (parsedConnective.equals(PARSED_OR_CONDITION))
			theConnective = ConditionBoolConnector.OR;
		else
			theConnective = ConditionBoolConnector.NO_BOOLEAN;

		this.conditionsBoolConnectors.put(condition, theConnective);
		PhotoSpread.trace(
				"Adding condition " + 
				condition + 
				" to " + 
				this + 
				". Connective: " + parsedConnective);
	}

	/**
	 * Add multiple conditions to expression
	 * 
	 * @param conditions
	 *            conditions to be added to the expression
	 */

	public void addConditions(ArrayList<PhotoSpreadCondition> conditions) {
		this._conditions.addAll(conditions);
		PhotoSpread.trace("Adding conditions " + conditions + " to " + this);
	}

	protected TreeSetRandomSubsetIterable<PhotoSpreadObject> applyConditionsAndSelection(
			TreeSetRandomSubsetIterable<PhotoSpreadObject> objects) throws IllegalArgumentException {

		// For remembering whether a condition is ORed, ANDed, etc.:
		ConditionBoolConnector booleanConnector = null;
		// Whether one object satisfies one condition
		boolean doesSatisfy = false;

		// NOTE: we are indexing some of these objects twice here,
		// once when we satisfy conditions, and then again
		// when we do the selections. Could likely be optimized.

		// Destination for objects that satisfy this formula
		// expression's conditions.

		TreeSetRandomSubsetIterable<PhotoSpreadObject> satisfyingObjects = null;

		// Note: we had a bug here before: we
		// just removed *non*-satisfying objects
		// from the passed-in objects collection
		// in the 'conditions' inner loop below.
		// That's wrong, because we were modifying
		// the collection that we were iterating over.
		// So, unless there are no conditions, we
		// now copy the objs that do satisfy from 'objects'
		// to 'satisfyingObjects':

		if (_conditions.isEmpty())
			satisfyingObjects = objects;
		else {
			satisfyingObjects = new TreeSetRandomSubsetIterable<PhotoSpreadObject>();
			satisfyingObjects.setIndexer(new PhotoSpreadObjIndexerFinder());
		}

		// This for loop is the heart of expression evaluation.
		// We run through each object and check whether it should
		// be in the result set or not:
		for (PhotoSpreadObject obj : objects) {
			
			boolean objInResultSet = false;
			
			for (PhotoSpreadCondition cond : _conditions) {
				
				// Is this condition to be ANDed or ORed with
				// previous conditions?
				booleanConnector = conditionsBoolConnectors.get(cond);
				
				// Evaluate the condition:
				doesSatisfy = cond.satisfiesCondition(obj);

				if (doesSatisfy)
					switch (booleanConnector) {
					case OR:
						// This object is a winner:
						satisfyingObjects.add(obj);
						break; // stop evaluating conditions; go on to next object
					case NO_BOOLEAN:
						// So far, obj qualifies:
						objInResultSet = true;
						continue;
					case AND:
						// We leave objInResultSet alone.
						// This condition does not *dis*-qualify the obj.
						continue;
					default:
						// can't be anything else.
					}
				else // obj does NOT satisfy this condition.
					switch (booleanConnector) {
					case OR:
						// We leave objInResultSet alone.
						// This condition does not newly qualify the obj.
						continue;
					case NO_BOOLEAN:
						// So far, obj does not get into the
						// result set. But we have to keep going,
						// in case an 'OR' condition comes later:
						objInResultSet = false;
						continue;
					case AND:
						// Same as NO_BOOLEAN:
						objInResultSet = false;
						continue;
					}
			}
			if (objInResultSet)
				satisfyingObjects.add(obj);
		}
		
		// Now we have the set of objects that satisfy
		// the formula. Next, pull out the attribute
		// values that were requested for selection
		// e.g. the year in: (=A1[Attr=value].Year
		if (this._selection == null)
			return satisfyingObjects;

		TreeSetRandomSubsetIterable<PhotoSpreadObject> selections = new TreeSetRandomSubsetIterable<PhotoSpreadObject>(
				edu.stanford.photoSpreadUtilities.PhotoSpreadComparatorFactory
				.createPSMetadataComparator());
		selections.setIndexer(new PhotoSpreadObjIndexerFinder());

		for (PhotoSpreadObject obj : satisfyingObjects) {
			String selection = obj.getMetaData(_selection);
			if (!selection.equals(Const.NULL_VALUE_STRING))
				// We *don't* want to uniquify selections. Each
				// selection, list A3.name or C4.name needs to be
				// separate objects, even if they are the same strings.
				// So we *do* create a new object each time:
				selections.add(new PhotoSpreadStringObject(obj.getCell(),
						selection));
		}

		return selections;
	}
}
