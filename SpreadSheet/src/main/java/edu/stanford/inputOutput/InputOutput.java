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
import edu.stanford.photoSpread.PhotoSpreadException.BadSheetFileContent;
import edu.stanford.photoSpread.PhotoSpreadException.FileIOException;
import edu.stanford.photoSpreadLoaders.XMLFileFilter;
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
	 *Saves the table represented by table model to a file in xml format.  
	 * @param  c  the component over which the save dialague should display
	 * @param  tableModel the tableModel containing data that should be saved

	 */

	static public void saveTable(Component c, PhotoSpreadTableModel tableModel) throws HeadlessException {

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
			try {
				// Create file
				FileWriter fstream = new FileWriter(exportFile.getPath());
				BufferedWriter out = new BufferedWriter(fstream);

				out.write("<?xml version='1.0' encoding='ISO-8859-1'?>" + System.getProperty("line.separator"));
				tableModel.toXML(out);
				//out.write(tableModel.toXML());
				//Close the output stream
				out.close();
			} catch (Exception e) {
				//Catch exception if any
				System.err.println("Error: " + e.getMessage());
			}
		}
	}

	/**
	 * Loads the table represented by table model to a file in xml format.  
	 * @param  c  the component over which the load dialague should display
	 * @param  tableModel the tableModel that will be loaded with data

	 */

	static public void loadTable(Component c, PhotoSpreadTableModel tableModel) throws HeadlessException {

		String priorReadDir = PhotoSpread.photoSpreadPrefs.getProperty(PhotoSpread.lastDirReadKey);
		final JFileChooser fc = new JFileChooser(priorReadDir);

		XMLFileFilter filter = new XMLFileFilter();
		fc.setFileFilter(filter);		

		fc.setMultiSelectionEnabled(false);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int returnVal = fc.showOpenDialog(c);
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;
		
		File importFile = fc.getSelectedFile();
		PhotoSpread.photoSpreadPrefs.setProperty(PhotoSpread.lastDirReadKey, importFile.getParent());

		XMLProcessor xmlProc = new XMLProcessor();

		try {

			xmlProc.loadXMLFile(importFile, tableModel);

		} catch (BadSheetFileContent e) {
			Misc.showErrorMsg(e.getMessage());
		} catch (FileIOException e) {
			Misc.showErrorMsg(e.getMessage());
		}

		tableModel.updateAllCells(Const.DO_EVAL);
	}
}
