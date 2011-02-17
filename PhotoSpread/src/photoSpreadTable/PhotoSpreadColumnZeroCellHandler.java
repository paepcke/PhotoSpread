/**
 * 
 */
package photoSpreadTable;

import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputAdapter;

import photoSpread.PhotoSpread;
import photoSpreadUtilities.Const;

/**
 * @author paepcke
 *
 */
public class PhotoSpreadColumnZeroCellHandler extends PhotoSpreadCellHandler {

	private static final long serialVersionUID = 1L;

	/****************************************************
	 * Constructor(s)
	 *****************************************************/


	public PhotoSpreadColumnZeroCellHandler (PhotoSpreadTable table, int row, PhotoSpreadCell cell) {

		super(table, cell);

/*		
		setLayout(new BorderLayout());

		JLabel westSpacer= new JLabel("Foo");
		add(westSpacer, BorderLayout.WEST);

		JPanel resizeHandle = new JPanel();
		resizeHandle.setPreferredSize(new Dimension(this.getPreferredSize().width, Const.rowResizeHandles));
		resizeHandle.setBorder(BorderFactory.createRaisedBevelBorder());

		add(resizeHandle, BorderLayout.SOUTH);

		RowHeightRegulationListener mouseHandler = new RowHeightRegulationListener(table, row);
		this.addMouseListener(mouseHandler);
		this.addMouseMotionListener(mouseHandler);
*/

	}

	/****************************************************
	 * Listener(s)
	 *****************************************************/

	@SuppressWarnings("unused")
	private class RowHeightRegulationListener extends MouseInputAdapter {

		PhotoSpreadTable _table = null;
		int _rowBeingResized = -1;
		int _cursorYWas = 0;
		int _thisRow = 0;
		int _defaultRowHeight = PhotoSpread.photoSpreadPrefs.getInt(PhotoSpread.sheetRowHeightMinKey);

		public RowHeightRegulationListener (PhotoSpreadTable table, int thisRow) {
			
			_table = table;
			_thisRow = thisRow;
		}

		public void mousePressed (MouseEvent e) {
			_cursorYWas = e.getYOnScreen();
		}

		public void mouseReleased (MouseEvent e) {
			_rowBeingResized = -1;
		}

		public void mouseClicked (MouseEvent e) {
			if (e.getClickCount() == 2)
				_table.setRowHeight(_thisRow, _defaultRowHeight);
		}

		public void mouseDragged (MouseEvent e) {

			if (_rowBeingResized >= 0)
				_table.adjustRowHeight(
						_rowBeingResized, 
						_cursorYWas, 
						e.getYOnScreen(),
						Const.motionSensitivity);

			_cursorYWas = e.getYOnScreen();
		}
	}


	/****************************************************
	 * Methods
	 *****************************************************/

}
