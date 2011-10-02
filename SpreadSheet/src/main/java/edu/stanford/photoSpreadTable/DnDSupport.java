/**
 * 
 */
package edu.stanford.photoSpreadTable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.InvalidDnDOperationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TooManyListenersException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import edu.stanford.photoSpread.PhotoSpread;
import edu.stanford.photoSpreadObjects.photoSpreadComponents.DraggableLabel;
import edu.stanford.photoSpreadObjects.photoSpreadComponents.WorkspaceObjectsPanel;
import edu.stanford.photoSpreadUtilities.CellCoordinates;
import edu.stanford.photoSpreadUtilities.Const;
import edu.stanford.photoSpreadUtilities.Misc;
import edu.stanford.photoSpreadUtilities.OsmoticOverlayTracker;

/**
 * @author paepcke
 * 
 * Visualy helps user while drag-n-dropping from/onto PhotoSpread cells.
 */
public class DnDSupport {

	protected static JLabel _dragInfoLabel = new JLabel();

	public static enum DnDVizStyle {
		CURSOR_TRACKER,
		STILL_LABEL
	};

	private static DragSource _dragSource;
	private static DragGestureListener _dragGestureListenerTable;
	// private static NaySayerTransferHandlerTable _transferHandlerNaySayerTable;
	private static DragGestureListener _dragGestureListenerWorkspace;
	private static TransferHandlerWorkspace _transferHandlerWorkspace;
	private static DragGestureListener _dragGestureListenerLabel;
	private static TransferHandlerLabel _transferHandlerLabel;
	protected static PhotoSpreadCell _sourceCell = null;

	
	private static PhotoSpreadTable _table = null;
	
	// private static ImageIcon _rightArrowIcon;
	private static JLabel _trashLabel = new JLabel();


	protected static Border _dndCellInfoBorder1 = BorderFactory.createBevelBorder(
			BevelBorder.LOWERED,
			Const.dndGhostBorderHighlightColor1,
			Const.dndGhostBorderShadowColor);

	protected static Border _dndCellInfoBorder2 = BorderFactory.createBevelBorder(
			BevelBorder.LOWERED,
			Const.dndGhostBorderHighlightColor2,
			Const.dndGhostBorderShadowColor);

	protected static OsmoticOverlayTracker _dndCellInfoPanel = 
		new OsmoticOverlayTracker(PhotoSpread.getCurrentSheetWindow());

	public static final String mimeAndClassInfo = DataFlavor.javaJVMLocalObjectMimeType + ";class=photoSpreadTable.PhotoSpreadCell";
	public static PhotoSpreadCellFlavor photoSpreadCellFlavor;
	
	// Make an instance of our new DataFlavor:
	static{
		try{
			photoSpreadCellFlavor = new PhotoSpreadCellFlavor(mimeAndClassInfo);
		}
		catch(ClassNotFoundException e){
			System.out.println("Class not found:PhotoSpread.PhotoSpreadCell");
		}
	}
	public static DataFlavor[] supportedImportExportFlavorsForTable = {
		photoSpreadCellFlavor,
		DataFlavor.javaFileListFlavor
	};
	// A more convenient ArrayList version of the table-supported flavors:
	public static ArrayList<DataFlavor> supportedImportExportFlavorsForTableArrayList;

	/****************************************************
	 * Constructor(s)
	 *****************************************************/

	public DnDSupport (JFrame tableWindow) {

		// Our info panel will be the given window's glass pane:
		tableWindow.setGlassPane(_dndCellInfoPanel);

		// Make the cursor trail and components invisible for now:
		_dndCellInfoPanel.setCursorTrailVisible(false);
		_dndCellInfoPanel.setComponentsVisible(false);
		
		_dragInfoLabel.setPreferredSize(PhotoSpread.photoSpreadPrefs.getDimension(PhotoSpread.dragGhostSizeKey));
		_dragInfoLabel.setBackground(Const.dndGhostBackgroundColor1);
		_dragInfoLabel.setBorder(_dndCellInfoBorder1);
		_dragInfoLabel.setOpaque(true);
		_dragInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);
		_dragInfoLabel.setText("");
				
		// _rightArrowIcon = Misc.createImageIcon("rightArrow.png", _dragInfoLabel.getPreferredSize());
		ImageIcon trashIcon = Misc.createImageIcon("img/grouchSmiling.png");
		
		_trashLabel.setIcon(trashIcon);
		_dndCellInfoPanel.add(_trashLabel, BorderLayout.CENTER);
		_trashLabel.setVisible(false);

		// Initialize the more convenient ArrayList version of the table-supported flavors:
		supportedImportExportFlavorsForTableArrayList = new ArrayList<DataFlavor>();
		for (int i=0; i<supportedImportExportFlavorsForTable.length; i++)
			supportedImportExportFlavorsForTableArrayList.add(supportedImportExportFlavorsForTable[i]);
		
		initDropTargetTable ();
		initDragSourcing ();

	}

	/****************************************************
	 *  PhotoSpreadFileListFlavor Inner Class
	 *****************************************************/

	/**
	 * @author paepcke
	 *
	 * DataFlavor that represents the built-in javaFileListFlavor
	 * DataFlavor. We have this subclass so that we can use
	 * polymorphism when asked to deliver a transferable in
	 * PhotoSpreadCell.java.
	 */

	public static class PhotoSpreadFileListFlavor extends DataFlavor {
		
		public PhotoSpreadFileListFlavor () throws ClassNotFoundException {
			super(DataFlavor.javaFileListFlavor.getMimeType(),
				  DataFlavor.javaFileListFlavor.getHumanPresentableName(),
				  Class.forName("java.util.List").getClassLoader());
		}
	}
	
	/****************************************************
	 * PhotoSpreadCellFlavor Inner Class
	 *****************************************************/

	/**
	 * @author paepcke
	 * 
	 * DataFlavor for a PhotoSpreadCell. This flavor is used
	 * just for dnd within the PhotoSpread application. It's
	 * obviously not understood outside.
	 */
	public static class PhotoSpreadCellFlavor extends DataFlavor {
		
		public PhotoSpreadCellFlavor (String mimeAndClassInfo) throws ClassNotFoundException {
			super(mimeAndClassInfo);
		}
	}

	/****************************************************
	 * Methods
	 *****************************************************/

	public static DnDSupport TableDnDVizFactory (
			JFrame tableWindow,
			PhotoSpreadTable table,
			DnDVizStyle vizStyle) {

		_table = table;

		switch (vizStyle) {

		case CURSOR_TRACKER:
			return new DnDSupport(tableWindow).new StillLabel(tableWindow);
		case STILL_LABEL:
			return new DnDSupport(tableWindow).new StillLabel(tableWindow);
		default:
			return null;
		}
	}

	private void initDropTargetTable () {
		
		DropTargetTable cellLocAndDropListener = 
			new DropTargetTable(_table, this); 

		DropTarget dropTarget = new DropTarget();
		dropTarget.setComponent(_table);
		dropTarget.setDefaultActions(DnDConstants.ACTION_COPY_OR_MOVE);
		try {
			dropTarget.addDropTargetListener(cellLocAndDropListener);
		} catch (TooManyListenersException e) {
			Misc.showErrorMsgAndStackTrace(e, "");
			//e.printStackTrace();
		}
		dropTarget.setActive(true);
		_table.setDropTarget(dropTarget);

	}
	
	/**
	 * We create a single DragSource that will be used
	 * as the source manager of all drags within PhotoSpread
	 */
	
	private void initDragSourcing () {
		
		_dragSource = new DragSource();
		
		// Give table its own drag starter sensor and install it:
		_dragGestureListenerTable = new DragGestureRecognizerTable();
		// _transferHandlerNaySayerTable = new NaySayerTransferHandlerTable();
		initComponentAsDragSource(_table);
		
		// Create a drag-start listener for the Workspace and for
		// labels. That is: handlers for when a drag is started
		// within the Workspace but not on a label, or directly
		// on a label, whether it's in the Workspace or in a cell.
		// Additionally, create a transfer handler for the Workspace.
		// The labels' transfer handler is part of the label class:
		
		_dragGestureListenerWorkspace = new DragGestureRecognizerWorkspace();
		_transferHandlerWorkspace = new TransferHandlerWorkspace();
		_dragGestureListenerLabel = new DragGestureRecognizerLabel();
	}

	
	/**
	 * Generates a drag gesture recognizer. Associates the
	 * given component with the (single) drag gesture listener
	 * that specializes on drags originating from the workspace.
	 * The unused WorkspaceObjectsPanel just allows us in the
	 * future to create initComponentAsDragSource() methods
	 * that associate JComponents not in the Workspace with
	 * our (single) DragSource object.  
	 * @param wsop Used only for polymorphism.
	 * @param sourceComponent JComponent in the WorkspaceObjectsPanel
	 */
	
	public static void initComponentAsDragSource (WorkspaceObjectsPanel wsop, JComponent sourceComponent) {

		_dragSource.createDefaultDragGestureRecognizer(
				sourceComponent,
				DnDConstants.ACTION_COPY_OR_MOVE,
				_dragGestureListenerWorkspace);
		sourceComponent.setTransferHandler(_transferHandlerWorkspace);
	}
	
	public static void initComponentAsDragSource (JComponent sourceLabel) {

		_dragSource.createDefaultDragGestureRecognizer(
				sourceLabel,
				DnDConstants.ACTION_COPY_OR_MOVE,
				_dragGestureListenerLabel);
		sourceLabel.setTransferHandler(_transferHandlerLabel);
	}

	public static void initComponentAsDragSource (PhotoSpreadTable sourceTable) {

		_dragSource.createDefaultDragGestureRecognizer(
				sourceTable,
				DnDConstants.ACTION_COPY_OR_MOVE,
				_dragGestureListenerTable);

		// Currently our own Table transfer handler class is not used. 
		// We let the JTable's default transfer handler manage the transfer.
		// sourceComponent.setTransferHandler(_transferHandlerTable);
	}
	
	public void setVisible (boolean visible) {

	}

	public void setText (String txt) {
		_dragInfoLabel.setText(txt);
		_dragInfoLabel.setIcon(null);
	}

	public void setText (CellCoordinates coords) {
		Misc.getCellAddress(coords);
		_dragInfoLabel.setIcon(null);
	}
	
	public void setIcon (ImageIcon icon) {
		_dragInfoLabel.setText("");
		_dragInfoLabel.setIcon(icon);
		_dragInfoLabel.repaint();
	}

	/**
	 * Called at the end of a drag-n-drop operation,
	 * or after its premature termination.
	 */

	public void clearVisualDnDFeedback() {

		this.setVisible(false);
		_dndCellInfoPanel.setVisible(false);
		clearTrashHover ();
		_dndCellInfoPanel.repaint();
	}
	
	public void showTrashHover () {
		_trashLabel.setVisible(true);	
	}
	
	public void clearTrashHover () {
		_trashLabel.setVisible(false);
	}

	public void visualCellChangeFeedback() {

		if (_dragInfoLabel.getBackground() == Const.dndGhostBackgroundColor1) {
			_dragInfoLabel.setBackground(Const.dndGhostBackgroundColor2);
			_dragInfoLabel.setBorder(_dndCellInfoBorder2);
		}
		else {
			_dragInfoLabel.setBackground(Const.dndGhostBackgroundColor1);
			_dragInfoLabel.setBorder(_dndCellInfoBorder1);
		}
	}
	
	public boolean isTrashDropZone (CellCoordinates coords) {
		if (coords == null) return false;
		return ((coords.row() == 0) &&
				(coords.column() == 0));
	}
	
	public boolean isTrashDropZone (int row, int col) {
		return ((row == 0) && (col == 0));
	}

	/****************************************************
	 * Inner Subclass CursorTracker
	 *****************************************************/

	public class CursorTracker extends DnDSupport {

		public CursorTracker(JFrame tableWindow) {

			super(tableWindow);

			// The little label will be the cursor trailer:
			_dndCellInfoPanel.setCursorTrailer(_dragInfoLabel);

			// How far should upper left corner be from 
			// tip of cursor?

			_dndCellInfoPanel.setCursorTrailOffset(
					new Dimension (
							((int) (_dragInfoLabel.getPreferredSize().width / 2)),
							((int)_dragInfoLabel.getPreferredSize().height / 2)));

			// Glass panes are invisible by default. Make visible:
			_dndCellInfoPanel.setVisible(true);

		}

		public void setVisible (boolean visible) {
			_dndCellInfoPanel.setCursorTrailVisible(visible);
			_dndCellInfoPanel.setVisible(visible);
		}

		public void setText (String txt) {
			super.setText(txt);
			_dndCellInfoPanel.setCursorTrailer(_dragInfoLabel);
		}
	}

	/****************************************************
	 * Inner Subclass StillLabel
	 *****************************************************/

	public class StillLabel extends DnDSupport {

		public StillLabel(JFrame tableWindow) {

			super(tableWindow);

			JPanel stuffPanel = new JPanel();
			stuffPanel.setOpaque(false);
			Dimension fillDim = new Dimension (
					((int) _dndCellInfoPanel.getWidth() / 2) -
					((int) _dragInfoLabel.getPreferredSize().width / 2),

					_dragInfoLabel.getPreferredSize().height);

			Misc.SpaceFiller leftFill = new Misc.SpaceFiller (fillDim, Const.IS_TRANSPARENT);
			Misc.SpaceFiller rightFill = new Misc.SpaceFiller (fillDim, Const.IS_TRANSPARENT);
			stuffPanel.add(leftFill);
			stuffPanel.add(_dragInfoLabel);
			stuffPanel.add(rightFill);
			
			// _dndCellInfoPanel.add(_dragInfoLabel, BorderLayout.NORTH);
			_dndCellInfoPanel.add(stuffPanel, BorderLayout.NORTH);

			// Glass panes are invisible by default. Make visible:
			//****_dndCellInfoPanel.setVisible(true);

		}

		/* (non-Javadoc)
		 * @see photoSpreadTable.DnDSupport#setVisible(boolean)
		 */
		@Override
		public void setVisible(boolean visible) {
			// _dndCellInfoPanel.setComponentsVisible(visible);
			_dndCellInfoPanel.setVisible(visible);
		}

		public void setText (String txt) {
			super.setText(txt);
		}
	}

	/****************************************************
	 * DragDropCellLoactor Inner Class
	 *****************************************************/


	class DropTargetTable extends DropTarget {

		private static final long serialVersionUID = 1L;

		PhotoSpreadTable _table = null;
		DnDSupport _dndVisualizer = null;
		CellCoordinates _oldCellCoords = null;
		CellCoordinates _newCellCoords = null;

		public DropTargetTable (PhotoSpreadTable table, DnDSupport dndVisualizer) {
			_table = table;
			_dndVisualizer = dndVisualizer;
		}

		public void updateDropTargetViz(CellCoordinates targetCellCoordinates) {

			try {

				_newCellCoords = targetCellCoordinates;

				// This method is called all the time during
				// drag. Even when there is no change. So
				// in that case return as quickly as possible:

				if (_newCellCoords.equals(_oldCellCoords))
					return;

				// If cell coordinates cant' be read, don't worry about it:
				if ((_newCellCoords == null) ||
						(_newCellCoords.row() == -1) ||
						(_newCellCoords.column() == -1))
					return;

				if (isTrashDropZone(_oldCellCoords) &&
					!isTrashDropZone(_newCellCoords))
					clearTrashHover ();
				_dndVisualizer.visualCellChangeFeedback();
				_oldCellCoords = _newCellCoords;
				_dndVisualizer.setText(_newCellCoords.toString());
			} catch (Exception e) {
				// just ignore the unlucky event data
			}
		}

		public void dragEnter(DropTargetDragEvent dragTargetEvent) {

			// printTrace ("DragEnter called");
			CellCoordinates sourceCoords = _table.getCellAddressUnderCursor();
			
			_dndVisualizer.setVisible(true);
			
			if (sourceCoords.column() == 0) {
				// _dndVisualizer.setIcon(_rightArrowIcon);
				return;
			}

			if (_sourceCell == null) {
				try {
					_sourceCell = _table.getCell(sourceCoords.row(), sourceCoords.column());
				} catch (Exception e) {
				}
			}
		}



		public void dragExit(DropTargetEvent dropEvent) {
			_dndVisualizer.setVisible(false);
		}


		/* (non-Javadoc)
		 * @see java.awt.dnd.DropTargetListener#dragOver(java.awt.dnd.DropTargetDragEvent)
		 */
		public void dragOver(DropTargetDragEvent dragEvent) {

			// printTrace ("dragOver called");
			// setText("Origin");
			// printTrace ("dragOver: " + _table.getColumnUnderCursor());
			// printTrace(_table.getCursor().getName());

			// JTable *seemingly* allows users to drag items out of
			// cells without those cells being activated first.
			// However, when users then drop, nothing happens. 
			// The easiest way to find whether the source cell
			// is activated is to see whether it is the displayed
			// cell of the Workspace:
			
			if (_sourceCell != _table.getWorkspace().getDisplayedCell()) {
				dragEvent.rejectDrag();
				return;
			}
			
			int targetRow = _table.getRowUnderCursor();
			int targetCol = _table.getColumnUnderCursor();
			
			if (_trashLabel.isVisible()) {
				if (! isTrashDropZone(targetRow, targetCol))
					clearTrashHover();
			}
			
			// Mouse cursor outside of sheet?:
			if ((targetRow == -1) || (targetCol == -1)) {
				dragEvent.rejectDrag();
				return;
			}
				
			PhotoSpreadCell targetCell = _table.getCell(targetRow, targetCol);
			
			if (targetCell.equals(_sourceCell) || 
					_sourceCell.isFormulaCell()) {
				updateDropTargetViz(new CellCoordinates(targetRow, targetCol));
				dragEvent.rejectDrag();
				return;
			}
			
			CellCoordinates targetCoords = targetCell.getCellCoordinates();
			
			if ((targetCol == 0) &&
				(! isTrashDropZone(targetCoords))) {
				dragEvent.rejectDrag();
				// _dndVisualizer.setIcon(_rightArrowIcon);
				// Ensure that updateDropTargetViz() knows
				// to switch its display properly when user
				// leaves column 0 again:
				_oldCellCoords = null;
				return;
			}
			else {
				dragEvent.acceptDrag(dragEvent.getDropAction());
				if (isTrashDropZone(targetCoords)) {
					showTrashHover ();
					// Ensure that updateDropTargetViz() knows
					// to switch its display properly when user
					// leaves column 0 again:
					_oldCellCoords = null;
					return;
				}
				updateDropTargetViz(new CellCoordinates(targetRow, targetCol));
				return;
			}
		}


		public void drop(DropTargetDropEvent dropEvent) {
			
			boolean dropSuccessful = false;
			CellCoordinates targetCoords = null;
			PhotoSpreadCell targetCell   = null; 

			// printTrace("drop was called");

			try {
				targetCoords = _table.getCellAddressAt(dropEvent.getLocation());
				
				if ((targetCoords.column() == 0) &&
					(! isTrashDropZone(targetCoords))) {
					dropEvent.rejectDrop();
					return;
				}

				targetCell = _table.getCell(targetCoords.row(), targetCoords.column());
				clearVisualDnDFeedback();
				
				if (dropEvent.isDataFlavorSupported(photoSpreadCellFlavor))
					dropSuccessful = dropPhotoSpreadInternal(dropEvent, targetCell);
				else
					if (dropEvent.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
						dropSuccessful = dropFromWindowsApp(dropEvent, targetCell);
				
				if (dropSuccessful)
					dropEvent.dropComplete(Const.DROP_SUCCEEDED);
				else
					dropEvent.dropComplete(Const.DROP_FAILED);
			} finally {
				_sourceCell = null;
			}
		}
		
		private boolean dropPhotoSpreadInternal(
				DropTargetDropEvent dropEvent,
				PhotoSpreadCell targetCell) {

			PhotoSpreadDragDropManager.setSourceCell(_sourceCell);
			PhotoSpreadDragDropManager.setDestCell(targetCell);
			
			// Did user drop over the trash can?
			
			if (isTrashDropZone(targetCell.getCellCoordinates())) {
				_sourceCell.removeObjects(_sourceCell.getSelectedObjects());
				if (_sourceCell.getObjects().size() == 0)
					_sourceCell.clear(Const.DO_EVAL, Const.DO_REDRAW);
			}
			else
				PhotoSpreadDragDropManager.executeDragDrop();
			
			// _table.getCellEditorFor(targetCell.getRow(), targetCell.getColumn());
			_table.getTableModel().fireTableCellUpdated(targetCell.getRow(), targetCell.getColumn());
			
			if (_sourceCell == _table.getWorkspace().getDisplayedCell())
				_table.getWorkspace().redraw();

			return true;
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		private boolean dropFromWindowsApp(
				DropTargetDropEvent dropEvent,
				PhotoSpreadCell targetCell) {

			java.util.List<File> fileList = null;
			ArrayList<File> resFileList = new ArrayList<File>();
			Transferable trans = dropEvent.getTransferable();
			
			try {
				
				
				//xferFlavors = trans.getTransferDataFlavors();
				//for (int i=0; i<xferFlavors.length; i++)
				//	printTrace(xferFlavors[i].getHumanPresentableName());

				// See whether the Java array list flavor is supported
				// by the drag source. That's what (at least) Windows explorer
				// provides:

				if (trans.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					dropEvent.acceptDrop (DnDConstants.ACTION_MOVE);
					Object rawTransData = trans.getTransferData(DataFlavor.javaFileListFlavor);
					if (Class.forName("java.util.List").isInstance(rawTransData)) {
						fileList = (java.util.List) rawTransData; 
					}
					
					// The DnD delivers Array$ArrayList, rather than ArrayList.
					// One can't apparently be cast to the other. Bite the bullet
					// and just copy over. It's not that much:
					
					resFileList.addAll(fileList);
					PhotoSpreadTable.loadFiles(targetCell, resFileList);
					return true;
				}

				if (trans.isDataFlavorSupported(DataFlavor.imageFlavor)) {
				}
				
				if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				}
				
			} catch (Exception e) {
				Misc.showErrorMsgAndStackTrace(e, "");
				//e.printStackTrace();
			}
			
			return false;
		}
				
		public void dropActionChanged(DropTargetDragEvent dropAction) {

			printTrace ("dropActionChanged called");

		}

		private void printTrace (String msg) {
			System.out.println(msg);
		}
	}

	/****************************************************
	 * NaySayerTransferHandlerTable Inner Class
	 *****************************************************/

	static class NaySayerTransferHandlerTable extends TransferHandler 
	implements Transferable { 
	
		private static final long serialVersionUID = 1L;
		
		public boolean canImport (TransferHandler.TransferSupport support) {
			// Can't import anything empty:
			return false;
		}

		public int getSourceActions(JComponent comp) {
			// Can't participate in copying, moving, or anything else:
			return NONE;
		}

		/* (non-Javadoc)
		 * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
		 */
		@Override
		public Object getTransferData(DataFlavor flavor)
				throws UnsupportedFlavorException, IOException {
			return null;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			DataFlavor res[] = {};
			return res;
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return false;
		}
	}

	
	/****************************************************
	 * TransferHandlerTable Inner Class
	 *****************************************************/

	/**
	 * Currently this class is not used. We let the JTable's default
	 * transfer handler manage the transfer. However, the 
	 * NaySayerTransferHandlerTable class is used sometimes.
	 *
	 */
	static class TransferHandlerTable extends TransferHandler 
	implements Transferable {
		
		private static final long serialVersionUID = 1L;

		public boolean canImport (TransferHandler.TransferSupport dndSupport) {

			int sourceActions = dndSupport.getSourceDropActions();
			
			switch (sourceActions) {
			case Const.COPY:
			case Const.COPY_FORCE:
			case Const.MOVE:
			case Const.MOVE_FORCE:
			case Const.COPY_OR_MOVE:
			case Const.COPY_OR_MOVE_FORCE_OR_NOT:
				return true;
			default:
				break;
			}
			
			return false;
		}
		
		protected Transferable createTransferable (DraggableLabel label) {
			return label.getCell();
		}

		/* (non-Javadoc)
		 * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
		 */
		@Override
		public Object getTransferData(DataFlavor flavor)
				throws UnsupportedFlavorException, IOException {
			if (_sourceCell != null)
				return _sourceCell.getTransferData(flavor);
			else
				return null;

		}

		/* (non-Javadoc)
		 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
		 */
		@Override
		public DataFlavor[] getTransferDataFlavors() {
			if (_sourceCell != null)
				return _sourceCell.getTransferDataFlavors();
			else
				return null;
		}

		/* (non-Javadoc)
		 * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
		 */
		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			if (_sourceCell != null)
				return _sourceCell.isDataFlavorSupported(flavor);
			else
				return false;
		}
	}

	/****************************************************
	 * DragGestureRecognizerTable Inner Class
	 *****************************************************/
	
	
	/**
	 * An instance of this class will be responsible for deciding
	 * about drag starts within the table when: (1) no cell is selected,
	 * or (2) when a cell is selected and the user begins a drag on
	 * the cell background, instead of on a label. The most tricky
	 * is when (3) no cell is selected and the user begins a drag on
	 * a label in a cell. This class' dragGestureRecocnized() will
	 * be called in all these cases, and we do want to start a drag
	 * only in case (3). In the other cases we gracefully do nothing,
	 * causing no drag operation to commence. 
	 */
	
	static class DragGestureRecognizerTable implements DragGestureListener {
	
		@Override
		public void dragGestureRecognized(DragGestureEvent dragGestureEvent) {
			
			// Point cursorLoc = dragGestureEvent.getDragOrigin();
			
			// CellCoordinates coordsCellUnderCursor = _table.getCellAddressAt(cursorLoc);
			// PhotoSpreadCell cell = _table.getCell(coordsCellUnderCursor);
			// _table.getCellEditor(coordsCellUnderCursor.row(), coordsCellUnderCursor.column());
			// _table.getCellEditorFor(coordsCellUnderCursor);
			return;
/*			try {
				dragGestureEvent.startDrag(Const.USE_DEFAULT_CURSOR, _transferHandlerNaySayerTable);
			} catch (InvalidDnDOperationException e) {
				return; // drag start failed. Maybe b/c another drag is in progress. Just ignore
			}
*/		}
	}

	/****************************************************
	 * TransferHandlerWorkspace Inner Class
	 *****************************************************/

	static class TransferHandlerWorkspace extends TransferHandler 
	implements Transferable {
		
		private static final long serialVersionUID = 1L;

		public boolean canImport (TransferHandler.TransferSupport support) {
			return false;
		}
		
		protected Transferable createTransferable (DraggableLabel label) {
			return label.getCell();
		}

		/* (non-Javadoc)
		 * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
		 */
		@Override
		public Object getTransferData(DataFlavor flavor)
				throws UnsupportedFlavorException, IOException {
			if (_sourceCell != null)
				return _sourceCell.getTransferData(flavor);
			else
				return null;

		}

		/* (non-Javadoc)
		 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
		 */
		@Override
		public DataFlavor[] getTransferDataFlavors() {
			if (_sourceCell != null)
				return _sourceCell.getTransferDataFlavors();
			else
				return null;
		}

		/* (non-Javadoc)
		 * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
		 */
		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			if (_sourceCell != null)
				return _sourceCell.isDataFlavorSupported(flavor);
			else
				return false;
		}
	}

	/****************************************************
	 * DragGestureRecognizerWorkspace Inner Class
	 *****************************************************/
	
	
	/**
	 * An instance of this class will be responsible for deciding
	 * about drag starts anywhere within the Workspace window, but
	 * on top of a label. In that case the instance of this class'
	 * dragGestureRecognized() method will be called. It initiates
	 * a drag via an instance of the TransferHandlerWorkspace if
	 * the drag began over a label. Else no drag operation is
	 * initiated.

	 */
	static class DragGestureRecognizerWorkspace implements DragGestureListener {
	
		@Override
		public void dragGestureRecognized(DragGestureEvent dragGestureEvent) {
			
			_sourceCell = ((DraggableLabel) dragGestureEvent.getComponent()).getCell();
			
			if (_sourceCell.getSelectedObjects().isEmpty()) 
				return;
			
			try {
				dragGestureEvent.startDrag(Const.USE_DEFAULT_CURSOR, _transferHandlerWorkspace);
			} catch (InvalidDnDOperationException e) {
				return; // drag start failed. Maybe b/c another drag is in progress. Just ignore
			}
		}
	}

	/****************************************************
	 * TransferHandlerLabel Inner Class
	 *****************************************************/
	
	public static class TransferHandlerLabel extends TransferHandler {

		private static final long serialVersionUID = 1L;
		
		public boolean canImport (TransferHandler.TransferSupport support) {
			// Labels can't import anything. At least for now:
			return false;
		}

		public int getSourceActions(JComponent comp) {
			return Const.COPY_OR_MOVE_FORCE_OR_NOT;
		}

		public Transferable createTransferable(DraggableLabel label) {
			return label.getCell();
		}

		// public void exportDone(JComponent comp, Transferable trans, int action) {
		//	return;
		// }
	}
	
	/****************************************************
	 * DragGestureRecognizerLabel Inner Class
	 *****************************************************/
	
	
	static class DragGestureRecognizerLabel implements DragGestureListener {
	
		@Override
		public void dragGestureRecognized(DragGestureEvent dragGestureEvent) {
			
			_sourceCell = ((DraggableLabel) dragGestureEvent.getComponent()).getCell();
			
			if (_sourceCell.getSelectedObjects().isEmpty()) 
				return;
			
			try {
				// dragGestureEvent.startDrag(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR), _transferHandlerWorkspace);
				dragGestureEvent.startDrag(Const.USE_DEFAULT_CURSOR, _transferHandlerWorkspace);
			} catch (InvalidDnDOperationException e) {
				return; // drag start failed. Maybe b/c another drag is in progress. Just ignore
			}
		}
	}
}