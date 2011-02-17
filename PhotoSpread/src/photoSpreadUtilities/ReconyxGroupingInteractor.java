/**
 * 
 */
package photoSpreadUtilities;

import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

import photoSpread.PhotoSpread;
import photoSpreadTable.PhotoSpreadCell;
import photoSpreadTable.PhotoSpreadTable;

/**
 * @author paepcke
 *
 */
/**
 * @author paepcke
 *
 * Created from the cell context menus. This class is
 * the entry point to the Reconyx grouping functionality.
 * An instance of this class shows a dialog that collects from the user
 * the Full Set Cell, the Subset Cell, and the Result Cell.
 * It then creates a ReconCameraGrouper that performs the
 * actual work.
 *
 */
public class ReconyxGroupingInteractor extends JDialog {

	private static final long serialVersionUID = 1L;

	PhotoSpreadTable _table;
	
	PhotoSpreadCell _fullCell = null;
	PhotoSpreadCell _subsetCell = null;
	PhotoSpreadCell _resCell = null;

	private static String _fullCellTxt = "Full-Set Cell";
	private static String _subsetCellTxt = "Subset Cell";
	private static String _resCellTxt = "Result Cell";

	JTextField _fullCellInFld = new JTextField("A1", 5);
	JTextField _subsetCellInFld = null;
	JTextField _resCellInFld = new JTextField("A2", 5);

	JButton _okButton = new JButton("OK");
	JButton _cancelButton = new JButton("Cancel");

	JPanel _panel;

	public static void performReconyxGrouping(PhotoSpreadCell subsetCell) {
		new ReconyxGroupingInteractor(subsetCell).setVisible(true);
	}

	public ReconyxGroupingInteractor (PhotoSpreadCell subsetCell) {

		super(PhotoSpread.getCurrentSheetWindow(), "Reconyx Photo Grouping", Const.MODAL);

		_subsetCell = subsetCell;
		_table = _subsetCell.getTable();
		_subsetCellInFld = new JTextField(subsetCell.getCellAddress(), 5);

		_panel = new JPanel();

		//     Full Cell: <text field>
		//     Sub  Cell: <text field>
		//     Res  Cell: <text Field>
		//       [OK]  [Cancel]
		_panel.setLayout(new GridLayout(4, 2));

		_panel.add(new Label(_fullCellTxt));
		_panel.add(_fullCellInFld);

		_panel.add(new Label(_subsetCellTxt));
		_panel.add(_subsetCellInFld);

		_panel.add(new Label(_resCellTxt));
		_panel.add(_resCellInFld);

		_panel.add(_okButton);
		_panel.add(_cancelButton);

		_okButton.addActionListener(new OKButtonListener());		
		_cancelButton.addActionListener(new CancelButtonListener());	

		this.add(_panel);
		pack();
	}

	/****************************************************
	 * Inner classes OK and Cancel Button Listeners
	 *****************************************************/

	class OKButtonListener implements ActionListener {

		public OKButtonListener() {
		}

		public void actionPerformed(ActionEvent e) {
			startGrouping();
		}
	}

	class CancelButtonListener implements ActionListener {

		public CancelButtonListener() {
		}

		public void actionPerformed(ActionEvent e) {
			while (!startGrouping()) {};
			dispose();
		}
	}

	/****************************************************
	 * Methods
	 *****************************************************/

	/**
	 * Called after user has entered the three cell addresses
	 * in the popup dialog. Collects the addresses from
	 * the text fields, error-checks, and creates a 
	 * ReconCameraGrouper object. Invokes expandToGroups()
	 * on that object.
	 * 
	 * @return True if everything is done. Else false.
	 */

	protected boolean startGrouping() {

		if (!verifyDialogEntries())
			return false;
		try {
			new ReconCameraGrouper(_fullCell, _subsetCell, _resCell).expandToGroups();
		} catch (Exception errorObj) {
			Misc.showErrorMsgOnSheetWindow(
					"Cannot start Reconyx grouping: " + errorObj.getMessage());
		} finally {
			dispose();
		}
		return true;
	}

	public boolean verifyDialogEntries () {

		CellCoordinates fullCellCoords = Misc.getCellAddress(_fullCellInFld.getText());
		CellCoordinates subsetCellCoords = Misc.getCellAddress(_subsetCellInFld.getText());
		CellCoordinates resCellCoords = Misc.getCellAddress(_resCellInFld.getText());

		if (fullCellCoords == null) {
			Misc.showErrorMsgOnSheetWindow(
					"Must enter address of cell with " +
			"all Reconyx photos, like 'A1'");
			return false;
		} else if (subsetCellCoords == null) {
			Misc.showErrorMsgOnSheetWindow("Must enter address of cell with " +
			"subset of Reconyx photos, like 'B1'");
			return false;
		} else if (resCellCoords == null) {
			Misc.showErrorMsgOnSheetWindow("Must enter address of cell " +
			"where full Reconyx groups should go, like 'C1'");
			return false;
		}

		if ((_fullCell = _table.getCell(fullCellCoords)) == null) {
			Misc.showErrorMsgOnSheetWindow(
					"Cannot find cell at '" +
					fullCellCoords.toString() +
			"' in this table.");
			return false;
		}

		if ((_subsetCell = _table.getCell(subsetCellCoords)) == null) {
			Misc.showErrorMsgOnSheetWindow(
					"Cannot find cell at '" +
					subsetCellCoords.toString() +
			"' in this table.");
			return false;
		}

		if ((_resCell = _table.getCell(resCellCoords)) == null) {
			Misc.showErrorMsgOnSheetWindow(
					"Cannot find cell at '" +
					subsetCellCoords.toString() +
			"' in this table.");
			return false;
		}
		return true;
	}
}