package photoSpreadParser.photoSpreadExpression.photoSpreadFunctions;


/**
 * Implementing this Interface means that the implementing
 * class can be the result of a PhotoSpread function. More
 * precisely: all PhotoSpread functions return an ArrayList
 * of a particular type. Implementing this Interface ensures
 * that the implementing class can be returned in one of 
 * these arrays.
 * 
 * @author paepcke
 *
 */
public interface FunctionResultable {

	/**
	 * Every FunctionResultable must be able to provide
	 * a PhotoSpreadConstant (or subclass of that class) 
	 * object that wraps it for use in function results.
	 * Examples: 
	 *    PhotoSpreadStringObject.getAsConstant() --> PhotoSpreadStringConstant
	 *    PhotoSpreadImage.getAsConstant() --> PhotoSpreadImageConstant
	 * @return An instance of PhotoSpreadConstant or one of its subclasses. This
	 * instance wraps the implementing object.
	 */
	// public PhotoSpreadConstant getAsConstant();
}
