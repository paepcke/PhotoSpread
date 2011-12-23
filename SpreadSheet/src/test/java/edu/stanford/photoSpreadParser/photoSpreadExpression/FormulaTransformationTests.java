package edu.stanford.photoSpreadParser.photoSpreadExpression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.security.InvalidParameterException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.stanford.photoSpreadParser.ExpressionParser;
import edu.stanford.photoSpreadParser.ParseException;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;
import edu.stanford.photoSpreadTable.PhotoSpreadTableModel;

public class FormulaTransformationTests {

	private final Boolean ROW_OR_COL_FIXED = true;
	private final Boolean ROW_OR_COL_LOOSE = false;

	// Table model with five rows, and six columns:
	PhotoSpreadTableModel tModel = new PhotoSpreadTableModel(5,6); 
	PhotoSpreadCell cell = new PhotoSpreadCell(tModel, 0, 1);
	
	java.io.StringReader strReader = null;
	java.io.Reader reader = null;
	ExpressionParser parser = null;
	PhotoSpreadExpression expr = null;
	
	@Before
	public void setUp() throws Exception {
/*		java.io.StringReader sr = new java.io.StringReader("=A1[hello = jello]");
		java.io.Reader r = new java.io.BufferedReader(sr);
		ExpressionParser parser = new ExpressionParser(r);
		PhotoSpreadExpression expr = parser.Expression();
		System.out.println(expr.toString());
*/	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testFormulaRenditions() {
		// Test the grammar objects' ability to render themselves
		// as formula strings:
		
		// PhotoSpreadSimpleCondition:
		PhotoSpreadSimpleCondition cond = new PhotoSpreadSimpleCondition("Species", "=", "fox"); 
		assertEquals("Bad formula rendition.", "Species=fox", cond.toFormula());
		
		// Cell ranges:
		PhotoSpreadCellRange range = new PhotoSpreadCellRange(
				this.ROW_OR_COL_LOOSE,  // boolean _startColFixed, 
				"A",                    // String _startCol, 
				this.ROW_OR_COL_LOOSE,  // boolean _startRowFixed, 
				1,                      // int _startRow will turn to 0-based: 1,  
				this.ROW_OR_COL_LOOSE,  // boolean _endColFixed,  
				"A",                    // String _endCol,  
				this.ROW_OR_COL_LOOSE,  // boolean _endRowFixed,  
				1);                     // int _endRow; will turn to 0-based: 1
		assertEquals("Bad toFormula(A1)", "A1", range.toFormula());
		
		range = new PhotoSpreadCellRange(
				this.ROW_OR_COL_FIXED,  // boolean _startColFixed, 
				"A",                    // String _startCol, 
				this.ROW_OR_COL_FIXED,  // boolean _startRowFixed, 
				1,                      // int _startRow will turn to 0-based: 1,  
				this.ROW_OR_COL_FIXED,  // boolean _endColFixed,  
				"A",                    // String _endCol,  
				this.ROW_OR_COL_FIXED,  // boolean _endRowFixed,  
				1);                     // int _endRow; will turn to 0-based: 1
		assertEquals("Bad toFormula($A$1)", "$A$1", range.toFormula());
		
		range = new PhotoSpreadCellRange(
				this.ROW_OR_COL_FIXED,  // boolean _startColFixed, 
				"A",                    // String _startCol, 
				this.ROW_OR_COL_LOOSE,  // boolean _startRowFixed, 
				1,                      // int _startRow will turn to 0-based: 1,  
				this.ROW_OR_COL_FIXED,  // boolean _endColFixed,  
				"A",                    // String _endCol,  
				this.ROW_OR_COL_LOOSE,  // boolean _endRowFixed,  
				1);                     // int _endRow; will turn to 0-based: 1
		assertEquals("Bad toFormula($A2)", "$A1", range.toFormula());
		
		range = new PhotoSpreadCellRange(
				this.ROW_OR_COL_LOOSE,  // boolean _startColFixed, 
				"A",                    // String _startCol, 
				this.ROW_OR_COL_FIXED,  // boolean _startRowFixed, 
				1,                      // int _startRow will turn to 0-based: 1,  
				this.ROW_OR_COL_LOOSE,  // boolean _endColFixed,  
				"A",                    // String _endCol,  
				this.ROW_OR_COL_FIXED,  // boolean _endRowFixed,  
				1);                     // int _endRow; will turn to 0-based: 1
		assertEquals("Bad toFormula(A$2)", "A$1", range.toFormula());
		
		range = new PhotoSpreadCellRange(
				this.ROW_OR_COL_LOOSE,  // boolean _startColFixed, 
				"ZZA",                  // String _startCol, 
				this.ROW_OR_COL_FIXED,  // boolean _startRowFixed, 
				1,                      // int _startRow will turn to 0-based: 1,  
				this.ROW_OR_COL_LOOSE,  // boolean _endColFixed,  
				"ZZA",                    // String _endCol,  
				this.ROW_OR_COL_FIXED,  // boolean _endRowFixed,  
				2);                     // int _endRow; will turn to 0-based: 1
		assertEquals("Bad toFormula(ZZA$1:ZZA$2)", "ZZA$1:ZZA$2", range.toFormula());

		// PhotoSpreadCellRangeCondition:
		PhotoSpreadCellRangeCondition cond1 = 
				new PhotoSpreadCellRangeCondition("10", ">", new PhotoSpreadCellRange("A1:A2"));
		assertEquals("Cell range condition failed.", "10 > A1:A2", cond1.toFormula());
		
		// PhotoSpreadConstantExpression:

		PhotoSpreadConstantExpression cExpr = new PhotoSpreadConstantExpression("foo", cell);
		assertEquals("Simple string constant", "foo" , cExpr.toFormula());
		
		cExpr.addConstant(new PhotoSpreadStringConstant(cell, "bar"));
		assertEquals("Two string constants.", "bar foo" , cExpr.toFormula());

		
		// PhotoSpreadExpression
		
		strReader = new java.io.StringReader("=A1[hello = jello]");
		reader = new java.io.BufferedReader(strReader);
		parser = new ExpressionParser(reader);
		expr = null;
		
		try {
			expr = parser.Expression();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		assertEquals("=A1[hello = jello].", "A1[hello=jello]", expr.toFormula());
		
		strReader = new java.io.StringReader("=A1[hello = jello].species");
		reader = new java.io.BufferedReader(strReader);
		parser = new ExpressionParser(reader);
		expr = null;
		try {
			expr = parser.Expression();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		assertEquals("=A1[hello = jello].species", "A1[hello=jello].species", expr.toFormula());

		strReader = new java.io.StringReader("=count(A1[hello = jello].species)");
		reader = new java.io.BufferedReader(strReader);
		parser = new ExpressionParser(reader);
		expr = null;
		try {
			expr = parser.Expression();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		assertEquals("=Count(A1[hello = jello].species)", "Count(A1[hello=jello].species)", expr.toFormula());

		strReader = new java.io.StringReader("=A1[hello = null].species");
		reader = new java.io.BufferedReader(strReader);
		parser = new ExpressionParser(reader);
		expr = null;
		try {
			expr = parser.Expression();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		assertEquals("=A1[hello = null].species", "A1[hello=null].species", expr.toFormula());
	}

	@Test
	public void testFormulaCopy() {

		strReader = new java.io.StringReader("=A1");
		reader = new java.io.BufferedReader(strReader);
		parser = new ExpressionParser(reader);
		expr = null;
		try {
			expr = parser.Expression();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		assertEquals("Copy expr 'A1 to one row higher'.", "A2", expr.copyExpression(1, 0));
		assertEquals("Copy expr 'A1' to one column over.", "B1", expr.copyExpression(0, 1));
		
		try {
			assertEquals("Copy expr 'A1' to one column over.", "B1", expr.copyExpression(0, -1));
			fail("Did not receive an exception for negative column offset.");
		} catch (InvalidParameterException e) {
			// expected.
		} catch (Exception e1) {
			fail("Did not receive correct type of exception for negative column offset.");
		}
		
		try {
			assertEquals("Copy expr 'A1' to one column over.", "B1", expr.copyExpression(-1, 0));
			fail("Did not receive an exception for negative column offset.");
		} catch (InvalidParameterException e) {
			// expected.
		} catch (Exception e1) {
			fail("Did not receive correct type of exception for negative column offset.");
		}

		strReader = new java.io.StringReader("=A1[hello = jello].species");
		reader = new java.io.BufferedReader(strReader);
		parser = new ExpressionParser(reader);
		expr = null;
		try {
			expr = parser.Expression();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		assertEquals("Copy formula '=A1[hello = jello].species' to one row down.", "A2[hello=jello].species", expr.copyExpression(1, 0));
		
		
		strReader = new java.io.StringReader("=count(A1[hello = jello].species)");
		reader = new java.io.BufferedReader(strReader);
		parser = new ExpressionParser(reader);
		expr = null;
		try {
			expr = parser.Expression();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		assertEquals("Copy formula '=count(A1[hello = jello].species)' to one row down.", "Count(A2[hello=jello].species)", expr.copyExpression(1, 0));
		
		strReader = new java.io.StringReader("=count(A1[hello = jello & species=fish])");
		reader = new java.io.BufferedReader(strReader);
		parser = new ExpressionParser(reader);
		expr = null;
		try {
			expr = parser.Expression();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		assertEquals("Copy formula '=count(A1[hello = jello & species=fish])' to one column over.", "Count(B1[hello=jello & species=fish])", expr.copyExpression(0, 1));
	}
}
