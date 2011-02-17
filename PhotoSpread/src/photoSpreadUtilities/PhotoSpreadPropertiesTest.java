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
public class PhotoSpreadPropertiesTest extends TestCase {

	PhotoSpreadProperties<String, String> propsWithoutDefaults = new PhotoSpreadProperties<String, String>();
	PhotoSpreadProperties<String, String> defaults = new PhotoSpreadProperties<String, String>();
	PhotoSpreadProperties<String, String> propsWithDefaults = new PhotoSpreadProperties<String, String>(defaults);

	protected void setUp() throws Exception {
		super.setUp();

	}


	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testPutGetString() {
		
		propsWithoutDefaults.put("Foo", "Bar");
		assertEquals("Retrieve strings.", "Bar", propsWithoutDefaults.get("Foo"));
		
		defaults.put("DefaultKey1", "DefaultValue1");
		assertEquals("Retrieve default str from empty props.", "DefaultValue1", propsWithDefaults.get("DefaultKey1"));

		assertNull("Get nonexisting property.", propsWithoutDefaults.get("Bluebell"));
	}
	
	public void testPutGetInt() {
		
		propsWithoutDefaults.put("Num1", "13");
		assertEquals("Retrieve properly formatted int.", 13, propsWithoutDefaults.getInt("Num1") - 0);
		
		propsWithoutDefaults.put("BadNum", "1a");
		try {
			propsWithoutDefaults.getInt("BadNum");
			fail ("Expected invalid pref value exception");
		} catch (Exception IllegalPreferenceValueException) {
			// expected
		}
	}
	
	public void testPutGetDimension() {
		
		propsWithDefaults.put("Dim1", "10 20");
		assertEquals("Well-formed dimension", new Dimension(10, 20), propsWithDefaults.getDimension("Dim1"));
	}

}
