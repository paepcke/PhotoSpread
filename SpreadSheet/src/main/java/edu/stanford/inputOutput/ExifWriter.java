package edu.stanford.inputOutput;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.ExifTagConstants;
import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.apache.sanselan.formats.tiff.constants.TiffFieldTypeConstants;
import org.apache.sanselan.formats.tiff.write.TiffOutputDirectory;
import org.apache.sanselan.formats.tiff.write.TiffOutputField;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;

import edu.stanford.photoSpreadObjects.PhotoSpreadFileObject;
import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadUtilities.MetadataIndexer;


/**
 * @author evanshieh
 *
 * Custom ExifWriter for PhotoSpread - writes attribute/value pairs to the "UserComment"
 * tag field (Tag ID: 0x9286) in the EXIF metadata associated with a file.
 */
public class ExifWriter {

	/****************************************************
	 * Public Methods
	 *****************************************************/

    /**
     * Writes the string "text" to the UserComment field of the file 
     * "file". Assumes that text been already formatted to fit program specifications
     * (key \t value):
     * @param file
     * @param text
     */
    public static void write (File file, String text) {
        File resultFile = null;
        OutputStream outputStream = null;
        TiffOutputSet outputSet = initOutputSet(file);
        
        if (outputSet != null) {         
            // Clears the userComment field, saving the non-PhotoSpread preceding text
            TiffOutputField userCommentBefore = outputSet.findField(TiffConstants.EXIF_TAG_USER_COMMENT);
            if (userCommentBefore != null) {
            	String beforeText = ExifReader.getPrecedingUserCommentData(file);
            	text = beforeText + " " + text; // Appends the previous text before the text to be added
            	outputSet.removeField(TiffConstants.EXIF_TAG_USER_COMMENT);  
            }
            
            // Writes to the field 
            try {                              
                TiffOutputField userComment = new TiffOutputField(
                           ExifTagConstants.EXIF_TAG_USER_COMMENT, 
                           TiffFieldTypeConstants.FIELD_TYPE_ASCII, 
                           text.length(), 
                           text.getBytes());
                TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
                exifDirectory.add(userComment);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Creates a stream using a temporary file
        try {
            resultFile = File.createTempFile("temp-" + System.currentTimeMillis(), ".jpeg");
            outputStream = new FileOutputStream(resultFile);
            outputStream = new BufferedOutputStream(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Writes metadata to the output stream
        try {
            new ExifRewriter().updateExifMetadataLossless(file, outputStream, outputSet);
        } catch (ImageReadException e) {
            e.printStackTrace();
        } catch (ImageWriteException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                    outputStream = null;
                } catch (IOException e) {
                	e.printStackTrace();
                }
            }
        }
        
        // Writes the result file over the original file
        try {
            FileUtils.copyFile(resultFile, file);
            resultFile = null;
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }
    
    /**
     * Writes the string "text" to the UserComment field of the file 
     * specified by "filePath". Assumes that text has already been 
     * formatted to fit program specifications.
     * @param filePath
     * @param text
     */
    public static void write (String filePath, String text) {
    	File file = null;
    	file = openFile(filePath); // Error checking? Use fileWriter instead?
    	if (file != null) write(file, text);
    }
    
    /**
     * Writes the string derived by the HashMap "tags" to the 
     * UserComment field of the file "file". Reformats the 
     * tags to fit a string of the format
     * 
     * [key \t value]
     * 
     * separated by lines.
     * @param file
     * @param tags
     */
    public static void write (File file, HashMap<String, String> tags) {
    	String text = "";
    	text = metadataToString(tags);
    	write(file, text);
    }
    
    /**
     * Writes the string derived by the HashMap "tags" to the 
     * UserComment field of the file specified by "filePath". 
     * Reformats the tags to fit a string of the format
     * 
     * [key \t value]   (Andreas: was [key:value]
     * 
     * separated by lines.
     * @param file
     * @param tags
     */
    public static void write (String filePath, HashMap<String, String> tags) {
    	String text = "";
    	text = metadataToString(tags);
    	File file = null;
    	file = openFile(filePath); // Error checking?
    	if (file != null) write(file, text);
    }
    
    /**
     * Writes the string derived by the ArrayList "tags" to the 
     * UserComment field of the file "file". Reformats the 
     * tags to fit a string of the format.
     * 
     * [key \t value]
     * 
     * separated by lines.
     * @param file Image file object
     * @param tags [[key1,value1], [key2,value2], ... ]
     */
    public static void write (File file, ArrayList< ArrayList<String> > tags) {
    	String text = "";
    	text = metadataToString(tags);
    	write(file, text);
    }
    
    /**
     * Writes the string derived by the ArrayList "tags" to the 
     * UserComment field of the file specified by "filePath". 
     * Reformats the tags to fit a string of the format
     * 
     * [key \t value]
     * 
     * separated by lines.
     * @param file Image file path
     * @param tags [[key1,value1], [key2,value2], ... ]
     */
    public static void write (String filePath, ArrayList< ArrayList<String> > tags) {
    	String text = "";
    	text = metadataToString(tags);
    	File file = null;
    	file = openFile(filePath); // Error checking?
    	if (file != null) write(file, text);
    }
    
    /**
     * Updates the metadata of all photo objects in
     * a given MetadataIndexer. This works in conjunction
     * with the method "getInverseMap()" specified in
     * MetadataIndexer.java. It will write to the tag
     * field "UserComment" with the format
     * 
     * [key \t value]
     * 
     * separated by lines.
     * 
     * @param indexer
     */
    public static void write (MetadataIndexer indexer) {
    	HashMap <PhotoSpreadObject, HashMap<String, String>> objectMap = indexer.getInverseMap(); 
    	HashMap<String, String> valueMap = null;
    	PhotoSpreadFileObject psFileObject = null;
    	String filePath = "";
    	
    	Iterator <PhotoSpreadObject> itPsObject = objectMap.keySet().iterator();
    	while (itPsObject.hasNext()) {
    		psFileObject = (PhotoSpreadFileObject) itPsObject.next();
    		filePath = psFileObject.getFilePath();
    		valueMap = objectMap.get(psFileObject);
    		write (filePath, valueMap);
    	}
    }
    
    public static void clearPhotoSpreadMetadata(String filePath) {
    	clearDataField(filePath, TiffConstants.EXIF_TAG_USER_COMMENT);
    }
    
    public static void clearDataField (String filePath, TagInfo tagLocation) {
        File file = openFile(filePath);
    	TiffOutputSet outputSet = initOutputSet(file);
    	
    	File resultFile = null;
        OutputStream outputStream = null;
        
        if (outputSet != null) {         
            // Clears the tagLocation field
            TiffOutputField userCommentBefore = outputSet.findField(tagLocation);
            if (userCommentBefore != null) outputSet.removeField(tagLocation);  
        }
        
     // Creates a stream using a temporary file
        try {
            resultFile = File.createTempFile("temp-" + System.currentTimeMillis(), ".jpeg");
            outputStream = new FileOutputStream(resultFile);
            outputStream = new BufferedOutputStream(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Writes metadata to the output stream
        try {
            new ExifRewriter().updateExifMetadataLossless(file, outputStream, outputSet);
        } catch (ImageReadException e) {
            e.printStackTrace();
        } catch (ImageWriteException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                    outputStream = null;
                } catch (IOException e) {
                	e.printStackTrace();
                }
            }
        }
        
        // Writes the result file over the original file
        try {
            FileUtils.copyFile(resultFile, file);
            resultFile = null;
        } catch (IOException e) {
            e.printStackTrace();
        }    
    }

	/****************************************************
	 * Private Methods
	 *****************************************************/
    
    /*
	 * Initializes the OutputSet (helper method for "private static void write(File file, String text)")
	 */
    private static TiffOutputSet initOutputSet(File file) {
    	// Initializes IImageMetadata
        IImageMetadata metadata = null;
        try {
            metadata = Sanselan.getMetadata(file);
        } catch (ImageReadException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Initializes jpegMetadata
        JpegImageMetadata jpegMetadata = null;
        if (metadata != null) jpegMetadata = (JpegImageMetadata) metadata;
        
        // Initializes TiffImageMetadata
        TiffImageMetadata exifMetadata = null;
        if (jpegMetadata != null) exifMetadata = jpegMetadata.getExif();
        
        // Initializes outputSet
        TiffOutputSet outputSet = new TiffOutputSet();
        if (exifMetadata != null) {
            try {
                outputSet = exifMetadata.getOutputSet();
            } catch (ImageWriteException e) {
                e.printStackTrace();
            }
        }
    	return outputSet;
    }
    
    /*
	 * Opens the file
	 */
    private static File openFile (String filePath) {
    	File result = null;
    	try {
    		result = new File (filePath);
    	} catch (Exception e){
			System.err.println("Error: " + e.getMessage());
		}
    	return result;
    }
  
    /*
	 * Stores a HashMap of metadata in a String, according to PhotoSpread parsing requirements
	 */
    private static String metadataToString(HashMap<String, String> tags) {
		String result = "";
    	Iterator<String> it = tags.keySet().iterator();
    	String key = "";
    	String value = "";
		while (it.hasNext()) {
			key = it.next();
			value = tags.get(key);
			if (result.equals("")) {
				result += "[" + key + "\t" + value + "]";
			} else {
				result += "\n" + "[" + key + "\t" + value + "]";
			}
		}
		return result;
	}
    
    /*
	 * Stores a two-dimensional ArrayList in a String, according to PhotoSpread parsing requirements
	 */
    private static String metadataToString(ArrayList< ArrayList<String> > tags) {
		String result = "";
    	String key = "";
    	String value = "";
    	ArrayList<String> entry = new ArrayList<String>();
		for (int i = 0; i<tags.size(); i++) {
			entry = tags.get(i);
			key = entry.get(0);
			value = entry.get(1);
			if (result.equals("")) {
				result += "[" + key + "\t" + value + "]";
			} else {
				result += "\n" + "[" + key + "\t" + value + "]";
			}
		}
		return result;
	}
}
