/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.photoSpreadObjects.photoSpreadComponents;

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
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

import edu.stanford.photoSpread.PhotoSpread;
import edu.stanford.photoSpread.PhotoSpreadException.CannotLoadImage;
import edu.stanford.photoSpreadObjects.PhotoSpreadFileObject;
import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadUtilities.ChangeManager;
import edu.stanford.photoSpreadUtilities.ComputableDimension;
import edu.stanford.photoSpreadUtilities.Const;
import edu.stanford.photoSpreadUtilities.Misc;
import edu.stanford.photoSpreadUtilities.PhotoSpreadContextMenu;
import edu.stanford.photoSpreadUtilities.Misc.ShowHelpAction;
import edu.stanford.photoSpreadUtilities.Misc.WindowCloseAction;

/**
 * 
 * @author skandel
 */
public class MetadataEditor extends JFrame {

	private static final long serialVersionUID = 1L;

	// The following instance var is required to
	// make the context menu work, even though it's
	// never accessed:
	JPopupMenu _popup;

	private static final int NUM_OF_TABLE_COLS = 2;
	private static final int NUM_OF_TABLE_ROWS = 25;

	// Vertical space between the save/save-and-exit/cancel buttons;
	private static final int _EDITED_ITEM_TO_ACTION_BUTTONS_HGAP = 5;
	private static final int ACTION_BUTTONS_VGAP = 3;

	PhotoSpreadObject _editedObject;
	ChangeManager _changeManager;
	// Name of this metadata editor instance.
	// Invented and used for the ChangeManager
	// interaction.
	String _editorName;
	JPanel _panel;
	JTable _table;
	DataTableModel _tableModel;
	TableRowSorter<DataTableModel> _metadataSorter = null;
	Comparator<Object> _metadataComparator = new MetadataComparator();
	PhotoSpreadContextMenu _contextMenu;
	JScrollPane _scrollPane;
	Dimension _workspacePanelSize;

	/****************************************************
	 * Constructor(s)
	 *****************************************************/

	public MetadataEditor() {

		this.addWindowListener(new MetadataEditorWindowListener());
		this.setTitle("PhotoSpread Metadata Editor (F1 for help)");

		_panel = new JPanel();
		_panel.setBackground(Color.DARK_GRAY);

		// _panel.setSize(PhotoSpread.photoSpreadPrefs.getDimension(PhotoSpread.editorSizeKey));

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
				"To do in Metadata Editor Window", 
				"HelpFiles/metadataEditorHelp.html", 
				this));
		
		// For cnt-a (select all), etc.:
		this.addKeyListener(new KeyBindEditorKeyListener());
	}

	public MetadataEditor(PhotoSpreadObject editableObject)
	throws NumberFormatException, NotBoundException {
		this();
		
		setEditedObject(editableObject);

		// Put selection on first cell:
		_table.setRowSelectionInterval(0,0);
		_table.setColumnSelectionInterval(0,0);
		pack();
	}

	/****************************************************
	 * Private (Inner) Classes
	 *****************************************************/

	class MetadataEditorWindowListener extends WindowAdapter {

		public void windowClosing(WindowEvent e) {
			if (JOptionPane.showConfirmDialog(
					_scrollPane, // Show dialog within the metadata editor window
					"Discard changes (if any)?", "Confirm", // Title
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
	 * Inner Classes: Listeners
	 *****************************************************/

	class KeyBindEditorKeyListener extends KeyAdapter {

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

		/**
		 * @return
		 */
		private int getRow() {
			// Which row is in focus?
			int currRow = _table.getEditingRow();
			if (currRow == -1)
				currRow = _table.getSelectedRow();
			if (currRow == -1)
				currRow = 0;
			return currRow;
		}

		/**
		 * 
		 */
		@SuppressWarnings("unused")
		private int getColumn() {
			// Which column is in focus?
			int currCol = _table.getEditingColumn();
			if (currCol == -1)
				currCol = _table.getSelectedColumn();
			if (currCol == -1)
				currCol = 0;
			return currCol;
		}
	} // end class ObjectPanelKeyListener

	class SaveButtonListener implements ActionListener {

		public SaveButtonListener() {
		}

		public void actionPerformed(ActionEvent e) {
			submitChanges();
		}
	}

	class SaveAndExitButtonListener implements ActionListener {

		public SaveAndExitButtonListener() {
		}

		public void actionPerformed(ActionEvent e) {
			submitChanges();
			ChangeManager.unregisterClient(_editorName);
			dispose();
		}
	}

	class CancelButtonListener implements ActionListener {

		public CancelButtonListener() {
		}

		public void actionPerformed(ActionEvent e) {
			// Just close the editor window:
			ChangeManager.unregisterClient(_editorName);
			dispose();
		}
	}

	/**
	 * @author paepcke
	 * 
	 *         We display the PhotoSpread objects whose metadata is being edited
	 *         in the metadata editor. But we don't want them selectable or
	 *         draggable there, because the respective handlers (on
	 *         DraggableLabel, for instance), assume that they live in the
	 *         context of an ObjetcsPanel. This assumption leads to a ... JPanel
	 *         cannot be cast to ...ObjectsPanel. We therefore swallow any mouse
	 *         events that occur on top of the edited object's representation.
	 * 
	 */
	class MetadataEditorOverrideStandardMotionListener extends
	MouseInputAdapter {

		public void mouseReleased(MouseEvent e) {
			if (e.getSource() == _editedObject) {
				e.consume();
			}
		}

		public void mouseEntered(MouseEvent e) {
			if (e.getSource() == _editedObject) {
				e.consume();
			}
		}

		public void mouseExited(MouseEvent e) {
			if (e.getSource() == _editedObject) {
				e.consume();
			}
		}

		public void mouseDragged(MouseEvent e) {
			if (e.getSource() == _editedObject) {
				e.consume();
			}
		}
	}

	/****************************************************
	 * Methods
	 *****************************************************/

	public void setEditedObject(PhotoSpreadObject value)
	throws NumberFormatException, NotBoundException {

		this._editedObject = value;
		this._editorName = value.getObjectID().toString();
		
		//********
		/*
		value.setMetaData(
				"@HomeCell", 
				value.getCell().getCellAddress());
		*/
		//********
        
		try {
			// Use ChangeManager to show visually whether user
			// has changed anything since last saving:

			ChangeManager.registerClient(_editorName, this);

		} catch (AlreadyBoundException e) {
			ChangeManager.unregisterClient(_editorName);
			try {
				ChangeManager.registerClient(_editorName, this);
			} catch (AlreadyBoundException e1) {
				e1.printStackTrace();
			}
		}
		loadEditor();
	}

	/**
	 * 
	 */
	public void selectAll() {
		if (_table != null)
			_table.selectAll();
	}

	private void loadEditor() {

		BoxLayout topWindowLayout = new BoxLayout(_panel, BoxLayout.PAGE_AXIS);
		_panel.setLayout(topWindowLayout);

		JButton saveChangesButton = new JButton("Save Changes");
		saveChangesButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		JButton saveAndExitButton = new JButton("Save & Stop Edit");
		saveAndExitButton.setAlignmentX(CENTER_ALIGNMENT);

		saveChangesButton.addActionListener(new SaveButtonListener());
		cancelButton.addActionListener(new CancelButtonListener());
		saveAndExitButton.addActionListener(new SaveAndExitButtonListener());

		// Panel for the submit/cancel buttons:
		JPanel buttonsPanel = new JPanel();

		// Put each button into its own horizontal box
		// with horizontal glue on each of its sides. 
		// This will make the buttons expand to the 
		// same size:

		JPanel saveChangesButtonPanel = new JPanel();
		BoxLayout saveChangesButtonLayout = new BoxLayout(saveChangesButtonPanel, BoxLayout.LINE_AXIS);
		saveChangesButtonPanel.setLayout(saveChangesButtonLayout);
		saveChangesButtonPanel.add(Box.createHorizontalGlue());
		saveChangesButtonPanel.add(saveChangesButton);
		saveChangesButtonPanel.add(Box.createHorizontalGlue());

		JPanel saveAndExitButtonPanel = new JPanel();
		BoxLayout saveAndExitButtonLayout = new BoxLayout(saveAndExitButtonPanel, BoxLayout.LINE_AXIS);
		saveAndExitButtonPanel.setLayout(saveAndExitButtonLayout);
		saveAndExitButtonPanel.add(Box.createHorizontalGlue());
		saveAndExitButtonPanel.add(saveAndExitButton);
		saveAndExitButtonPanel.add(Box.createHorizontalGlue());

		JPanel cancelButtonPanel = new JPanel();
		BoxLayout cancelButtonLayout = new BoxLayout(cancelButtonPanel, BoxLayout.LINE_AXIS);
		cancelButtonPanel.setLayout(cancelButtonLayout);
		cancelButtonPanel.add(Box.createHorizontalGlue());
		cancelButtonPanel.add(cancelButton);
		cancelButtonPanel.add(Box.createHorizontalGlue());

		// Make all buttons as wide and high as the widest/highest among them:

		Dimension preferredDims[] = { 
				saveChangesButton.getPreferredSize(),
				saveAndExitButton.getPreferredSize(),
				cancelButton.getPreferredSize() };

		Dimension maxPreferredDim = ComputableDimension.maxWidthHeight(preferredDims);
		saveChangesButton.setPreferredSize(maxPreferredDim);
		saveAndExitButton.setPreferredSize(maxPreferredDim);
		cancelButton.setPreferredSize(maxPreferredDim);

		BoxLayout buttonLayout = new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS);
		buttonsPanel.setLayout(buttonLayout);
		buttonsPanel.setAlignmentX(CENTER_ALIGNMENT);
		buttonsPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		buttonsPanel.setBackground(Color.LIGHT_GRAY);

		buttonsPanel.add(saveChangesButtonPanel);
		buttonsPanel.add(Box.createRigidArea(new Dimension(0,
				ACTION_BUTTONS_VGAP)));
		buttonsPanel.add(saveAndExitButtonPanel);
		buttonsPanel.add(Box.createRigidArea(new Dimension(0,
				ACTION_BUTTONS_VGAP)));
		buttonsPanel.add(cancelButtonPanel);

		// Make a panel containing a visual representation
		// of the edited item and the Submit/cancel button panel:

		JPanel editeeAndButtons = new JPanel();
		BoxLayout editeeAndButtonsLayout = new BoxLayout(editeeAndButtons,
				BoxLayout.LINE_AXIS);
		editeeAndButtons.setLayout(editeeAndButtonsLayout);
		editeeAndButtons.setBorder(BorderFactory.createLoweredBevelBorder());
		editeeAndButtons.setBackground(Color.gray);

		editeeAndButtons.add(Box.createRigidArea(new Dimension(
				_EDITED_ITEM_TO_ACTION_BUTTONS_HGAP, 0)));
		try {
			int editeeScaledWidth = PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.workspaceMaxObjWidthKey);
			int editeeScaledHeight= PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.workspaceMaxObjHeightKey);
			
			DraggableLabel editeeVisualRep = (DraggableLabel) _editedObject.getWorkspaceComponent(editeeScaledWidth, editeeScaledHeight);
			
			editeeVisualRep.setPreferredSize(new Dimension(editeeScaledWidth, editeeScaledHeight));
			editeeAndButtons.add(editeeVisualRep);
		} catch (CannotLoadImage e) {
			// We are not loading an image here, so this error
			// doesn't come up.
		}
		editeeAndButtons.add(Box.createRigidArea(new Dimension(
				_EDITED_ITEM_TO_ACTION_BUTTONS_HGAP, 0)));
		editeeAndButtons.add(buttonsPanel);
		editeeAndButtons.add(Box.createRigidArea(new Dimension(
				_EDITED_ITEM_TO_ACTION_BUTTONS_HGAP, 0)));
		editeeAndButtons.setAlignmentX(CENTER_ALIGNMENT);

		// Top: edited object plus submit/cancel:
		_panel.add(editeeAndButtons);

		/* Create TablePanel for Table and Column Header */
		JPanel tablePanel = new JPanel();
		BoxLayout tableAndHeaderLayout = new BoxLayout(tablePanel,
				BoxLayout.PAGE_AXIS);
		tablePanel.setLayout(tableAndHeaderLayout);

		// The table of metadata attributes and values:

		_tableModel = new DataTableModel(_editedObject);
		_table = new JTable(_tableModel);
		_metadataSorter = new TableRowSorter<DataTableModel>(_tableModel);

		for (int col=0; col<NUM_OF_TABLE_COLS; col++)
			_metadataSorter.setComparator(col, _metadataComparator);
		_table.setRowSorter(_metadataSorter);
		addContextMenu();

		// Give the table sort capabilities 
		// in both of the columns:
		_table.setAutoCreateRowSorter(true);

		_table.setBackground(Const.metaDataEditorBackGroundColor);
		_table.setForeground(Const.metaDataEditorForeGroundColor);
		_table.setSelectionForeground(Color.gray);
		_table.setFont(Const.TABLE_FONT);

		_table.addKeyListener(new KeyBindEditorKeyListener());

		_table.setVisible(true);
		// ****_table.setMinimumSize(new Dimension(100, 100));

		tablePanel.add(_table.getTableHeader());
		tablePanel.add(_table);

		this.setBackground(Color.darkGray);
		// Attr/value table header and body:
		_panel.add(tablePanel);

		_panel.validate();
	}

	private void submitChanges() {
		_editedObject.setMetaData(((DataTableModel) this._table.getModel())
				.getData());
		try {
			_editedObject.getCell().evaluate(Const.DO_REDRAW);
		} catch (Exception e) {
			Misc.showErrorMsgAndStackTrace(e, "");
			//e.printStackTrace();
		}
		ChangeManager.markClean(_editorName);
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

		_contextMenu.addMenuItem("Clear row", new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				_tableModel.clearRow();
				// see NOTE1 above on need for following statement:
				_table.editingStopped(new ChangeEvent(this));
			}
		} 
		);

		_contextMenu.addMenuItem("Delete row", new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				_tableModel.removeRow();
				// see NOTE1 above on need for following statement:
				_table.editingStopped(new ChangeEvent(this));
			}
		} 
		);

		_contextMenu.addMenuItem("Re-alphabetize",new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				_tableModel.reAlphabetize();
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

		/*		_contextMenu.addMenuItem("Clear cell",new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				clearCell();
			}
		} 
		);

		_contextMenu.addMenuItem("Insert Table",new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				loadTable();
			}
		} 
		);

		_contextMenu.addMenuItem("Copy",new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				copy();
			}
		} 
		);

		_contextMenu.addMenuItem("Paste",new java.awt.event.ActionListener() {

			public void actionPerformed(ActionEvent e) {
				paste();
			}
		} 
		);
		 */
		_table.addMouseListener(_contextMenu.getPopupListener());
	}

	/****************************************************
	 * DataTableModel
	 *****************************************************/


	class DataTableModel extends AbstractTableModel {

		public static final int ATTR_NAME_COL = 0;
		public static final int VALUE_COL = 1;
		public static final int NONE_FILE_EMPTY_ROWS = 1;
		public static final int FILE_OBJECTS_EMPTY_ROWS = 2;

		private static final long serialVersionUID = 1L;

		private String[] columnNames = { "<HTML><H3>Attribute</H3></HTML>", "<HTML><H3>Value</H3></HTML>" };
		private ArrayList<ArrayList<String>> data;
		
		// How many initial rows in the metadata editor
		// should be read-only:
		private int numOfReadOnlyRows = -1;


		public DataTableModel(PhotoSpreadObject _editedObject) {
			data = _editedObject.getMetaDataSet();
			// Add some empty rows:
			for (int i = 0; i < NUM_OF_TABLE_ROWS; i++) {
				data.add(makeEmptyRow());
			}
			// File objects have the file name and the obj id
			// as read-only entries in the metadata editor.
			// Instances of other types only have the ID:
			if (_editedObject instanceof PhotoSpreadFileObject)
				numOfReadOnlyRows = FILE_OBJECTS_EMPTY_ROWS; 
			else
				numOfReadOnlyRows = NONE_FILE_EMPTY_ROWS;
		}
		
		public void dispose() {

		}

		public Class<String> getColumnClass(int columnIndex) {
			return String.class;
		}

		public Comparator<?> getComparator (int col) {
			return _metadataComparator;
		}

		private ArrayList<String> makeEmptyRow() {
			ArrayList<String> newRow = new ArrayList<String>();
			newRow.add("");
			newRow.add("");
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

			for (int rowNum = firstRowNum; rowNum <= lastRowNum; rowNum++)
				data.remove(rowNum);

			ChangeManager.markDirty(_editorName);
			fireTableRowsDeleted(firstRowNum, lastRowNum);
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

			ArrayList<String> rowToClear;

			for (int rowNum = firstRowNum; rowNum <= lastRowNum; rowNum++) {
				rowToClear = data.get(rowNum);
				rowToClear.set(ATTR_NAME_COL, "");
				rowToClear.set(VALUE_COL, "");
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
			if (row > numOfReadOnlyRows - 1)
				return true;
			else
				return false;
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

			return data.get(row).get(col);
		}

		public void setValueAt(Object value, int rowNum, int colNum) {

			if ((rowNum >= getRowCount()) || (rowNum < 0))
				return;
			if ((colNum >= getColumnCount()) || (colNum < 0))
				return;

			data.get(rowNum).set(colNum, (String) value);
			ChangeManager.markDirty(_editorName);
			fireTableCellUpdated(rowNum, colNum);
		}

		public void reAlphabetize () {

			TreeMap<String, String> sorter = new TreeMap<String, String>();

			for (ArrayList<String> attrValPair : data)
				sorter.put(attrValPair.get(ATTR_NAME_COL), attrValPair.get(VALUE_COL));

			data.clear();

			for (String attr : sorter.keySet()) {
				ArrayList<String> row = new ArrayList<String>();
				row.add(attr);
				row.add(sorter.get(attr));
				data.add(row);
			}
			fireTableDataChanged();
		}


		public ArrayList<ArrayList<String>> getData() {
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
	 * Class Metadata
	 *****************************************************/

	/*	private class MetadataComparator implements Comparator<String> {

		Collator _defaultComparator = Collator.getInstance();

		public int compare(String metadata1, String metadata2) {

			if (metadata1.isEmpty())
				return Const.BIGGER;
			return _defaultComparator.compare(metadata1, metadata2); 
		}
	}
	 */	

	private class MetadataComparator implements Comparator<Object> {

		Collator _defaultComparator = Collator.getInstance();

		public int compare(Object metadata1, Object metadata2) {

			if (((String) metadata1).isEmpty())
				return Const.BIGGER;
			return _defaultComparator.compare(metadata1, metadata2); 
		}
	}

}
