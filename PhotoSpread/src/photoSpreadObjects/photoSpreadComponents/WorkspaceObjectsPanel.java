/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package photoSpreadObjects.photoSpreadComponents;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.rmi.NotBoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import photoSpread.PhotoSpread;
import photoSpread.PhotoSpreadException.CannotLoadImage;
import photoSpread.PhotoSpreadException.IllegalArgumentException;
import photoSpreadObjects.PhotoSpreadObject;
import photoSpreadTable.DnDSupport;
import photoSpreadTable.PhotoSpreadCell;
import photoSpreadTable.PredictableEquiSizedGridLayout;
import photoSpreadUtilities.ComputableDimension;
import photoSpreadUtilities.Const;
import photoSpreadUtilities.Misc;
import photoSpreadUtilities.PhotoSpreadContextMenu;
import photoSpreadUtilities.TreeSetRandomSubsetIterable;

/**
 *
 * @author skandel
 * Workspace-specific UI aspects for all tables. One
 * instance serves all cells.
 */

public class WorkspaceObjectsPanel extends ObjectsPanel {

	private static final long serialVersionUID = 1L;

	private Workspace _workspace;

	private int _workspaceObjWidth = 0;
	private int _workspaceObjHeight = 0;

	// At which percentage of full image size we are currently
	// displaying this cell in the Workspace:
	private int _currentImageSizePercentage = Const.defaultInitialImageSizePercentage;
	private PhotoSpreadAddable _biggestDisplayedObject = null;

	private HashMap<PhotoSpreadCell, CellWorkspaceState> _cellStates = 
		new HashMap<PhotoSpreadCell, CellWorkspaceState>();


	/*************************************************
	 * Constructors
	 *************************************************/

	public WorkspaceObjectsPanel(Workspace _workspace) {
		
		// Suppress addition of the context menu for
		// cells. We'll do a special one for 
		// the workspace:
		
		super(Const.DONT_ADD_CELL_CONTEXT_MENU);
		
		this._workspace = _workspace;
		this.setBackground(Const.workspaceBackgroundColor);
        // *****this.setBorder(new MatteBorder(Const.workspaceInsets, Color.DARK_GRAY));
		setDrawnLabels(new LinkedHashMap<PhotoSpreadObject, PhotoSpreadAddable>());

		this.setLayout(new PredictableEquiSizedGridLayout(
				PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.workspaceNumColsKey),
				PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.workspaceHGapKey),
				PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.workspaceVGapKey)));

		_workspaceObjWidth = PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.workspaceObjWidthKey);
		_workspaceObjHeight = PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.workspaceObjHeightKey);
		_objWidth = _workspaceObjWidth;
		_objHeight= _workspaceObjHeight;
		setObjectsPerRow(PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.workspaceNumColsKey));

		addContextMenu();
	}
	
	/****************************************************
	 * Getters/Setters for WorkspaceObjectsPanel
	 *****************************************************/
	
	public Workspace getWorkspace () {
		return _workspace;
	}
	
	public void setBiggestDisplayedObject(PhotoSpreadAddable _biggestDisplayedObject) {
		this._biggestDisplayedObject = _biggestDisplayedObject;
	}

	public PhotoSpreadAddable getBiggestDisplayedObject() {
		return _biggestDisplayedObject;
	}

	/****************************************************
	 * Private (Inner) Classes
	 *****************************************************/
	
	/**
	 * @author paepcke
	 *
	 * Holds layout state of one cell's Workspace. Used to
	 * restore each cell's Workspace layout when user switches
	 * to that cell. All instances of this class are held in
	 * a HashMap in the outer class (WorspacePanel). 
	 * 
	 */

	private class CellWorkspaceState {

		private int _numRows; 
		private int _numCols;
		private int _imageSizePercentage;
		private ComputableDimension _workspaceWindowSize;
		private int _objectWidth;
		private int _objectHeight;
		private int _page;
		private int _hGap;
		private int _vGap;

		/****************************************************
		 * Constructor(s)
		 *****************************************************/

		// Regular constructor with all the data passed in:

		protected CellWorkspaceState (
				int numWorkspaceRows, 
				int numWorkspaceCols,
				int imageSizePercentage,
				ComputableDimension workspaceWindowSize,
				int objectWidth,   // In Workspace, not in the sheet cells
				int objectHeight,  // In Workspace, not in the sheet cells
				int page,           // Current page in the Workspace
				int hGap,
				int vGap
		) {
			_numRows = numWorkspaceRows;
			setSavedNumCols(numWorkspaceCols);
			setSavedImageSizePercentage(imageSizePercentage);
			setSavedWorkspaceWindowSize(workspaceWindowSize);
			setSavedObjectWidth(objectWidth);
			setSavedObjectHeight(objectHeight);
			setSavedPageNum(page);
			setSavedVGap(vGap);
			setSavedVGap(hGap);
		}

		// Copy constructor:

		protected CellWorkspaceState (CellWorkspaceState aWSState) {
			this(
					aWSState._numRows,
					aWSState._numCols,
					aWSState._imageSizePercentage,
					new ComputableDimension(aWSState._workspaceWindowSize),
					aWSState._objectWidth,
					aWSState._objectHeight,
					aWSState._page,
					aWSState._hGap,
					aWSState._vGap);
		}

		/****************************************************
		 * Getters/Setters (of inner class CellWorkspaceState)
		 *****************************************************/

		void setSavedNumRows(int _numRows) {
			this._numRows = _numRows;
		}

		int getSavedNumRows() {
			return _numRows;
		}

		void setSavedNumCols(int _numCols) {
			this._numCols = _numCols;
		}

		int getSavedNumCols() {
			return _numCols;
		}

		void setSavedWorkspaceWindowSize(ComputableDimension _workspaceWindowSize) {
			this._workspaceWindowSize = new ComputableDimension (_workspaceWindowSize);
		}

		ComputableDimension getSavedWorkspaceWindowSize() {
			return _workspaceWindowSize;
		}

		void setSavedPageNum(int _page) {
			this._page = _page;
		}

		int getSavedPageNum() {
			return _page;
		}

		private void setSavedImageSizePercentage(int _imageSizePercentage) {
			this._imageSizePercentage = _imageSizePercentage;
		}

		private int getSavedImageSizePercentage() {
			return _imageSizePercentage;
		}

		private void setSavedObjectWidth(int _objectWidth) {
			this._objectWidth = _objectWidth;
		}

		private int getSavedObjectWidth() {
			return _objectWidth;
		}

		private void setSavedObjectHeight(int _objectHeight) {
			this._objectHeight = _objectHeight;
		}

		private int getSavedObjectHeight() {
			return _objectHeight;
		}

		private void setSavedVGap(int vGap) {
			_vGap = vGap;
		}

		@SuppressWarnings("unused")
		private int getSavedVGap() {
			return _vGap;
		}

		@SuppressWarnings("unused")
		private void setSavedHGap(int hGap) {
			_hGap = hGap;
		}

		@SuppressWarnings("unused")
		private int getSavedHGap() {
			return _hGap;
		}


		/****************************************************
		 * Methods (of inner class CellWorkspaceState
		 *****************************************************/

	} // end inner class CellWorkspaceState


	/*************************************************
	 * Methods
	 *************************************************/

	public void invalidate() {
		super.invalidate();
		((PredictableEquiSizedGridLayout) getLayout()).invalidateLayout(this);
	}
	
	public boolean isFocusable () {
		return true;
	}
	
	/**
    Redraw one sheet cell panel
	 * @throws NotBoundException 
	 * @throws NumberFormatException 
	 */

	public void redraw() throws NumberFormatException, NotBoundException{
		redrawPanel();
	}


	/**
	 * Redraw the central panel of the Workspace window 
	 */
	@Override
	public void redrawPanel(){

		// Ensure that the 'objs-per-row' slider on the Workspace
		// window is set to the correct position:

		updateObjsPerRowSizer();

		// Ensure that the 'obj-size' slider on the Workspace
		// window is set to the correct position:

		updateObjSizeSizer();

		int fittableItems = getNumFittableObjsOnWorkspace();
		populatePanel(getColumns(), fittableItems);
		padWorkspacePanel(fittableItems, _maxDisplayedItemDim);
		
		if(getDisplayedCell() != null){
			_workspace.setEnabledButtons(_page);
			if ((this.getDisplayedCell().isFormulaCell() &&
					!this.getDisplayedCell().getFormula().isEmpty()))
				_workspace.setTitle("Workspace for " + this.getDisplayedCell().toString());
			else
				_workspace.setTitle("Workspace for Cell " + this.getDisplayedCell().getCellAddress());
		}
		// ****************************
		setPreferredSize(((PredictableEquiSizedGridLayout) getLayout()).preferredLayoutSize(this));
		//this.getLayout().layoutContainer(_workspace);
		// _workspace.getLayout().layoutContainer(_workspace);
		//_workspace.invalidate();
		//_workspace.validate();
				
		// makeWindowFit(new ComputableDimension(layoutManDim));
		//***************************
		
		if (_drawnLabels.size() < fittableItems)
			_workspace.repaint();
		//makeWindowFit(new ComputableDimension(getPreferredSize()));
 		_workspace.pack();

		saveNumRowsState();
	}
	
	/**
	 * Forces panel to contain some number of items.
	 * The pad items will be black boxes that participate in
	 * the layout but are not otherwise visible or accounted.
	 * 
	 * @param desiredNumObjs Number of items caller wishes this WorkspaceObject to contain.
	 * @param padItemDimension Dimension of the filler objects to be used.
	 */
	void padWorkspacePanel (int desiredNumObjs, ComputableDimension padItemDimension) {
		
		int currNumItems = getDrawnLabels().size();
		JPanel padPanel = null;
		
		if ((currNumItems >= desiredNumObjs) ||
			(padItemDimension == null))
			return;
		
		if (padItemDimension.equals(new Dimension (0,0)))
			return;
				
		for (int i=0; i < desiredNumObjs - currNumItems; i++) {
			
			padPanel = new JPanel();
			padPanel.add(new Box.Filler(padItemDimension, padItemDimension, padItemDimension));
			this.add(padPanel);
		}
	}

	/**
	 * Populates the Workspace
	 * with objects that are stored in the cell that is
	 * associated with this Objects/Workspace panel.
	 * 
	 * Caller is responsible for setting the _objWidth and
	 * _objHeight instance variables to the sizes that new
	 * instantiations of, for instance, photos, are to be
	 * dimensioned.
	 * 
	 * @param numCols Number of columns to lay out 
	 * @param numObjsFittableOnThisPage Total number of objects to be shown in this panel
	 */
	public void populatePanel(int numCols, int numObjsFittableOnThisPage){

		int indxFirstObjIncl;

		// While we build up the display of the objects
		// in the Workspace panel, we look for the largest
		// one. We'll use that later the size sample if user slides
		// the object size slider of the Workspace:

		_maxDisplayedItemDim = new ComputableDimension();
		
		if ((getDisplayedCell() == null) ||
			(numObjsFittableOnThisPage == 0) ||
			(numCols == 0))
			return;

		PhotoSpreadObject oneObject = null;
		TreeSetRandomSubsetIterable<PhotoSpreadObject> currCellObjs = getDisplayedCell().getObjects();
		int numCurrCellObjs = currCellObjs.size();

		// Remove all objects from this panel:
		this.removeAll();
		
		// Remember which labels are currently materialized in
		// memory. This way we don't unnecessarily go to disk.
		// TODO: We could save more of the images than just one
		// pane. Right now it's just one pane's worth:
		
		HashMap<PhotoSpreadObject, PhotoSpreadAddable> previousDrawnLabels = 
			new HashMap<PhotoSpreadObject,PhotoSpreadAddable>(_drawnLabels);

		_drawnLabels.clear();

		if (getDisplayedCell().isFormulaCell()) {
			this.setToolTipText("" + numCurrCellObjs + " objects: " + getDisplayedCell().getFormula());
		}
		else {
			this.setToolTipText("" + numCurrCellObjs + " objects");
		}

		if (numCurrCellObjs == 0)
			return;

		int theoreticallyShownSoFar = _page * numObjsFittableOnThisPage; 
		if (theoreticallyShownSoFar >= numCurrCellObjs)
			indxFirstObjIncl = numCurrCellObjs - numObjsFittableOnThisPage;
		else
			indxFirstObjIncl = theoreticallyShownSoFar;
			
		
		// We'll fit into this display either the full
		// number that physically fits, or what's left
		// in the cell to show that's not been shown; 
		// whichever is less:
		
		int indxLastObjExcl= indxFirstObjIncl + 
			Math.min(numCurrCellObjs - indxFirstObjIncl, numObjsFittableOnThisPage);

		Iterator<PhotoSpreadObject> cellObjIterator = null;

		try {
			// Special iterator that runs only between those 
			// objects in the current cell that are at indices
			// [indxFirstObject, indxLastObject) in the sort
			// order:

			cellObjIterator = currCellObjs.iterator(indxFirstObjIncl, indxLastObjExcl);

		} catch (IllegalArgumentException e) {
			Misc.showErrorMsgAndStackTrace(e, "");
			//System.err.println(e.getMessage());
			//e.printStackTrace();
			return;
		}

		// No 'biggest item' found yet:
		setBiggestDisplayedObject(null);

		ComputableDimension nextAddableSize;

		while (cellObjIterator.hasNext()) {

			try {

				oneObject = cellObjIterator.next();

			} catch (java.util.NoSuchElementException e) {
				System.err.println(e.getMessage());
			}
			if(oneObject != null){

				// TODO: ****** reuse/cash/double-buffer component objects here *****:

				// getObjHeight() and getObjWidth() return the *desired* dimensions,
				// not the natural dimensions of any object:
				// Try to get the cached image:
				PhotoSpreadAddable addable = previousDrawnLabels.get(oneObject);
				if (addable == null){
					try {
						addable = 
							(PhotoSpreadAddable) getObjectComponent(
									oneObject, getObjHeight(), getObjWidth());
					} catch (CannotLoadImage e) {
						Misc.showErrorMsg(e.getMessage());
						continue;
					}
				}

				addable.setCell(getDisplayedCell());
				JComponent thisComponent = addable.getComponent();
				thisComponent.setPreferredSize(new ComputableDimension(getObjHeight(), getObjWidth()));
				DnDSupport.initComponentAsDragSource(this, thisComponent);
				this.add(thisComponent);
				_drawnLabels.put(oneObject, addable);

				// See whether this item is larger than any so far:
				nextAddableSize = addable.getNaturalSize();
				if (nextAddableSize.compareTo(_maxDisplayedItemDim) == Const.BIGGER) {
					// If so, remember its size and location in the array:
					_maxDisplayedItemDim = nextAddableSize;
					setBiggestDisplayedObject(addable);
				}


				// If this just-drawn object is noted as 'selected'
				// in the cell that we're drawing, then highlight
				// this label:
				if (getDisplayedCell().isObjectSelected(oneObject)) {
					addable.highlight();
				}
			}
		}
	}
	
	
	/**
	 * Handle any type of mouse clicking that ObjectPanel's 
	 * clickLabel() does not handle. That's only ALT_LEFT_CLICK.
	 * That combination fires up a zoomer window for the most
	 * recently clicked label in the Workspace, i.e. the label
	 * that the user just alt-left-clicked on.
	 * 
	 * We let ObjectPanel's clickLabel() do everything that happens
	 * for NORMAL_LEFT_CLICK. But then we also add the zoom action. 
	 * @see photoSpreadObjects.photoSpreadComponents.ObjectsPanel#clickLabel(photoSpreadObjects.photoSpreadComponents.DraggableLabel, int)
	 */
	public boolean clickLabel(DraggableLabel label, int clickType){
		
		boolean clickHandled = super.clickLabel(label, clickType);
		
		if (clickHandled) 
			return clickHandled;
		
		switch(clickType){

		case(ObjectsPanel.ALT_LEFT_CLICK):
			super.clickLabel(label, ObjectsPanel.NORMAL_LEFT_CLICK);
			_workspace.spawnZoomerWindow();
		break;
		default:
			return false;
		}
		return true;
	}

	/**
	 * Given a sheet cell object, we create a new cell state
	 * object for that cell and place it in the _cellStates HashMap.
	 * 
	 * NOTE: If the Workspace is modified by the user before selecting
	 * any cell, the Workspace shape (width, height, etc.) will become
	 * the default for all cells.
	 * 
	 * NOTE: if user manipulates the workspace size before selecting
	 * any cell, then this method will be called with null. The first
	 * time this happens we go ahead and create a cell state, which we
	 * add to _cellStates. We can think of this cell state as the state
	 * of the Workspace window before any cell is selected.
	 * 
	 * Next time this method is called with null, we will find that 
	 * state and update it. The moment a non-null cell is passed in,
	 * we provide the null-state as its 'new' state.
	 *
	 * So the main branch of this method only runs once.
	 * @param cell Cell whose state is to be represented by the new cellState object, or null.
	 * @return New cell state object
	 */
	private CellWorkspaceState createCellWorkspaceStatus (PhotoSpreadCell cell) {

		CellWorkspaceState cellState = _cellStates.get(null);

		if (cell == null) {
			// Already created a cell state for the null cell?
			if (cellState != null) {
				return cellState;
			}
			else {
				// Never created any cell state object for null cell: 
				cellState = createDefaultCellState();
				_cellStates.put(null, cellState);
				return cellState;
			}
		} else {
			// A cell was selected for the first time. Give it the
			// state of the null cell as its initial state:
			cellState = new CellWorkspaceState(cellState);
			_cellStates.put(cell, cellState);
			return cellState;
		}

	}

	/**
	 * @param cellState
	 * @return
	 */
	public CellWorkspaceState createDefaultCellState() {

		CellWorkspaceState cellState = null;

		int workspaceWidth;
		int workspaceHeight;
		String wsWidthHeight = (String) PhotoSpread.photoSpreadPrefs.getProperty(PhotoSpread.workspaceSizeKey);

		String[] twoNumStrings = wsWidthHeight.split("[ \t\n\f\r]");
		workspaceWidth = Integer.parseInt(twoNumStrings[0].trim());
		workspaceHeight = Integer.parseInt(twoNumStrings[1].trim());

		int wsObjHeight;
		wsObjHeight = PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.workspaceObjHeightKey);
		int wsObjWidth = PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.workspaceObjWidthKey);
		int defaultNumRows = Math.max(1, workspaceHeight / wsObjHeight); 
		int vGap = PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.workspaceVGapKey);
		int hGap = PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.workspaceHGapKey);

		cellState = new CellWorkspaceState(
				defaultNumRows,                                     	  // Number of rows in Workspace
				PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.workspaceNumColsKey),  		  // Number of cols in Workspace
				Const.defaultInitialImageSizePercentage,                  // Image size as percentage
				new ComputableDimension(workspaceWidth, workspaceHeight), // Size of Workspace window
				wsObjWidth,                                               // Workspace objecst width/height.
				wsObjHeight,
				0, // _page                                              // Initial page to show.
				hGap,
				vGap);

		return cellState;
	}

	@SuppressWarnings("unused")
	private CellWorkspaceState takeCellSnapshot () {

		// Layout of this Workspace panel:
		PredictableEquiSizedGridLayout layoutManager = (PredictableEquiSizedGridLayout) getLayout();

		CellWorkspaceState cellState = new CellWorkspaceState(
				layoutManager.getRows(),
				layoutManager.getColumns(),
				getCurrentImageSizePercentage(),
				new ComputableDimension (_workspace.getSize()),
				getObjWidth(),
				getObjHeight(),
				_page,
				getHGap(),
				getVGap());

		return cellState;
	}

	/**
	 * Given a cell state object, set the Workspace to reflect that state.
	 * @param savedCellWorkspaceState
	 */
	private void setCellState(CellWorkspaceState savedCellWorkspaceState) {

		int prevNumCols = savedCellWorkspaceState.getSavedNumCols();
		int prevNumRows = savedCellWorkspaceState.getSavedNumRows();
		int prevObjWidth = savedCellWorkspaceState.getSavedObjectWidth();
		int prevObjHeight = savedCellWorkspaceState.getSavedObjectHeight();
		int prevImgSizePerc = savedCellWorkspaceState.getSavedImageSizePercentage();
		int prevPageNum = savedCellWorkspaceState.getSavedPageNum();
		// ComputableDimension wsSize = savedCellWorkspaceState.getSavedWorkspaceWindowSize();
		PredictableEquiSizedGridLayout layoutManager = (PredictableEquiSizedGridLayout) getLayout();

		layoutManager.setColumns(prevNumCols);
		_objWidth = prevObjWidth;
		_objHeight = prevObjHeight;
		_page = prevPageNum;
		populatePanel(prevNumCols, prevNumCols * prevNumRows);

		// Now really make the changes: setColumnsRevalidate() will cause the
		// layout to be recomputed and the window to be resized:

		layoutManager.setColumnsRevalidate(prevNumCols, this);
		ComputableDimension layoutManagerPrefSize = 
			(ComputableDimension) layoutManager.preferredLayoutSize(this);

		this.setPreferredSize(layoutManagerPrefSize);

		_workspace.setColNumSliderValue(prevNumCols);
		_workspace.setObjHeightSliderValue(prevImgSizePerc);

		_workspace.pack();

	}

	/**
	 * Restore the currently active workspace to its
	 * defaults as specified in the user's preferences file
	 * or the our hardwired system defaults. This default
	 * state becomes this cell's saved state:
	 */
	public void resetCellWorkspaceToDefault() {

		CellWorkspaceState defaultCellState =  createDefaultCellState();
		setCellState(defaultCellState);
		_cellStates.put(getDisplayedCell(), defaultCellState);
		
		// If any arrow-key selections were going on, abandon them:
		WorkspaceSelector.endSelectionXaction();
		
		_drawnLabels.clear();
	}

	public int getColumns () {
		return ((PredictableEquiSizedGridLayout) this.getLayout()).getColumns();
	}

	public int getRows() {
		return ((PredictableEquiSizedGridLayout) this.getLayout()).getRows();
	}
	/**
	 * Sets the number of objects, a.k.a. columns in this Workspace.
	 * Caller needs to call redrawPanel() sometime after calling this!
	 * 
	 * @param objsPerRow Desired number of objects in one row of the Workspace
	 */
	public void setObjectsPerRow (int objsPerRow) {

		PredictableEquiSizedGridLayout layoutManager = (PredictableEquiSizedGridLayout) this.getLayout();

		// Set the number of *desired* columns in the layout manager.
		// This move does not lay anything out yet:

		layoutManager.setColumns(objsPerRow);

		// We'll now have more room in the layout (once the re-layout will happen).
		// Prepare the Workspace panel to contain those additional objects:

		populatePanel(objsPerRow, getNumFittableObjsOnWorkspace());

		// Now really make the changes: setColumnsRevalidate() will cause the
		// layout to be recomputed and the window to be resized:

		layoutManager.setColumnsRevalidate(objsPerRow, this);
		ComputableDimension layoutManagerPrefSize = 
			(ComputableDimension) layoutManager.preferredLayoutSize(this);

		this.setPreferredSize(layoutManagerPrefSize);
		_workspace.pack();
		saveNumColsState();
		saveWorkspaceWindowSizeState();
	}

	public void setDisplayedCell(PhotoSpreadCell cell) {

		_displayedCell = cell;

		/* We just changed cells, so one of two
		   activities need to take place:
		     - If the cell was clicked on for the
		        first time, we create a CellWorkspaceState
		        object and save UI related settings in it.
		     - If the cell has already been thus initialized,
		       we restore the cell's Workspace appearance. 
		 */

		CellWorkspaceState savedCellWorkspaceState = _cellStates.get(cell);

		if (savedCellWorkspaceState == null) {

			// Never displayed this cell before. Create a saved-state
			// for this cell, initializing to the defaults, or to 
			// the user preferences from the pref file:

			savedCellWorkspaceState = createCellWorkspaceStatus(cell);
		}			
		// User visited this cell earlier. Recreate the Workspace state:

		setCellState(savedCellWorkspaceState);
		_workspace.redraw();
	}


	/**
	 * Set window size of this WorkspacePanel just large
	 * enough to accommodate whatever is in there right
	 * now.
	 */

	public void makeWindowFit () {
		makeWindowFit(new ComputableDimension(_workspace.getMinimumSize()));
	}

	/**
	 * Enlarges or shrinks the enclosing window to 
	 * fit the size of this panel. Units are pixels.
	 * 
	 * @param width. If value is UNCHANGED the current width is retained.
	 * @param height. If value is UNCHANGED the current height is retained.
	 */

	public void makeWindowFit (ComputableDimension widthHeight) {

		// ComputableDimension oldSize = new ComputableDimension( _workspace.getSize());

		int width = widthHeight.width;
		int height= widthHeight.height;

		if (width == Const.UNCHANGED)
			width = this.getWidth();
		if (height == Const.UNCHANGED)
			width = this.getHeight();

		_workspace.setSize(width, height);

		saveWorkspaceWindowSizeState();
	}


	@Override
	protected Component getObjectComponent(PhotoSpreadObject object, int height, int width) throws CannotLoadImage {
		return object.getObjectComponent(height, width);
	}

	public void updateObjsPerRowSizer() {
		updateObjsPerRowSizer(getColumns());
	}

	public void updateObjsPerRowSizer(int newSliderVal) {
		_workspace.setColNumSliderValue(newSliderVal);
	}

	public void updateObjSizeSizer() {
		if (getDisplayedCell() != null)
			updateObjsSizeSizer(getCurrentImageSizePercentage());
	}

	public void updateObjsSizeSizer(int newSliderVal) {
		_workspace.setObjHeightSliderValue(newSliderVal);
	}

	/**
	 * Set objects' width. Currently sets height = width
	 * @param objWidth
	 */

	protected void setObjWidth(int objWidth) {

		int maxObjWidth = 0;
		int maxObjHeight = 0;

		maxObjWidth = PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.workspaceMaxObjWidthKey);
		maxObjHeight = PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.workspaceMaxObjHeightKey);

		if (objWidth > maxObjWidth)
			objWidth = maxObjWidth;

		_objWidth = objWidth;
		_objHeight = objWidth;

		if (_objHeight > maxObjHeight)
			_objHeight = maxObjHeight;

		saveObjectWidthState();
		saveObjectHeightState();
	}

	/**
	 * Set objects' height. Currently sets width = height
	 * @param picHeight
	 */

	protected void setObjHeight(int objHeight) {

		int maxObjWidth = 0;
		int maxObjHeight = 0;

		maxObjWidth = PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.workspaceMaxObjWidthKey);
		maxObjHeight = PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.workspaceMaxObjHeightKey);

		_workspaceObjHeight = Math.min(maxObjHeight, objHeight);
		// For now: keep objects square:
		_workspaceObjWidth  = Math.min(maxObjWidth, objHeight);

		saveObjectHeightState();
		saveObjectWidthState();
	}


	public void setObjWidth(int width, Boolean doRedraw) {
		_workspaceObjWidth = width;
		if (doRedraw)
			redrawPanel();
		saveObjectWidthState();
	}

	public int getObjWidth() {
		// return _workspaceObjWidth;
		return _objWidth;
	}

	public int getObjHeight() {
		// return _workspaceObjHeight;
		return _objHeight;
	}

	public int getPage() {
		return _page;
	}

	public void setPage(int _page, boolean redraw) {
		this._page = _page;
		if (redraw) {
			redrawPanel();
			savePageState();
		}
	}
	
	public void setPage(int _page) {
		this.setPage(_page, Const.DO_REDRAW);
	}

	/**
	 * Compute current page number in Workspace based on
	 * number of objects in the associated cell and the
	 * number of objects that fit into one Workspace panel.
	 * @return Current page number base 0.
	 */
	public int getLastPage(){

		int numFittableObjsThisPage = this.getNumFittableObjsOnWorkspace();
		int numObjsInCurrCell = this.getDisplayedCell().getObjects().size();

		if (numFittableObjsThisPage == 0)
			return 0;
		// Trailing '-1' is to turn into base 0:
		return Math.max(1, (int) Math.ceil((float) numObjsInCurrCell / (float) numFittableObjsThisPage)) - 1;
	}

	/**
	 * @param _objectHGap the _objectHGap to set
	 */
	protected void setHGap(int objectHGap) {
		((PredictableEquiSizedGridLayout) this.getLayout()).setHGapRevalidate(objectHGap, this);
	}

	/**
	 * @return the _objectHGap
	 */
	protected int getHGap() {
		return ((PredictableEquiSizedGridLayout) this.getLayout()).getHGap();		
	}

	/**
	 * @param _objectVGap the _objectVGap to set
	 */
	protected void setVGap(int objectVGap) {
		((PredictableEquiSizedGridLayout) this.getLayout()).setVGap(objectVGap);		
	}

	/**
	 * @return the _objectVGap
	 */
	protected int getVGap() {
		return ((PredictableEquiSizedGridLayout) this.getLayout()).getVGap();
	}



	/*
	 * Compute how many objects fit into the current
	 * size of the Workspace, given our fixed number
	 * across the horizontal, and the dimensions of 
	 * the current Workspace size.
	 * 
	 *  (non-Javadoc)
	 * @see photoSpreadObjects.photoSpreadComponents.ObjectsPanel#getObjsPerPage()
	 */
	protected int getNumFittableObjsOnWorkspace() {
		int numCols = getColumns ();
		int numRows = getRows();

		return numCols * numRows;
	}


	public void setCurrentImageSizePercentage(int newImageSizePercentage) {

		PredictableEquiSizedGridLayout layoutManager = (PredictableEquiSizedGridLayout) this.getLayout();

		_currentImageSizePercentage = newImageSizePercentage;
		setObjectsPerRow(layoutManager.getColumns());
		saveImageSizePercentageState();
	}


	public int getCurrentImageSizePercentage() {
		return _currentImageSizePercentage;
	}

	private CellWorkspaceState currCellStatusObj () {

		CellWorkspaceState cellStatusObj = _cellStates.get(getDisplayedCell());
		if (cellStatusObj == null)
			return createCellWorkspaceStatus(getDisplayedCell());
		else
			return cellStatusObj;
	}

	private void saveNumColsState () {
		currCellStatusObj().setSavedNumCols(getColumns());
	}

	public void saveNumRowsState () {
		currCellStatusObj().setSavedNumRows(((PredictableEquiSizedGridLayout) this.getLayout()).getRows());
	}

	private void saveImageSizePercentageState () {
		currCellStatusObj().setSavedImageSizePercentage(getCurrentImageSizePercentage());
	}

	public void saveWorkspaceWindowSizeState () {
		currCellStatusObj().setSavedWorkspaceWindowSize(new ComputableDimension(_workspace.getSize()));
	}

	private void saveObjectWidthState () {
		currCellStatusObj().setSavedObjectWidth(getObjWidth());
	}

	private void saveObjectHeightState () {
		currCellStatusObj().setSavedObjectHeight(getObjHeight());
	}

	private void savePageState () {
		currCellStatusObj().setSavedPageNum(getPage());
	}

	private void addContextMenu() {

		JMenuItem menuItem;
		
		_contextMenu = new PhotoSpreadContextMenu();
		
		_contextMenu.addMenuItem("Select All",new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				_workspace.selectAll();
			}
		} 
		);
		
		_contextMenu.addMenuItem("Select visibles only",new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				_workspace.deSelectAll();
				_workspace.selectAllVisible();
			}
		} 
		);

		_contextMenu.addMenuItem("Deselect all",new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				_workspace.deSelectAll();
			}
		} 
		);

		_contextMenu.addMenuItem("Deselect visibles only",new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				_workspace.deSelectAllVisible();
			}
		} 
		);		

		menuItem = _contextMenu.addMenuItem("Copy",new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// copy();
			}
		} 
		);
		menuItem.setEnabled(false);

		menuItem = _contextMenu.addMenuItem("Paste",new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// paste();
			}
		} 
		);
		menuItem.setEnabled(false);
		
		this.addMouseListener(_contextMenu.getPopupListener());
	}
}
