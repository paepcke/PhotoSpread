/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package inputOutput;

import java.io.File;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import photoSpread.PhotoSpreadException;
import photoSpread.PhotoSpreadException.BadSheetFileContent;
import photoSpread.PhotoSpreadException.BadUUIDStringError;
import photoSpread.PhotoSpreadException.FileIOException;
import photoSpreadObjects.PhotoSpreadObject;
import photoSpreadObjects.PhotoSpreadStringObject;
import photoSpreadTable.PhotoSpreadCell;
import photoSpreadTable.PhotoSpreadTableModel;
import photoSpreadUtilities.Const;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;

/**
 * 
 * @author skandel
 */
public class XMLProcessor {

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
	}

	class DomTableUnmarshaller {

		PhotoSpreadTableModel _tableModel;

		public DomTableUnmarshaller(PhotoSpreadTableModel _tableModel) {
			this._tableModel = _tableModel;
		}

		public void unmarshallTable(Node rootNode) throws BadSheetFileContent {
			Node n;
			NodeList nodes = rootNode.getChildNodes();
			int row = 0;
			for (int i = 0; i < nodes.getLength(); i++) {
				n = nodes.item(i);
				// System.out.println("table " + i);
				if (n.getNodeType() == Node.ELEMENT_NODE) {

					if (n.getNodeName().equals(XMLProcessor.ROW_ELEMENT)) {
						unmarshallRow(n, row);
						row++;
					} else {
						// System.out.println("Unexpected node-type in Table " +
						// n.getNodeName());
					}
				} else {

				}
			}
		}

		private void unmarshallRow(Node rowNode, int row)
				throws BadSheetFileContent {
			Node n;
			NodeList nodes = rowNode.getChildNodes();
			int col = 1;
			for (int i = 0; i < nodes.getLength(); i++) {
				n = nodes.item(i);

				if (n.getNodeType() == Node.ELEMENT_NODE) {

					if (n.getNodeName().equals(XMLProcessor.CELL_ELEMENT)) {
						PhotoSpreadCell cell = unmarshallCell(n, row, col);
						_tableModel.setValueAt(cell, row, col);

						col++;
					} else {
						// System.out.println("Unexpected node-type in Row");
					}
				} else {
					// unexpected node-type in Catalog
				}
			}
		}

		private PhotoSpreadCell unmarshallCell(Node cellNode, int row, int col)
				throws BadSheetFileContent {

			// Don't make a new cell, use the existing one:
			// PhotoSpreadCell cell = new PhotoSpreadCell(_tableModel, row,
			// col);
			PhotoSpreadCell cell = _tableModel.getCell(row, col);
			Node n;
			NodeList nodes = cellNode.getChildNodes();

			for (int i = 0; i < nodes.getLength(); i++) {
				n = nodes.item(i);

				if (n.getNodeType() == Node.ELEMENT_NODE) {

					if (n.getNodeName().equals(
							XMLProcessor.CELL_FORMULA_ELEMENT)) {
						cell.setFormula(unmarshallText(n), Const.DONT_EVAL,
								Const.DONT_REDRAW);
						// System.out.println(unmarshallText(n));

					} else if (n.getNodeName().equals(
							XMLProcessor.OBJECTS_ELEMENT)) {

						unmarshallObjects(n, cell);

					} else {
						// System.out.println("Unexpected node-type in Cell" +
						// n.getNodeName());
					}
				} else {
					// unexpected node-type in Catalog
				}
			}

			return cell;
		}

		private void unmarshallObjects(Node objectsNode, PhotoSpreadCell cell)
				throws BadSheetFileContent {
			Node n;
			NodeList nodes = objectsNode.getChildNodes();

			for (int i = 0; i < nodes.getLength(); i++) {
				n = nodes.item(i);

				if (n.getNodeType() == Node.ELEMENT_NODE) {

					if (n.getNodeName().equals(XMLProcessor.OBJECT_ELEMENT)) {
						PhotoSpreadObject object = unmarshallObject(n, cell);
						// System.out.println("object " + object.toString());

						cell.addObject(object);

					} else {
						// System.out.println("Unexpected node-type in Objects");
					}
				} else {
					// unexpected node-type in Catalog
				}
			}
		}

		private PhotoSpreadObject unmarshallObject(Node objectNode,
				PhotoSpreadCell cell) throws BadSheetFileContent {

			PhotoSpreadObject object = null;
			ArrayList<String> constructorArgs;
			Node n;
			NodeList nodes = objectNode.getChildNodes();
			String objectType = unmarshallAttribute(objectNode,
					XMLProcessor.OBJECT_TYPE_ELEMENT,
					PhotoSpreadStringObject.class.getName());

			try {
				for (int i = 0; i < nodes.getLength(); i++) {
					n = nodes.item(i);

					if (n.getNodeType() == Node.ELEMENT_NODE) {

						if (n
								.getNodeName()
								.equals(
										XMLProcessor.OBJECT_CONSTRUCTOR_ARGUMENTS_ELEMENT)) {
							constructorArgs = unmarshallConstructorArguments(n);

							object = PhotoSpreadObject.getInstance(cell,
									objectType, constructorArgs);

						} else if (n.getNodeName().equals(
								XMLProcessor.TAGS_ELEMENT)) {
							unmarshallTags(n, object);

						} else {
							// System.out.println("Unexpected node-type in Object");
						}
					} else {
						// unexpected node-type in Catalog
					}
				}
			} catch (BadUUIDStringError e) {
				throw new BadSheetFileContent("Attempt to use bad object ID for re-creating an object: " + e.getMessage());
			}

			return object;
		}

		private void unmarshallTags(Node tagsNode, PhotoSpreadObject object) {

			Node n;
			NodeList nodes = tagsNode.getChildNodes();

			for (int i = 0; i < nodes.getLength(); i++) {
				n = nodes.item(i);

				if (n.getNodeType() == Node.ELEMENT_NODE) {

					if (n.getNodeName().equals(XMLProcessor.TAG_ELEMENT)) {

						unmarshallTag(n, object);

					} else {
						// System.out.println("Unexpected node-type in Tags");
					}
				} else {
					// unexpected node-type in Catalog
				}
			}
		}

		private void unmarshallTag(Node tagNode, PhotoSpreadObject object) {
			Node n;
			NodeList nodes = tagNode.getChildNodes();
			String attr = "";
			String value = "";
			for (int i = 0; i < nodes.getLength(); i++) {
				n = nodes.item(i);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					if (n.getNodeName().equals(
							XMLProcessor.TAG_ATTRIBUTE_ELEMENT)) {
						attr = unmarshallText(n);
					} else if (n.getNodeName().equals(
							XMLProcessor.TAG_VALUE_ELEMENT)) {
						value = unmarshallText(n);
					} else {
						// System.out.println("Unexpected node-type in Tag");
					}
				} else {
					// unexpected node-type in Catalog
				}
			}
			// System.out.println(attr + ", " + value);
			object.setMetaData(attr, value);
		}

		private ArrayList<String> unmarshallConstructorArguments(
				Node argumentsNode) throws BadSheetFileContent {

			ArrayList<String> constructorArgs = new ArrayList<String>();
			Node n;
			NodeList nodes = argumentsNode.getChildNodes();
			short nodeType;

			for (int i = 0; i < nodes.getLength(); i++) {
				n = nodes.item(i);

				// Check the node type. In principle
				// are looking for constructor arguments
				// here. But newlines show up as text
				// nodes, which we just skip over:

				nodeType = n.getNodeType();
				if (nodeType == Node.TEXT_NODE)
					continue;

				if (nodeType == Node.ELEMENT_NODE) {

					if (n.getNodeName().equals(
							XMLProcessor.OBJECT_CONSTRUCTOR_ARGUMENT_ELEMENT)) {

						String arg = unmarshallText(n);

						constructorArgs.add(arg);

					} else {
						throw new PhotoSpreadException.BadSheetFileContent(
								"Bad saved sheet file: '"
										+ _xmlFilePath
										+ "'. (Expected XML tag '"
										+ XMLProcessor.OBJECT_CONSTRUCTOR_ARGUMENT_ELEMENT
										+ "' but found '" + n.getNodeName());

					}
				} else {
					throw new PhotoSpreadException.BadSheetFileContent(
							"Bad saved sheet file: '" + _xmlFilePath
									+ "'. (Expected XML node type '"
									+ Node.ELEMENT_NODE + "' but found '"
									+ n.getNodeType() + "'");
				}
			}

			return constructorArgs;
		}

		private String unmarshallText(Node textNode) {
			StringBuffer buf = new StringBuffer();

			Node n;
			NodeList nodes = textNode.getChildNodes();

			for (int i = 0; i < nodes.getLength(); i++) {
				n = nodes.item(i);

				// Extract text or CDATA-encapsulated text:
				if ((n.getNodeType() == Node.TEXT_NODE)
						|| (n.getNodeType() == Node.CDATA_SECTION_NODE)) {
					buf.append(n.getNodeValue());
				} else {
					// expected a text-only node!
				}
			}
			return buf.toString();
		}

		private String unmarshallAttribute(Node node, String name,
				String defaultValue) {
			Node n = node.getAttributes().getNamedItem(name);
			return (n != null) ? (n.getNodeValue()) : (defaultValue);
		}
	}

	public static String ROW_ELEMENT = "row";
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
