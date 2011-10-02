/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.photoSpreadTable;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import edu.stanford.inputOutput.InputOutput;
import edu.stanford.photoSpread.PhotoSpread;
import edu.stanford.photoSpreadObjects.PhotoSpreadTableObject;
import edu.stanford.photoSpreadObjects.photoSpreadComponents.KeyBindEditor;
import edu.stanford.photoSpreadUtilities.Const;
import edu.stanford.photoSpreadUtilities.Misc;

/**
 *
 * @author skandel
 */
public class PhotoSpreadTableMenu extends JMenuBar{

	private static final long serialVersionUID = 1L;

	String[ ] fileItems = new String[ ] {  "Exit" };

	char[ ] fileShortcuts = { 'N','O','S','X' };

	private static PhotoSpreadTableObject _tableObject;

	public PhotoSpreadTableMenu(PhotoSpreadTableObject _tableObject)  {

		PhotoSpreadTableMenu._tableObject = _tableObject;

		JMenu fileMenu = new JMenu("File");
		JMenu optionsMenu = new JMenu("Options");
		JMenu helpMenu = new JMenu("Help");
		JMenu helpSubmenu = new JMenu("Help Contents");
		
		// Assemble the File menus with mnemonics.
		
		assembleFileMenu(fileMenu);
		assembleOptionsMenu(optionsMenu);
		assembleHelpMenu(helpMenu, helpSubmenu);
		
		// Finally, add all the menus to the menu bar.
		add(fileMenu);
		add(optionsMenu);
		add(helpMenu);

	}

	private void assembleFileMenu (JMenu fileMenu) {
		
		addMenuItem(fileMenu, "New Sheet", 'N', new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				PhotoSpreadTableMenu.getTableObject().clear();
				PhotoSpreadTableMenu.getTableModel().updateAllCells(Const.DONT_EVAL);
				PhotoSpreadTableMenu.getTable().getWorkspace().redraw();
				// PhotoSpreadTableMenu.getTable().getWorkspace().pack();
			}
		});

		addMenuItem(fileMenu, "Open Sheet", 'O', new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				PhotoSpreadTableMenu.getTableObject().clear();  	   
				InputOutput.loadTable(PhotoSpreadTableMenu.this, 
						      PhotoSpreadTableMenu.getTableModel());
			}
		});

		addMenuItem(fileMenu, "Save Sheet As", 'S', new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				InputOutput.saveTable(PhotoSpreadTableMenu.this, 
						      PhotoSpreadTableMenu.getTableModel());
			}
		});

		addMenuItem(fileMenu, "Exit", 'X', new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				System.exit(0);
			}
		});

	}

	
	private void assembleOptionsMenu (JMenu optionsMenu) {
		
		addMenuItem(optionsMenu, "Edit drag-n-drop key bindings", 'D', new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				new KeyBindEditor(_tableObject.getTable());
			}
		});
	}
	
	private void assembleHelpMenu (JMenu helpMenu, JMenu helpSubmenu) {

		JMenuItem f1ReminderMenuItem = new JMenuItem(">>>F1: Help in all windows<<<");
		f1ReminderMenuItem.setEnabled(false);
		f1ReminderMenuItem.setAlignmentY(CENTER_ALIGNMENT);
		helpMenu.add(f1ReminderMenuItem);
		
		
		// Nested submenu of Help menu: help for each of the PhotoSpread windows:
		addMenuItem(helpSubmenu, "Help on this sheet window", 'S', new ActionListener () {
			public void actionPerformed(ActionEvent event) {
				new Misc.ShowHelpAction(
						"To do in Sheet Window", 
						"HelpFiles/sheetHelp.html", 
						PhotoSpread.getCurrentSheetWindow()).makeHelpPaneVisible();
			}
		});
		addMenuItem(helpSubmenu, "Help on the workspace window", 'S', new ActionListener () {
			public void actionPerformed(ActionEvent event) {
				new Misc.ShowHelpAction(
						"To do in Workspace Window", 
						"HelpFiles/workspaceHelp.html", 
						PhotoSpread.getCurrentSheetWindow()).makeHelpPaneVisible();
			}
		});
		addMenuItem(helpSubmenu, "Help on the zoomer window", 'S', new ActionListener () {
			public void actionPerformed(ActionEvent event) {
				new Misc.ShowHelpAction(
						"To do in Zoomer Window", 
						"HelpFiles/zoomerHelp.html", 
						PhotoSpread.getCurrentSheetWindow()).makeHelpPaneVisible();
			}
		});
		addMenuItem(helpSubmenu, "Help on the metadata editor window", 'S', new ActionListener () {
			public void actionPerformed(ActionEvent event) {
				new Misc.ShowHelpAction(
						"To do in Metadata Editor Window", 
						"HelpFiles/metadataEditorHelp.html", 
						PhotoSpread.getCurrentSheetWindow()).makeHelpPaneVisible();
			}
		});
		
		addMenuItem(helpSubmenu, "Help on the key bindings editor window", 'S', new ActionListener () {
			public void actionPerformed(ActionEvent event) {
				new Misc.ShowHelpAction(
						"To do in Keybindings Editor Window", 
						"HelpFiles/keyboardShortcutsHelp.html", 
						PhotoSpread.getCurrentSheetWindow()).makeHelpPaneVisible();
			}
		});
		
		helpMenu.add(helpSubmenu);
		
		addMenuItem(helpMenu, "About PhotoSpread", 'D', new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				Misc.showInfoMsg(
						"PhotoSpread Version " + PhotoSpread.version, PhotoSpread.getCurrentSheetWindow());
			}
		});
	}
	
	private void addMenuItem(JMenu menu, String menuItem, char shortCut, ActionListener actionListener){
		JMenuItem item = new JMenuItem(menuItem, shortCut);
		item.addActionListener(actionListener);
		menu.add(item);
	}

	private static PhotoSpreadTableModel getTableModel() {
		return PhotoSpreadTableMenu._tableObject.getTableModel();
	}
	
	private static PhotoSpreadTableObject getTableObject() {
		return PhotoSpreadTableMenu._tableObject;
	}
	
	private static PhotoSpreadTable getTable() {
		return PhotoSpreadTableMenu._tableObject.getTable();
	}

	
	/*   public static void main(String s[ ]) {
      JFrame frame = new JFrame("Simple Menu Example");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setJMenuBar(new PhotoSpreadTableMenu(new PhotoSpreadTableObject(null)  ));
      frame.pack(  );
      frame.setVisible(true);
   }
	 */
}
