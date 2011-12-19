/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.photoSpreadLoaders;

import javax.swing.ImageIcon;

import edu.stanford.photoSpread.PhotoSpreadException.BadUUIDStringError;
import edu.stanford.photoSpread.PhotoSpreadException.CannotLoadImage;
import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadObjects.photoSpreadComponents.DraggableLabel;
import edu.stanford.photoSpreadUtilities.Const;
import edu.stanford.photoSpreadUtilities.Misc;

/**
 *
 * @author skandel
 */
public class PhotoSpreadImageLoader {
	
	@SuppressWarnings("unused")
	private String _fileName = "";
        
    public PhotoSpreadImageLoader(){
        
    }
    
    /**
 *Creates a draggable image label containing image with source from a given url  
 * @param  parentObject the object that the label represents
 * @param  url  the source of the image
     * @param height height of the image
     * @param width width of the image
     * @return the draggable label created
     * @throws CannotLoadImage 
 
 */
    public DraggableLabel getImageComponent(PhotoSpreadObject parentObject, String url, int height, int width) 
    throws CannotLoadImage{
        ImageIcon icon;
        _fileName = url;
        icon = createImageIcon(url);
        
        DraggableLabel imageLabel = new DraggableLabel(parentObject, height, width);       
        if(icon != null){
                    
            //****ImageIcon thumbnailIcon = new ImageIcon(Misc.getScaledImage(icon, height, width));  
            ImageIcon thumbnailIcon =  new ImageIcon(Misc.getProportiallyScaledImage(icon, Const.DimensionSide.WIDTH, width));  
            imageLabel.setIcon(thumbnailIcon);
            
        }
        return imageLabel;
     }
    
     /**
      *Creates a draggable image label containing image with source from a given url, using dimensions of original image  
      * @param  parentObject the object that the label represents
      * @param  url  the source of the image
      * @return the draggable label created
      * @throws CannotLoadImage 

      */
    
     public DraggableLabel getImageComponent(PhotoSpreadObject parentObject, String url) throws CannotLoadImage{
        ImageIcon icon;
        _fileName = url;
        icon = createImageIcon(url);
        
        DraggableLabel imageLabel = new DraggableLabel(parentObject);       
        if(icon != null){
            imageLabel.setIcon(icon);
        }
        return imageLabel;
     }
    
    /**
     * A better version of this method is in Misc.
     * Creates an ImageIcon if the path is valid.
     * @param String - resource path
     * @param String - description of the file
     * @throws BadUUIDStringError 
     * @throws CannotLoadImage 
     */
     private ImageIcon createImageIcon(String path) throws CannotLoadImage {
    	 _fileName = path;
    	 ImageIcon iIcon = new ImageIcon(path);
    	 return iIcon;
    }
  }
