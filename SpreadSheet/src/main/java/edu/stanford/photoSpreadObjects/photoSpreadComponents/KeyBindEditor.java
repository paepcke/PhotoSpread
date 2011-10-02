package edu.stanford.photoSpreadObjects.photoSpreadComponents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.Externalizable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

import edu.stanford.photoSpread.PhotoSpread;
import edu.stanford.photoSpread.PhotoSpreadException;
import edu.stanford.photoSpread.PhotoSpreadException.IllegalArgumentException;
import edu.stanford.photoSpread.PhotoSpreadException.KeyBindingsFileSyntaxError;
import edu.stanford.photoSpreadLoaders.XMLFileFilter;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;
import edu.stanford.photoSpreadTable.PhotoSpreadDragDropManager;
import edu.stanford.photoSpreadTable.PhotoSpreadTable;
import edu.stanford.photoSpreadTable.PhotoSpreadTableModel;
import edu.stanford.photoSpreadUtilities.CellCoordinates;
import edu.stanford.photoSpreadUtilities.ChangeManager;
import edu.stanford.photoSpreadUtilities.ComputableDimension;
import edu.stanford.photoSpreadUtilities.Const;
import edu.stanford.photoSpreadUtilities.Misc;
import edu.stanford.photoSpreadUtilities.PhotoSpreadContextMenu;
import edu.stanford.photoSpreadUtilities.Misc.ShowHelpAction;
import edu.stanford.photoSpreadUtilities.Misc.WindowCloseAction;

/**
 * @author paepcke
 *
 */

public class KeyBindEditor extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final int NUM_OF_TABLE_ROWS = 25;
	private static final int KEY_SEQUENCE_COLUMN = 0;
	private static final int CELL_ADDRESS_COLUMN = 1;
	
	private static final int ACTION_BUTTONS_HGAP = 3;

	private static final String BINDINGS_OPEN_TAG = "<bindings>\n";
	private static final String DND_BINDING_OPEN_TAG= "  <dndBindingSpec>\n";
	private static final String KEY_SPEC_OPEN_TAG = "    <keySpec>";
	private static final String CELL_SPEC_OPEN_TAG = "    <cellSpec>";

	private static final String BINDINGS_CLOSE_TAG = "</bindings>";
	private static final String DND_BINDING_CLOSE_TAG = "  </dndBindingSpec>\n";
	private static final String KEY_SPEC_CLOSE_TAG = "</keySpec>\n";
	private static final String CELL_SPEC_CLOSE_TAG = "</cellSpec>\n";

	// The following instance var is required to
	// make the context menu work, even though it's
	// never accessed:
	JPopupMenu _popup;

	ChangeManager _changeManager;
	// Name of this key bindings editor instance.
	// Invented and used for the ChangeManager
	// interaction.
	String _editorName = "BindingsEditor";
	JPanel _panel;
	KeyBindingsTable _table;
	DataTableModel _tableModel;
	PhotoSpreadTable _photoSpreadSheet = null;
	TableRowSorter<DataTableModel> _metadataSorter = null;
	PhotoSpreadContextMenu _contextMenu;
	JScrollPane _scrollPane;
	Dimension _workspacePanelSize;

	KeyBindEditor _thisWindow = null;


	/****************************************************
	 * Constructor(s)
	 *****************************************************/

	public KeyBindEditor(PhotoSpreadTable photoSpreadTable) {

		_photoSpreadSheet = photoSpreadTable;
		this.addWindowListener(new KeyBindEditorWindowListener());
		this.setTitle("PhotoSpread Key Bindings Editor");

		_thisWindow = this;
		_thisWindow.setLocationRelativeTo(PhotoSpread.getCurrentSheetWindow());
		_thisWindow.setLocationRelativeTo(_photoSpreadSheet);

		_panel = new JPanel();
		_panel.setBackground(Color.DARK_GRAY);

		// Safety: catch the 'close window operation' in
		// an event handler to warn about uncommitted changes.

		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		_scrollPane = new JScrollPane(_panel);
		_scrollPane
		.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		_scrollPane
		.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.add(_scrollPane);

		WindowCloseAction winCloseAction = new Misc().new WindowCloseAction(this);
		Misc.bindKey(this, "control W", winCloseAction);
		Misc.bindKey(this, "F1", new ShowHelpAction(
				"To do in Key Bindings Editor Window", 
				"HelpFiles/keyboardShortcutsHelp.html", 
				this));
		
		initEditor();
		pack();
		setVisible(true);
	}

	public KeyBindEditor(PhotoSpreadTable photoSpreadTable, File xmlBindingsFile) throws IOException { 
		this(photoSpreadTable);
		loadBindings(xmlBindingsFile);

	}

	/****************************************************
	 * KeyStrokeSpec Inner Class
	 *****************************************************/

	private class KeyStrokeSpec implements Externalizable {

		private static final long serialVersionUID = 1L;

		KeyStroke _keyStroke = null;
		String _keySpec = "";

		public KeyStrokeSpec (String spec) throws IllegalArgumentException {

			if (spec == null)
				return;

			if (spec.isEmpty())
				return;

			// Ensure correctness of spec, and assign a
			// normalized spec string to _keySpec:
			_keyStroke = validateKeystrokeSpec(spec);

			if (_keyStroke == null)
				throw new PhotoSpreadException.IllegalArgumentException("");
		}

		public boolean isEmpty () {
			return (_keySpec.isEmpty());
		}

		public String toString () {
			return _keySpec;
		}

		public KeyStroke getKeyStroke () {
			return _keyStroke;
		}

		/**
		 * Given a Java KeyStroke string specification,
		 * return a corresponding KeyStroke instance
		 * if the spec is correct. Else throw
		 * up a dialog to tell user; then return false.
		 * 
		 * As a side effect: assign to instance var _keySpec 
		 * a normalized version of the passed-in spec. Most
		 * importantly, this means to capitalize the character.
		 * Ex: 'control a' is turned into 'control A'. Without
		 * this change the binding will not work later on.
		 * 
		 * @param keyStrokeSpec See getKeyStroke() of class KeyStroke. Examples:
		 * "control shift a", "alt shift X"
		 * @return true if the string successfully generates a KeyStroke instance. False otherwise.
		 */

		public KeyStroke validateKeystrokeSpec(String keyStrokeSpec) {

			String modifiers = "";
			String theChar   = "";

			// Given something like "control shift x" we need to generate:
			// KeyStroke.getKeyStroke("control shift typed x"). I.e. we must
			// splice the word 'typed' before the letter:

			keyStrokeSpec.trim();

			int charSpecStart = keyStrokeSpec.lastIndexOf(KeyEvent.VK_SPACE);
			if (charSpecStart > 0) {
				modifiers = keyStrokeSpec.substring(0, charSpecStart);
				theChar   = keyStrokeSpec.substring(charSpecStart);
			} else
				theChar   = keyStrokeSpec;

			theChar = theChar.toUpperCase();

			KeyStroke testStroke = KeyStroke.getKeyStroke(
					modifiers + " typed " + theChar);

			if (testStroke == null) {
				Misc.showErrorMsg(
						"Incorrect keystroke specification '" +
						keyStrokeSpec + 
						"' . Examples: " +
						" 'control shift a';  'alt X';  'b'  ", 
						_thisWindow);
				return null;
			}
			_keySpec = modifiers.trim() + " " + theChar;

			return testStroke;
		}

		/* (non-Javadoc)
		 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
		 */
		@Override
		public void readExternal(ObjectInput in) throws IOException,
		ClassNotFoundException {

			_keySpec = in.readLine();
			_keySpec = Misc.trim(_keySpec, "<keySpec>");
			_keySpec = Misc.trim(_keySpec, "</keySpec>");
			_keyStroke = validateKeystrokeSpec(_keySpec);
		}

		/* (non-Javadoc)
		 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
		 * 
		 * Note: ObjectOutput is an interface that is implemented,
		 * for instance, by ObjectOutputStream.
		 */
		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeBytes("<keySpec>" + _keySpec + "</keySpec>");
			out.writeBytes("\n");
		}
	}

	/****************************************************
	 * CellSpec Inner Class
	 *****************************************************/

	private class CellSpec implements Externalizable {

		private static final long serialVersionUID = 1L;

		CellCoordinates _cellCoords = null;
		String _cellSpec = "";

		// Don't write the PhotoSpreadCell out during serialization;
		// we'll reconstruct it upon reading in:
		transient PhotoSpreadCell _cell = null;

		public CellSpec (String spec) throws IllegalArgumentException {

			if (spec == null)
				return;

			if (spec.isEmpty())
				return;

			_cell = validateCellSpec(spec);
			if (_cell == null)
				throw new PhotoSpreadException.IllegalArgumentException("");
		}

		public boolean isEmpty () {
			return (_cellSpec.isEmpty());
		}

		public PhotoSpreadCell getCell () {
			return _cell;
		}

		public CellCoordinates getCellCoordinates () {
			return _cellCoords;
		}

		public String toString () {
			return _cellSpec;
		}

		/**
		 * Given an Excel column name specification,
		 * return true if the spec is correct. Else throw
		 * up a dialog to tell user; then return false.
		 * 
		 * As a side effect, assign a normalized version
		 * of the passed-in cell address to the instance
		 * variable _cellSpec. The normalized version is
		 * space-trimmed and capitalized.
		 * 
		 * @param cellSpec Examples: "C1", "AB390"
		 * @return true if the string successfully identifies a PhotoSpreadCell instance. False otherwise.
		 */

		public PhotoSpreadCell validateCellSpec(String cellSpec) {

			PhotoSpreadCell cell = null;

			cellSpec.trim();
			cellSpec = cellSpec.toUpperCase();

			CellCoordinates cellCoords = Misc.getCellAddress(cellSpec);

			if (cellCoords != null) {
				cell = _photoSpreadSheet.getCell(cellCoords);
			}

			if ((cellCoords == null) ||
					(cell == null)) {
				Misc.showErrorMsg(
						"Incorrect destination cell specification '" +
						cellSpec + 
						"' . Must specify an *existing* cell. Examples: " +
						" 'C3';  'Ab243' (assuming you have that many cells)", 
						_thisWindow);
				return null;
			}

			_cellSpec = cellSpec;
			return cell; 
		}

		// Take care of serialization:

		/*		private void writeObject (ObjectOutputStream out) throws IOException {
			// Nothing special to do on output: the photospread cell won't be written:
			out.defaultWriteObject();
		}

		private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
			in.defaultReadObject();
			validateCellSpec(_cellSpec);
		}

		 */		/* (non-Javadoc)
		 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
		 */
		@Override
		public void readExternal(ObjectInput in) throws IOException,
		ClassNotFoundException {

			_cellSpec = in.readLine();
			_cellSpec = Misc.trim(_cellSpec, "<cellSpec>");
			_cellSpec = Misc.trim(_cellSpec, "</cellSpec>");
			_cell = validateCellSpec(_cellSpec);
		}

		/* (non-Javadoc)
		 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
		 */
		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeBytes("<cellSpec>" + _cellSpec + "</cellSpec>");
			out.writeBytes("\n");
		}
	}

	/****************************************************
	 * DnDKeyBindSpec Inner Class
	 *****************************************************/

	private class DnDKeyBindSpec {

		KeyStroke _keyStroke = null;
		KeyStrokeSpec _keySpec = null;
		CellSpec _cellSpec = null;

		public DnDKeyBindSpec (KeyStrokeSpec keySpec, CellSpec cellSpec) {
			_keySpec = keySpec;
			_keyStroke = keySpec.getKeyStroke();
			_cellSpec = cellSpec;
		}

		public DnDKeyBindSpec (String keySpec, String cellSpec) throws IllegalArgumentException {
			setBindingSpec(keySpec, cellSpec);
		}

		public boolean isEmpty () {
			return (_keySpec.isEmpty() && _cellSpec.isEmpty());
		}

		@SuppressWarnings("unused")
		public KeyStroke getKeyStroke () { 
			return _keyStroke;
		}

		public CellCoordinates getDestinationCellCoordinates () {
			if (_cellSpec == null) 
				return null;
			return _cellSpec.getCellCoordinates();
		}

		public KeyStrokeSpec getKeySpec() { 
			return _keySpec;
		}

		public CellSpec getCellSpec () {
			return _cellSpec;
		}

		@SuppressWarnings("unused")
		public PhotoSpreadCell getDestinationCell () {
			return _photoSpreadSheet.getCell(getDestinationCellCoordinates());
		}

		public void setBindingSpec (String keySpec, String cellSpec) throws IllegalArgumentException {

			_keySpec = new KeyStrokeSpec(keySpec);
			_keyStroke = _keySpec.getKeyStroke();
			_cellSpec = new CellSpec(cellSpec);
		}

		public void setKeySpec (String keySpec) throws IllegalArgumentException {
			set (new KeyStrokeSpec(keySpec));
		}

		public void setCellSpec (String cellSpec) throws IllegalArgumentException {
			set (new CellSpec(cellSpec));
		}

		public void set (KeyStrokeSpec keySpec) {
			_keySpec = keySpec;
			_keyStroke = _keySpec.getKeyStroke();
		}

		public void set (CellSpec cellSpec) {
			_cellSpec= cellSpec;
		}


		@SuppressWarnings("unused")
		public void writeExternal(ObjectOutput out) throws IOException {

			out.writeBytes("<dndKeyBindSpec>\n");
			_keySpec.writeExternal(out);
			_cellSpec.writeExternal(out);
			out.writeBytes("</dndKeyBindSpec>\n");
		}
	}


	/****************************************************
	 * KeyBindEditorWindowListener Inner Class
	 *****************************************************/

	class KeyBindEditorWindowListener extends WindowAdapter {

		public void windowClosing(WindowEvent e) {
			if (JOptionPane.showConfirmDialog(_thisWindow, // make dialog appear within the editor window,
					// not at screen center.
					"Discard changes?", "Confirm", // Title
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

				// Unregister from the ChangeManager:
				_tableModel.dispose();
				dispose();
			}
		}

		public void windowDeiconified(WindowEvent e) {
		}
	}

	/****************************************************
	 * Listeners: KeyBindKeyListener Inner Class
	 *****************************************************/

	class KeyBindKeyListener extends KeyAdapter {

		public void keyPressed(KeyEvent e) {

			if (e.getID() == KeyEvent.KEY_PRESSED) {

				// Get indices of first and last currently selected rows:
				int firstSelectedRow = _table.getSelectionModel()
				.getAnchorSelectionIndex();
				int lastSelectedRow = _table.getSelectionModel()
				.getLeadSelectionIndex();

				int currRow = getRow();
				char typedChar = e.getKeyChar();

				switch (typedChar) {

				case KeyEvent.VK_DELETE: // Main or Numpad delete key

					// NumPad Delete key?
					if (e.getKeyLocation() == KeyEvent.KEY_LOCATION_NUMPAD)
						_tableModel.removeRow(firstSelectedRow, lastSelectedRow);
					else {
						// Main keyboard Delete key:

						// Clear current row (i.e. make it empty; leave row in
						// place):

						_tableModel.clearRow(firstSelectedRow, lastSelectedRow);

						// Put edit focus to first of the cleared rows:
						int selectionUpMove = lastSelectedRow - firstSelectedRow;
						for (; selectionUpMove > 0; selectionUpMove--)
							_table.transferFocusBackward();
					}

					// NOTE1: This next statement ensures that
					// repainting of the table is completed
					// correctly. Without it, the cell with
					// focus will retain its old value (Swing
					// bug as far as I'm concerned!):

					_table.editingStopped(new ChangeEvent(this));
					break;

				case KeyEvent.VK_ENTER:

					// If we don't consume this key event, the default
					// JTable behavior interferes:

					e.consume();

					// _table.getSelectionModel().clearSelection();

					if (currRow >= _tableModel.getRowCount() - 1) {
						_tableModel.appendRow();
						_table.editingStopped(new ChangeEvent(this));
					}
					int selectedRowNum = _table.getSelectedRow();
					int selectedColNum = _table.getSelectedColumn();

					if (selectedColNum >= _table.getColumnCount() - 1)
						_table.changeSelection(
								selectedRowNum + 1,            // Select next row, ... 
								0, 					           // ... column 0
								Const.DONT_TOGGLE_SELECTION,   // Set selection there, don't toggle it
								Const.DONT_EXTEND_SELECTION);  // Clear the previous selection
					else
						_table.changeSelection(
								currRow,                	   // Select same row, ... 
								selectedColNum + 1,	           // ... next column
								Const.DONT_TOGGLE_SELECTION,   // Set selection there, don't toggle it
								Const.DONT_EXTEND_SELECTION);  // Clear the previous selection

					_table.editCellAt(_table.getSelectedRow(), _table.getSelectedColumn());

					break;

				case KeyEvent.VK_INSERT:
					_tableModel.addRowBefore(currRow);
					// see NOTE1 above on need for following statement:
					_table.editingStopped(new ChangeEvent(this));

				default:
					break;
				}
			}
		}

		public void keyTyped(KeyEvent e) {

			if (e.getID() == KeyEvent.KEY_TYPED) {

				char typedChar = e.getKeyChar();

				switch (typedChar) {
				// This branch, intended for cnt-s, unfortunately never
				// gets control:
				case KeyEvent.VK_S:

					if (e.getModifiersEx() == KeyEvent.VK_CONTROL)
						return;
					break;

				default:
					break;
				}
			}
		}

		private int getRow() {
			// Which row is in focus?
			int currRow = _table.getEditingRow();
			if (currRow == -1)
				currRow = _table.getSelectedRow();
			if (currRow == -1)
				currRow = 0;
			return currRow;
		}
	} // end class ObjectPanelKeyListener


	/****************************************************
	 * Action Listener: SaveButtonListener Inner Class
	 *****************************************************/

	class SaveButtonListener implements ActionListener {

		KeyBindEditor _theEditor = null;

		public SaveButtonListener(KeyBindEditor theEditor) {
			_theEditor = theEditor;
		}

		public void actionPerformed(ActionEvent e) {

			File exportFile = null; 

			// Misc.showErrorMsg("Saving of key bindings not yet implemented.", _thisWindow);

			String priorWriteDir = PhotoSpread.photoSpreadPrefs.getProperty(PhotoSpread.lastDirWrittenKey);
			final JFileChooser fc = new JFileChooser(priorWriteDir);

			XMLFileFilter filter = new XMLFileFilter();
			fc.setFileFilter(filter);		

			fc.setMultiSelectionEnabled(false);
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

			int returnVal = fc.showSaveDialog(_thisWindow);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				exportFile = fc.getSelectedFile();
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
			}
			else return;

			try {
				_theEditor.saveBindings(exportFile);
			} catch (IOException e1) {
				Misc.showErrorMsg("Failed to save bindings: " + e1.getMessage());
			}
		}
	}


	/****************************************************
	 * Action Listener: LoadButtonListener Inner Class
	 *****************************************************/

	class LoadButtonListener implements ActionListener {

		KeyBindEditor _theEditor = null;

		public LoadButtonListener(KeyBindEditor theEditor) {
			_theEditor = theEditor;
		}

		public void actionPerformed(ActionEvent e) {

			File importFile = null; 

			String priorReadDir = PhotoSpread.photoSpreadPrefs.getProperty(PhotoSpread.lastDirReadKey);
			final JFileChooser fc = new JFileChooser(priorReadDir);

			XMLFileFilter filter = new XMLFileFilter();
			fc.setFileFilter(filter);		

			fc.setMultiSelectionEnabled(false);
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

			int returnVal = fc.showOpenDialog(_thisWindow);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				importFile = fc.getSelectedFile();
				PhotoSpread.photoSpreadPrefs.setProperty(PhotoSpread.lastDirReadKey, importFile.getParent());

				// Make sure file gets a .xml extension:
				try {
					importFile = new File (Misc.ensureFileExtension(importFile.getPath(), "xml"));
				} catch (java.text.ParseException e1) {
					// Exception when a directory is passed into ensureFileExtension
					// GUI file chooser prevents that.
					e1.printStackTrace();
				}
			}

			try {
				_theEditor.loadBindings(importFile);
			} catch (IOException e1) {
				Misc.showErrorMsg("Failed to save bindings: " + e1.getMessage());
			}
		}
	}

	/****************************************************
	 * Action Listener: SubmitButtonListener Inner Class
	 *****************************************************/

	class SubmitButtonListener implements ActionListener {

		public SubmitButtonListener() {
		}

		public void actionPerformed(ActionEvent e) {
			// Misc.showErrorMsg("Saving of key bindings not yet implemented.", _thisWindow);
			submitChanges();
			return;
		}
	}

	/****************************************************
	 * Action Listener: CancelButtonListener Inner Class
	 *****************************************************/

	class CancelButtonListener implements ActionListener {

		public CancelButtonListener() {
		}

		public void actionPerformed(ActionEvent e) {
			// Just close the editor window:
			ChangeManager.unregisterClient(_editorName);
			dispose();
		}
	}



	/****************************************************
	 * Methods
	 * @throws IOException 
	 *****************************************************/

	public void saveBindings (File xmlFile) throws IOException {

		BufferedWriter writeFD = null;

		try {
			writeFD = new BufferedWriter(new FileWriter(xmlFile));


			writeFD.write(BINDINGS_OPEN_TAG);

			for (DnDKeyBindSpec dndSpec : _tableModel.getData()) {

				if (dndSpec.isEmpty())
					continue;

				writeFD.write(DND_BINDING_OPEN_TAG);

				writeFD.write(KEY_SPEC_OPEN_TAG);
				writeFD.write(dndSpec.getKeySpec().toString());
				writeFD.write(KEY_SPEC_CLOSE_TAG);

				writeFD.write(CELL_SPEC_OPEN_TAG);
				writeFD.write(dndSpec.getCellSpec().toString());
				writeFD.write(CELL_SPEC_CLOSE_TAG);

				writeFD.write(DND_BINDING_CLOSE_TAG);
			}

			writeFD.write(BINDINGS_CLOSE_TAG);

		} catch (Exception e) {
			Misc.showErrorMsg(
					"Cannot save key bindings: " +
					e.getMessage(), 
					_thisWindow); // window to show error msg in
		} finally {
			if (writeFD != null)
				writeFD.close();
		}
	}

	public void loadBindings (File xmlFile) throws IOException {

		BufferedReader readFD = null;
		boolean gotBinding = false;

		try {

			_tableModel.removeAllRows();

			readFD = new BufferedReader(new FileReader(xmlFile));

			String bindingsTag = readFD.readLine().trim();

			if (! bindingsTag.equals(BINDINGS_OPEN_TAG.trim()))
				throw new PhotoSpreadException.KeyBindingsFileSyntaxError(
						"Bindings file '" +
						xmlFile.getAbsolutePath() +
						"' does not begin with '" +
						BINDINGS_OPEN_TAG +
						"'. Begins with '" +
						bindingsTag +
				"' instead.");
			do {
				gotBinding = restoreBinding(readFD);
			} while (gotBinding);

		} catch (Exception e) {
			Misc.showErrorMsg(
					"Cannot load key bindings: " +
					e.getMessage(), 
					_thisWindow); // window to show error msg in
		} finally {
			if (readFD != null)
				readFD.close();
		}

		_tableModel.ensureMinRows();
	}

	public boolean restoreBinding(BufferedReader in) 
	throws IOException, KeyBindingsFileSyntaxError, IllegalArgumentException {


		try {
			String dndBindingTag = in.readLine().trim();

			if (! dndBindingTag.equals(DND_BINDING_OPEN_TAG.trim())) {

				// Reached end of the <bindings> body? 
				if (dndBindingTag.equals(BINDINGS_CLOSE_TAG.trim()))
					return false;

				throw new PhotoSpreadException.KeyBindingsFileSyntaxError(
						"Expected tag '" +
						DND_BINDING_OPEN_TAG +
						"'. Got '" +
						dndBindingTag +
				"' instead.");

			}
			KeyStrokeSpec keySpec   = readKeyStrokeSpecObject(in);
			CellSpec cellSpec = readCellSpecObject(in);

			_tableModel.appendRow(new DnDKeyBindSpec(keySpec, cellSpec));

			// Suck in the closing tag:
			dndBindingTag = in.readLine().trim();

			if (! dndBindingTag.equals(DND_BINDING_CLOSE_TAG.trim()))

				throw new PhotoSpreadException.KeyBindingsFileSyntaxError (
						"Expected tag '" +
						DND_BINDING_CLOSE_TAG +
						"'. Got '" +
						dndBindingTag +
				"' instead.");
		} catch (EOFException e) {
			return false;
		}
		return true;
	}

	public KeyStrokeSpec readKeyStrokeSpecObject(BufferedReader in) 
	throws IOException, KeyBindingsFileSyntaxError, IllegalArgumentException {

		String keySpec = "";

		try {
			keySpec = in.readLine().trim();

			if (! keySpec.startsWith(KEY_SPEC_OPEN_TAG.trim()))

				throw new PhotoSpreadException.KeyBindingsFileSyntaxError(
						"Expected tag '" +
						KEY_SPEC_OPEN_TAG +
						"'. Got '" +
						keySpec+
				"' instead.");

			if (! keySpec.endsWith(KEY_SPEC_CLOSE_TAG.trim()))

				throw new PhotoSpreadException.KeyBindingsFileSyntaxError (
						"Expected tag '" +
						KEY_SPEC_CLOSE_TAG +
						"'. Got '" +
						keySpec +
				"' instead.");

			int endPos = keySpec.indexOf(KEY_SPEC_CLOSE_TAG.trim()); 

			keySpec = keySpec.substring(KEY_SPEC_OPEN_TAG.trim().length(), endPos);


		} catch (EOFException e) {
			return null;
		}
		return new KeyStrokeSpec(keySpec);
	}

	public CellSpec readCellSpecObject(BufferedReader in) 
	throws IOException, KeyBindingsFileSyntaxError, IllegalArgumentException {

		String cellSpec = "";

		try {
			cellSpec = in.readLine().trim();

			if (! cellSpec.startsWith(CELL_SPEC_OPEN_TAG.trim()))

				throw new PhotoSpreadException.KeyBindingsFileSyntaxError(
						"Expected tag '" +
						CELL_SPEC_OPEN_TAG +
						"'. Got '" +
						cellSpec +
				"' instead.");

			if (! cellSpec.endsWith(CELL_SPEC_CLOSE_TAG.trim()))

				throw new PhotoSpreadException.KeyBindingsFileSyntaxError (
						"Expected tag '" +
						CELL_SPEC_CLOSE_TAG +
						"'. Got '" +
						cellSpec +
				"' instead.");

			int endPos = cellSpec.indexOf(CELL_SPEC_CLOSE_TAG.trim()); 

			cellSpec = cellSpec.substring(CELL_SPEC_OPEN_TAG.trim().length(), endPos);

		} catch (EOFException e) {
			return null;
		}
		return new CellSpec(cellSpec);
	}


	public void selectAll() {
		if (_table != null)
			_table.selectAll();
	}

	private void initEditor() {

		_panel.setLayout(new BorderLayout());

		JButton cancelButton = new JButton("Cancel");
		cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		JButton submitButton = new JButton("Apply Bindings");
		submitButton.setAlignmentX(CENTER_ALIGNMENT);

		JButton saveButton = new JButton("Save Bindings");
		saveButton.setAlignmentX(CENTER_ALIGNMENT);

		JButton loadButton = new JButton("Load Bindings");
		cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);


		cancelButton.addActionListener(new CancelButtonListener());
		submitButton.addActionListener(new SubmitButtonListener());
		saveButton.addActionListener(new SaveButtonListener(this));
		loadButton.addActionListener(new LoadButtonListener(this));

		// Panel for the submit/cancel buttons:
		JPanel buttonsPanel = new JPanel();

		// Put each button into its own horizontal box
		// with horizontal glue on each of its sides. 
		// This will make the buttons expand to the 
		// same size:

		JPanel submitButtonPanel = new JPanel();
		BoxLayout saveAndExitButtonLayout = new BoxLayout(submitButtonPanel, BoxLayout.LINE_AXIS);
		submitButtonPanel.setLayout(saveAndExitButtonLayout);
		submitButtonPanel.add(Box.createHorizontalGlue());
		submitButtonPanel.add(submitButton);
		submitButtonPanel.add(Box.createHorizontalGlue());

		JPanel cancelButtonPanel = new JPanel();
		BoxLayout cancelButtonLayout = new BoxLayout(cancelButtonPanel, BoxLayout.LINE_AXIS);
		cancelButtonPanel.setLayout(cancelButtonLayout);
		cancelButtonPanel.add(Box.createHorizontalGlue());
		cancelButtonPanel.add(cancelButton);
		cancelButtonPanel.add(Box.createHorizontalGlue());

		JPanel saveButtonPanel = new JPanel();
		BoxLayout saveButtonLayout = new BoxLayout(saveButtonPanel, BoxLayout.LINE_AXIS);
		saveButtonPanel.setLayout(saveButtonLayout);
		saveButtonPanel.add(Box.createHorizontalGlue());
		saveButtonPanel.add(saveButton);
		saveButtonPanel.add(Box.createHorizontalGlue());

		JPanel loadButtonPanel = new JPanel();
		BoxLayout loadButtonLayout = new BoxLayout(loadButtonPanel, BoxLayout.LINE_AXIS);
		loadButtonPanel.setLayout(loadButtonLayout);
		loadButtonPanel.add(Box.createHorizontalGlue());
		loadButtonPanel.add(loadButton);
		loadButtonPanel.add(Box.createHorizontalGlue());

		// Make all buttons as wide and high as the widest/highest among them:

		Dimension preferredDims[] = { 
				saveButton.getPreferredSize(),
				loadButton.getPreferredSize(),
				submitButton.getPreferredSize(),
				cancelButton.getPreferredSize() };

		Dimension maxPreferredDim = ComputableDimension.maxWidthHeight(preferredDims);
		submitButton.setPreferredSize(maxPreferredDim);
		cancelButton.setPreferredSize(maxPreferredDim);
		saveButton.setPreferredSize(maxPreferredDim);
		loadButton.setPreferredSize(maxPreferredDim);

		BoxLayout buttonLayout = new BoxLayout(buttonsPanel, BoxLayout.X_AXIS);
		buttonsPanel.setLayout(buttonLayout);
		buttonsPanel.setAlignmentX(CENTER_ALIGNMENT);
		buttonsPanel.setBorder(BorderFactory.createLoweredBevelBorder());

		buttonsPanel.setBackground(Color.LIGHT_GRAY);

		// Make the buttons flush right:
		buttonsPanel.add(Box.createHorizontalGlue());

		buttonsPanel.add(cancelButton);

		// Space between the buttons:
		buttonsPanel.add(Box.createRigidArea(new Dimension(0,
				ACTION_BUTTONS_HGAP)));

		buttonsPanel.add(saveButton);

		// Space between the buttons:
		buttonsPanel.add(Box.createRigidArea(new Dimension(0,
				ACTION_BUTTONS_HGAP)));

		buttonsPanel.add(loadButton);

		// Space between the buttons:
		buttonsPanel.add(Box.createRigidArea(new Dimension(0,
				ACTION_BUTTONS_HGAP)));

		buttonsPanel.add(submitButton);

		// Create TablePanel for Table and Column Header 
		JPanel tablePanel = new JPanel();
		BoxLayout tableAndHeaderLayout = new BoxLayout(tablePanel,
				BoxLayout.PAGE_AXIS);
		tablePanel.setLayout(tableAndHeaderLayout);

		// The table of metadata attributes and values:

		_tableModel = new DataTableModel(getExistingBindings());

		_table = new KeyBindingsTable(_tableModel);

		addContextMenu();

		_table.setBackground(Const.metaDataEditorBackGroundColor);
		_table.setForeground(Const.metaDataEditorForeGroundColor);
		_table.setSelectionForeground(Color.gray);
		_table.setFont(Const.TABLE_FONT);

		_table.addKeyListener(new KeyBindKeyListener());

		_table.setVisible(true);
		_table.setMinimumSize(new Dimension(100, 100));

		tablePanel.add(_table.getTableHeader());
		tablePanel.add(_table);

		this.setBackground(Color.darkGray);

		// Attr/value table header and body:
		_panel.add(tablePanel, BorderLayout.CENTER);

		// Add control buttons at bottom:
		_panel.add(buttonsPanel, BorderLayout.SOUTH);


		JLabel title = new JLabel("<HTML><H1>Set Drag-Drop Key Shortcuts</H1></HTML>");

		title.setBackground(Color.LIGHT_GRAY);
		JPanel titlePanel = new JPanel();

		titlePanel.add(title);

		_panel.add(titlePanel, BorderLayout.NORTH);

		_panel.validate();
	}
	
	private ArrayList<DnDKeyBindSpec> getExistingBindings () {
		
		ActionMap actMap = _photoSpreadSheet.getActionMap();
		Object [] actionKeys = actMap.allKeys();
		ArrayList<DnDKeyBindSpec> dndBindings = new ArrayList<DnDKeyBindSpec>();
		Action theAction = null;
		
		for (Object key : actionKeys) {
			
			theAction = actMap.get(key);
			
			// Is the action one of our dnd actions?
			if (! (theAction instanceof AutoDnDAction))
				continue;
			
			try {
				dndBindings.add(new DnDKeyBindSpec ((String) key, ((AutoDnDAction) theAction).getDestinationCell().getCellAddress()));
			} catch (IllegalArgumentException e) {
				Misc.showErrorMsgAndStackTrace(e, "");
				// e.printStackTrace();
			}
		}
		return dndBindings;
	}

	private void submitChanges() {

		AutoDnDAction autoDnDAction = null;
		int count = 0;

		for (DnDKeyBindSpec rowData : _tableModel.getData()) {

			if (rowData.getCellSpec().toString().isEmpty() ||
					rowData.getKeySpec().toString().isEmpty())
				continue;

			autoDnDAction = new AutoDnDAction(rowData.getCellSpec().getCell());
			Misc.bindKey(
					_photoSpreadSheet, 
					rowData.getKeySpec().toString(), 
					autoDnDAction);
			Misc.bindKey(
					_photoSpreadSheet.getWorkspace(), 
					rowData.getKeySpec().toString(), 
					autoDnDAction);
			count++;
		}

		boolean quitNow = Misc.showConfirmMsg(
				"Bound " +
				count +
				" key(s). Exit bindings editor without saving bindings to a file?", 
				_thisWindow);

		if (quitNow) {
			ChangeManager.unregisterClient(_editorName);
			dispose();
		}
	}



	/****************************************************
	 * AutoDnDAction Action
	 *****************************************************/

	class AutoDnDAction extends AbstractAction {

		private static final long serialVersionUID = 1L;
		PhotoSpreadCell _destCell = null;

		public AutoDnDAction (PhotoSpreadCell destCell) {
			_destCell = destCell;
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			PhotoSpreadCell srcCell = _photoSpreadSheet.getWorkspace().getDisplayedCell();
			if (_destCell.equals(srcCell)) {
				Misc.showErrorMsg(
						"Attempt to drag from/to same cell: " +
						srcCell.getCellAddress() +
				". Will ignore.");
				return;
			}
			PhotoSpreadDragDropManager.setSourceCell(srcCell);
			PhotoSpreadDragDropManager.setDestCell(_destCell);

			PhotoSpreadDragDropManager.executeDragDrop();

			_photoSpreadSheet.getTableModel().fireTableCellUpdated(_destCell.getRow(), _destCell.getColumn());

			if (srcCell == _photoSpreadSheet.getWorkspace().getDisplayedCell())
				_photoSpreadSheet.getWorkspace().redraw();

			/*			//********
			Misc.showConfirmMsg(
					"Would drag from " +
					srcCell.getCellAddress() +
					" to " +
					_destCell.getCellAddress() +
			".");
			 */		
		}
		
		public PhotoSpreadCell getDestinationCell () {
			return _destCell;
		}
	}

	/****************************************************
	 * Context Menu Actions
	 *****************************************************/
	private void addContextMenu(){


		_contextMenu = new PhotoSpreadContextMenu();
		_contextMenu.addMenuItem("Add row above", new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				_tableModel.addRowBefore();
				// see NOTE1 above on need for following statement:
				_table.editingStopped(new ChangeEvent(this));
			}
		} 
		);

		_contextMenu.addMenuItem("Add row below", new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				_tableModel.addRowAfter();
				// see NOTE1 above on need for following statement:
				_table.editingStopped(new ChangeEvent(this));
			}
		} 
		);

		_contextMenu.addMenuItem("Clear row [DELETE]", new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				_tableModel.clearRow();
				// see NOTE1 above on need for following statement:
				_table.editingStopped(new ChangeEvent(this));
			}
		} 
		);

		_contextMenu.addMenuItem("Delete row [numpad-DEL]", new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				_tableModel.removeRow();
				// see NOTE1 above on need for following statement:
				_table.editingStopped(new ChangeEvent(this));
			}
		} 
		);

		_contextMenu.addMenuItem("Clear all", new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				_tableModel.clear();
				// see NOTE1 above on need for following statement:
				_table.editingStopped(new ChangeEvent(this));
			}
		} 
		);

		_contextMenu.addMenuItem("Select All",new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				selectAll();
			}
		} 
		);

		_table.addMouseListener(_contextMenu.getPopupListener());
	}

	/****************************************************
	 * KeyBindingsTable Inner Class
	 *****************************************************/

	/**
	 * We subclass JTable purely to do our own tooltips
	 * for each column.
	 *
	 */
	class KeyBindingsTable extends JTable {
		
		private static final long serialVersionUID = 1L;

		public KeyBindingsTable(DataTableModel model) {
			super(model);
		}

		/**
		 * Provide examples via tooltips, depending
		 * on which column the mouse is over.
		 * 
		 * @see javax.swing.JTable#getToolTipText(java.awt.event.MouseEvent)
		 */
		public String getToolTipText(MouseEvent e) {

			String tip = null;
			java.awt.Point p = e.getPoint();
			int colIndex = columnAtPoint(p);
			int realColumnIndex = convertColumnIndexToModel(colIndex);

			if (realColumnIndex == KEY_SEQUENCE_COLUMN) {
				
				tip = "Examples: 'control x', 'alt shift Y', ... ";

			} else if (realColumnIndex == CELL_ADDRESS_COLUMN) {
				
				tip = "Examples: 'C2', 'a4', ..."; 
			}
			return tip;
		}
	}

	/****************************************************
	 * DataTableModel Inner Class
	 *****************************************************/


	class DataTableModel extends AbstractTableModel {

		public static final int ATTR_NAME_COL = 0;
		public static final int VALUE_COL = 1;

		private static final long serialVersionUID = 1L;

		private String[] columnNames = { "<HTML><H3>Key Sequence</H3></HTML>", "<HTML><H3>Cell Address</H3></HTML>" };
		private ArrayList<DnDKeyBindSpec> data;

		public DataTableModel() {

			data = new ArrayList<DnDKeyBindSpec>();

			// Add some empty rows:
			ensureMinRows();
		}

		public DataTableModel (ArrayList<DnDKeyBindSpec> initialData) {
			data = initialData;
			ensureMinRows();
		}
		
		public DataTableModel(HashMap<KeyStrokeSpec, CellSpec> keyStrokeToCellSpecMap) {
			for (KeyStrokeSpec keySpec : keyStrokeToCellSpecMap.keySet()) {
				data.add(new DnDKeyBindSpec(keySpec, keyStrokeToCellSpecMap.get(keySpec)));
			}
			ensureMinRows();
		}


		/**
		 * Make sure that at least a set minimum of
		 * rows are available in the table.
		 */
		private void ensureMinRows () {

			if (data.size() >= NUM_OF_TABLE_ROWS)
				return;

			int emptyRowsToAdd = NUM_OF_TABLE_ROWS - data.size();
			for (int i = 0; i < emptyRowsToAdd; i++) {
				data.add(makeEmptyRow());
			}
		}

		public void dispose() {

		}

		public Class<?> getColumnClass(int columnIndex) {

			return String.class;
		}

		public Comparator<?> getComparator (int col) {
			return null; // _metadataComparator;
		}

		private DnDKeyBindSpec makeEmptyRow() {
			DnDKeyBindSpec newRow = null;
			try {
				newRow = new DnDKeyBindSpec(new KeyStrokeSpec(""), new CellSpec(""));
			} catch (IllegalArgumentException e) {
				// 
			}
			return newRow;
		}

		public void addRowBefore() {

			// Get indices of first and last currently selected rows:
			int firstSelectedRow = _table.getSelectionModel()
			.getAnchorSelectionIndex();

			addRowBefore(firstSelectedRow);
		}

		public void addRowBefore(int rowNum) {

			int[] rowSpecs = {rowNum};
			if (!areTableCoordsLegal(rowSpecs)) return;

			data.add(rowNum, makeEmptyRow());
			ChangeManager.markDirty(_editorName);
			fireTableRowsInserted(rowNum, rowNum);
		}

		public void addRowAfter () {

			int firstSelectedRow = _table.getSelectionModel()
			.getAnchorSelectionIndex();

			if (firstSelectedRow == getRowCount() - 1)
				appendRow();
			else
				addRowBefore(firstSelectedRow + 1);
		}


		public void appendRow() {
			int newRowIndex = getRowCount();
			data.add(makeEmptyRow());
			ChangeManager.markDirty(_editorName);
			fireTableRowsInserted(newRowIndex, newRowIndex);
		}

		public void appendRow (DnDKeyBindSpec rowData) {
			int newRowIndex = getRowCount();
			data.add(rowData);
			ChangeManager.markDirty(_editorName);
			fireTableRowsInserted(newRowIndex, newRowIndex);
		}

		public void removeAllRows() {
			removeRow(0, data.size() - 1);
		}

		public void removeRow() {

			// Get indices of first and last currently selected rows:
			int firstSelectedRow = _table.getSelectionModel()
			.getAnchorSelectionIndex();

			removeRow(firstSelectedRow);
		}

		public void removeRow(int rowNum) {
			removeRow(rowNum, rowNum);
		}

		public void removeRow(int firstRowNum, int lastRowNum) {

			int[] rowSpecs = { firstRowNum, lastRowNum };
			if (!areTableCoordsLegal(rowSpecs))
				return;

			// Delete rows backwards so we don't remove
			// them under our butt:

			for (int rowNum = lastRowNum; rowNum >= firstRowNum; rowNum--)
				data.remove(rowNum);

			ChangeManager.markDirty(_editorName);
			fireTableRowsDeleted(firstRowNum, lastRowNum);
		}

		public void clear () {
			clearRow(0, data.size() - 1);
		}

		public void clearRow() {

			// Get indices of first and last currently selected rows:
			int firstSelectedRow = _table.getSelectionModel()
			.getAnchorSelectionIndex();

			clearRow(firstSelectedRow);
		}

		public void clearRow(int rowNum) {
			clearRow(rowNum, rowNum);
		}

		public void clearRow(int firstRowNum, int lastRowNum) {

			int[] rowSpecs = { firstRowNum, lastRowNum };
			if (!areTableCoordsLegal(rowSpecs))
				return;

			DnDKeyBindSpec rowToClear;

			for (int rowNum = firstRowNum; rowNum <= lastRowNum; rowNum++) {
				rowToClear = data.get(rowNum);
				try {
					rowToClear.setBindingSpec("", "");
				} catch (IllegalArgumentException e) {
					// Two empty strings are fine.
				}
			}
			ChangeManager.markDirty(_editorName);
			fireTableRowsUpdated(firstRowNum, lastRowNum);
		}

		public void clearCell(int rowNum, int colNum) {

			int[] rowSpecs = { rowNum };
			int[] colSpecs = { colNum };
			if (!areTableCoordsLegal(rowSpecs, colSpecs))
				return;

			// setValueAt() will fire the update event:
			setValueAt("", rowNum, colNum);
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return true;
		}

		@Override
		public String getColumnName(int col) {
			return columnNames[col];
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return data.size();
		}

		public Object getValueAt(int row, int col) {

			DnDKeyBindSpec rowData = data.get(row);
			if (rowData == null)
				return "";

			switch (col) {
			case 0:
				return rowData.getKeySpec().toString();
			case 1:
				return rowData.getCellSpec().toString();
			default:
				return "";
			}
		}

		public void setValueAt(Object value, int rowNum, int colNum) {

			if ((rowNum >= getRowCount()) || (rowNum < 0))
				return;
			if ((colNum >= getColumnCount()) || (colNum < 0))
				return;

			DnDKeyBindSpec rowData = data.get(rowNum);

			try {
				switch (colNum) {
				case 0:
					rowData.setKeySpec((String) value);
					break;
				case 1:
					rowData.setCellSpec((String) value);
					break;
				default:
					break;
				}
			} catch (PhotoSpreadException IllegalArgumentException) {
				// We are not allowed to throw an error, else
				// the JTable contract is violated.
			}
			ChangeManager.markDirty(_editorName);
			fireTableCellUpdated(rowNum, colNum);
		}

		public ArrayList<DnDKeyBindSpec> getData() {
			return data;
		}

		private boolean areTableCoordsLegal(int[] rows) {
			for (int rowNum : rows) {
				if ((rowNum >= getRowCount()) || (rowNum < 0))
					return false;
			}
			return true;
		}

		private boolean areTableCoordsLegal(int[] rows, int[] cols) {
			for (int rowNum : rows) {
				if ((rowNum >= getRowCount()) || (rowNum < 0))
					return false;
			}
			for (int colNum : rows) {
				if ((colNum >= columnNames.length) || (colNum < 0))
					return false;
			}
			return true;
		}
	}

	/****************************************************
	 * Main and/or Testing Methods
	 *****************************************************/

	// Key strings used for the preferences properties data structure:

	public static final String prefsFileKey = "prefsFile";
	public static final String csvFieldDelimiterKey = "csvFieldDelimiter";
	public static final String lastDirWrittenKey = "lastDirWritten";
	public static final String lastDirReadKey = "lastDirRead";
	public static final String workspaceSizeKey = "workspaceSize";
	public static final String editorSizeKey = "editorSize";
	public static final String sheetSizeKey = "sheetSize";
	public static final String formulaEditorStripSizeKey = "formulaEditorStripSize";
	public static final String dragGhostSizeKey = "dragGhostSize";
	public static final String sheetRowHeightMinKey = "sheetRowHeightMin";
	public static final String sheetColWidthMinKey = "sheetColWidthMin";
	public static final String sheetObjsInCellKey = "sheetObjsInCell";
	public static final String sheetCellObjsWidthKey = "sheetCellObjsWidth";

	public static final String sheetNumColsKey = "sheetNumCols";
	public static final String sheetNumRowsKey = "sheetNumRows";
	public static final String workspaceNumColsKey = "workspaceNumCols";
	public static final String workspaceHGapKey = "workspaceHGap";
	public static final String workspaceVGapKey = "workspaceVGap";
	public static final String workspaceObjWidthKey = "workspaceObjWidth";
	public static final String workspaceObjHeightKey = "workspaceObjHeight";
	public static final String workspaceMaxObjWidthKey = "workspaceMaxObjWidth";
	public static final String workspaceMaxObjHeightKey = "workspaceMaxObjHeight";

	private static void initDefaultProperties() {

		// Set all the defaults in the separate defaults properties.
		// They will be used if the program asks for some property 
		// that's not set:

		PhotoSpread.photoSpreadDefaults.setProperty(csvFieldDelimiterKey, ",");
		PhotoSpread.photoSpreadDefaults.setProperty(lastDirWrittenKey, System.getProperty("user.dir"));
		PhotoSpread.photoSpreadDefaults.setProperty(lastDirReadKey, System.getProperty("user.dir"));
		PhotoSpread.photoSpreadDefaults.setProperty(editorSizeKey, "830 940");
		PhotoSpread.photoSpreadDefaults.setProperty(sheetSizeKey, "650 500");
		PhotoSpread.photoSpreadDefaults.setProperty(formulaEditorStripSizeKey, "400 30");
		PhotoSpread.photoSpreadDefaults.setProperty(dragGhostSizeKey, "50 50");
		PhotoSpread.photoSpreadDefaults.setProperty(sheetRowHeightMinKey, "60");
		PhotoSpread.photoSpreadDefaults.setProperty(sheetColWidthMinKey, "80");
		PhotoSpread.photoSpreadDefaults.setProperty(sheetObjsInCellKey, "10");
		PhotoSpread.photoSpreadDefaults.setProperty(sheetCellObjsWidthKey, "50");
		PhotoSpread.photoSpreadDefaults.setProperty(sheetNumColsKey, "8");
		PhotoSpread.photoSpreadDefaults.setProperty(sheetNumRowsKey, "8");
		PhotoSpread.photoSpreadDefaults.setProperty(workspaceNumColsKey, "1");
		PhotoSpread.photoSpreadDefaults.setProperty(workspaceHGapKey, "2");
		PhotoSpread.photoSpreadDefaults.setProperty(workspaceVGapKey, "2");
		PhotoSpread.photoSpreadDefaults.setProperty(workspaceObjWidthKey, "500");
		// For now we make all items square:
		PhotoSpread.photoSpreadDefaults.setProperty(workspaceObjHeightKey,
				PhotoSpread.photoSpreadDefaults.getProperty(workspaceObjWidthKey));
		//photoSpreadDefaults.setProperty(workspaceObjHeightKey, "500");
		PhotoSpread.photoSpreadDefaults.setProperty(workspaceMaxObjWidthKey, "500");
		PhotoSpread.photoSpreadDefaults.setProperty(workspaceMaxObjHeightKey, "500");
		PhotoSpread.photoSpreadDefaults.setProperty(workspaceSizeKey, "500 500");
	}


	public static void main (String[] args) {
		initDefaultProperties();

		new KeyBindEditor(new PhotoSpreadTable(new PhotoSpreadTableModel(), new JFrame()));
		// System.exit(0);

	}


}
