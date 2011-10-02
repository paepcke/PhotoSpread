/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.photoSpreadObjects.photoSpreadComponents;

import edu.stanford.inputOutput.CsvWriter;
import edu.stanford.inputOutput.ExifWriter;
import edu.stanford.inputOutput.InputOutput;

import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

//import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import edu.stanford.photoSpread.PhotoSpread;
import edu.stanford.photoSpread.PhotoSpreadException.BadUUIDStringError;
import edu.stanford.photoSpread.PhotoSpreadException.CannotLoadImage;
import edu.stanford.photoSpread.PhotoSpreadException.FormulaError;
import edu.stanford.photoSpread.PhotoSpreadException.IllegalArgumentException;
import edu.stanford.photoSpreadLoaders.CSVFileFilter;
import edu.stanford.photoSpreadLoaders.ImageFileFilter;
import edu.stanford.photoSpreadLoaders.PhotoSpreadFileImporter;
import edu.stanford.photoSpreadObjects.PhotoSpreadFileObject;
import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadObjects.PhotoSpreadTableObject;
import edu.stanford.photoSpreadTable.DnDSupport;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;
import edu.stanford.photoSpreadTable.PhotoSpreadCell.CSVRecord;
import edu.stanford.photoSpreadTable.PhotoSpreadCell.CSVSpreadsheet;
import edu.stanford.photoSpreadUtilities.ComputableDimension;
import edu.stanford.photoSpreadUtilities.Const;
import edu.stanford.photoSpreadUtilities.Misc;
import edu.stanford.photoSpreadUtilities.PhotoSpreadContextMenu;
import edu.stanford.photoSpreadUtilities.ReconyxGroupingInteractor;
import edu.stanford.photoSpreadUtilities.TreeSetRandomSubsetIterable;
import edu.stanford.photoSpreadUtilities.PhotoSpreadContextMenu.PhotoSpreadRadioButtonSubMenu;
import edu.stanford.photoSpreadUtilities.PhotoSpreadContextMenu.PhotoSpreadSubMenu;

/**
 *
 * @author skandel
 * 
 * UI aspects of cells.
 */
public class ObjectsPanel extends JPanel{

	private static final long serialVersionUID = 1L;
	protected PhotoSpreadCell _displayedCell;

	PhotoSpreadContextMenu _contextMenu = null;
	PhotoSpreadSubMenu _sortSubmenu = null;
	PhotoSpreadRadioButtonSubMenu _showSortKeySubmenu = null;


	public ComputableDimension _maxDisplayedItemDim;
	protected int _page = 0;

	protected LinkedHashMap<PhotoSpreadObject, PhotoSpreadAddable> _drawnLabels;

	public static final int NORMAL_LEFT_CLICK = MouseEvent.BUTTON1_DOWN_MASK & (~MouseEvent.SHIFT_DOWN_MASK | ~MouseEvent.CTRL_DOWN_MASK); 
	public static final int SHIFT_LEFT_CLICK = MouseEvent.SHIFT_DOWN_MASK;
	public static final int CTRL_LEFT_CLICK = MouseEvent.CTRL_DOWN_MASK;
	public static final int ALT_LEFT_CLICK = MouseEvent.ALT_DOWN_MASK;

	private DraggableLabel _lastLabelClicked;

	protected int _objWidth;
	protected int _objHeight;  // Currently always kept equal to _objWidth
	
	/*************************************************
	 * Constructors
	 *************************************************/
	public ObjectsPanel (boolean addContexMenu) {
		this(null, addContexMenu);
	}

	public ObjectsPanel (PhotoSpreadCell thisPanelsCell) {
		this(thisPanelsCell, Const.ADD_CELL_CONTEXT_MENU);
	}

	public ObjectsPanel(PhotoSpreadCell thisPanelsCell, boolean addContexMenu) {

		if (thisPanelsCell != null)
			_displayedCell = thisPanelsCell;
		
		if (addContexMenu)
			addContextMenu();
		_page = 0;

		_drawnLabels = new LinkedHashMap<PhotoSpreadObject,PhotoSpreadAddable>();
		setLastLabelClicked(null);
		set_objWidth(PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.sheetCellObjsWidthKey));
		// All sheet cells are square, initially:
		set_objHeight(PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.sheetCellObjsWidthKey));

		setBorder(Const.cellBorder);

	}

	/*************************************************
	 * Internal Classes
	 *************************************************/


	class ObjectsPanelKeyListener extends KeyAdapter {

		public void keyTyped(KeyEvent e) {

			if(e.getID() == KeyEvent.KEY_TYPED) {
				// switch (KeyEvent.KEY_TYPED) {
				switch (e.getKeyChar()) {
				case KeyEvent.VK_C: 
					if(e.getModifiersEx() == KeyEvent.VK_CONTROL) {
						selectAll();
						break;
					} // end case cnt-a pressed
				default:
					break;
				}
			}
		}
	} // end class ObjectPanelKeyListener

	/*************************************************
	 * Methods
	 *************************************************/


	/**
     Redraw one sheet cell panel
	 * @throws NotBoundException 
	 * @throws NumberFormatException 
	 */

	public void redraw() throws NumberFormatException, NotBoundException{
		redrawPanel();
	}

	public void setDisplayedCell(PhotoSpreadCell cell) {
		_displayedCell = cell;
		// _page = 0;
		redrawPanel();
	}

	protected int getNumFittableObjsInCell() {

		int theObjsPerPage = 0;

		theObjsPerPage = PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.sheetObjsInCellKey);
		return theObjsPerPage;
	}

	/**
	 * Redraws the panel

	 */

	public void redrawPanel(){

		set_objWidth(PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.sheetCellObjsWidthKey));
		// All sheet cells are square, initially:
		set_objHeight(PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.sheetCellObjsWidthKey));

		populatePanel(Math.max(1, (this.getWidth() / get_objWidth())), getNumFittableObjsInCell());
		invalidate();
		repaint();
		// this.repaint(this.getBounds());
		// this.paintBorder(getGraphics());

	}

	/**
	 * Populates one sheet cell 
	 * with objects that are stored in the cell that is
	 * associated with this Objects panel.
	 * 
	 * Caller is responsible for setting the _objWidth and
	 * _objHeight instance variables to the sizes that new
	 * instantiations of, for instance, photos, are to be
	 * dimensioned.
	 * 
	 * @param numCols Number of columns to lay out 
	 * @param numObjsFittableIntoThisCell Total number of objects to be shown in this panel
	 */
	public void populatePanel(int numCols, int numObjsFittableIntoThisCell){

		if(_displayedCell == null) return;

		PhotoSpreadObject oneObject = null;
		TreeSetRandomSubsetIterable<PhotoSpreadObject> currCellObjs = 
			_displayedCell.getObjects();
		int numCurrCellObjs = currCellObjs.size();

		// The following used to be a slight
		// optimization. But with the cell draw
		// optimization, this quick return caused
		// objects in a cell still to be visible
		// even if they this cell was cleared of
		// all objects in the model;
		// if (numCurrCellObjs == 0)
		// 	return;

		// Remove all objects from this cell's panel:
		this.removeAll();

		setLastLabelClicked(null);

		// Remember which labels are currently materialized in
		// memory. This way we don't unnecessarily to to disk.
		// TODO: We could save more of the images than just one
		// pane. Right now it's just one pane's worth:
		
		HashMap<PhotoSpreadObject, PhotoSpreadAddable> previousDrawnLabels = 
			new HashMap<PhotoSpreadObject,PhotoSpreadAddable>(_drawnLabels);
		_drawnLabels.clear();

		if (_displayedCell.isFormulaCell()) {
			this.setToolTipText("" + numCurrCellObjs + " objects: " + _displayedCell.getFormula());
		}
		else {
			this.setToolTipText("" + numCurrCellObjs + " objects");
		}

		if (numCurrCellObjs > 0) {

			// Add cell's objects to this newly
			// cleared cell panel:

			Iterator<PhotoSpreadObject> cellObjIterator = null;

			try {

				// For now: always just show the first 10 or so objs in a cell:
				cellObjIterator = currCellObjs.iterator(0, Math.min(numObjsFittableIntoThisCell, numCurrCellObjs));

			} catch (IllegalArgumentException e) {
				Misc.showErrorMsgAndStackTrace(e, "");
				//System.err.println(e.getMessage());
				//e.printStackTrace();
			}

			while (cellObjIterator.hasNext()) {

				try {

					oneObject = cellObjIterator.next();

				} catch (java.util.NoSuchElementException e) {
					System.err.println(e.getMessage());
				}
				if(oneObject != null){

					// Try to get the cached image:
					PhotoSpreadAddable addable = previousDrawnLabels.get(oneObject);
					if (addable == null)
						try {
							addable = (PhotoSpreadAddable) getObjectComponent(oneObject, get_objHeight(), get_objWidth());
						} catch (CannotLoadImage e) {
							Misc.showErrorMsg(e.getMessage());
							continue;
						}

					addable.setCell(_displayedCell);
					JComponent thisComponent = addable.getComponent();
					// thisComponent.setPreferredSize(new ComputableDimension(get_objHeight(), get_objWidth()));
					DnDSupport.initComponentAsDragSource(thisComponent);
					this.add(thisComponent);
					_drawnLabels.put(oneObject, addable);

					// If this just-drawn object is noted as 'selected'
					// in the cell that we're drawing, then highlight
					// this label:
					if (_displayedCell.isObjectSelected(oneObject)) {
						addable.highlight();
					}
				}
			}
		} // end if (numCurrObjs != 0) ... of adding cell's objects to this ObjectPanel

		if(numCurrCellObjs == 0 && _displayedCell.isFormulaCell()){
			this.add(new JLabel("(Empty Set)"));
		}
	}

	/**
	 * Removes all images (DraggableLabesl) that are on the currently visible
	 * Workspace page from the layout.
	 */
	public void removeAllFromLayout() {
		for (PhotoSpreadAddable draggableLabel : _drawnLabels.values())
			this.getLayout().removeLayoutComponent(draggableLabel.getComponent());
	}
	
	protected Component getObjectComponent(PhotoSpreadObject object, int height, int width) throws CannotLoadImage {

		if(object!= null){
			return object.getObjectComponent(height, width);
		}

		return null;
	}

	void selectAll(){
		Iterator<PhotoSpreadAddable> it = _drawnLabels.values().iterator();
		while(it.hasNext()){
			it.next().highlight();
		}
		this._displayedCell.selectAllObjects();
	}


	public void clearSelected(){

		if (_drawnLabels.size() == 0) return;

		Iterator<PhotoSpreadAddable> it = _drawnLabels.values().iterator();
		while(it.hasNext()){
			it.next().unhighlight();
		}
		this._displayedCell.deselectAll();
	}

	/**
	 * triggers that a label in the panel has been clicked
	 * @param label the label that was clicked
	 * @param clickType the type of click (NORMAL, SHIFT, CTRL)

	 */

	public boolean clickLabel(DraggableLabel label, int clickType){

		// All but alt-left-click are handled here. 
		// So set the result optimistically to true,
		// unless proven otherwise below:

		boolean clickHandled = true;

		PhotoSpreadCell cell = label.getCell();

		// The PhotoSpreadObject that this label represents:
		PhotoSpreadObject parentObject = label.getParentObject();

		// If user was selecting/deselecting using the
		// shift-arrow keys, then that activity is terminated
		// with any mouse click:

		WorkspaceSelector.endSelectionXaction();

		switch(clickType){
		case(ObjectsPanel.NORMAL_LEFT_CLICK):

			if(cell.isObjectSelected(parentObject)){
				cell.deselectObject(parentObject);
				label.unhighlight();
			}
			else {
				Iterator<PhotoSpreadAddable> it = _drawnLabels.values().iterator();
				while(it.hasNext()){
					it.next().unhighlight();
				}
				cell.deselectAll();

				cell.selectObject(parentObject);
				label.highlight(); 
			}
		break;
		case(ObjectsPanel.CTRL_LEFT_CLICK):


			if(cell.isObjectSelected(parentObject)){
				cell.deselectObject(parentObject);
				label.unhighlight();
			}
			else{
				cell.selectObject(parentObject);
				label.highlight(); 
			}
		break;
		case(ObjectsPanel.SHIFT_LEFT_CLICK):

			if(getLastLabelClicked() != null){

				// I hate turning the values (i.e. Label objects) of the
				// _drawnLabels LinkedHashMap into an array here. But that's
				// what _drawnLabels used to be. I don't have time/patience to adjust
				// the code in the remainder of this case branch to something
				// nicer:
				ArrayList<PhotoSpreadAddable> drawnLabelArray = new ArrayList<PhotoSpreadAddable>(_drawnLabels.values());

				int lastClickIndex = drawnLabelArray.indexOf(getLastLabelClicked());
				if (lastClickIndex < 0) {
					Misc.showErrorMsg("Both items of a shift-click must be in one visible area.");
					break;
				}
				int currentClickIndex = drawnLabelArray.indexOf(label);

				if(lastClickIndex < currentClickIndex){
					for(int i = lastClickIndex; i <= currentClickIndex; i++){
						PhotoSpreadAddable l = drawnLabelArray.get(i);
						l.highlight();
						cell.selectObject(l.getParentObject());
					}
				}
				else{
					for(int i = currentClickIndex; i <= lastClickIndex; i++){
						PhotoSpreadAddable l = drawnLabelArray.get(i);
						l.highlight();
						cell.selectObject(l.getParentObject());
					}
				}
			}

		break;
		default:
			clickHandled = false;
		break;
		}

		setLastLabelClicked(label);
		return clickHandled;
	}

	public Insets insets () {
		return Const.tableCellInsets;
	}


	private void addContextMenu(){

		_contextMenu = new PhotoSpreadContextMenu(_displayedCell);

		_contextMenu.addMenuItem("Load Object Files Into this Cell",new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				loadFiles(_displayedCell);
			}
		} 
		);

		_contextMenu.addMenuItem("Import Objs and their Metadata Into this Cell from CSV",new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					importFromCSV();
				} catch (BadUUIDStringError e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		} 
		);

		_contextMenu.addMenuItem("Export Metadata of this Cell's Objects to CSV",new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				exportToCSV();
			}
		} 
		);	
		
		_contextMenu.addMenuItem("Save Metadata of this Cell's Objects to Exif",new java.awt.event.ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				writeToExif();
			}
		}
		);
		
/*		_contextMenu.addMenuItem("Refresh display",new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				
				PhotoSpreadTableModel tModel = _displayedCell.getTableModel();
				tModel.fireTableCellUpdated(_displayedCell.getRow(), _displayedCell.getColumn());
			}
		} 
		);	
*/
		// Specifying sort order:
		addMetadataSortMenuItem();
		

		_contextMenu.addMenuItem("Select All",new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				selectAll();
			}
		} 
		);

		_contextMenu.addMenuItem("Clear cell",new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				clearCell();
			}
		} 
		);

/*		_contextMenu.addMenuItem("Insert Table",new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				loadTable();
			}
		} 
		);
*/
		_contextMenu.addMenuItem("Copy",new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				copy();
			}
		} 
		);

		_contextMenu.addMenuItem("Paste",new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				paste();
			}
		} 
		);

		_contextMenu.addMenuItemSeparator();
		
		_contextMenu.addMenuItem("Reconyx Group",new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				ReconyxGroupingInteractor.performReconyxGrouping(_displayedCell);
			}
		} 
		);
		
		
		
		this.addMouseListener(_contextMenu.getPopupListener());
	}

	private void copy(){
		this._displayedCell.getTableModel().copyToClipboard();
	}

	private void paste(){
		PhotoSpreadCell destCell = this._displayedCell;
		PhotoSpreadCell sourceCell = null;
		
		// Cannot paste into a cell with a formula:
		if (destCell.isFormulaCell()) {
			Misc.showErrorMsg("Cannot paste into a formula cell");
			return;
		}
		
		destCell.setFormula(
				Const.OBJECTS_COLLECTION_INTERNAL_TOKEN, 
				Const.DONT_EVAL, Const.DONT_REDRAW);
		
		try {
			sourceCell = (PhotoSpreadCell) destCell.getTableModel().getClipboard();
		} catch (RuntimeException e) {
			Misc.showErrorMsg(
					"Items from cell " + 
					sourceCell.getCellAddress() +
					" cannot be copy/pasted into " +
					destCell.getCellAddress() +
					".");
			return;
		}
		// The following commented-out line properly creates an in-memory
		// duplicate of these objects. This is a possible semantic for
		// Copy/paste. But it seems too confusing to do this. Instead
		// we have copy/paste just reference the original objects:
		// destCell.assimilateObjects(sourceCell.getSelectedObjects());
		destCell.addObjects(sourceCell.getSelectedObjects());
		try {
			destCell.evaluate(Const.DO_REDRAW);
		} catch (FormulaError e) {
			Misc.showErrorMsg(
					"Objects from cell " +
					sourceCell.getCellAddress() +
					" cause the formula of cell " +
					destCell.getCellAddress() +
					" to become invalid.");
		}
	}

	@SuppressWarnings("unused")
	private void saveTable(){
		InputOutput.saveTable(this,  _displayedCell.getTableModel());

	}

	@SuppressWarnings("unused")
	private void loadTableFromXML(){

		InputOutput.loadTable(this, this._displayedCell.getTableModel());
	}

	/**
	 * Ask for a csv file name and import the
	 * PhotoSpreadObjects and their metadata from
	 * that csv file into the cell that contains
	 * this ObjectsPanel.
	 * @throws BadUUIDStringError 
	 */
	private void importFromCSV() throws BadUUIDStringError{
		// TODO: handle Excel format

		File importFile = Misc.getCSVFileNameFromUser();
		if (importFile == null)
			// User cancelled out of the file chooser box:
			return;

		int numObjsInCellBeforeImport = _displayedCell.getObjects().size();
		int numImportedCSVRecords =
			PhotoSpreadFileImporter.importCSV(importFile, _displayedCell);
		int numObjsInCellAfterImport = _displayedCell.getObjects().size();
		
		if (numObjsInCellAfterImport > 0)
			this._displayedCell.setFormula(Const.OBJECTS_COLLECTION_INTERNAL_TOKEN, Const.DO_EVAL, Const.DO_REDRAW);

		// fireTableCellUpdated();  (Done in setFormula();
		if (importFile != null)
			JOptionPane.showMessageDialog(
					this.getParent(), 
					"Done loading " + 
					importFile.getName() + 
					"' (" +
					numImportedCSVRecords +
					" CSV records, adding " +
					(numObjsInCellAfterImport - numObjsInCellBeforeImport) +
					" new objects to " +
					_displayedCell.getCellAddress() +
					").");
	}

	/**
	 * Ask for a target .csv file path and export all
	 * objects from this ObjectsPanel with their
	 * metadata to that .csv file.
	 */
	private void exportToCSV(){

		String priorWriteDir = PhotoSpread.photoSpreadPrefs.getProperty(PhotoSpread.lastDirWrittenKey);

		final JFileChooser fc = new JFileChooser(priorWriteDir);

		CSVFileFilter filter = new CSVFileFilter();
		fc.setFileFilter(filter);		

		fc.setMultiSelectionEnabled(false);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int returnVal = fc.showSaveDialog(this);
		//In response to a button click:
		//int returnVal = fc.showOpenDialog(this);
		if(returnVal == JFileChooser.APPROVE_OPTION){
			File exportFile = fc.getSelectedFile();
			PhotoSpread.photoSpreadPrefs.setProperty(PhotoSpread.lastDirWrittenKey, exportFile.getParent());
			// Make sure file gets a .csv extension:
			try {
				exportFile = new File (Misc.ensureFileExtension(exportFile.getPath(), "csv"));
			} catch (java.text.ParseException e1) {
				// Exception when a directory is passed into ensureFileExtension
				// GUI file chooser prevents that.
				e1.printStackTrace();
			}

			exportFile.setWritable(true);
			try{
				CsvWriter out = new CsvWriter(exportFile.getPath());
				out.setConformToExcel(true);
				CSVSpreadsheet csvObjectRecords = _displayedCell.toCSV();
				Iterator<CSVRecord> row = csvObjectRecords.iterator();
				while (row.hasNext()) {
					out.writeRecord(row.next());
				}
				out.close();
			} catch (Exception e){//Catch exception if any
				System.err.println("Error: " + e.getMessage());
			}
			JOptionPane.showMessageDialog(this.getParent(), "Done writing to '" + exportFile.getName() + "'.");
		}
	}
	
	/**
	 * Writes the current metadata of all objects in the 
	 * cell to their EXIF attr/val pairs in the field
	 * "UserComment"
	 */
	private void writeToExif() {
		// Get set of PhotoSpreadObjects
		TreeSetRandomSubsetIterable<PhotoSpreadObject> currCellObjs = _displayedCell.getObjects();
		int numCurrCellObjs = currCellObjs.size();
		
		if (numCurrCellObjs > 0) {
			// Iterate across set of PhotoSpreadObjects
			Iterator<PhotoSpreadObject> cellObjIterator = null;
			try {
				cellObjIterator = currCellObjs.iterator();
			} catch (Exception e) {
			// } catch (IllegalArgumentException e) {
				Misc.showErrorMsgAndStackTrace(e, "");
			}
			
			int numFilesWrittenTo = 0;
			while (cellObjIterator.hasNext()) {
				// Initialize PhotoSpreadObject
				PhotoSpreadFileObject photoObject = null;
				try {
					photoObject = (PhotoSpreadFileObject) cellObjIterator.next();
				} catch (java.util.NoSuchElementException e) {
					System.err.println(e.getMessage());
				}
				
				// Get file path, metadata, and write
				String filePath = photoObject.getFilePath();
				ArrayList< ArrayList<String> > metadata = photoObject.getMetaDataSet();
				ExifWriter.write(filePath, metadata);
				
				// Confirm success
				numFilesWrittenTo++;
			}
			JOptionPane.showMessageDialog(this.getParent(), "Done writing to " + numFilesWrittenTo + " files.");
		} else {
			JOptionPane.showMessageDialog(this.getParent(), "There are no objects in this cell.");
		}
	}

	@SuppressWarnings("unused")
	private void loadTable(){
		PhotoSpreadTableObject t = new PhotoSpreadTableObject(_displayedCell);
		this._displayedCell.addObject(t);

		fireTableCellUpdated();
	}


	public static void loadFiles(PhotoSpreadCell cell){

		final JFileChooser fc = new JFileChooser(PhotoSpread.photoSpreadPrefs.getProperty(PhotoSpread.lastDirReadKey));

		ImageFileFilter filter = new ImageFileFilter();
		fc.setFileFilter(filter);		

		fc.setMultiSelectionEnabled(true);
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		//In response to a button click:
		int returnVal = fc.showOpenDialog(null);
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;

		loadFiles(cell, fc.getSelectedFiles());

	}

	public static boolean loadFiles (PhotoSpreadCell cell, ArrayList<File> fileObjList) {
		return doFileLoading(cell, Misc.flattenFiles(fileObjList));
	}

	public static boolean loadFiles (PhotoSpreadCell cell, File[] fileObjArray) {
		return doFileLoading(cell, Misc.flattenFiles(fileObjArray));
	}

	private static boolean doFileLoading (PhotoSpreadCell cell, ArrayList<File> files) {

		try {
			if (files.size() > 0)
				PhotoSpread.photoSpreadPrefs.setProperty(PhotoSpread.lastDirReadKey, files.get(0).getParent());
			for(int i = 0; i < files.size(); i++){
				cell.addObject(files.get(i));
			}
			cell.setFormula(Const.OBJECTS_COLLECTION_INTERNAL_TOKEN, Const.DO_EVAL, Const.DO_REDRAW);
			// fireTableCellUpdated(); (Done in setFormula();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private void clearCell() {
		if (JOptionPane.showConfirmDialog(
				this, // Show dialog within the sheet window, not in center of screen (which null would do)
				"Clear this cell? (Though no files will be affected)",
				"Confirm",  // Title
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			this._displayedCell.clear();
		}

		// fireTableCellUpdated();  (Called in the above clear() method.
	}
	
	protected void addMetadataSortMenuItem (){

		_sortSubmenu = new PhotoSpreadSubMenu("Specify sort order");
		_showSortKeySubmenu = new PhotoSpreadRadioButtonSubMenu ("See-&-Pick sort key (slow if many items)", _displayedCell);
		
		_sortSubmenu.addMenuItem("Specify sort key",new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				requestSortKeyFromUserAndSort();
			}
		} 
		);
		
		// Add the sort key collection menu to the sort submenu:
		_sortSubmenu.addMenuItemSubMenu(_showSortKeySubmenu, null);

		// Add the sort submenu to the main context menu:
		_contextMenu.add(_sortSubmenu);
	}
	

	protected void requestSortKeyFromUserAndSort () {
		
		if (_displayedCell.getObjects().isEmpty()) {
			Misc.showInfoMsg(
					"No objects to sort in cell " +
					_displayedCell.getCellAddress() +
					".", 
					this    // Frame to show msg in.
					);
			return;
		}
		
		String sortField = (String) JOptionPane.showInputDialog (
				_displayedCell.getTable(),      // Window to center dialog box in
				"Enter metadata field name to sort on:",
				"Sort field entry",    			// Text in the title bar
				JOptionPane.QUESTION_MESSAGE,   // Type of msg (e.g. for the icon)
				null, 			// icon
				null,			// array of selection values
				PhotoSpreadContextMenu.getCurrentMetadataSortKey ()   // initial value in text box
		);
		if (sortField == null)
			return;
		if (sortField.isEmpty())
			return;

		PhotoSpreadContextMenu.setCurrentMetadataSortKey (sortField);
		_displayedCell.sortObjects(sortField);
	}
	
	protected void set_objWidth(int _objWidth) {
		this._objWidth = _objWidth;
	}

	protected int get_objWidth() {
		return _objWidth;
	}

	protected void set_objHeight(int _objHeight) {
		this._objHeight = _objHeight;
	}

	protected int get_objHeight() {
		return _objHeight;
	}

	public void setLastLabelClicked(DraggableLabel lastLabelClicked) {
		this._lastLabelClicked = lastLabelClicked;
	}

	public DraggableLabel getLastLabelClicked() {
		return _lastLabelClicked;
	}

	public void fireTableCellUpdated() {
		this._displayedCell.getTableModel().fireTableCellUpdated(_displayedCell.getRow(), _displayedCell.getColumn());
	}

	protected void setDrawnLabels(LinkedHashMap<PhotoSpreadObject,PhotoSpreadAddable> _drawnLabels) {
		this._drawnLabels = _drawnLabels;
	}

	protected LinkedHashMap<PhotoSpreadObject,PhotoSpreadAddable> getDrawnLabels() {
		return _drawnLabels;
	}

	public PhotoSpreadCell getDisplayedCell() {
		return _displayedCell;
	}
	
	/**
	 * We consider all of the associated cell's 
	 * objects to be the component count of this
	 * panel. Note that this number is different from
	 * the currently *displayed* number of components.
	 * 
	 * @see java.awt.Container#getComponentCount()
	 */
	public int getUserComponentCount() {

		// During startup some vars won't yet
		// be initialized, causing null pt exceptions.
		// In that case, return 0:
		
		try {
			return _displayedCell.getObjects().size();
		} catch (Exception e) {
			return 0;
		}
	}
	
	
/*	public int getComponentCount () {
		
		int orig = 0;
		int mine = 0;
		// During startup some vars won't yet
		// be initialized, causing null pt exceptions.
		// In that case, return 0:
		
		try {
			//********
			orig = super.getComponentCount();
			mine = _displayedCell.getObjects().size();
			if (orig != mine) {
				throw new InvalidParameterException();
			}
			//********
			// return _displayedCell.getObjects().size();
			return super.getComponentCount();
		} catch (Exception e) {
			if (orig != mine) {
				System.out.println("Comps: orig:" + orig + ". Mine: " + mine);
				try {
					Thread.sleep(10);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			return 0;
		}
	}
*/

}
