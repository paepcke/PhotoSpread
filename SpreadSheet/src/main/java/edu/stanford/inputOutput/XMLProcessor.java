/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

// TODO: Replace the SUN internal DOMParser with something standard.

package edu.stanford.inputOutput;

import java.io.File;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.stanford.photoSpread.PhotoSpreadException;
import edu.stanford.photoSpread.PhotoSpreadException.BadSheetFileContent;
import edu.stanford.photoSpread.PhotoSpreadException.BadUUIDStringError;
import edu.stanford.photoSpread.PhotoSpreadException.FileIOException;
import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;
import edu.stanford.photoSpreadTable.PhotoSpreadTableModel;
import edu.stanford.photoSpreadUtilities.Const;

//import com.sun.org.apache.xerces.internal.parsers.DOMParser;

/**
 * 
 * @author skandel
 */

public class XMLProcessor {

	public static String NUM_COLS_ELEMENT = "numCols";
	public static String NUM_ROWS_ELEMENT = "numRows";
	public static String COL_ELEMENT = "col";
	public static String CELL_ELEMENT = "cell";
	public static String CELL_FORMULA_ELEMENT = "cellFormula";
	public static String OBJECT_CONSTRUCTOR_ARGUMENTS_ELEMENT = "objectConstructorArguments";
	public static String OBJECT_CONSTRUCTOR_ARGUMENT_ELEMENT = "objectConstructorArgument";
	public static String OBJECT_TYPE_ELEMENT = "objectType";
	public static String OBJECTS_ELEMENT = "objects";
	public static String OBJECT_ELEMENT = "object";
	public static String TAGS_ELEMENT = "tags";
	public static String TAG_ELEMENT = "tag";
	public static String TAG_ATTRIBUTE_ELEMENT = "attribute";
	public static String TAG_VALUE_ELEMENT = "value";
	
	
	/**
	 *Loads data from xml format into a tableModel
	 * 
	 * @param f
	 *            file containing data in xml format
	 * @param tableModel
	 *            the tableModel that will be loaded with data
	 */

	String _xmlFilePath = "";

	public void loadXMLFile(File f, PhotoSpreadTableModel tableModel)
			throws FileIOException, BadSheetFileContent {

		_xmlFilePath = f.getPath();

		try {
			DomTableUnmarshaller domUms = new DomTableUnmarshaller(tableModel);
			DOMParser domParser = new DOMParser();
			domParser.parse(_xmlFilePath);

			Document doc = domParser.getDocument();

			domUms.unmarshallTable(doc.getDocumentElement());
		} catch (java.io.IOException e) {
			throw new PhotoSpreadException.FileIOException("Sheet file '"
					+ _xmlFilePath + "' cannot be read. (" + e.getMessage()
					+ ").");
		} catch (SAXException e) {
			throw new PhotoSpreadException.BadSheetFileContent(
					"Bad saved sheet file: '" + _xmlFilePath
							+ "'. (SAXException: " + e.getMessage() + ").");
		}
		
		// Force evaluation of all cells:
		tableModel.fireTableDataChanged();
	}

	class DomTableUnmarshaller {

		PhotoSpreadTableModel _tableModel;
		XPath xpathProcessor = null;
		final String xpathGetCellsExpr   = "//" + XMLProcessor.CELL_ELEMENT;
		final String xpathGetCellFormula = XMLProcessor.CELL_FORMULA_ELEMENT;
		final String xpathGetObjsExpr    = XMLProcessor.OBJECTS_ELEMENT + "/" + XMLProcessor.OBJECT_ELEMENT;
		final String xpathGetObjTypeAttr = "@" + XMLProcessor.OBJECT_TYPE_ELEMENT;
		final String xpathGetObjConstructors = XMLProcessor.OBJECT_CONSTRUCTOR_ARGUMENTS_ELEMENT + "/" + XMLProcessor.OBJECT_CONSTRUCTOR_ARGUMENT_ELEMENT;
		final String xpathGetObjConstArg = XMLProcessor.OBJECT_CONSTRUCTOR_ARGUMENT_ELEMENT;
		final String xpathGetTagsExpr    = XMLProcessor.TAGS_ELEMENT + "/" + XMLProcessor.TAG_ELEMENT;
		final String xpathGetTagAttrNameExpr = XMLProcessor.TAG_ATTRIBUTE_ELEMENT;
		final String xpathGetTagAttrValExpr = XMLProcessor.TAG_VALUE_ELEMENT;

		public DomTableUnmarshaller(PhotoSpreadTableModel _tableModel) {
			this._tableModel = _tableModel;
			xpathProcessor = XPathFactory.newInstance().newXPath();
		}
		
		/**
		 * Given an XML document node object that represents a PhotoSpread table
		 * sheet, materialize that table into the current (on-screen) table. 
		 * The XML document was typically extracted from an XML file that was
		 * the result of a Save Sheet operation in a prior PhotoSpread session.
		 * 
		 * In this current version it is an error if the on-screen table has
		 * different dimensions than the table that is being restored. An
		 * appropriate error message will appear on the screen.
		 * 
		 * @param rootNode  The document node below, and including the <table> element.
		 * @throws BadSheetFileContent
		 */
		public void unmarshallTable(Node rootNode) throws BadSheetFileContent {
			
			NodeList cellNodeList = null;
			int numRows = -1;
			int numCols = -1;
			String numRowsStr = "";
			String numColsStr = "";
			
			try {
				// Get table attributes numRows and numCols to have the
				// dimensions of the table being unmarshalled. We'll use that
				// for checking integrity of the XML file:
				numRowsStr = xpathProcessor.evaluate("@numRows", rootNode);
				numColsStr = xpathProcessor.evaluate("@numCols", rootNode);
				numRows = Integer.parseInt(numRowsStr);
				numCols = Integer.parseInt(numColsStr);
				
				// Get a set of Cell node objects:
				cellNodeList = (NodeList) xpathProcessor.evaluate(xpathGetCellsExpr, rootNode, XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
				throw new BadSheetFileContent("Could not retrieve any cells from XML: " + e.getMessage());
			} catch (NumberFormatException e1) {
				throw new BadSheetFileContent("Attribute numRows or numCols in the Table node are not integers:" +
						"numRows is '" + numRowsStr + "', and numCols is '" + numColsStr + "'"); 
			}
			
			// Does the dimension declaration in the XML file's
			// Table element attributes match
			// the row and col numbers of the current table?
			// The -1 accounts for Column 0, which just holds the row nums.
			if (numRows != _tableModel.getRowCount() || numCols != _tableModel.getColumnCount() - 1) {
				throw new BadSheetFileContent("Current onscreen table has dimensions : " +
						_tableModel.getRowCount() +
						" rows X " +
						_tableModel.getColumnCount() +
						" columns. But table being loaded declares in the Table element that it is " +
						numRows + " rows X " +
						numCols + " columns.");
			}

			// Check num of cells in existing, on-screen table against these dimensions.
			int numCells = cellNodeList.getLength();
			// numCells is the true number of Cell elements in the XML file.
			// Compare this with the true row and column counts of the current
			// table. The -1 subtracts Col 0, which holds the row numbers:

			if (numCells != _tableModel.getRowCount() * (_tableModel.getColumnCount() -1)) {
				// TODO: We should just create a new table with the proper dimensions, 
				//       instead of throwing an error.
				throw new BadSheetFileContent("Current onscreen table has " +
						_tableModel.getRowCount() * (_tableModel.getColumnCount() -1) +
						"cells. But the table being loaded has " +
						numCells + " cells.");
			}
			
			// Fill in all cells, with their formulas and possibly 
			// contained objects:
			unmarshallCellList(cellNodeList);
		}

		/**
		 * Given a list of Cell nodes, take each node, and find the corresponding cell
		 * in the the existing on-screen cell. Populate that cell with any cell
		 * formula or PhotoSpread object.
		 * 
		 * @param cellNodeList XML nodes representing Cell objects
		 * @throws BadSheetFileContent
		 */
		private void unmarshallCellList(NodeList cellNodeList)
				throws BadSheetFileContent {
			
			int numCells = cellNodeList.getLength();
			
			// Unmarshall each Cell node in turn:
			int rowNum = -1;
			int colNum = -1;

			// For each cell, get its row and col numbers. Then do the
			// grunt work for that cell:
			for (int cellIndex = 0; cellIndex < numCells; cellIndex++) {
				Node cellNode = cellNodeList.item(cellIndex);
				String rowNumStr = "";
				String colNumStr = "";
				try {
					rowNumStr = xpathProcessor.evaluate("@rowNum", cellNode);
					rowNum = Integer.parseInt(rowNumStr);
					colNumStr = xpathProcessor.evaluate("@colNum", cellNode);
					colNum = Integer.parseInt(colNumStr);
					unmarshallCell(cellNode, rowNum, colNum);
				} catch (NumberFormatException e1) {
					new BadSheetFileContent("PhotoSpread sheet XML defective. Expected row or column number; got rowNum: '" +
											rowNumStr + "', and colNum: '" + colNumStr + "'");
				} catch (XPathExpressionException e2) {
					new BadSheetFileContent("PhotoSpread sheet XML defective. " +
							"Error while finding cell rowNum or colNum attribute:" +
							e2.getMessage());
				}
			}
		}
			
		
		/**
		 * Given one XML Cell node, and its destination row/column pair, get the cell's
		 * formula from the XML and put it into the on-screen's corresponding cell. Then
		 * get any objects that are stored in the cell, unmarshall those, and add them to
		 * the on-screen cell.
		 *   
		 * @param cellNode XML node object of one cell as stored in the XML file.
		 * @param row The row from which the cell came in the original table, and to which
		 * it will go in the on-screen table.
		 * @param col The column from which the cell came in the original table, and to which
		 * it will go in the on-screen table.
		 * @return A PhotoSpreadCell object with its formula and any stored objects installed.
		 * The cell object will be the already existing cell in the on-screen table.
		 * @throws BadSheetFileContent
		 */
		private PhotoSpreadCell unmarshallCell(Node cellNode, int row, int col)
				throws BadSheetFileContent {

			String formulaStr = "";
			
			// Don't make a new cell, use the existing one:
			// PhotoSpreadCell cell = new PhotoSpreadCell(_tableModel, row,
			// col);
			PhotoSpreadCell cell = _tableModel.getCell(row, col);
			
			try {
				// Recover this cell's formula from the XML. 
				formulaStr = xpathProcessor.evaluate(xpathGetCellFormula, cellNode);
			} catch (XPathExpressionException e) {
				// If no formula XML is in the formula, that's fine.
				// The formula strings in the on-screen are initialized to 
				// be empty.
			}
			if (!formulaStr.isEmpty())
				cell.setFormula(formulaStr, Const.DONT_EVAL, Const.DONT_REDRAW);
			
			// If a cell contains one or more objects, rather than a
			// formula, then the formula string will be the special
			// constant OBJECTS_COLLECTION_INTERNAL_TOKEN:
			if (formulaStr.equals(Const.OBJECTS_COLLECTION_INTERNAL_TOKEN))
				// Materialize and add the objects to the cell:
				unmarshallObjects(cellNode, cell);
			
			return cell;
		}

		/**
		 * Given one XML cell node that is known to contain objects,
		 * materialize those objects, and add them to the cell.
		 * 
		 * @param cellNode XML node object
		 * @param cell The existing PhotoSpreadCell object under construction.
		 * @throws BadSheetFileContent
		 */
		private void unmarshallObjects(Node cellNode, PhotoSpreadCell cell)
				throws BadSheetFileContent {
			
			NodeList objsNodeList = null;
			try {
				objsNodeList = (NodeList) xpathProcessor.evaluate(xpathGetObjsExpr, cellNode, XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
				throw new BadSheetFileContent("Could not retrieve any cells from XML: " + e.getMessage());
			}

			int numObjs = objsNodeList.getLength();
			String objType = "";
			for (int objIndex=0; objIndex < numObjs; objIndex++) {
				Node objNode = objsNodeList.item(objIndex);
				try {
					// Get PhotoSpread type of the obj (e.g. edu.stanford.photoSpreadObjects.PhotoSpreadImage):
					objType = xpathProcessor.evaluate(xpathGetObjTypeAttr, objNode);
				} catch (XPathExpressionException e) {
					throw new BadSheetFileContent("Could not retrieve type of cell object number " +
							objIndex + "of cell " + cell);
				}
				
				// Get the arguments we need to give the PhotoSpread object constructor
				// to materialize it properly:
				ArrayList<String> constrArgs = unmarshallConstructorArguments(objNode);
				PhotoSpreadObject object = null;
				try {
					// Mint a PhotoSpread object as it was when the prior session's
					// sheet was saved to the XML file:
					object = PhotoSpreadObject.getInstance(cell, objType, constrArgs);
				} catch (BadUUIDStringError e) {
					throw new BadSheetFileContent("Could not create PhotoSpread object of type " +
							objType +
							" for cell " + cell);
				}

				// Add any tags that may be present in the XML file for this object:
				unmarshallTags(objNode, object);
				cell.addObject(object);
			}
		}

		/**
		 * Each XML cell element in an XML-saved sheet contains all arguments to be passed to
		 * a PhotoSpread object constructor, so that the object will look like
		 * the orginal.
		 * 
		 * @param objNode The XML node object that contains the constructor argument nodes. 
		 * @return An ArrayList of strings containing the constructor arguments.
		 * @throws BadSheetFileContent
		 */
		private ArrayList<String> unmarshallConstructorArguments(
				Node objNode) throws BadSheetFileContent {

			ArrayList<String> constructorArgs = new ArrayList<String>();
			NodeList constrArgsNodeList = null;
			
			// Get the XML node list of all the original cell's contained objects:
			try {
				constrArgsNodeList = (NodeList) xpathProcessor.evaluate(xpathGetObjConstructors, 
																		objNode, 
																		XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
				throw new BadSheetFileContent("Could not retrieve constructor args for cell." + e.getMessage());
			}

			int numArgs = constrArgsNodeList.getLength();
			String constrArg = "";
			// Now get the string value of each constructor arg (i.e. the
			// text between the constructor argument opening and closing tags:
			for (int argIndex=0; argIndex < numArgs; argIndex++) {
				Node constrArgNode = constrArgsNodeList.item(argIndex);
				constrArg = constrArgNode.getTextContent();
				constructorArgs.add(constrArg);
			}

			return constructorArgs;
		}

		
		/**
		 * For a given XML object node, recover the corresponding PhotoSpread object's 
		 * attr/value tags. Those tags were stored in the XML file being unmarshalled when
		 * the original sheet was saved.
		 * 
		 * @param objNode XML node object whose tag sub-elements are to be recovered from the XML
		 * @param object The materialized PhotoSpread object that was unmarshalled by the caller of 
		 * this method.
		 * @throws BadSheetFileContent
		 */
		private void unmarshallTags(Node objNode, PhotoSpreadObject object) throws BadSheetFileContent {

			NodeList tagsNodeList = null;
			// Get a list of all <tag> node objects:
			try {
				tagsNodeList = (NodeList) xpathProcessor.evaluate(xpathGetTagsExpr, objNode, XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
				throw new BadSheetFileContent("Could not retrieve tag for object " + object + " from XML: " + e.getMessage());
			}

			int numTags= tagsNodeList.getLength();
			// For each tag, recover the attribute name and value, and 
			// add the pair to the PhotoSpread object's metadata:
			for (int tagIndex=0; tagIndex < numTags; tagIndex++) {
				Node tagNode = tagsNodeList.item(tagIndex);
				String attrName = "";
				String attrValue = "";
				try {
					attrName  = xpathProcessor.evaluate(xpathGetTagAttrNameExpr, tagNode);
					attrValue = xpathProcessor.evaluate(xpathGetTagAttrValExpr, tagNode);
				} catch (XPathExpressionException e) {
					throw new BadSheetFileContent("Could not retrieve tag attribute or value for object " + object + " from XML: " + e.getMessage());
				}
				object.setMetaData(attrName, attrValue);
			}
		}
	}


	public class XMLHandler extends DefaultHandler {

		@Override
		public void startElement(String arg0, String localName, String arg2,
				org.xml.sax.Attributes arg3) throws SAXException {
			System.out.println(arg3.getValue(0));

		}

		public void endElement(String namespaceURI, String localName,
				String qualifiedName) throws SAXException {

			// if (localName.equals("double")) inDouble = false;

		}

		public void characters(char[] ch, int start, int length)
				throws SAXException {

			if (true) {
				for (int i = start; i < start + length; i++) {
					// System.out.print(ch[i]);
				}
			}

		}

	}
}
