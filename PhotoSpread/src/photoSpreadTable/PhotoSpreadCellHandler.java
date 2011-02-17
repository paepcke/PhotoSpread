/**
 * 
 */
package photoSpreadTable;

import java.awt.event.MouseEvent;
import java.rmi.NotBoundException;
import java.util.EventObject;

import javax.swing.CellEditor;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.event.MouseInputAdapter;

import photoSpreadObjects.photoSpreadComponents.ObjectsPanel;
import photoSpreadUtilities.Const;

/**
 * @author paepcke
 *
 */
public class PhotoSpreadCellHandler extends ObjectsPanel 
implements CellEditor, CellEditorListener {

	private static final long serialVersionUID = 1L;

	private boolean _isDirty = false;
	private PhotoSpreadTable _table = null;
	private PhotoSpreadCell _myCell = null;
	protected EventListenerList _eventListeners = new EventListenerList();
	protected ChangeEvent _changeEvent = new ChangeEvent(this);
	
	// Drag 'n Drop Support:
	protected static PhotoSpreadCellHandler _dndOriginator = null;
	protected static PhotoSpreadCellHandler _currentDropTarget = null;
	
	
	/****************************************************
	 * Constructor(s)
	 *****************************************************/


	public PhotoSpreadCellHandler(
			PhotoSpreadTable table,
			PhotoSpreadCell cell) {
		
		super(cell);
		
		_table = table;
		_myCell = cell;
		
		// Request notification of change events.
		// This PhotoSpreadCellHandler will then
		// be called whenever fireXXX() methods
		// are invoked for the cell to which this
		// PhotoSpreadCellHandler is the editor. 
		// Usually this will result in the method
		// editingStopped(ChangeEvent), or 
		// editingCancelled(ChangeEvent) being invoked:
		// Note that we sometimes fire those events from
		// this very PhotoSpreadCellHandler:
		
		addCellEditorListener(this);
		
		// Listen for events signaling mouse-entered/mouse-exited 
		// into this cell. We provide visual feedback 
		// when the cursor is in the cell:
		
		addMouseListener(new CellMouseManager());
	}
	
	/****************************************************
	 * Getter/Setter(s)
	 *****************************************************/
	
	// public void isDirty(boolean isDirty) {
    //	this._isDirty = isDirty;
	//}

	public boolean isDirty() {
		return _isDirty;
	}

	/****************************************************
	 * Listener(s)
	 *****************************************************/

	private class CellMouseManager extends MouseInputAdapter {
		
		// This listener just clears drag feedback
		// in case the drag was abandoned, and drop() was
		// therefore not called. This happens when the mouse
		// is released somwhere in the window other than on
		// a cell, or when it is released on the cell that
		// it started on:
		
		public void mouseReleased (MouseEvent e) {
			// if (_currentDropTarget != null)
			_table.getDndViz().clearVisualDnDFeedback();
			_currentDropTarget = null;
		}
	}
	
	/****************************************************
	 * Methods Required by the CellEditor Interface
	 *****************************************************/

	/** 
	 * Callers who are involved in editing 
	 * invoke this method when they are done
	 * making changes. 
	 * 
	 * This method does not do any of the then
	 * necessary update work. It just fires a 
	 * change event to all registered listeners.
	 * The parent table is one such listener. This
	 * PhotoSpreadCellHandler itself is a listener
	 * for such events as well. So the method
	 * editingStopped() will be called later as a 
	 * result of the ChangeEvent firing. At that
	 * point we update the screen.
	 * 
	 * @see javax.swing.CellEditor#stopCellEditing()
	 */
	@Override
	public boolean stopCellEditing() {
		_isDirty = true;
		fireEditingStopped();
		return true;
	}
	
	/** 
	 * Callers who are involved in editing 
	 * invoke this method when they are done
	 * making changes. 
	 * 
	 * This method does not do any of the then
	 * necessary update work. It just fires a 
	 * change event to all registered listeners.
	 * The parent table is one such listener. This
	 * PhotoSpreadCellHandler itself is a listener
	 * for such events as well. So the method
	 * editingStopped() will be called later as a 
	 * result of the ChangeEvent firing. At that
	 * point we update the screen.
	 *
	 * @see javax.swing.CellEditor#cancelCellEditing()
	 */
	@Override
	public void cancelCellEditing() {
		_isDirty = true;
		fireEditingCanceled();
	}

	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	@Override
	public Object getCellEditorValue() {
		return _myCell;
	}

	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#isCellEditable(java.util.EventObject)
	 */
	@Override
	public boolean isCellEditable(EventObject anEvent) {
		// Always return true here. This same method was
		// called earlier in the table model. It ensured
		// that column 0 (the row numbers) are not editable.
		// We'll never get asked about that column here:
		return true;
	}

	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#addCellEditorListener(javax.swing.event.CellEditorListener)
	 */
	@Override
	public void addCellEditorListener(CellEditorListener changeEventListener) {
		_eventListeners.add(CellEditorListener.class, changeEventListener);
	}

	
	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#removeCellEditorListener(javax.swing.event.CellEditorListener)
	 */
	@Override
	public void removeCellEditorListener(CellEditorListener changeEventListener) {
		_eventListeners.remove(CellEditorListener.class, changeEventListener);
	}

	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#shouldSelectCell(java.util.EventObject)
	 */
	@Override
	public boolean shouldSelectCell(EventObject anEvent) {
		return true;
	}
	
	/****************************************************
	 * Methods Required by the CellEditorListener Interface
	 *****************************************************/

	
	/**
	 * This method is invoked when the cell that we are 
	 * editing had changed. Don't call this method directly.
	 * This method is only invoked by fired change events.
	 * To indicate a finished editing activity call method
	 * stopCellEditing() instead.
	 * 
	 * @see javax.swing.event.CellEditorListener#editingStopped(javax.swing.event.ChangeEvent)
	 */
	public void editingStopped (ChangeEvent e) {
		try {

			// Repaint the cell:
			redraw();
			// Repaint parts of the workspace if the workspace
			// currently shows this cell:
			if (_table.getWorkspace().getDisplayedCell() == _myCell)
				_table.getWorkspace().redraw();
			
		} catch (NumberFormatException e1) {
			e1.printStackTrace();
		} catch (NotBoundException e1) {
			e1.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.CellEditorListener#editingCanceled(javax.swing.event.ChangeEvent)
	 */
	public void editingCanceled(ChangeEvent e) {
		return;
	}

	
	/****************************************************
	 * Support Methods
	 *****************************************************/

	protected void fireEditingStopped () {

		CellEditorListener listener;
		Object[] listeners = _eventListeners.getListenerList();

		for (int i = 0; i < listeners.length; i++) {
			
			// Is this listener a CellEditorListener?
			if (listeners[i] == CellEditorListener.class) {
				listener = (CellEditorListener) listeners[i + 1];
				listener.editingStopped(_changeEvent);
			} 
		} 
	}

	protected void fireEditingCanceled () {

		CellEditorListener listener;
		Object[] listeners = _eventListeners.getListenerList();

		for (int i = 0; i < listeners.length; i++) {
			
			// Is this listener a CellEditorListener?
			if (listeners[i] == CellEditorListener.class) {
				listener = (CellEditorListener) listeners[i + 1];
				listener.editingCanceled(_changeEvent);
			} 
		} 
	}

	public void activeHighlight() {
		this.setBackground(Const.activeCellBackgroundColor);
	}
	
	public void activeUnHighlight() {
		setBackground(null);
	}
	
	public boolean isActiveHighlighted() {
		if (getBackground() == Const.activeCellBackgroundColor)
			return true;
		else
			return false;
	}
	
	public void hoverHighlight () {

	}

	public void unHoverHighlight () {
	}
}
    