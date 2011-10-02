/**
 * 
 */
package edu.stanford.photoSpreadUtilities.singleton;

import junit.framework.TestCase;
import edu.stanford.photoSpread.PhotoSpreadException.BadSingletonInvocation;
/**
 * @author paepcke
 *
 */
public class SingletonTest extends TestCase {
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testUniqueness () {
		
		SingletonTestHelper firstInstance = null;
		SingletonTestHelper secondInstance = null;
		
		try {
			
			firstInstance = SingletonTestHelper.get();
			secondInstance = SingletonTestHelper.get();
			
		} catch (BadSingletonInvocation e) {
			fail("Instantiation failed (bad singleton name?): '" + e.getMessage() + "'.");
		} catch (Exception e) {
			fail("Instantiation failed: '" + e.getMessage() + "'.");
		}
		
		try {
			
		assertEquals("Two instances should be identical", firstInstance.objID, secondInstance.objID);
		
		} catch (NullPointerException e) {
			fail("Inst1: '" + firstInstance + "'.");
			fail("Inst2: '" + secondInstance + "'.");
			// e.printStackTrace();
		}
		
	}
}
