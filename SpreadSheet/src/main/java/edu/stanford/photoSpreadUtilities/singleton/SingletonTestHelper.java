/**
 * 
 */
package edu.stanford.photoSpreadUtilities.singleton;

import edu.stanford.photoSpread.PhotoSpreadException.BadSingletonInvocation;

import edu.stanford.photoSpreadUtilities.UUID;

/**
 * @author paepcke
 *
 */
public class SingletonTestHelper extends Singleton {

	public UUID objID = new UUID("Singleton");
	

	public static SingletonTestHelper get() throws BadSingletonInvocation {
		String fullClassName = SingletonTestHelper.class.getCanonicalName(); 
		// System.out.println("Class name: '" + fullClassName + "'.");
		return (SingletonTestHelper) Singleton.get(fullClassName);
	}
}
