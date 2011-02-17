/**
 * 
 */
package photoSpreadParser.photoSpreadExpression;

import junit.framework.TestCase;
import photoSpread.PhotoSpread;
import photoSpreadObjects.PhotoSpreadObject;
import photoSpreadObjects.PhotoSpreadStringObject;
import photoSpreadParser.photoSpreadNormalizedExpression.PhotoSpreadNormalizedExpression;
import photoSpreadTable.PhotoSpreadCell;
import photoSpreadTable.PhotoSpreadTableModel;
import photoSpreadUtilities.TreeSetRandomSubsetIterable;

/**
 * @author paepcke
 *
 */
public class PhotoSpreadConditionTest extends TestCase {

	PhotoSpreadTableModel _tableModel = null;
	
	PhotoSpreadCondition _condLeftLowerCase = null;
	PhotoSpreadCondition _condLeftUpperCase = null;
	PhotoSpreadCondition _condLeftMixedCase	= null;
	
	PhotoSpreadCell _cell_flower = null;
	PhotoSpreadCell _cell_Animal = null;
	PhotoSpreadCell _cell_auThORs = null;
	
	class PhotoSpreadStringCondition extends PhotoSpreadCondition {

		public PhotoSpreadStringCondition(String _lhs, String op) {
			super(_lhs, op);
		}

		@Override
		public void forceObject(PhotoSpreadObject object) {
			// TODO Auto-generated method stub
		}

		@Override
		public boolean satisfiesCondition(PhotoSpreadObject object) {
			return _compOp.satisfiesOperator(_lhs, ((PhotoSpreadStringObject) object).toString());
		}

		@Override
		public String toString() {
			return null;
		}

		@Override
		public TreeSetRandomSubsetIterable<PhotoSpreadObject> evaluate(
				PhotoSpreadCell cell) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public PhotoSpreadNormalizedExpression normalize(PhotoSpreadCell cell) {
			throw new RuntimeException("Normalize not implemented for PhotoSpreadCondition");
		}
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		// Numrows:
		PhotoSpread.photoSpreadPrefs.setProperty(PhotoSpread.sheetNumRowsKey, "10");
		// numCols
		PhotoSpread.photoSpreadPrefs.setProperty(PhotoSpread.sheetNumColsKey, "12");
	
		_tableModel = new PhotoSpreadTableModel();
		
		_cell_flower =  new PhotoSpreadCell(new PhotoSpreadTableModel(), 1,1);
		_cell_Animal=  new PhotoSpreadCell(new PhotoSpreadTableModel(), 1,2);
		_cell_auThORs =  new PhotoSpreadCell(new PhotoSpreadTableModel(), 1,3);
		
		_cell_flower.addObject(new PhotoSpreadStringObject(_cell_flower, "flower"));
		_cell_Animal.addObject(new PhotoSpreadStringObject(_cell_Animal, "Animal"));
		_cell_auThORs.addObject(new PhotoSpreadStringObject(_cell_auThORs, "auThORs"));

		_condLeftLowerCase = new PhotoSpreadStringCondition("flower",  "=");
		_condLeftUpperCase = new PhotoSpreadStringCondition("Animal",  "=");
		_condLeftMixedCase = new PhotoSpreadStringCondition("auThORs",  "=");
	}


	public void testSatisfiesOperator () {
		
		assertEquals("Left lower case, right lower case", true, _condLeftLowerCase.satisfiesCondition(
				new PhotoSpreadStringObject(_cell_flower, "flower")));
		assertEquals("Left lower case, right Upper case", true, _condLeftLowerCase.satisfiesCondition(
				new PhotoSpreadStringObject(_cell_flower, "Flower")));
		
		assertEquals("Left Upper case, right Upper case", true, _condLeftLowerCase.satisfiesCondition(
				new PhotoSpreadStringObject(_cell_Animal, "Animal")));
		assertEquals("Left Upper case, right lower case", true, _condLeftLowerCase.satisfiesCondition(
				new PhotoSpreadStringObject(_cell_flower, "animal")));
		
		

	}
}
