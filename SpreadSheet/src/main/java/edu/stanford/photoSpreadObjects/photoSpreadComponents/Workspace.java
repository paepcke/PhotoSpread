/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.photoSpreadObjects.photoSpreadComponents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.stanford.photoSpread.PhotoSpread;
import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;
import edu.stanford.photoSpreadTable.PhotoSpreadDragDropManager;
import edu.stanford.photoSpreadTable.PredictableEquiSizedGridLayout;
import edu.stanford.photoSpreadUtilities.ComputableDimension;
import edu.stanford.photoSpreadUtilities.Const;
import edu.stanford.photoSpreadUtilities.Misc;
import edu.stanford.photoSpreadUtilities.SuperPositioner;
import edu.stanford.photoSpreadUtilities.Zoomer;
import edu.stanford.photoSpreadUtilities.Const.Direction;
import edu.stanford.photoSpreadUtilities.Misc.ShowHelpAction;

/**
 *
 * @author skandel
 */

/**
 * Modification History Jul-4-2008 Initialized min/max values of workspace image
 * slider (_colNumSetSlider) to 1 and 100, respectively. The default zero causes
 * div-by-zero later on.
 * 
 */

public class Workspace extends JFrame {

	private static final long serialVersionUID = 1L;
	private WorkspaceObjectsPanel _workspacePanel;

	JFrame _theSheetWindow;
	private JSlider _colNumSetSlider;
	private JSlider _objHeightSetSlider;
	JButton _nextSet;
	JButton _prevSet;
	JButton _homeSet;
	JButton _endSet;
	JLabel _pageViewer;

	JButton _testButton;
	JButton _zoomButton;

	JPanel _navPanel = new JPanel();

	private boolean _disabledColNumSliderService = false;
	private boolean _disabledObjHeightSliderService = false;

	SuperPositioner _resizeOverlay;

	protected int _userPrefWindowHeight = Const.INVALID;

	/*************************************************
	 * Constructors
	 *************************************************/

	public Workspace(JFrame theSheetWindow) {

		_theSheetWindow = theSheetWindow;

		// Set up the main window:

		this.setBackground(Color.BLACK);
		this.setForeground(Color.WHITE);

		// Setup the photos/row slider:

		setColNumSetSlider(new JSlider(JSlider.HORIZONTAL, 0, 30, 1)); // Min/Max/initial
		// values:
		// 0/30/1
		getColNumSetSlider().setMajorTickSpacing(2);
		getColNumSetSlider().setMinorTickSpacing(1);
		getColNumSetSlider().setPaintTicks(true);
		getColNumSetSlider().setPaintLabels(true);
		getColNumSetSlider().setToolTipText(
				"Modifies number of Workspace objects per row.");

		// Set up the object height adjuster:
		// Min/Max values: 10/110%. Initial: 100%

		setObjHeightSetSlider(new JSlider(JSlider.VERTICAL, 10, 110, 100)); // Min/Max/initial
		// values:
		// 10/110/100
		getObjHeightSetSlider().setMajorTickSpacing(10);
		// _objHeightSetSlider.setMinorTickSpacing(1);
		getObjHeightSetSlider().setPaintTicks(true);
		getObjHeightSetSlider().setPaintLabels(true);
		getObjHeightSetSlider().setToolTipText(
				"Modifies size of Workspace objects");

		this.setTitle("PhotoSpread Workspace");

		// We'll catch close-window operations in
		// the window listener methods below:
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		this.setLayout(new BorderLayout());

		_workspacePanel = new WorkspaceObjectsPanel(this);
		_workspacePanel.setPreferredSize(new ComputableDimension(
				PhotoSpread.photoSpreadPrefs
						.getInt(PhotoSpread.workspaceObjWidthKey),
				PhotoSpread.photoSpreadPrefs
						.getInt(PhotoSpread.workspaceObjHeightKey)));

		// Enable arrow shift-arrow-keys on the Workspace panel:

		WorkspaceSelector.init(this);

		this.add(_workspacePanel, BorderLayout.CENTER);
		this.add(getColNumSetSlider(), BorderLayout.NORTH);
		this.add(getObjHeightSetSlider(), BorderLayout.WEST);

		initializeButtons();

		_pageViewer = new JLabel("1/1");

		_navPanel.add(_zoomButton);
		// _navPanel.add(_testButton);
		/*
		 * Dimension zoomButtonSpaceOnRightDim = new Dimension
		 * (Const.WorkspaceNavBarButtonGroupSpace, 0); Box.Filler filler = new
		 * Box.Filler( zoomButtonSpaceOnRightDim, zoomButtonSpaceOnRightDim,
		 * zoomButtonSpaceOnRightDim);
		 * 
		 * _navPanel.add(filler);
		 */
		_navPanel.add(Box.createHorizontalStrut(5));
		JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
		sep.setPreferredSize(new Dimension(2, 0));
		_navPanel.add(sep);
		_navPanel.add(Box.createHorizontalStrut(5));

		_navPanel.add(_homeSet);
		_navPanel.add(_prevSet);
		_navPanel.add(_nextSet);
		_navPanel.add(_endSet);
		_navPanel.add(_pageViewer);
		_homeSet.setEnabled(false);

		// _navPanel.setBackground(Const.inactiveCellBackgroundColor);u
		_navPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED,
				Const.inactiveCellBackgroundColor, Color.BLACK));

		getColNumSetSlider().addChangeListener(
				CursorController.createChangeListener(getColNumSetSlider(),
						new WSColNumSliderListener(this)));
		getObjHeightSetSlider().addMouseListener(
				new WSObjHeightSliderMouseListener(this));
		getObjHeightSetSlider().addChangeListener(
				new WSObjHeightSliderChangeListener(_workspacePanel));

		this.add(_navPanel, BorderLayout.SOUTH);
		this.addWindowListener(new WSWindowListener(this));
		this.addComponentListener(new WSJFrameComponentListener(this));

		// Prevent the two sliders from stealing the input
		// focus. In particular, they like to grab the left/right
		// arrow keys:
		getColNumSetSlider().setFocusable(false);
		getObjHeightSetSlider().setFocusable(false);

		// Mouse wheel listener: a Java bug will cause an infinite
		// loop in event processing if a mouse wheel listener is added
		// to a top level, i.e. heavyweight component, like a JFrame.
		// One must attach the listener to a lightweight component,
		// like a JPanel! Wrong: this.addMouseWheelListener(new ...

		_workspacePanel.addMouseWheelListener(new scrollMouseWheelListener());

		// Zoom commands:
		Misc.bindKey(_workspacePanel, "control PLUS", ZoomSpawner);
		Misc.bindKey(_workspacePanel, "control EQUALS", ZoomSpawner);
		Misc.bindKey(_workspacePanel, "control shift EQUALS", ZoomSpawner);

		Misc.bindKey(_workspacePanel, "control MINUS", ZoomSpawner);
		Misc.bindKey(_workspacePanel, "control shift MINUS", ZoomSpawner);

		// Keyboard shortcuts:
		Misc.bindKey(_workspacePanel, "control A", new SelectAllVisibleAction(
				this));
		Misc.bindKey(_workspacePanel, "control shift A", new SelectAllAction(
				this));
		Misc.bindKey(_workspacePanel, "control W",
				new Misc.AppExitWithConfirmAction("Really exit PhotoSpread?",
						this));

		Misc.bindKey(this, "F1", new ShowHelpAction(
				"To do in Workspace Window", "HelpFiles/workspaceHelp.html",
				this));
	}

	/*************************************************
	 * Internal Classes
	 *************************************************/

	class WSWindowListener extends WindowAdapter {

		Workspace _workspace;

		public WSWindowListener(Workspace workspace) {
			_workspace = workspace;
		}

		public void windowClosing(WindowEvent e) {
			Misc.exitIfUserWants(
					"Closing Workspace Window Exits PhotoSpread. Do it?",
					_workspace);
		}

		public void windowDeiconified(WindowEvent e) {
			_theSheetWindow.setState(NORMAL);
			_theSheetWindow.setVisible(true);

		}

		/*
		 * public void windowActivated(WindowEvent e) {
		 * _workspacePanel.requestFocusInWindow(); }
		 */
	}
	
	class WSJFrameComponentListener extends ComponentAdapter {

		Workspace _workspace;
		boolean numColsSliderLocked = false;

		public WSJFrameComponentListener(Workspace workspace) {
			_workspace = workspace;
		}

		/**
		 * 
		 * Workspace window has been resized. We check whether the workspace
		 * needs to be laid out again, i.e. whether rows can be added or
		 * removed. We also save the new window dimensions for this cell.
		 * 
		 * @param Details
		 *            of the resize event.
		 */
		public void componentResized(ComponentEvent e) {
			
			// Should we wait here so that we don't lose
			// a resize event?
			if (numColsSliderLocked)
				return;
			
			numColsSliderLocked = true;

			try {
				// ComputableDimension newWorkspaceSize;
				// newWorkspaceSize = new ComputableDimension(getSize());

				// We refuse to let users make the window wider
				// by hand. They are to use the slider for that.
				// But if they make the Workspace window large enough
				// to accommodate more rows, we'll do it.

				Dimension prefDim = null;

				// Get a hold of the layout:
				PredictableEquiSizedGridLayout panelLayout = (PredictableEquiSizedGridLayout) getWorkspacePanel()
						.getLayout();

				int numPossibleRows = panelLayout
						.getNumRowsAllSameHeightComponents(_workspace
								.getWorkspacePanel(), getWorkspacePanel()
								.getObjHeight());

				// if (numPossibleRows == getWorkspacePanel().getRows()) {
				if (panelLayout.isValid()) {
					// Just tighten the Workspace window to its prior size
					// again:
					pack();
					return;
				}

				if (numPossibleRows > 0) {
					// The new page number (given the new number of rows)
					// will differ from the current one. Find the new one:

					int numObjsShownWithOldPageNum = (getPage()
							* panelLayout.getRows() * panelLayout.getColumns())
							+ getDrawnLabels().size();
					
					// int numObjsShownWithOldPageNum = (getPage() + 1) *
					// panelLayout.getRows() * panelLayout.getColumns();
					int newPage = ((int) Math
							.ceil(((float) numObjsShownWithOldPageNum / ((float) numPossibleRows * (float) panelLayout
									.getColumns())))) - 1;
					
					_workspacePanel.setPage(newPage, Const.DONT_REDRAW);
				} else
					// If user sized higher enough to lose one
					// or more rows, then numPossibleRows will
					// be 0. Set that to our minimum:
					numPossibleRows = 1;

				// Fill the internal representation of the panel with
				// the additional (or fewer) objects:
				int currNumCols = getWorkspacePanel().getColumns();
				int fittableItems = numPossibleRows * currNumCols;

				getWorkspacePanel().populatePanel(panelLayout.getColumns(),
						fittableItems);
				getWorkspacePanel().padWorkspacePanel(fittableItems,
						getWorkspacePanel()._maxDisplayedItemDim);

				// Recalculate the layout
				panelLayout.validateNumRows(_workspace.getDisplayedCell()
						.getObjects().size());

				// Have the Workspace panel express the wish to occupy the new
				// size
				prefDim = panelLayout.preferredLayoutSize(getWorkspacePanel());
				getWorkspacePanel().setPreferredSize(prefDim);

				setEnabledButtons(getPage());
				_userPrefWindowHeight = prefDim.height;
				// Tighten the Workspace window around the new ensemble:
				pack();

				getWorkspacePanel().saveWorkspaceWindowSizeState();
				getWorkspacePanel().saveNumRowsState();
			} finally {
				numColsSliderLocked = false;
			}
		}
	}

	class WSColNumSliderListener implements ChangeListener {

		JFrame _workspaceWindow;

		public WSColNumSliderListener(JFrame workspaceWindow) {
			_workspaceWindow = workspaceWindow;
			setFocusable(false);
		}

		// We assume that by the time we get here,
		// it's already clear that user stopped
		// sliding:

		public void stateChanged(ChangeEvent e) {

			if (_disabledColNumSliderService)
				return;

			int sliderVal = ((JSlider) e.getSource()).getValue();

			if (sliderVal == 0) {
				// Setting objs/row to 0 resizes the workspace
				// to normal (and thereby sets the slider to 1):
				getWorkspacePanel().resetCellWorkspaceToDefault();
				return;
			}

			if (sliderVal == getWorkspacePanel().getColumns())
				return;
			
			try {
				_disabledColNumSliderService = true;
				getWorkspacePanel().setObjectsPerRow(sliderVal);
				_workspacePanel.redrawPanel();
			} finally {
				_disabledColNumSliderService = false;
			}
			setEnabledButtons(getPage());
		}
	}

	class WSColNumSliderMouseListener extends MouseAdapter {

		WorkspaceObjectsPanel _workspacePanel;

		public WSColNumSliderMouseListener(WorkspaceObjectsPanel workspacePanel) {
			_workspacePanel = workspacePanel;
		}

		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				_workspacePanel.resetCellWorkspaceToDefault();
			}
		}
	}

	class WSObjHeightSliderChangeListener implements ChangeListener {

		JPanel _panel;
		boolean resizeSliderLocked = false;

		public WSObjHeightSliderChangeListener(JPanel workspacePanel) {
			_panel = workspacePanel;
		}

		// We assume that by the time we get here,
		// it's already clear that user stopped
		// sliding:

		public void stateChanged(ChangeEvent e) {

			WorkspaceObjectsPanel wsPanel = getWorkspacePanel();

			if (_disabledObjHeightSliderService || resizeSliderLocked)
				return;

			try {
				resizeSliderLocked = true;

				int sliderVal = ((JSlider) e.getSource()).getValue();

				// If user simply clicked somewhere on the slider
				// scale, then we don't have a resize overlay showing
				// the resize of a sample image. In that
				// case we go and adjust the Workspace's object width
				// a percentage of its current value:

				if (_resizeOverlay == null) {
					setObjSizeToPercentage(sliderVal);
					wsPanel.setCurrentImageSizePercentage(sliderVal);
					wsPanel.invalidate();
					return;
				}

				// Else user is sliding the object resize slider:
				_resizeOverlay.resizeItem(sliderVal);
				wsPanel.invalidate();
			} finally {
				resizeSliderLocked = false;
			}
		}
	} // end. class WSObjHeightSliderChangeListener

	class WSObjHeightSliderMouseListener extends MouseAdapter {

		JFrame _workspaceWindow;

		public WSObjHeightSliderMouseListener(JFrame workspaceWindow) {
			_workspaceWindow = workspaceWindow;
		}

		public void mousePressed(MouseEvent e) {

			PhotoSpreadAddable biggestDrawnItem = getWorkspacePanel()
					.getBiggestDisplayedObject();

			if (biggestDrawnItem == null) {
				// We'll let the superposition use a rectangle as size sample:
				_resizeOverlay = new SuperPositioner(getWorkspacePanel(),
						"Size sample");
				// Have overlay create a sample the size of a default-sized
				// Workspace object:
				_resizeOverlay
						.setInitialItemSize(getDefaultInitialWorkspaceObjSize());
			} else {
				// We'll use a currently displayed pictures as size sample:
				_resizeOverlay = new SuperPositioner(getWorkspacePanel(),
						biggestDrawnItem.getComponent(), "Size sample");
				// Have overlay create a sample the size of a default-sized
				// Workspace object:
				_resizeOverlay
						.setInitialItemSize(getDefaultInitialWorkspaceObjSize());
			}
			_resizeOverlay.setVisible(true);
			pack();
		}

		public void mouseReleased(MouseEvent e) {

			if (_resizeOverlay == null)
				return;

			int sliderVal = ((JSlider) e.getSource()).getValue();

			setObjSizeToPercentage(sliderVal);
			getWorkspacePanel().setCurrentImageSizePercentage(sliderVal);

			// ComputableDimension newObjSize = _resizeOverlay.getItemSize();
			// _workspacePanel.setObjWidth(newObjSize.width);

			_resizeOverlay.dispose();
			_resizeOverlay = null;
			((Workspace) _workspaceWindow).redraw();
			repaint();
		}
	}

	/**
	 * @author paepcke
	 * 
	 *         Handle the mouse wheel for scrolling in the workspace. The
	 *         'getScrollAmount() provides the platform dependent number of
	 *         units (in our case Workspace screens) to advance per wheel notch.
	 *         This quantity can be set in the platform dependend mouse control
	 *         panel.
	 * 
	 *         We do not implement block scroll (as in page up/down).
	 * 
	 */
	private class scrollMouseWheelListener extends MouseAdapter {

		public void mouseWheelMoved(MouseWheelEvent e) {

			// We only implement scrolling in units (as with arrow keys).
			// We do not implement WHEEL_BLOCK_SCROLL, the page up/down
			// alternative:

			if (e.getScrollType() != MouseWheelEvent.WHEEL_UNIT_SCROLL)
				return;

			int notches = e.getWheelRotation();
			if (notches < 0)
				nextPage(e.getScrollAmount());
			else
				prevPage(e.getScrollAmount());

			e.consume();
		}
	}

	/*************************************************
	 * Action Listeners
	 *************************************************/

	static Action ZoomSpawner = new AbstractAction() {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			((WorkspaceObjectsPanel) e.getSource()).getWorkspace()
					.spawnZoomerWindow(Const.ALL);
		}
	};

	/**
	 * Action listener associated with the 'Test' button in the Workspace
	 * window. This button is normally disabled (in the initializer above), but
	 * one can enable it and run some test by changing the code here. You can
	 * make any changes in this listener.
	 * 
	 * @param comp
	 * @return
	 */
	private ActionListener testButtonListener(Component comp) {
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// spawnZoomerWindow(Const.ALL);
				return;
			}
		};
		return listener;
	}

	private ActionListener zoomButtonListener(Component comp) {
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				spawnZoomerWindow(Const.ALL);
				return;
			}
		};
		return listener;
	}

	private ActionListener nextPageListener(Component comp) {
		ActionListener listener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				int page = _workspacePanel.getPage() + 1;
				_workspacePanel.setPage(page);
				setEnabledButtons(page);
				WorkspaceSelector.userChangedPage(Direction.FORWARD);
			}
		};
		// Now wrap the wait cursor code around this
		// listener and return the wrapped result:
		return CursorController.createActionListener(comp, listener);
	}

	private ActionListener prevPageListener(Component comp) {
		ActionListener listener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				int page = _workspacePanel.getPage() - 1;
				_workspacePanel.setPage(page);
				setEnabledButtons(page);
				WorkspaceSelector.userChangedPage(Direction.BACKWARD);
			}
		};
		// Now wrap the wait cursor code around this
		// listener and return the wrapped result:
		return CursorController.createActionListener(comp, listener);
	}

	private ActionListener homePageListener(Component comp) {
		ActionListener listener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				int currPage = getPage();
				_workspacePanel.setPage(0);
				setEnabledButtons(0);
				if (currPage != 0)
					WorkspaceSelector.userChangedPage(Direction.BACKWARD);
			}
		};
		// Now wrap the wait cursor code around this
		// listener and return the wrapped result:
		return CursorController.createActionListener(comp, listener);
	}

	private ActionListener endPageListener(Component comp) {
		ActionListener listener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				int currPage = getPage();
				int page = _workspacePanel.getLastPage();
				_workspacePanel.setPage(page);
				setEnabledButtons(page);
				if (currPage != page)
					WorkspaceSelector.userChangedPage(Direction.FORWARD);
			}
		};
		// Now wrap the wait cursor code around this
		// listener and return the wrapped result:
		return CursorController.createActionListener(comp, listener);
	}

	/****************************************************
	 * Keyboard Commands
	 *****************************************************/

	private class SelectAllAction extends AbstractAction {

		private static final long serialVersionUID = 1L;
		Workspace _workspace;

		public SelectAllAction(Workspace workspace) {
			_workspace = workspace;
		}

		public void actionPerformed(ActionEvent e) {
			_workspace.selectAll();
		}
	}

	private class SelectAllVisibleAction extends AbstractAction {

		private static final long serialVersionUID = 1L;
		Workspace _workspace;

		public SelectAllVisibleAction(Workspace workspace) {
			_workspace = workspace;
		}

		public void actionPerformed(ActionEvent e) {
			_workspace.selectAllVisible();
		}
	}

	@SuppressWarnings("unused")
	private class ProgrammaticDnDAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		PhotoSpreadCell _destCell = null;
		PhotoSpreadCell _srcCell = null;

		public ProgrammaticDnDAction(PhotoSpreadCell destCell) {
			_destCell = destCell;
			_srcCell = _workspacePanel.getDisplayedCell();
		}

		public ProgrammaticDnDAction(PhotoSpreadCell srcCell,
				PhotoSpreadCell destCell) {
			_destCell = destCell;
			_srcCell = srcCell;
		}

		public void actionPerformed(ActionEvent e) {
			PhotoSpreadDragDropManager.setSourceCell(_srcCell);
			PhotoSpreadDragDropManager.setDestCell(_destCell);
			PhotoSpreadDragDropManager.executeDragDrop();
		}
	}

	/*************************************************
	 * Methods
	 *************************************************/

	public void reset() {
		_workspacePanel.resetCellWorkspaceToDefault();
		_userPrefWindowHeight = Const.INVALID;
	}

	public void reset(boolean redraw) {
		reset();
		if (redraw == Const.DO_REDRAW)
			repaint();
	}

	private void initializeButtons() {

		_zoomButton = new JButton("Zoom");
		_zoomButton.setEnabled(true);
		_zoomButton.addActionListener(zoomButtonListener(_zoomButton));

		_testButton = new JButton("Test");
		_testButton.setEnabled(true);
		_testButton.addActionListener(testButtonListener(_testButton));

		_nextSet = new JButton("Next");
		_nextSet.setEnabled(false);
		// _nextSet.setMnemonic('n');
		// _zoomButton.setDisplayedMnemonicIndex(0);
		_nextSet.addActionListener(nextPageListener(_nextSet));

		_prevSet = new JButton("Prev");
		_prevSet.setEnabled(false);
		// _nextSet.setMnemonic('p');
		_prevSet.addActionListener(prevPageListener(_prevSet));

		_homeSet = new JButton("Home");
		_homeSet.setEnabled(false);
		// _nextSet.setMnemonic('h');
		_homeSet.addActionListener(homePageListener(_homeSet));

		_endSet = new JButton("End");
		_endSet.setEnabled(false);
		// _nextSet.setMnemonic('e');
		_endSet.addActionListener(endPageListener(_endSet));
	}

	protected void spawnZoomerWindow(int max) {

		ComputableDimension windowOffset = Const.ZoomWindowsOffset;
		int nthLabel = 0;
		Zoomer zoomer;
		ComputableDimension zoomerPosition;
		ComputableDimension newZoomerPosition;

		DraggableLabel latestLabel = getLastLabelClicked();
		String filePath = "";
		ArrayList<PhotoSpreadAddable> selObjs = new ArrayList<PhotoSpreadAddable>();

		// If no item in the Workspace is selected
		// then we check whether exactly one (unselected)
		// item is visible in the Workspace. If only one
		// is visible, we select it and zoom. Otherwise
		// we throw up an error message:

		if (latestLabel == null)
			if (getDrawnLabels().size() == 1) {
				LinkedHashMap<PhotoSpreadObject, PhotoSpreadAddable> drawnLabels = getDrawnLabels();
				// selectObject(getDrawnLabels().get(0));
				selectObject(drawnLabels.get(drawnLabels.keySet().iterator()
						.next()));
			} else {
				Misc.showErrorMsg("Cannot zoom: nothing selected.", this); // Show
				// msg
				// inside
				// Workspace
				// window
				return;
			}

		switch (max) {
		case Const.LAST_CLICKED:
			selObjs.add(latestLabel);
			max = 1;
			break;
		case Const.ALL:
			for (PhotoSpreadAddable shownLabel : getDrawnLabels().values()) {
				if (isObjectSelected(shownLabel))
					selObjs.add(shownLabel);
			}
			max = selObjs.size();
			break;
		default:
			break;
		}

		for (PhotoSpreadAddable selObj : selObjs) {
			if (--max < 0)
				break;
			filePath = (String) selObj.getParentObject().toString();
			try {
				zoomer = new Zoomer(filePath);
				if (nthLabel++ == 0)
					continue;

				zoomerPosition = new ComputableDimension(zoomer.getLocation());
				newZoomerPosition = zoomerPosition.plus(windowOffset
						.times(nthLabel++));
				zoomer.setLocation(newZoomerPosition.toPoint());

			} catch (IOException e) {

				boolean wantContinue = Misc.showConfirmMsg(
						"Cannot zoom: bad file path: '" + filePath
								+ "'. Continue creating zoom windows?", this); // Show
				// msg
				// within
				// Workspace
				// window
				if ((wantContinue && (selObj != selObjs.get(selObjs.size() - 1)))
						&& max > 0)
					continue;
				else
					return;
			}
		}
	}

	protected void spawnZoomerWindow() {
		spawnZoomerWindow(Const.LAST_CLICKED);
	}

	/**
	 * Flip Workspace to next page if possible.
	 * 
	 * @return True if there was a page to flip to. False if we were already on
	 *         the last page.
	 */
	public boolean nextPage() {
		return nextPage(1);
	}

	/**
	 * Advance a given number of pages.
	 * 
	 * @param pages
	 *            Number of pages to add to current page number.
	 * @return True if page flip succeeded. If flip would have gone past last
	 *         page, do nothing and return false.
	 */
	public boolean nextPage(int pages) {

		int newPageNum = _workspacePanel.getPage() + pages;
		int lastPageNum = _workspacePanel.getLastPage();
		if (newPageNum > lastPageNum)
			return false;
		_workspacePanel.setPage(newPageNum);
		setEnabledButtons(newPageNum);
		return true;
	}

	/**
	 * Flip Workspace to previous page if possible.
	 * 
	 * @return True if there was a page to flip to. False if we were already on
	 *         the first page.
	 */
	public boolean prevPage() {
		return prevPage(1);
	}

	/**
	 * Go back a given number of pages.
	 * 
	 * @param pages
	 *            Number of pages to add to current page number.
	 * @return True if page flip succeeded. If flip would have gone past first
	 *         page, do nothing and return false.
	 */

	public boolean prevPage(int pages) {

		int newPageNum = _workspacePanel.getPage() - pages;
		if (newPageNum < 0)
			return false;
		_workspacePanel.setPage(newPageNum);
		setEnabledButtons(newPageNum);
		return true;
	}

	public PhotoSpreadCell getDisplayedCell() {
		return _workspacePanel.getDisplayedCell();
	}

	public boolean isObjectSelected(PhotoSpreadAddable label) {
		return _workspacePanel.getDisplayedCell().isObjectSelected(
				label.getParentObject());
	}

	public void flipObjectSelection(PhotoSpreadAddable label,
			boolean updateMostRecentlyClicked) {
		if (_workspacePanel.getDisplayedCell().isObjectSelected(
				label.getParentObject()))
			deSelectObject(label, updateMostRecentlyClicked);
		else
			selectObject(label, updateMostRecentlyClicked);
	}

	/**
	 * Select a PhotoSpreadAddable in the Workspace. Ex: DraggableLabel.
	 * 
	 * @param label
	 *            The Addable to select
	 * @param updateMostRecentlyClicked
	 *            Whether the "most recently clicked" label should also be
	 *            updated.
	 */
	public void selectObject(PhotoSpreadAddable label,
			boolean updateMostRecentlyClicked) {
		label.highlight();
		_workspacePanel.getDisplayedCell()
				.selectObject(label.getParentObject());
		if (updateMostRecentlyClicked)
			_workspacePanel.setLastLabelClicked((DraggableLabel) label);
	}

	/**
	 * Select a PhotoSpreadAddable in the Workspace. Ex: DraggableLabel.
	 * 
	 * @param label
	 *            The label to select.
	 */
	public void selectObject(PhotoSpreadAddable label) {
		selectObject(label, false);
	}

	public void deSelectObject(PhotoSpreadAddable label,
			boolean updateMostRecentlyClicked) {
		label.unhighlight();
		_workspacePanel.getDisplayedCell().deselectObject(
				label.getParentObject());
		if (updateMostRecentlyClicked)
			_workspacePanel.setLastLabelClicked((DraggableLabel) label);
	}

	public void deSelectObject(PhotoSpreadAddable label) {
		deSelectObject(label, false);
	}

	public void selectAll() {
		_workspacePanel.getDisplayedCell().selectAllObjects();
		selectAllVisible();
	}

	public void deSelectAll() {
		_workspacePanel.clearSelected();
	}

	public void deSelectAllVisible() {

		Iterator<PhotoSpreadAddable> visibleLablesIt = _workspacePanel
				.getDrawnLabels().values().iterator();

		while (visibleLablesIt.hasNext()) {
			deSelectObject(visibleLablesIt.next());
		}
	}

	public void selectAllVisible() {

		Iterator<PhotoSpreadAddable> visibleLablesIt = _workspacePanel
				.getDrawnLabels().values().iterator();

		while (visibleLablesIt.hasNext()) {
			selectObject(visibleLablesIt.next());
		}
	}

	public void setColNumSliderValue(int newVal) {

		if (getColNumSetSlider().getValue() == newVal)
			return;

		try {
			_disabledColNumSliderService = true;
			getColNumSetSlider().setValue(newVal);
			getColNumSetSlider().validate();
			getColNumSetSlider().repaint();
		} finally {
			_disabledColNumSliderService = false;
		}
	}

	public void setObjHeightSliderValue(int newVal) {

		if (getObjHeightSetSlider().getValue() == newVal)
			return;
		try {
			_disabledObjHeightSliderService = true;
			getObjHeightSetSlider().setValue(newVal);
			_workspacePanel.setCurrentImageSizePercentage(newVal);
			getObjHeightSetSlider().revalidate();
			getObjHeightSetSlider().repaint();
		} finally {
			_disabledObjHeightSliderService = false;
		}
	}

	/**
	 * Obtain dimensions Workspace objects as initially sized by default.
	 * 
	 * @return Dimension width/height of initial object sizes in Workspace.
	 */
	public ComputableDimension getDefaultInitialWorkspaceObjSize() {

		ComputableDimension res = null;
		res = new ComputableDimension(PhotoSpread.photoSpreadPrefs
				.getInt(PhotoSpread.workspaceObjWidthKey),
				PhotoSpread.photoSpreadPrefs
						.getInt(PhotoSpread.workspaceObjHeightKey));
		return res;
	}

	/**
	 * enables/disables navigation based on page being viewed
	 * 
	 * @param page
	 *            The page that the workspace is currently displaying (base 0)
	 */

	public void setEnabledButtons(int page) {
		if (page <= 0) {
			_prevSet.setEnabled(false);
			_homeSet.setEnabled(false);
		} else {
			_prevSet.setEnabled(true);
			_homeSet.setEnabled(true);
		}
		int lastPage = _workspacePanel.getLastPage();
		if (page >= lastPage) {
			_nextSet.setEnabled(false);
			_endSet.setEnabled(false);
		} else {
			_nextSet.setEnabled(true);
			_endSet.setEnabled(true);
		}

		// For human consumption: be 1-based:
		this._pageViewer.setText("" + (page + 1) + "/" + (lastPage + 1));
	}

	public int getPage() {
		return _workspacePanel.getPage();
	}

	public int getLastPage() {
		return _workspacePanel.getLastPage();
	}

	public void setDisplayedCell(PhotoSpreadCell cell)
			throws NumberFormatException, NotBoundException {
		_workspacePanel.removeAll();
		// The following will repaint:
		_workspacePanel.setDisplayedCell(cell);
	}

	public void setColNumSetSlider(JSlider _colNumSetSlider) {
		this._colNumSetSlider = _colNumSetSlider;
	}

	public JSlider getColNumSetSlider() {
		return _colNumSetSlider;
	}

	public void setObjHeightSetSlider(JSlider _objHeightSetSlider) {
		this._objHeightSetSlider = _objHeightSetSlider;
	}

	public JSlider getObjHeightSetSlider() {
		return _objHeightSetSlider;
	}

	public JPanel getNavPanel() {
		return _navPanel;
	}

	private void setObjSizeToPercentage(int percentage) {
		_workspacePanel.setObjWidth(PhotoSpread.photoSpreadPrefs
				.getInt(PhotoSpread.workspaceObjWidthKey)
				* percentage / 100);
	}

	protected LinkedHashMap<PhotoSpreadObject, PhotoSpreadAddable> getDrawnLabels() {
		return _workspacePanel.getDrawnLabels();
	}

	protected DraggableLabel getLastLabelClicked() {
		return _workspacePanel.getLastLabelClicked();
	}

	protected void setLastLabelClicked(DraggableLabel newLabel) {
		_workspacePanel.setLastLabelClicked(newLabel);
	}

	void setWorkspacePanel(WorkspaceObjectsPanel _workspacePanel) {
		this._workspacePanel = _workspacePanel;
	}

	public WorkspaceObjectsPanel getWorkspacePanel() {
		return _workspacePanel;
	}

	/**
	 * Bind a key to an action. The binding will be active while the Workspace
	 * window is selected.
	 * 
	 * @param keyDescription
	 *            A string describing the key as per
	 *            KeyStroke.getKeyStroke(String). Ex: "alt A" or "ctrl UP" (for
	 *            up-arrow). Key names are the <keyName> part in VK_<keyName>
	 * @param action
	 *            Action object to invoke when key is pressed.
	 */
	/*
	 * public void bindKey (String keyDescription, Action action) {
	 * 
	 * InputMap keyMap =
	 * _workspacePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW); ActionMap
	 * actionMap = _workspacePanel.getActionMap();
	 * 
	 * keyMap.put(KeyStroke.getKeyStroke(keyDescription), keyDescription);
	 * actionMap.put(keyDescription, action); }
	 */
	public void resize() {

		if (_workspacePanel == null)
			return;

		ComputableDimension layoutManagerPrefSize = (ComputableDimension) _workspacePanel
				.getLayout().preferredLayoutSize(this);
		setSize(getInsets().left + layoutManagerPrefSize.width
				+ getObjHeightSetSlider().getWidth() + getInsets().top,
				layoutManagerPrefSize.height + _navPanel.getHeight()
						+ getInsets().bottom);
	}

	public Insets getInsets() {
		return Const.workspaceInsets;
	}

	/**
	 * Redraws workspace
	 */

	public void redraw() {

		// To prevent circular call chains
		// _workspace -> _workspacePanel constructor -> _workspace.redraw()
		// during startup:
		if (_workspacePanel == null)
			return;

		try {
			_workspacePanel.redraw();

		} catch (NumberFormatException e) {
			Misc.showErrorMsgAndStackTrace(e, "");
			// e.printStackTrace();
		} catch (NotBoundException e) {
			Misc.showErrorMsgAndStackTrace(e, "");
			// e.printStackTrace();
		}
	}

	public void pack() {

		if (_userPrefWindowHeight != Const.INVALID) {
			Dimension currDim = _workspacePanel.getPreferredSize();
			_workspacePanel.setPreferredSize(new Dimension(currDim.width,
					_userPrefWindowHeight));
		}
		super.pack();
	}
}
