/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.stanford.photoSpreadTable;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeSet;

import javax.management.RuntimeErrorException;
import javax.swing.JOptionPane;

import edu.stanford.inputOutput.XMLProcessor;
import edu.stanford.photoSpread.PhotoSpread;
import edu.stanford.photoSpread.PhotoSpreadException;
import edu.stanford.photoSpread.PhotoSpreadException.BadUUIDStringError;
import edu.stanford.photoSpread.PhotoSpreadException.FormulaError;
import edu.stanford.photoSpread.PhotoSpreadException.FormulaSyntaxError;
import edu.stanford.photoSpread.PhotoSpreadException.IllegalArgumentException;
import edu.stanford.photoSpreadLoaders.PhotoSpreadFileImporter;
import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadObjects.PhotoSpreadStringObject;
import edu.stanford.photoSpreadParser.photoSpreadExpression.PhotoSpreadConstantExpression;
import edu.stanford.photoSpreadParser.photoSpreadExpression.PhotoSpreadExpression;
import edu.stanford.photoSpreadParser.photoSpreadNormalizedExpression.PhotoSpreadNormalizedExpression;
import edu.stanford.photoSpreadTable.DnDSupport.PhotoSpreadCellFlavor;
import edu.stanford.photoSpreadTable.DnDSupport.PhotoSpreadFileListFlavor;
import edu.stanford.photoSpreadUtilities.CellCoordinates;
import edu.stanford.photoSpreadUtilities.Const;
import edu.stanford.photoSpreadUtilities.MetadataIndexer;
import edu.stanford.photoSpreadUtilities.Misc;
import edu.stanford.photoSpreadUtilities.ObjectUniquenessReference;
import edu.stanford.photoSpreadUtilities.PhotoSpreadComparatorFactory;
import edu.stanford.photoSpreadUtilities.PhotoSpreadHelpers;
import edu.stanford.photoSpreadUtilities.PhotoSpreadObjIndexerFinder;
import edu.stanford.photoSpreadUtilities.TreeSetRandomSubsetIterable;
/**
 *
 * @author skandel
 */
public class PhotoSpreadCell 
implements Transferable, ObjectUniquenessReference<PhotoSpreadObject> {


	static private String COLNUM_ATTRIBUTE_NAME = "colNum";
	static private String ROWNUM_ATTRIBUTE_NAME = "rowNum";
	public static String FILEPATH = "@filename";
	public static String OBJECT_ID = "@ID";

	private PhotoSpreadExpression _expression;
	private TreeSetRandomSubsetIterable<PhotoSpreadObject> _objects;
	private Comparator<PhotoSpreadObject> _currentComparator = null;
	// private PhotoSpreadComparatorFactory.MetadataComparator _metadataComparator = null;
	
	private String _formula;
	// Place to hold the source cell of a formula copy/paste:
	
	private PhotoSpreadTableModel _tableModel;
	private ArrayList<PhotoSpreadCell> _dependents;
	private ArrayList<PhotoSpreadCell> _references;
	private PhotoSpreadNormalizedExpression _normalizedExpression;
	private int _row;
	private int _col;
	private String _currentSortKey = Const.DEFAULT_SORT_KEY;
	private HashMap<PhotoSpreadObject, PhotoSpreadObject> _selectedObjects;
	
	private MetadataIndexer _metadataIndexer = null;
	
	/****************************************************
	 * Constructor(s)
	 *****************************************************/
	
	
	public PhotoSpreadCell(PhotoSpreadTableModel tableModel, int row, int col, String formula){
		this(tableModel, row, col); 
		setFormula(formula, Const.DO_EVAL, Const.DO_REDRAW);
		tableModel.fireTableCellUpdated(row, col);
	}
	
	public PhotoSpreadCell(PhotoSpreadTableModel tableModel, int row, int col){
		this._tableModel = tableModel;
		this._col = col;
		this._row = row;
		_formula = "";
		_expression = new PhotoSpreadConstantExpression("", this);
		_objects = new TreeSetRandomSubsetIterable<PhotoSpreadObject>(
				PhotoSpreadComparatorFactory.createPSMetadataComparator());
		// Have the stored objects indexed. This is *not* an in
		// index on the metadata! Just an index to ensure that 
		// we can identify a String object by its string.
		_objects.setIndexer(new PhotoSpreadObjIndexerFinder());
		_selectedObjects = new HashMap<PhotoSpreadObject, PhotoSpreadObject>();
		_dependents = new ArrayList<PhotoSpreadCell>();
		_references = new ArrayList<PhotoSpreadCell>();
		_normalizedExpression = null;
		_currentSortKey = PhotoSpread.photoSpreadPrefs.getProperty(PhotoSpread.cellSortKeyKey);
				//Const.DEFAULT_SORT_KEY;
		// PhotoSpread.trace("New cell: " + this);
	}

	/****************************************************
	 * Class CSVRecord
	 *****************************************************/

	/**
	 * Wrapper class around ArrayList<String>. Each element
	 * is the value of one column in a CSVSpreadsheet.
	 *
	 */
	public class CSVRecord extends ArrayList<String> {
	
		private static final long serialVersionUID = 1L;
		
	}
	
	/****************************************************
	 * Class CSVSpreadsheet
	 *****************************************************/
	
	/**
	 * Wrapper class around ArrayList<CSVRecord>. An instance
	 * of this class holds one spreadsheet in CSV format.
	 * Each element is one CSVRecord, i.e. one row of values
	 * in the spreadsheet.
	 *
	 */
	public class CSVSpreadsheet extends ArrayList<CSVRecord> {
		
		private static final long serialVersionUID = 1L;
		private HashSet<String> _attrs = new HashSet<String>();
		
		/**
		 * Add the metadata keys of one object to an
		 * existing hash set of other objects' keys.
		 * 
		 * @param obj PhotoSpread object whose metadata keys are to be added to the attrs set.
		 * @param attrs Possibly empty HashSet<String> with other metadata keys.
		 */
		
		public void addMetadataKeys (PhotoSpreadObject obj) {
			_attrs.addAll(obj.getMetaDataKeySet());
		}
		
		public void addHeaderRowFromKeys () {
		
			CSVRecord csvRecord = new CSVRecord();
			
			// Create the header row field by field.
			// First field (a.k.a. column) is always the file path to the object:
			//csvRecord.add(FILEPATH);
			
			// Second column is UUID:
			//csvRecord.add(OBJECT_ID);
			
			Iterator<String> keysIt = _attrs.iterator();        
			while(keysIt.hasNext()){
				csvRecord.add(keysIt.next());
			}
			
			add(csvRecord);
		}
		
		/**
		 * Given one PhotoSpreadObject, add its metadata to
		 * this spreadsheet as one row (i.e. one CSVRecord).
		 * <b>Note</b>: You must call addMetadataKeys() first
		 * (repeatedly if you wish) to let this spreadsheet
		 * know about all of the attributes (a.k.a. metadata
		 * keys) that will be in this spreadsheet. You may
		 * also wish first to call addHeaderRowFromKeys() to
		 * put a header row as the first record. This call
		 * is not required if this spreadsheet is not to contain
		 * such a header row.
		 *  
		 * @param obj Object whose metadata is to be added.
		 */
		public void addMetadata (PhotoSpreadObject obj) {

			CSVRecord csvRecord = new CSVRecord();
			
			//csvRecord.add((String) obj.toString());
			//csvRecord.add((String) obj.);
			//csvRecord.add(obj.getObjectID().toString());
			Iterator<String> keysIt = _attrs.iterator();        
			while(keysIt.hasNext()){
				String key = keysIt.next();
				String value = obj.getMetaData(key);
				if(key.equals(PhotoSpreadCell.OBJECT_ID)) value = "=\'"+value+"\'";
				csvRecord.add(value);
			}
			add(csvRecord);
		}
	}

	/****************************************************
	 * Methods
	 *****************************************************/
	
	public String toString(){
		//return "Cell at " + _col +  " col " + getRow() + " row with formula " + _formula;  
		return "<Cell " + getCellAddress() +  " with formula '" + _formula + "'>";  
	}

	/**
	 * Obtain cell's Excel-like address.
	 * @return Excel-like address: A2, C5, AC149, ...
	 */
	public String getCellAddress() {
		// 
		if (_col == 0) return "";
		return getColumnName() + getRowOriginOne();
	}

	public CellCoordinates getCellCoordinates () {
		return new CellCoordinates(_row, _col);
	}
	
	
	/**
	 * Returns the column name of this cell
	 * @return Column name (A, B, C, ...)
	 */
	
	public String getColumnName () {
		// If this cell is in column 0 (the row numbers)?
		if (_col == 0) return "";
		return Misc.intToExcelCol(_col);
	}

	/**
	 * Returns the row integer, origin 1.
	 * 
	 * @return the _row
	 */
	public int getRowOriginOne() {
		return _row + 1;   // (_row is 0-origin)
	}
	
	/**
	 * Returns cell's row number with origin 1.
	 * @return Row number (origin 1)
	 */
	public int getRow () {
		return _row;
	}
	/**
	 * @return Column number of this cell object (origin 0)
	 */
	public int getColumn() {
		return _col;
	}

	public String getFormula(){
		return _formula;
	}
	
	public Boolean isItemCollection() {
		return _formula.equals(Const.OBJECTS_COLLECTION_INTERNAL_TOKEN);
	}
	
	public Comparator<PhotoSpreadObject> getComparator() {
		return _currentComparator;
	}
	
	public void setMetadataIndexer(MetadataIndexer _metadataIndexer) {
		this._metadataIndexer = _metadataIndexer;
	}

	public MetadataIndexer getMetadataIndexer() {
		return _metadataIndexer;
	}
	
	public boolean hasMetadataIndexer () {
		return (_metadataIndexer != null);
	}
	
	public void invalidateMetadataIndexer (
			PhotoSpreadObject obj, 
			String attr, 
			String oldValue,
			String newValue) {
		
		if (_metadataIndexer != null)
			_metadataIndexer.updateIndex (obj, attr, oldValue, newValue);
	}

	public void clearMetadataIndexer() {
		if (hasMetadataIndexer())
			_metadataIndexer.clear();
	}
	
	/* Andreas: added second  and third parms and made evaluate() and redraw
	 * 			contingent on these new parameters. Refactored
	 * 			all callers to set these to true.
	 */
	public void setFormula(String value, Boolean reEvaluateCell, Boolean reDrawTable){
		
		String savedFormula = _formula;
		ArrayList<PhotoSpreadCell> savedDependencyParents = new ArrayList<PhotoSpreadCell>();
		ArrayList<PhotoSpreadCell> savedReferences = new ArrayList<PhotoSpreadCell>();
		
		_formula = value;

		// Go through the list of cells that this cell
		// references. On the way, take two actions:
		// save the refered-to cell, and remember every
		// cell that we depend on (removeDependent(this)
		// returns true):
		
		for(PhotoSpreadCell reference : _references) {
			savedReferences.add(reference);
			if (reference.removeDependent(this))
				savedDependencyParents.add(reference);
		}
		
		clearReferences();

		if (reEvaluateCell) {
			// Each cell that gets
			// changed by this following evaluate()
			// will fire its own changed-event:
			while (true) {
				try {
					evaluate(reDrawTable);
					break;
				} catch (PhotoSpreadException.FormulaError e) {
					
					// Bad formula; either syntax or parameter values. 
					// Put up a dialog box that lets
					// user modify the formula and click a Resubmit
					// or a Cancel button. If resubmit, we do that
					// by hopping up to the start of the loop. If
					// cancel, we restore the cell's state to what
					// it was before the change attempt:
					
					if (offerFormulaCorrection(e)) {
						getTable().getFormulaEditor().repaint();
						continue;
					}
					else {
						_formula = savedFormula;
						clearReferences();
						for (PhotoSpreadCell reference : savedReferences)
							_references.add(reference);
						for (PhotoSpreadCell dependenceParent : savedDependencyParents) 
							dependenceParent.addDependent(this);
						break; // ... out of the while(true) loop
					}
				}
			}
		}

		// Indicate that this cell's value has changed:\
		if (reDrawTable)
			_tableModel.fireTableCellUpdated(_row, _col);
	}
	
	private boolean offerFormulaCorrection(PhotoSpreadException.FormulaError e) {
		
		String correctedFormula = (String) JOptionPane.showInputDialog (
				getTable(),                     // Window to center dialog box in
				e.getMessage(), 
				"Formula Error",    			// Text in the title bar
				JOptionPane.ERROR_MESSAGE,      // Type of msg (e.g. for the icon)
				null, // icon
				null, // array of selection values
				_formula // initial selection value
		);
		
		// Did user cancel out?
		if (correctedFormula == null)
			return false;
		
		_formula = correctedFormula;
		return true;
	}
	
	public void pasteFormula(){
		PhotoSpreadCell copiedCell = getTable().getTableModel().getFormulaClipboard();
		if (copiedCell == null) 
			throw new RuntimeErrorException(new Error("pasteFormula was called when _copiedFormulaCell was null."));
		int rowOffset = this.getRowOriginOne() - copiedCell.getRowOriginOne();
		int colOffset = this._col - copiedCell._col;
		String equals = "";
		if(copiedCell.isFormulaCell()){
			equals = "=";
		}
		try {
			this.setFormula(equals + copiedCell.getExpression().copyExpression(rowOffset, colOffset), 
					Const.DO_EVAL, 
					Const.DO_REDRAW);
		} catch (InvalidParameterException e) {
			Misc.showErrorMsg(e.getMessage());
			return;
		}

	}
	
	public PhotoSpreadTable getTable () {
		return getTableModel().getTable();
	}
	
	public void requestFocus () {
		if (getTable().getSelectedCell() == this)
			releaseFocus();
		getTable().setColumnSelectionInterval(_col, _col);
		getTable().setRowSelectionInterval(_row, _row);
	}
	
	public void releaseFocus () {
//		getTable().clearSelection();
		// Requesting focus on the non-focusable col 0 has
		// the effect of releasing focus on whatever cell is
		// selected.
		getTable().setColumnSelectionInterval(0, 0);
		getTable().setRowSelectionInterval(_row, _row);
	}
	
	public PhotoSpreadExpression getExpression(){
		return _expression;
	}
	
	public PhotoSpreadNormalizedExpression getNormalizedExpression() {
		return _normalizedExpression;
	}
	
	public void setNormalizedExpression(PhotoSpreadNormalizedExpression _normalizedExpression) {
		this._normalizedExpression = _normalizedExpression;
	}
	
	public TreeSetRandomSubsetIterable<PhotoSpreadObject> getObjects() {
		return _objects;
	}

	/**
	 * evaluates the cell using the cells formula and then updating any cells that refer to this cell 
	 * @param redrawTable TODO
	 * @throws FormulaSyntaxError 
	 * */

	public void evaluate(boolean redrawTable) throws FormulaError{

		PhotoSpreadExpression expression = _tableModel.evaluate(_formula, this);
		this._expression = expression;
		try {
			
			setObjects(expression.evaluate(this));
			sortObjects();
			
		} catch (PhotoSpreadException.IllegalArgumentException e) {
			throw new PhotoSpreadException.FormulaError(e.getMessage());
		}
		this._normalizedExpression = expression.normalize(this);

		for(int i = 0; i < _dependents.size(); i++){
			_dependents.get(i).evaluate(redrawTable);
		}
		if (redrawTable)
			_tableModel.fireTableCellUpdated(_row, _col);
	}
	public void addDependent(PhotoSpreadCell dependent){
		if ((!_dependents.contains(dependent)) &&
			(dependent != this)) {
			_dependents.add(dependent);
		}
	}
	public boolean removeDependent(PhotoSpreadCell dependent){
		if(_dependents.contains(dependent)){
			_dependents.remove(dependent);
			return true;
		} else
			return false;
	}
	
	public void addReference(PhotoSpreadCell reference){
		if(!_references.contains(reference)){
			_references.add(reference);
		}
	}
	private void clearReferences(){ 
		_references.clear();
	}

	public PhotoSpreadTableModel getTableModel(){
		return this._tableModel;
	}

	/**
	 * Set this cell's objects store. This store must
	 * already contain an object indexer. Else we throw
	 * an error.
	 * @param _objects The new store with or without objects inside.
	 */
	public void setObjects(TreeSetRandomSubsetIterable<PhotoSpreadObject> objects) 
	throws PhotoSpreadException.IllegalArgumentException {
		
		if (! _objects.hasIndexer())
			throw new PhotoSpreadException.IllegalArgumentException(
					"Cell object store must have an indexer.");
		
		this._objects = objects;
	}

	public void addObject(PhotoSpreadObject object){
		_objects.add(object);
	}
	
	public void addObject(File file) throws BadUUIDStringError, FileNotFoundException, IOException{
		_objects.add(PhotoSpreadFileImporter.importFile(file, this));
	}
	
	public void addObjects(ArrayList<PhotoSpreadObject> toAdd){
		_objects.addAll(toAdd);
	}
	
	public void addObjects(TreeSetRandomSubsetIterable<PhotoSpreadObject> toAdd) {
		_objects.addAll(toAdd);
	}
	
	/**
	 * Add COPIES of these objects to this cell AND set those 
	 * copieed object's home cell to this cell:
	 * @param toAdd
	 */
	public void assimilateObjects(ArrayList<PhotoSpreadObject> toAdd) {
		for (PhotoSpreadObject obj : toAdd) {
			PhotoSpreadObject objCopy = obj.copyObject();
			objCopy.setCell(this);
			_objects.add(objCopy);
		}
	}
	
	/**
	 * Clear this cell.
	 */
	public void clear() {
		clear(Const.DO_EVAL, Const.DO_REDRAW);
	}
	
	public void clear(boolean evaluate, boolean redraw) {
		clearObjects();
		_expression = new PhotoSpreadConstantExpression("", this);
		_selectedObjects.clear();
		
		// Ensure that all dependents update
		// themselves, given that this cell is
		// now empty:
		if (evaluate)
			try {
				evaluate(redraw);
			} catch (Exception e) {
				Misc.showErrorMsgAndStackTrace(e, "");
				//e.printStackTrace();
			}
		_dependents.clear();
		_references.clear();
		_normalizedExpression = null;
		clearObjects();
		setFormula("", evaluate, redraw);
		_tableModel.fireTableCellUpdated(_row, _col);
	}
	
	public void clearObjects(){
		_objects.clear();
	}
	public void removeObject(PhotoSpreadObject object){
		_objects.remove(object);
	}
	public void removeObjects(ArrayList<PhotoSpreadObject> toRemove){

		_objects.removeAll(toRemove);

	}
	public Iterator<PhotoSpreadObject> getObjectsIterator(){
		return _objects.iterator();
	}
	public void selectAllObjects() {
		Iterator<PhotoSpreadObject> it = getObjectsIterator();
		while(it.hasNext()) {
			selectObject(it.next());
		}
	}
	public void selectObject(PhotoSpreadObject object){
		_selectedObjects.put(object, object);
	}
	public void deselectObject(PhotoSpreadObject object){
		_selectedObjects.remove(object);
	}
	public void toggleSelectionObject(PhotoSpreadObject object){
		if(_selectedObjects.containsKey(object)){
			deselectObject(object);
		}
		else{
			selectObject(object);
		}
	}
	public void deselectAll(){
		_selectedObjects.clear();
	}
	
	public ArrayList<PhotoSpreadObject> getSelectedObjects(){
		ArrayList<PhotoSpreadObject> objects = new ArrayList<PhotoSpreadObject>();
		Iterator<Entry<PhotoSpreadObject, PhotoSpreadObject>> it = _selectedObjects.entrySet().iterator();
		while(it.hasNext()){
			PhotoSpreadObject object = it.next().getKey();
			objects.add(object);
		}

		return objects;
	}

	public boolean isObjectSelected(PhotoSpreadObject object){
		return (_selectedObjects.containsKey(object));
	}
	
	public boolean isFormulaCell(){
		return _formula.startsWith("=");
	}

	public boolean isObjectCollection(){
		return _formula == Const.OBJECTS_COLLECTION_INTERNAL_TOKEN; 	
	}
	
	public PhotoSpreadCell forceObject(PhotoSpreadObject object, Boolean reEvaluateCell, Boolean reDrawTable) 
			throws IllegalArgumentException{
		return _normalizedExpression.forceObject(object, reEvaluateCell, reDrawTable);
	}
	
	/****************************************************
	 * Methods for DnD, Copy/Paste, and Export
	 *****************************************************/
	
/*	public Object getTransferData(DataFlavor requestedFlavor) throws UnsupportedFlavorException, IOException {
		return this;
	}
*/	
	
	public Object getTransferData(DataFlavor requestedFlavor) throws UnsupportedFlavorException, IOException {
		
		try {
			if (requestedFlavor.getRepresentationClass().getName().equals("java.util.List"))
				return getTransferData(new PhotoSpreadFileListFlavor());
		
			if (requestedFlavor.getRepresentationClass().getName().equals("photoSpreadTable.PhotoSpreadCell"))
				return getTransferData((PhotoSpreadCellFlavor) requestedFlavor);
			
		} catch (ClassNotFoundException e) {
			// let the throw below do the throwing.
		}
		
		throw new UnsupportedFlavorException(requestedFlavor);

		// Can't throw this nicer msg, b/c getTransferData() must throw
		// only exactly the prescribed exceptions.
/*		throw new PhotoSpreadException.UnsupportedDataFlavor(
				"PhotoSpread cannot deliver drag/drop data using DataFlavor '" +
				requestedFlavor.getHumanPresentableName() +
				".'");
*/	}
	
	public Object getTransferData(PhotoSpreadCellFlavor requestedFlavor) throws UnsupportedFlavorException, IOException {
		return this;
	}

	public Object getTransferData(PhotoSpreadFileListFlavor requestedFlavor) throws UnsupportedFlavorException, IOException {
		
		// String tmpDir = System.getProperty("java.io.tmpdir");
		// String imgFileSuffix = ".jpg";
		// String csvFileSuffix = ".csv";
		ArrayList<File> objFilesList = new ArrayList<File>();
		
		// Go through all selected objects in this cell and
		// put its file name (packaged in a File obj) into
		// the result list. In addition, for each obj, generate 
		// a tmporary csv file containing the respective obj's
		// metadata. The csv files will be deleted when the
		// app exits.
		
		Iterator<PhotoSpreadObject> it = getSelectedObjects().iterator();
		
		while (it.hasNext()) {
			
			// First the PhotoSpread object itself:
			PhotoSpreadObject selectedObj = it.next();
			String objPath = (String) selectedObj.toString();
			objFilesList.add(new File(objPath));
			
			// Now build the tmp csv file:
			// ****   TODO.
		}
		return objFilesList;
	}
	
	public DataFlavor[] getTransferDataFlavors() {
		return DnDSupport.supportedImportExportFlavorsForTable;
	}
	
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return (DnDSupport.supportedImportExportFlavorsForTableArrayList.contains(flavor));
	}
	
	public String toXML() {
		
		StringBuffer xml = new StringBuffer();
		
		xml.append("<" + XMLProcessor.CELL_ELEMENT + " " +
				   COLNUM_ATTRIBUTE_NAME + "=\"" + getColumn() + "\"" + " " + 
				   ROWNUM_ATTRIBUTE_NAME + "=\"" + getRow() + "\"" +
				   ">" + System.getProperty("line.separator"));
		
		if (getSortKey() != null) {
			xml.append(PhotoSpreadHelpers.getXMLElement(XMLProcessor.CELL_SORT_KEY_ELEMENT, getSortKey()));
		}
		if (!_formula.isEmpty())
			xml.append(PhotoSpreadHelpers.getXMLElement(XMLProcessor.CELL_FORMULA_ELEMENT, _formula));
		
		if(this._formula.equals(Const.OBJECTS_COLLECTION_INTERNAL_TOKEN)){
			
			xml.append(PhotoSpreadHelpers.getXMLElement(XMLProcessor.OBJECTS_ELEMENT, PhotoSpreadHelpers.TagType.startTag));
			Iterator<PhotoSpreadObject> it = _objects.iterator();
			
			while(it.hasNext()){
				// Marshall one object within this cell:
				xml.append(it.next().toXML());
			}
			
			xml.append(PhotoSpreadHelpers.getXMLElement(XMLProcessor.OBJECTS_ELEMENT, PhotoSpreadHelpers.TagType.endTag));
		}
		
		xml.append("</" + XMLProcessor.CELL_ELEMENT + ">" + System.getProperty("line.separator"));
		return xml.toString();
	}
	
	public void toXML(BufferedWriter out){
		
		String xmlStr = toXML();
		try{
			out.write(xmlStr);
		}
		catch(java.io.IOException e){
			throw new RuntimeException(
					"Could not write XML to disk. Error: " +
					e.getMessage());
		}
	}
	
	/**
	 * Generates a spreadsheet of this cell in csv
	 * format.
	 * @return ArrayList of ArrayLists. Each inner ArrayList
	 * is a row that holds the metadata of one object.
	 */
	public CSVSpreadsheet toCSV(){
		
		CSVSpreadsheet resSheet = new CSVSpreadsheet();
		
		// Iterator across all the cell's objects:
		Iterator<PhotoSpreadObject> objectIt = _objects.iterator();
		
		// Collect the keys for all the metadata for all objects:
		while(objectIt.hasNext()){
			PhotoSpreadObject nextObject = objectIt.next();
			resSheet.addMetadataKeys(nextObject);
		}
		
		resSheet.addHeaderRowFromKeys ();
		
		objectIt = _objects.iterator();
		while(objectIt.hasNext())
			resSheet.addMetadata(objectIt.next());
		
		return resSheet;
	}
	
	public void doSort (
			TreeSetRandomSubsetIterable<PhotoSpreadObject> newObjectSet, 
			Comparator<PhotoSpreadObject> comp) {
		// Note: we assume here that the passed-in set already
		//       has an indexer installed. We ensure that in
		// 		 all callers to this method.
		newObjectSet.addAll(_objects);
		_objects = newObjectSet;
		_currentComparator = comp;
		_tableModel.fireTableCellUpdated(_row, _col);
	}
	
	public void sortObjects (Comparator<PhotoSpreadObject> comp) {
		TreeSetRandomSubsetIterable<PhotoSpreadObject> newObjectSet = 
			new TreeSetRandomSubsetIterable<PhotoSpreadObject>();
		newObjectSet.setIndexer(new PhotoSpreadObjIndexerFinder());
		doSort(newObjectSet, comp);
	}
	
	public void sortObjects (PhotoSpreadComparatorFactory.MetadataComparator comp, String metadataField) {
		if (metadataField != null) {
			comp.addMetadataSortKey(metadataField);
			comp.addMetadataSortKey("@UUID");
		}
		TreeSetRandomSubsetIterable<PhotoSpreadObject> newObjectSet = 
			new TreeSetRandomSubsetIterable<PhotoSpreadObject>(comp);
		
		newObjectSet.setIndexer(new PhotoSpreadObjIndexerFinder());
		doSort(newObjectSet, comp);
	}

	public void sortObjects (String metadataField) {
		// Only sort if there is a sort key:
		if ((metadataField == null) ||
			(metadataField.isEmpty()) ||
			(metadataField.equalsIgnoreCase("null"))) {
			return;
		}
		sortObjects(PhotoSpreadComparatorFactory.createPSMetadataComparator(), metadataField);
	}
	
	public void sortObjects() {
		if (_currentSortKey != null) {
			sortObjects(_currentSortKey);
		}
	}
	
	public String getSortKey() {
		return _currentSortKey;
	}
	
	public void setSortKey(String sortKey) {
		_currentSortKey = sortKey;
	}
	
	public TreeSet<String> getAllMetadataKeys () {
		
		TreeSet<String> allMetadataAttrNames = new TreeSet<String>();
		
		Iterator<PhotoSpreadObject> it = _objects.iterator();
		
		while (it.hasNext()) {
			PhotoSpreadObject obj = it.next();
			allMetadataAttrNames.addAll(obj.getMetaDataKeySet());
		}
		return allMetadataAttrNames;
	}

	public PhotoSpreadStringObject find(String str) {
		return _objects.find(str);
	}

	public PhotoSpreadObject find(Double dbl) {
		return _objects.find(dbl);
	}

	public PhotoSpreadObject find(File obj) {
		return _objects.find(obj);
	}
	
	public PhotoSpreadObject find(PhotoSpreadTable tbl) {
		return _objects.find(tbl);
	}
	
	/**
	 * Given a metadataKey and a metadataValue,
	 * return all objects in this cell who have
	 * metadataValue under key metadataKey.
	 * This method uses a metadata indexer, if
	 * one is installed. 
	 * 
	 * @param metadataKey
	 * @param metadataValue
	 * @return HashSet of qualifying objects.
	 */
	public HashSet<PhotoSpreadObject> find(String metadataKey, String metadataValue) {
		
		if (hasMetadataIndexer())
			return _metadataIndexer.get(metadataKey, metadataValue);
		
		HashSet<PhotoSpreadObject> res = new HashSet<PhotoSpreadObject>();
		
		for (PhotoSpreadObject obj : _objects)
			if (obj.getMetaData(metadataKey).equals(metadataValue))
				res.add(obj);
		
		return res;
	}
}
