/**
 * 
 */
package photoSpreadUtilities;

import java.awt.Dimension;

import junit.framework.TestCase;


/**
 * @author paepcke
 *
 */
public class ComputableDimensionTest extends TestCase {
	
	ComputableDimension testDimEqComps = new ComputableDimension(100, 100);
	ComputableDimension testDimNEqComps = new ComputableDimension(33, 75);
	ComputableDimension testDimGT100Comps = new ComputableDimension(1, 110);

	int eqCompsWidth = testDimEqComps.width;
	int eqCompsHeight= testDimEqComps.height;
	int NEqCompsWidth = testDimNEqComps.width;
	int NEqCompsHeight = testDimNEqComps.height;
	int GTCompsWidth= testDimGT100Comps.width;
	int GTCompsHeight = testDimGT100Comps.height;
	
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

	/**
	 * Test method for {@link photoSpreadUtilities.ComputableDimension#percent(int)}.
	 */
	public final void testPercent() {
		
		assertEquals("50 Percent 100/100", new ComputableDimension(eqCompsWidth/2, eqCompsHeight/2), testDimEqComps.percent(50));
		assertEquals("50 Percent 33/75", new ComputableDimension(NEqCompsWidth * 50 /100, NEqCompsHeight * 50 /100), testDimNEqComps.percent(50));
		assertEquals("1 Percent 1/110", new ComputableDimension(GTCompsWidth / 100, GTCompsHeight / 100), testDimGT100Comps.percent(1));
	}
	
	public final void testEquals() {
		assertTrue("Identity", testDimEqComps.equals(testDimEqComps));
		assertTrue("Non-Identity", testDimNEqComps.equals(new Dimension(NEqCompsWidth, NEqCompsHeight)));
		assertFalse("Non-equality", testDimNEqComps.equals(new Dimension(NEqCompsWidth, eqCompsWidth)));
	}

}
