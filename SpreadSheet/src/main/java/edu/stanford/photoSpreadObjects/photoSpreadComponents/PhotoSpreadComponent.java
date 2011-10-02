/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.photoSpreadObjects.photoSpreadComponents;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.rmi.NotBoundException;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;
import edu.stanford.photoSpreadUtilities.PhotoSpreadContextMenu;

/**
 *
 * @author skandel
 */
public class PhotoSpreadComponent {
    PhotoSpreadObject _parentObject;
    PhotoSpreadCell _cell;
    Component _component;
    
    public PhotoSpreadComponent(PhotoSpreadObject _parentObject, Component _component) {
        this._parentObject = _parentObject;
        this._cell = null;
        initialize();
    }
    
    public void setCell(PhotoSpreadCell cell){
       
        this._cell = cell;
    }
    
    public PhotoSpreadCell getCell(){
        return _cell;
    }
    
    public Component getComponent(){
        return _component;
    }
    
    public PhotoSpreadObject getParentObject(){
        return _parentObject;
    }

    
    
    public void highlight(){
        
    }
    
    public void unhighlight(){
       
    }
    
    private void initialize(){
        
        makeDraggable();
        addMenu();
        
     
    }
    
    private void addMenu(){
        PhotoSpreadContextMenu menu = new PhotoSpreadContextMenu();
        menu.addMenuItem("Edit Meta Data",  new java.awt.event.ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    try {
						editMetaData();
					} catch (NumberFormatException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (NotBoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                }
            }
                
        );
        
        _component.addMouseListener(menu.getPopupListener());
    }
    
    private void editMetaData() throws NumberFormatException, NotBoundException {
        MetadataEditor editor = new MetadataEditor();
        // Don't know what this size contols:
        editor.setSize(new Dimension(500, 500));
        editor.setEditedObject(this._parentObject);
       
        editor.setVisible(true);
    }
    
    private void makeDraggable(){
      //_component.setTransferHandler(new FromTransferHandler());
      MouseListener mouseListener = 
            new MouseAdapter() {
          
          public void mousePressed(MouseEvent e) {
           
              if(e.getButton() == MouseEvent.BUTTON1){
                JComponent comp = (JComponent)e.getSource();
                TransferHandler handler = 
                    comp.getTransferHandler();

                if(handler != null){

                    handler.exportAsDrag(
                        comp, e, TransferHandler.COPY);
                }
                int clickType = ObjectsPanel.NORMAL_LEFT_CLICK;
                if(e.isShiftDown()){
                    clickType = ObjectsPanel.SHIFT_LEFT_CLICK;
                }
                if(e.isControlDown()){
                    clickType = ObjectsPanel.CTRL_LEFT_CLICK;
                }
                if(e.isAltDown()) {
                    clickType = ObjectsPanel.ALT_LEFT_CLICK;
                }
                
                ((ObjectsPanel) 
                		PhotoSpreadComponent.
                		this.
                		getComponent().
                		getParent()).
                		clickLabel((DraggableLabel) e.getSource(), clickType);    
             
            }
          }
        };
        _component.addMouseListener(mouseListener);
    }
    
    class FromTransferHandler extends TransferHandler {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public int getSourceActions(JComponent comp) {
            return COPY_OR_MOVE;
        }

        public Transferable createTransferable(JComponent comp) {
               
            

            return PhotoSpreadComponent.this._cell;
        }
        
        public void exportDone(JComponent comp, Transferable trans, int action) {
            
          
            if (action != MOVE) {
                return;
            }
        }
    }
}
