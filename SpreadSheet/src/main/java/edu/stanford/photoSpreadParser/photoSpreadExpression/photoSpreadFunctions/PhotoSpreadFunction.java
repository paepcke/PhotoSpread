/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.photoSpreadParser.photoSpreadExpression.photoSpreadFunctions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import edu.stanford.photoSpread.PhotoSpread;
import edu.stanford.photoSpread.PhotoSpreadException;
import edu.stanford.photoSpread.PhotoSpreadException.FormulaArgumentsError;
import edu.stanford.photoSpread.PhotoSpreadException.FormulaError;
import edu.stanford.photoSpread.PhotoSpreadException.IllegalArgumentException;
import edu.stanford.photoSpreadObjects.PhotoSpreadDoubleObject;
import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadObjects.PhotoSpreadStringObject;
import edu.stanford.photoSpreadParser.photoSpreadExpression.PhotoSpreadComputable;
import edu.stanford.photoSpreadParser.photoSpreadExpression.PhotoSpreadContainerExpression;
import edu.stanford.photoSpreadParser.photoSpreadExpression.PhotoSpreadDoubleConstant;
import edu.stanford.photoSpreadParser.photoSpreadExpression.PhotoSpreadFormulaExpression;
import edu.stanford.photoSpreadParser.photoSpreadExpression.PhotoSpreadStringConstant;
import edu.stanford.photoSpreadParser.photoSpreadNormalizedExpression.PhotoSpreadNormalizedExpression;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;
import edu.stanford.photoSpreadUtilities.Const;
import edu.stanford.photoSpreadUtilities.Misc;
import edu.stanford.photoSpreadUtilities.TreeSetRandomSubsetIterable;

import org.apache.commons.lang3.text.WordUtils;

/**
 *
 * @author skandel. Massively modified by Andreas Paepcke (i.e. it's not Sean's fault!)
 */

public abstract class PhotoSpreadFunction extends PhotoSpreadFormulaExpression 
implements PhotoSpreadComputable {

	private enum FuncReturnTypes {
		DOUBLE,
		PHOTO_SPREAD_OBJECT
	}

	HashMap<String , FuncReturnTypes> _returnType = 
	    new HashMap<String , FuncReturnTypes>() {

		private static final long serialVersionUID = 1L;

		{
	    	put("Sum", FuncReturnTypes.DOUBLE);
	    	put("Avg", FuncReturnTypes.DOUBLE);
	    	put("Count", FuncReturnTypes.DOUBLE);
	    	put("Min", FuncReturnTypes.DOUBLE);
	    	put("Max", FuncReturnTypes.DOUBLE);
	    	put("Union", FuncReturnTypes.PHOTO_SPREAD_OBJECT);
	    }
	};
	
	protected static long _numOfTerms = 0;
	PhotoSpreadCell _cell = null;
	private String _functionName = Const.NULL_VALUE_STRING;
	protected ArrayList<PhotoSpreadFormulaExpression> _arguments = 
		new ArrayList<PhotoSpreadFormulaExpression>();

	AllArgEvalResults _allArgResults = 
		new AllArgEvalResults();

	private static Class<?> _photoSpreadFunctionClass = null; 
	private static String _packageName;
	
	/****************************************************
	 * Constructors
	 *****************************************************/

	public PhotoSpreadFunction() {
		this("<uninitialized>");
	}

	public PhotoSpreadFunction(String functionName) {
		super();
		_functionName = functionName.toLowerCase();
		// Need name of package where all functions reside:
		_packageName = this.getClass().getPackage().getName();
		// Get a hold on the PhotoSpreadFunction class
		// object, just for speed later on:
		try {
		_photoSpreadFunctionClass = 
			Class.forName(_packageName + ".PhotoSpreadFunction");
		} catch (ClassNotFoundException e) {
			Misc.showErrorMsgAndStackTrace(e, "");
			//e.printStackTrace();
		}
	}
	
	/****************************************************
	 * Getters/Setters for PhotoSpreadFunction
	 *****************************************************/

	private void setName(String funcName) {
		_functionName = funcName;
	}

	protected ArrayList<PhotoSpreadFormulaExpression> getArguments() {
		return _arguments;
	}

	public String getFunctionName() {
		return _functionName;
	}
	
	public void setCell (PhotoSpreadCell cell) {
		_cell = cell;
	}

	public PhotoSpreadCell getCell () {
		return _cell;
	}

	/****************************************************
	 * Methods
	 *****************************************************/
          
	public String toString () {
		String argsStr = "";

		for (PhotoSpreadFormulaExpression arg : _arguments) {
			if (arg != _arguments.get(0))
				argsStr += ", ";
			argsStr += arg;
		}

		return "<PhotoSpreadFunction " + _functionName + "(" + argsStr + ")>";
	}

	/**
	 * Create a function object from the function name
	 * @param _functionName the name of the function to be created
	 */

	public static PhotoSpreadFunction getInstance(String functionName, PhotoSpreadCell cell) {
		
		PhotoSpreadFunction func = null;

		PhotoSpread.trace("Creating PhotoSpreadFunction('" + functionName + "')");

		// Capitalize the function name so that all function calls
		// in the language may use function names lower case:
		String funcNameCapped  =  WordUtils.capitalize(functionName);

		try {
			// Instantiate the proper PhotoSpreadFunction subclass. That
			// precise subclass is unknown at this time, but will be known
			// at runtime when this method is invoked. We do promise the
			// compiler that whatever the instantiated function will be,
			// it will be a subclass of PhotoSpreadFunction:


			String fullyQualifiedFuncName = 
				"edu.stanford.photoSpreadParser.photoSpreadExpression.photoSpreadFunctions." +
				funcNameCapped; 
			
			Class<? extends PhotoSpreadFunction> functionClass  = 
				Class.forName(fullyQualifiedFuncName).asSubclass(PhotoSpreadFunction.class);
			func = (PhotoSpreadFunction) functionClass.newInstance();
			func.setName(funcNameCapped);
			func.setCell(cell);
		}
		catch ( ClassNotFoundException ex ){
			throw new RuntimeException( 
					ex + 
					" Function class '" + 
					funcNameCapped + 
					"' must be in class path");
		}
		catch( InstantiationException ex ){
			throw new RuntimeException( ex + " Function classes class must be concrete");
		}
		catch( IllegalAccessException ex ){
			throw new RuntimeException( 
					ex + 
					" Function class must have a no-arg constructor");
		}
		return func;
	}

	/**
	 * Each runtime (i.e. actual) argument to the function is represented by a 
	 * formula expression of the proper type: a PhotoSpreadStringConstant
	 * object for a string argument, a PhotoSpreadContainerExpression
	 * for an argument like A1. These argument objects are kept
	 * in an ArrayList.
	 * @param argument the argument to add to the function.
	 */
	public void addArgument(PhotoSpreadFormulaExpression argument){
		_arguments.add(argument);
		PhotoSpread.trace("Adding arg " + argument + " to " + this);
	}

	/* 
	 * Every function must be able to <code>evaluate()</code> itself.
	 * NOTE: <code>evaluate()</code> is different from <code>valueOf()</code>.
	 * <code>Evaluate()</code> returns a set of PhotoSpreadObject instances that
	 * wrap the raw Java results of applying the function to its arguments.
	 * <code>ValueOf</code> returns the actual Java item. The <code>valueOf()</code>
	 * for <code>sum()</code> therefore returns <code>Double</code>s, while
	 * its <code>evaluate()</code> method returns PhotoSpreadDoubleConstant
	 * instances.
	 * 
	 * @see photoSpreadParser.photoSpreadExpression.PhotoSpreadFormulaExpression#evaluate(photoSpreadTable.PhotoSpreadCell)
	 */
	public abstract TreeSetRandomSubsetIterable<PhotoSpreadObject> evaluate(PhotoSpreadCell cell)
	throws PhotoSpreadException.FormulaError;

	/* I *believe* that normalization is not required for functions.
	 * The actual arguments all get normalized individually.(?)
	 * 
	 * @see photoSpreadParser.photoSpreadExpression.PhotoSpreadFormulaExpression#normalize(photoSpreadTable.PhotoSpreadCell)
	 */
	@Override
	public PhotoSpreadNormalizedExpression normalize(PhotoSpreadCell cell) {
		return new PhotoSpreadNormalizedExpression(cell);
		// throw new RuntimeException("Normalization of functions is unimplemented.");
	}
	
	/****************************************************
	 * Methods that Execute Functions
	 *****************************************************/

	/* 
	 * This method is the workhorse of function execution. It resolves
	 * all runtime argument to ArrayLists of PhotoSpreadDoubleConstants,
	 * which are wrapped Java Doubles. 
	 * 
	 * I don't like the dispatch procedure I introduced in this
	 * method. There must be a cleaner way. The problem is this:
	 * The arguments to a PhotoSpread function may be of any 
	 * PhotoSpread type. Example: =sum(3, A1, avg(3,7)).
	 * The code in this method takes argument by argument and
	 * turns it into a PhotoSpreadDoubleConstant object, i.e. 
	 * into a wrapped Java double.
	 * 
	 * This process of evaluating each argument requires a different
	 * handler method for each type. In the above example: a handler
	 * for PhotoSpreadDoubleConstant, PhotoSpreadContainerExpression,
	 * and PhotoSpreadFunction. At compile time these types are not
	 * yet known. 
	 */

	public AllArgEvalResults  valueOfArgs() 
	throws FormulaError {

		for (PhotoSpreadFormulaExpression arg  : getArguments()) {
			try {
				// For each argument to the function, call method 'getTermValues()',
				// which returns an ArgEvalResult; essentially an array of 
				// results values:
				
				// Determine the argument's class:
				Class<? extends PhotoSpreadFormulaExpression> argClass  = 
					arg.getClass();
				
				// Is the argument a (nested) function call? In that
				// case the class would be the very specific function
				// class: Sum, Avg, etc.:
				
				if (_photoSpreadFunctionClass.isAssignableFrom(argClass))
					
					// If yes, cast the function obj argument UP to
					// PhotoSpreadFunction, so that method dispatch
					// will properly call the proper getTermValue()
					// method below:
					
					_allArgResults.addAllOneArgResults(getParmValue((PhotoSpreadFunction) arg));
				else {
					// Argument is of type PhotoSpreadDoubleConstant, 
					// PhotoSpreadStringConstant, PhotoSpreadContainerExpression,
					// etc. (i.e. not a nested function call).
					// Because Java doesn't have built-in runtime polymorphism,
					// we dispatch here, using Java's reflection capabilities.
					// We thereby find the relevant getParmValue() method to call. 
					// The funky parameter 'new Class[] {argClass}' creates an 
					// array of Class objects on the fly. The getMethod() 
					// method requires an array of all the desired method's arguments.
					
					Method relevantGetTermValuesMethod = 
						this.getClass().getMethod("getParmValue", new Class[] {argClass});
	
					// ... and invoke the found method with the argument:
					_allArgResults.addAllOneArgResults((ArgEvalResult<?>) 
							relevantGetTermValuesMethod.invoke(this, new Object[] {arg}));
				}
			} catch (NoSuchMethodException e) {
				throw new PhotoSpreadException.FormulaArgumentsError(
						"In function " +
						_functionName +
						": illegal argument: '" +
						arg +
						"'. Don't know how to handle this argument to '" +
						_functionName +
						"'.");
			} catch (IllegalAccessException e) {
				throw new PhotoSpreadException.FormulaArgumentsError(
						"In function " +
						_functionName +
						": illegal argument: '" +
						arg +
						"'. Don't know how to handle this argument to '" +
						_functionName +
						"'.");
			} catch (InvocationTargetException e) {
				throw new PhotoSpreadException.FormulaArgumentsError(
						"In function " +
						_functionName +
						": illegal argument: '" +
						arg +
						"'. " + 
						((e.getMessage() == null) ? "" : "Additional info: '"));
			}
		} // end For loop
		return _allArgResults;
	}

	/**
	 * Compute an argument of type function call. For instance, compute
	 * the 'avg' in the following <code>sum</code> function: =sum(2, 4, avg(6, 2)):
	 * @param funcObj The function object that wraps the function call argument.
	 * @return ArrayList of wrapped Doubles. In the example above the list
	 * would only contain a single object.
	 * @throws FormulaArgumentsError
	 */
	protected ArgEvalResult<?> getParmValue(PhotoSpreadFunction funcObj) throws FormulaError {
		
		switch (_returnType.get(funcObj.getFunctionName())) {
		
		case DOUBLE:
			ArgEvalResult<PhotoSpreadDoubleObject> resFuncCall_Double = 
				_allArgResults.<PhotoSpreadDoubleObject>newResultSet();
			resFuncCall_Double.add((PhotoSpreadDoubleObject) funcObj.valueOf());
			return resFuncCall_Double;
			// break;
		case PHOTO_SPREAD_OBJECT:
			ArgEvalResult<PhotoSpreadObject> resFuncCall_Obj = 
				_allArgResults.<PhotoSpreadObject>newResultSet();
			resFuncCall_Obj.add((PhotoSpreadObject) funcObj.valueOf());
			return resFuncCall_Obj;
			// break;
		default:
			throw new PhotoSpreadException.FormulaArgumentsError(
					"In function " +
					_functionName +
					": illegal argument: '" +
					funcObj.getFunctionName() +
					"'.");
		}
	}
	
	/**
	 * String arguments are inappropriate for the functions
	 * we have as of this writing. Our functions so far only
	 * operate on numeric arguments. So we throw an error in
	 * this method. If we add functions that take string args,
	 * (like concat(str1, str2)), those functions need to override.
	 * 
	 * @param strConstTerm
	 * @return never returns nicely.
	 * @throws IllegalArgumentException
	 */
	public ArgEvalResult<PhotoSpreadStringObject> getParmValue(PhotoSpreadStringConstant strConstTerm) {
		
		// Make a return structure to return PhotoSpreadStringObject instances:
		
		ArgEvalResult<PhotoSpreadStringObject> res = new ArgEvalResult<PhotoSpreadStringObject>();
		
		// Extract a PhotoSpreadStringObject from the passed-in PhotoSpreadStringConstant
		// (We downcast from the getObject() return type of PhotoSpreadObject:
		
		PhotoSpreadStringObject constObj = (PhotoSpreadStringObject)strConstTerm.getObject();

		res.add(constObj);
		return res;
	}

	/**
	 * Object-valued arguments: we don't have them right now.
	 * If we ever do, this method should just return them, wrapped
	 * in a return wrapper.
	 * @param obj
	 * @return never returns nicely.
	 * @throws IllegalArgumentException
	 */
	public ArgEvalResult<PhotoSpreadObject> getParmValue(PhotoSpreadObject obj) 
	throws IllegalArgumentException {
		throw new PhotoSpreadException.IllegalArgumentException(
				"In function " +
				_functionName +
				"() cannot use an 'object' (" +
				obj +
				") as parameter to a numeric function.");
	}

	/**
	 * Resolving a container argument to a function: we retrieve
	 * each element of the container, try to force it to a PhotoSpreadDoubleConstant,
	 * and return it. If the conversion fails, we throw a runtime
	 * formula error.
	 * @param containerTerm The container expression that holds the
	 * items. Ex: A1
	 * @return ArrayList of PhotoSpreadDoubleConstant that correspond
	 * to all the elements in the container.
	 * @throws IllegalArgumentException
	 */
	public ArgEvalResult<PhotoSpreadObject> getParmValue(PhotoSpreadContainerExpression containerTerm) 
	throws IllegalArgumentException {

		// Evaluate the container expression to get a 
		// set of PhotoSpreadObject instances. Then put
		// them all into a result construct:
		
		return new ArgEvalResult<PhotoSpreadObject>(containerTerm.evaluate(_cell));
	}

	/**
	 * Resolving a Double constant argument is simple. Just
	 * package the passed-in argument into an ArrayList, and
	 * we're done.
	 * @param doubleConstTerm
	 * @return
	 */
	
	public ArgEvalResult<PhotoSpreadDoubleObject> getParmValue(PhotoSpreadDoubleConstant doubleConstTerm) {
		
		// Make a return structure to return PhotoSpreadDoubleObject instances:
		
		ArgEvalResult<PhotoSpreadDoubleObject> res = new ArgEvalResult<PhotoSpreadDoubleObject>();
		
		// Extract a PhotoSpreadDoubleObject from the passed-in PhotoSpreadDoubleConstant
		// (We downcast from the getObject() return type of PhotoSpreadObject:
		
		PhotoSpreadDoubleObject constObj = (PhotoSpreadDoubleObject)doubleConstTerm.getObject();

		res.add(constObj);
		return res;
		
		// Yes, yes, the whole method could be written in one line:
		//  return new ArgEvalResult<PhotoSpreadDoubleObject>((PhotoSpreadDoubleObject)doubleConstTerm.getObject());
		// ... but would you want to wade through that statement???
	}
}
