/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.photoSpreadObjects.photoSpreadComponents;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.rmi.NotBoundException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.event.MouseInputAdapter;

import edu.stanford.photoSpread.PhotoSpread;
import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadTable.DnDSupport;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;
import edu.stanford.photoSpreadUtilities.ComputableDimension;
import edu.stanford.photoSpreadUtilities.Const;
import edu.stanford.photoSpreadUtilities.Misc;
import edu.stanford.photoSpreadUtilities.PhotoSpreadContextMenu;

/**
 *
 * @author skandel
 */
public class DraggableLabel extends JLabel implements PhotoSpreadAddable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private PhotoSpreadObject _parentObject;
	private PhotoSpreadCell _cell;
	private ComputableDimension _imageSize = new ComputableDimension(0, 0);
	private ComputableDimension _unscaledSize = null;
	
	/****************************************************
	 * Constructor(s)
	 *****************************************************/

	// Pass the object that this label represents (e.g. an image):

	public DraggableLabel(PhotoSpreadObject _parentObject) {
		this._parentObject = _parentObject;
		this._cell = null;
		initialize();
	}

	public DraggableLabel(PhotoSpreadObject _parentObject, int height, int width) {
		this(_parentObject);
		_imageSize = (new ComputableDimension(width, height));
	}

	public DraggableLabel(Icon arg0, PhotoSpreadObject _parentObject) {
		super(arg0);
		this._parentObject = _parentObject;
		initialize();
	}

	public DraggableLabel(Icon image, int horizontalAlignment, PhotoSpreadObject _parentObject) {
		super(image, horizontalAlignment);
		this._parentObject = _parentObject;
		initialize();
		_imageSize = (new ComputableDimension(image.getIconWidth(), image.getIconHeight()));
	}

	public DraggableLabel(String text, PhotoSpreadObject _parentObject) {
		super(text);
		this._parentObject = _parentObject;
		initialize();
		_imageSize = (new ComputableDimension(this.getSize()));
	}

	public DraggableLabel(String text, int horizontalAlignment, PhotoSpreadObject _parentObject) {
		super(text, horizontalAlignment);
		this._parentObject = _parentObject;
		initialize();
		_imageSize = (new ComputableDimension(this.getSize()));
	}

	public DraggableLabel(String text, Icon image, int horizontalAlignment, PhotoSpreadObject _parentObject) {
		super(text, image, horizontalAlignment);
		this._parentObject = _parentObject;
		initialize();
		_imageSize = (new ComputableDimension(this.getSize()));		
	}

	/****************************************************
	 * Methods
	 *****************************************************/

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)\
	 * 
	 * We need to override pointComponent(), so that images
	 * are properly resized in the Workspace when the size
	 * slider is operated. Without this override, the images
	 * get clipped, rather than sized.
	 */
	
	@Override
	protected void paintComponent (Graphics g) {
		super.paintComponent (g);
		ImageIcon theIcon = (ImageIcon)getIcon();
		// For 'text' draggable labels, the Icon part of this DraggableLabel is null:
		if (theIcon != null) {
			Image theImage = theIcon.getImage();
			if (theImage != null)
				g.drawImage (theImage, 0, 0, getWidth (), getHeight (), null);
		}
	}
	
	public void setCell(PhotoSpreadCell cell){

		this._cell = cell;
	}

	public PhotoSpreadCell getCell(){
		return _cell;
	}

	public JComponent getComponent(){
		return this;
	}

	public PhotoSpreadObject getParentObject(){
		return _parentObject;
	}
	
	public void setUnscaledSize(int x, int y) {
		_unscaledSize = new ComputableDimension(x, y);
	}

	public void setUnscaledSize(ComputableDimension dim) {
		_unscaledSize = dim;
	}
	
	/**
	 * Returns the dimensions of the original image, if this
	 * label wraps an image. These dimensions are the size
	 * of the original image file.
	 * 
	 * @return _unscaledSize if it was set, else null:
	 */
	public ComputableDimension getUnscaledSize() {
		return _unscaledSize;
	}
	
	
	/**
	 * highlights the label

	 */

	public void highlight(){
		this.setBorder(Const.labelHighlightBorder);
	}

	/**
	 * removes highlight from label

	 */

	public void unhighlight(){
		this.setBorder(null);
	}
	
	public boolean isHighlighted() {
		if (this.getBorder() != null)
			return true;
		else
			return false;
	}

	public Insets insets () {
		return Const.draggableLabelInsets;
	}
	
	
	/**
	 * Returns the original size of the represented object.
	 * @return the _originalSize
	 */

	public ComputableDimension getNaturalSize() {

		int maxWidth = 0;
		int maxHeight= 0;

		maxWidth = PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.workspaceMaxObjWidthKey);
		maxHeight = PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.workspaceMaxObjHeightKey);

		if (_imageSize.compareTo(
				new ComputableDimension(maxWidth, maxHeight)) == Const.BIGGER)
			return new ComputableDimension(maxWidth, maxHeight);
		else
			return _imageSize;
	}

	public int compareTo(PhotoSpreadAddable thatAddable) {
		return this._imageSize.compareTo(thatAddable.getNaturalSize());
	}

	private void initialize(){

		makeDraggable();
		addMenu();
/*		setBorder(BorderFactory.createEmptyBorder(
				3,    // top
				100,    // left
				3,    // bottom
				10));  // right
*/	
		}

	private void addMenu(){
		PhotoSpreadContextMenu menu = new PhotoSpreadContextMenu();

		// Contex menu item Edit Metadata:
		menu.addMenuItem("Edit Metadata",  
				new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editMetaData();
			} // end Edit Metadata action performed
		} // end Edit Metadata action listener
		); // end Edit Metadata add menu

		/*       // Contex menu item Select All:        
        menu.addMenuItem("Select All",  
          new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectAll();
            } // end SelectAll action performed
        } // end SelectAll action listener
        );  // end SelectAll add menu
		 */      
		this.addMouseListener(menu.getPopupListener());
	}

	/**
	 * Return a new metadata editor that's open
	 * and visible.
	 * @return
	 */

	private MetadataEditor editMetaData() {

		MetadataEditor editor = new MetadataEditor();

		editor.setSize(PhotoSpread.photoSpreadPrefs.getDimension(PhotoSpread.editorSizeKey));

		try {
			editor.setEditedObject(this._parentObject);
		} catch (NumberFormatException e) {
			Misc.showErrorMsgAndStackTrace(e, "Fatal error.");
			//System.out.println(e.getMessage());
			//e.printStackTrace();
			System.exit(-1);
		} catch (NotBoundException e) {
			Misc.showErrorMsgAndStackTrace(e, "Fatal error.");
			//System.out.println(e.getMessage());
			//e.printStackTrace();
			System.exit(-1);
		}

		editor.setVisible(true);
		return editor;
	}

	/*    private void selectAll() {

    	// Get all objects in the enclosing cell
    	// and call highlight on each of them. This
    	// takes care of making the View look highlit:

    	Iterator<PhotoSpreadObject> it = _cell.getObjectsIterator();
    	while (it.hasNext()) {
    		it.next()).highlight();
    	}

    	// Now make the cell Model know that all its contents 
    	// are highlit:
    	_cell.selectAllObjects();

    }
	 */    
	
	/**
	 * @author paepcke
	 * This class listens for mouse events on an individual item 
	 * (e.g. image). It handles single clicks (selection), and 
	 * double-clicks (start Metadata Editor).  
	 * 
	 * In addition, it solves the following problem: the 
	 * user selects an image in the workspace, and drags it to the sheet window.
	 * They now go back to the Workspace window, click-down on 
	 * a second image, and start dragging it right away, before
	 * releasing the mouse. Without the action of this class,
	 * the originally selected image is still selected, and gets
	 * dragged a second time. 
	 * 
	 * To avoid this confusing inconvenience, Workspace listens for
	 * Workspace-window focus-gained events (see Workspace.java}. 
	 * If the Workspace sees such an event, it sets the 
	 * maybeFocusPlusDragEvent flag to true. 
	 * 
	 * Each DraggableLabel, when detecting a drag event (mouseDragged()),
	 * checks whether it resides in the current Workspace, and whether
	 * that Workspace just gained focus, without the mouse having been
	 * release before the drag. If so, we de-select everything
	 * in the Workspace window, and select only the item being dragged,
	 * i.e. this item.
	 */

	private class PhotoSpreadMouseMotionListener extends MouseInputAdapter {

		@Override
		public void mouseReleased(MouseEvent e) {

			if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2){
				((DraggableLabel) e.getSource()).editMetaData();
			}

			if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1){

				int clickType = ObjectsPanel.NORMAL_LEFT_CLICK;
				int clickModifier = e.getModifiersEx();

				if ((clickModifier & ObjectsPanel.SHIFT_LEFT_CLICK) == ObjectsPanel.SHIFT_LEFT_CLICK) {
					clickType = ObjectsPanel.SHIFT_LEFT_CLICK;
				}
				if ((clickModifier & ObjectsPanel.CTRL_LEFT_CLICK) == ObjectsPanel.CTRL_LEFT_CLICK) {
					clickType = ObjectsPanel.CTRL_LEFT_CLICK;
				}
				
				// If this DraggableLabel is embedded in a table
				// cell, then act on the mouse button release.
				// Else ignore the release:
				try {
					((ObjectsPanel)
							DraggableLabel.this.getParent()).
							clickLabel((DraggableLabel) e.getSource(), clickType);
				} catch (ClassCastException exception) {
					// Just ignore
				}
			}
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {		
			
			Workspace ws = PhotoSpread.getCurrentWorkspaceWindow();
			if (!ws.isObjectSelected(DraggableLabel.this)) {
				ws.deSelectAll();
				ws.selectObject(DraggableLabel.this);
				return;
			}
		}
	}

	private void makeDraggable() {

		this.setTransferHandler(new DnDSupport.TransferHandlerLabel());

		PhotoSpreadMouseMotionListener mouseListener = new PhotoSpreadMouseMotionListener();
		this.addMouseListener(mouseListener);
		this.addMouseMotionListener(mouseListener);
	}

}
