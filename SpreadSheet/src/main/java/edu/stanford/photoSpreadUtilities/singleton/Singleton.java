/**
 * 
 */
package edu.stanford.photoSpreadUtilities.singleton;

import edu.stanford.photoSpread.PhotoSpreadException.*;


/** @author paepcke
 * 
 * Abstract class for implementing singleton
 * objects. Instructions:
 * 	- Subclass Singleton
 *  - Do NOT create a constructor
 *  - Create the method 
 *        public static <YourSubclassName> get(), which
 *    should call Singleton.get(<YourSubclassName>)
 *    
 *  Example:
	<pre>
	 public class MySingleton extends Singleton {

  		public static MySingleton get() throws BadSingletonInvocation {
		    String fullClassName = MySingleton.class.getCanonicalName(); 
   		    return (MySingleton) Singleton.get(fullClassName);
  	    }
  	 }
	</pre>
 *
 */
public abstract class Singleton {

	protected static Singleton soleInstance = null;

	protected Singleton() {

		// Note the 'protected' declaration. Only subclasses
		// in this or other packages, or other classes in this 
		// (singleton) package are allowed to instantiate 
		// this class.

	}

	/**
	 * @return Either the one-and-only instance of your
	 * subclass, or, if none exists, a new instance. Your
	 * get() method should just call super.get(<YourSubclassName>)
	 * @throws BadSingletonInvocation 
	 */

	protected static Singleton get(String subClassName) throws BadSingletonInvocation {

		if (soleInstance == null)
			try {

				soleInstance = (Singleton) Class.forName(subClassName).newInstance();

			} catch (java.lang.ClassNotFoundException e) {
				throw new BadSingletonInvocation(
					     "Class '" + 
					     subClassName + 
					     "' not found. Must use fully package-qualified name.");
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			return soleInstance;
	}

	public Object clone() throws CloneNotSupportedException {

		// Don't allow cloning, else more 
		// than a singleton would exist:
		throw new CloneNotSupportedException(); 
	}
}
