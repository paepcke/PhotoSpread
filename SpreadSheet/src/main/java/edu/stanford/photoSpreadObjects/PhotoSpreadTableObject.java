/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.photoSpreadObjects;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import edu.stanford.photoSpread.PhotoSpread;
import edu.stanford.photoSpreadObjects.photoSpreadComponents.DraggablePanel;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;
import edu.stanford.photoSpreadTable.PhotoSpreadTable;
import edu.stanford.photoSpreadTable.PhotoSpreadTableModel;
import edu.stanford.photoSpreadTable.photoSpreadFormulaEditor.PhotoSpreadFormulaEditor;
import edu.stanford.photoSpreadUtilities.Const;
import edu.stanford.photoSpreadUtilities.Misc;

import edu.stanford.photoSpreadUtilities.UUID;

/**
 *
 * @author skandel
 */
public class PhotoSpreadTableObject extends PhotoSpreadObject {

	PhotoSpreadTable _table;

	/****************************************************
	 * Constructor(s)
	 *****************************************************/
	
	public PhotoSpreadTableObject(PhotoSpreadCell _cell, PhotoSpreadTable _table) {
		// Tables don't have UUIDs, so pass up null:
		super(_cell, null);
		this._table = _table;
	}

	public PhotoSpreadTableObject(PhotoSpreadCell _cell) {
		// Tables don't have UUIDs, so pass up null:
		super(_cell, null);
		this._table = new PhotoSpreadTable(new PhotoSpreadTableModel(), null);
		addSheetOrnamentation();
	}
	
	public PhotoSpreadTableObject(JFrame enclosingWindow) {
		super(null, null);
		enclosingWindow.addWindowListener(new SheetWindowListener());
		this._table = new PhotoSpreadTable(new PhotoSpreadTableModel(), enclosingWindow);
		addSheetOrnamentation();
	}
	
	private void addSheetOrnamentation() {
		_table.setBorder(BorderFactory.createLoweredBevelBorder());
		
	}
	
	
	/****************************************************
	 * Private (Inner) Classes
	 *****************************************************/
	
	class SheetWindowListener extends WindowAdapter {

		public void windowClosing(WindowEvent e) {
			Misc.exitIfUserWants("Exiting PhotoSpread. Do it?");
		}

		public void windowDeiconified(WindowEvent e) {
		}
	}
	
	/****************************************************
	 * Methods
	 *****************************************************/
	
	@Override
	public Component getObjectComponent(int height, int width) {

		JPanel tablePanel = new JPanel();
		DraggablePanel fullPanel = new DraggablePanel(this);

		PhotoSpreadFormulaEditor formulaEditor = _table.getFormulaEditor();

		// Size of the formula editor strip:
		
			formulaEditor.setMinimumSize(PhotoSpread.photoSpreadPrefs.getDimension(PhotoSpread.formulaEditorStripSizeKey));

		
		tablePanel.setLayout(new BorderLayout());
		tablePanel.setBorder(BorderFactory.createLoweredBevelBorder());


		tablePanel.add(_table.getTableHeader(), BorderLayout.PAGE_START);
		JScrollPane scrollPane = new JScrollPane(_table);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tablePanel.add(scrollPane, BorderLayout.CENTER);
		tablePanel.setVisible(true);
		
		fullPanel.setLayout(new BorderLayout());
		tablePanel.setBorder(BorderFactory.createLoweredBevelBorder());
		
		// Don't know what this size controls:
		fullPanel.setMinimumSize(new Dimension(900, 100));
		fullPanel.add(formulaEditor, BorderLayout.PAGE_START);
		fullPanel.add(tablePanel, BorderLayout.CENTER);

		return fullPanel;
	}

	@Override
	public String toXML() {
		return this._table.getPhotoSpreadModel().toXML();
	}

	@Override
	public String constructorArgsToXML() {
		return "";

	}

	public PhotoSpreadTable getTable() {
		return _table;
	}
	
	public PhotoSpreadTable valueOf() {
		return _table;
	}
	
	@Override
	public Double toDouble() throws ClassCastException {
		throw new ClassCastException("Cannot convert from a table to a number.");
	}

	@Override
	public String toString() throws ClassCastException {
		return "PhotoSpread Table in cell " + getCell().toString();
	}

	@Override
	public <T extends Object>  boolean contentEquals (T uuid) {
		return (getObjectID().equals((UUID) uuid));
	}

	public PhotoSpreadTableModel getTableModel() {
		return _table.getPhotoSpreadModel();
	}

	/**
	 * Confirm with user that they do want to clear the
	 * whole table. 
	 * @return Return true if user chose to go through with the clear, and the clear
	 * was therefore executed. Return false if user canceled.
	 */
	public Boolean clear(){
		if (JOptionPane.showConfirmDialog(_table,
											"Clear whole table? (No files will be affected)",
											"Confirm",  // Title
											JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			_table.getPhotoSpreadModel().clear();
			_table.getFormulaEditor().setText("");
			_table.getWorkspace().reset(Const.DO_REDRAW);
			_table.getWorkspace().pack();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public PhotoSpreadTableObject copyObject() {
		throw new RuntimeException("Cannot copy a PhotoSpread table.");
	}
	
}
