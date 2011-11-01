/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.inputOutput;

import java.awt.Component;
import java.awt.HeadlessException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.naming.InvalidNameException;
import javax.swing.JFileChooser;

import edu.stanford.photoSpread.PhotoSpread;
import edu.stanford.photoSpread.PhotoSpreadException;
import edu.stanford.photoSpread.PhotoSpreadException.BadSheetFileContent;
import edu.stanford.photoSpread.PhotoSpreadException.FileIOException;
import edu.stanford.photoSpread.PhotoSpreadException.FormulaError;
import edu.stanford.photoSpreadLoaders.XMLFileFilter;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;
import edu.stanford.photoSpreadTable.PhotoSpreadTableModel;
import edu.stanford.photoSpreadUtilities.Const;
import edu.stanford.photoSpreadUtilities.Misc;

/**
 *
 * @author skandel
 */
public class InputOutput {

	static public String normalizePath(String path) throws InvalidNameException {
		if (path == null) throw new InvalidNameException ("PhotoSpread: Path '" + path + "' is not a valid path.");
		File tmpPath = new File(path);
		try {
			return tmpPath.getCanonicalPath();
		} catch (IOException e) {
			throw new InvalidNameException("PhotoSpread: Path '" + path + " cannot be converted to proper pathname.");
		}
	}

	/**
	 * Saves the table represented by table model to a file in xml format.
	 * This message will communicate with the user to obtain the target file info.  
	 * @param  c is a GUI component over which the user dialog windows will be appear.
	 * @param  tableModel the tableModel containing data that should be saved
	 * @return true if all went well, else false;
	 * @throws PhotoSpreadException 
	 * 
	 */
	static public boolean saveTable(Component c, PhotoSpreadTableModel tableModel) 
			throws HeadlessException, PhotoSpreadException {
		String priorWriteDir = PhotoSpread.photoSpreadPrefs.getProperty(PhotoSpread.lastDirWrittenKey);
		final JFileChooser fc = new JFileChooser(priorWriteDir);

		XMLFileFilter filter = new XMLFileFilter();
		fc.setFileFilter(filter);		

		fc.setMultiSelectionEnabled(false);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int returnVal = fc.showSaveDialog(c);
		//In response to a button click:
			//int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File exportFile = fc.getSelectedFile();
			PhotoSpread.photoSpreadPrefs.setProperty(PhotoSpread.lastDirWrittenKey, exportFile.getParent());

			// Make sure file gets a .xml extension:
			try {
				exportFile = new File (Misc.ensureFileExtension(exportFile.getPath(), "xml"));
			} catch (java.text.ParseException e1) {
				// Exception when a directory is passed into ensureFileExtension
				// GUI file chooser prevents that.
				e1.printStackTrace();
			}
			exportFile.setWritable(true);
			return saveTable(exportFile, tableModel);
		}
		return true;
	}

	
	/**
	 *Saves the table represented by table model to a file in xml format.  
	 * @param  exportFile must be a writeable destination file.
	 * @param  tableModel the tableModel containing data that should be saved
	 * @return true if all went well, else false;
	 * 
	 */
	
	static public boolean saveTable(File exportFile, PhotoSpreadTableModel tableModel) 
			throws HeadlessException, PhotoSpreadException {
		try {
			// Create file
			FileWriter fstream = new FileWriter(exportFile.getPath());
			BufferedWriter out = new BufferedWriter(fstream);

			out.write("<?xml version='1.0' encoding='ISO-8859-1'?>" + System.getProperty("line.separator"));
			tableModel.toXML(out);
			//out.write(tableModel.toXML());
			//Close the output stream
			out.close();
			return true;
		} catch (Exception e) {
			//Catch exception if any
			throw new PhotoSpreadException.FileIOException(e.getMessage());
		}
	}
	

	/**
	 * Loads the table represented by table model to a file in xml format.  
	 * @param  guiComponent  the component over which the load dialague should display.
	 * @param  tableModel the tableModel that will be loaded with data

	 */

	static public void loadTable(Component guiComponent, PhotoSpreadTableModel tableModel) throws HeadlessException {	
		
		File importFile;
		String priorReadDir = PhotoSpread.photoSpreadPrefs.getProperty(PhotoSpread.lastDirReadKey);
		final JFileChooser fc = new JFileChooser(priorReadDir);

		XMLFileFilter filter = new XMLFileFilter();
		fc.setFileFilter(filter);		

		fc.setMultiSelectionEnabled(false);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int returnVal = fc.showOpenDialog(guiComponent);
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;

		importFile = fc.getSelectedFile();
		PhotoSpread.photoSpreadPrefs.setProperty(PhotoSpread.lastDirReadKey, importFile.getParent());
		
		loadTable(importFile, tableModel);
	}
	
	/**
	 * Workhorse for loading the table represented by table model to a file in xml format.  
	 * @param  importFile is the XML file that contains the XMLified table of a former session.
	 * @param  tableModel the existing tableModel that will be loaded with data.
	 */
	
	static public boolean loadTable(File importFile, PhotoSpreadTableModel tableModel) throws HeadlessException {
		
		XMLProcessor xmlProc = new XMLProcessor();

		try {

			xmlProc.loadXMLFile(importFile, tableModel);

		} catch (BadSheetFileContent e) {
			Misc.showErrorMsg(e.getMessage());
			return false;
		} catch (FileIOException e) {
			Misc.showErrorMsg(e.getMessage());
			return false;
		}

		// The following nested loop is a bit of a hack.
		// In order to really get all the cells showing
		// everything correctly, we need to evaluate every
		// cell, and set all cell formulas to their already
		// existing formulas again...I know...
		for (int col=1; col < tableModel.getColumnCount(); col++) {
			for (int row=0; row < tableModel.getRowCount(); row++) {
				PhotoSpreadCell cell = tableModel.getCell(row, col);
				try {
					cell.evaluate(Const.DO_REDRAW);
				} catch (FormulaError e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}
}
