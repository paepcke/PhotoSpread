/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package photoSpreadTable;

import photoSpread.PhotoSpreadException;

/**
 *
 * @author skandel
 */
public class PhotoSpreadDragDropManager {

	private static PhotoSpreadCell _sourceCell;
	private static PhotoSpreadCell _destCell;

	/****************************************************
	 * Constructor(s)
	 *****************************************************/

	/****************************************************
	 * Getter(s)/Setter(s)
	 *****************************************************/

	public static void setSourceCell(PhotoSpreadCell sourceCell) {
		_sourceCell = sourceCell;
	}

	public static PhotoSpreadCell getSourceCell() {
		return _sourceCell;
	}

	public static void setDestCell(PhotoSpreadCell destCell) {
		_destCell = destCell;
	}

	public static PhotoSpreadCell getDestCell() {
		return _destCell;
	}

	/****************************************************
	 * Methods
	 *****************************************************/


	/**
	 * executes the drag drop

	 * */

	public static void executeDragDrop(){

		if ((_sourceCell == null) ||
				(_destCell == null))
			throw new RuntimeException(
					new PhotoSpreadException.DnDSourceOrDestNotSet(
							"Must set both source and destination cell before drag/drop execution."));

		_destCell.getTableModel().moveSelectedObjects(_sourceCell, _destCell);
		// The following table cell update call appears to be unnecessary,
		// because the moveObjects() call immediately above triggers
		// UI updates where needed.
		// _destCell.getTableModel().fireTableCellUpdated(_destCell.getRow(), _destCell.getColumn());
	}
}
