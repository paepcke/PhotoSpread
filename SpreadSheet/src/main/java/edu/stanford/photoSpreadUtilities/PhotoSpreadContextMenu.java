/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.photoSpreadUtilities;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import edu.stanford.photoSpreadTable.PhotoSpreadCell;

/**
 *
 * @author skandel
 */
public class PhotoSpreadContextMenu extends JPopupMenu {

	private static final long serialVersionUID = 1L;
	MouseListener _popupListener;
	PhotoSpreadCell _displayedCell;

	/****************************************************
	 * Constructor(s)
	 *****************************************************/

	public PhotoSpreadContextMenu(){
		this(null);
	}

	public PhotoSpreadContextMenu(PhotoSpreadCell cell){

		_displayedCell = cell;
		_popupListener = new PopupListener();
		this.addPopupMenuListener((PopupMenuListener) _popupListener);


		// We force this popup menu to be
		// medium- or heavyweight, because
		// otherwise the OsmoticGlassPane
		// cannot 'see' the popup menu and
		// therefore won't properly forward
		// mouse events to the menu. This might
		// be obsolete, because the OsmoticGlassPane
		// has been re-engineered. But I don't
		// have time to test whether this statement
		// can go away:

		this.setLightWeightPopupEnabled(false);
	}
	
	public String getCurrentMetadataSortKey () {
		return _displayedCell.getSortKey();
	}

	public void setCurrentMetadataSortKey (String key) {
		_displayedCell.setSortKey(key);
	}

	/****************************************************
	 * Methods
	 *****************************************************/


	public MouseListener getPopupListener(){
		return _popupListener;
	}

	public JMenuItem addMenuItem(String menuLabel, ActionListener listener ){
		JMenuItem menuItem = new JMenuItem(menuLabel);
		menuItem.addActionListener(listener);
		this.add(menuItem);
		return menuItem;
	}

	public void addMenuItemSeparator() {
		this.addSeparator();
	}

	/*    public void addMenuItem(String menuLabel, PhotoSpreadContextMenu submenu) {
    	JMenuItem menuItem = new JMenuItem(menuLabel);
    	this.add(menuItem);
    }
	 */

	/****************************************************
	 * PhotoSpreadSubmenu Inner Class
	 *****************************************************/

	public static class PhotoSpreadSubMenu extends JMenu {

		private static final long serialVersionUID = 1L;

		public PhotoSpreadSubMenu (String label) {
			super(label);
		}

		public JMenuItem addMenuItem(String menuLabel, ActionListener listener ){

			JMenuItem menuItem = new JMenuItem(menuLabel);
			menuItem.addActionListener(listener);
			this.add(menuItem);
			return menuItem;
		}

		public JMenuItem addMenuItemSubMenu(JMenu subMenu, ActionListener listener ){

			subMenu.addActionListener(listener);
			this.add(subMenu);
			return subMenu;
		}

	}

	/****************************************************
	 * PhotoSpreadRadioButtonSubMenu Inner Class
	 *****************************************************/

	public static class PhotoSpreadRadioButtonSubMenu extends JMenu {

		private static final long serialVersionUID = 1L;
		private JButtonGroup _buttonGroup;
		private PhotoSpreadCell _displayedCell;

		public PhotoSpreadRadioButtonSubMenu (String menuLabel, PhotoSpreadCell cell) {
			super(menuLabel);
			_displayedCell = cell;
			_buttonGroup = new JButtonGroup();
			this.addMouseListener(new AllSortKeysMenuItemMouseDragListener(this, _displayedCell));
		}

		public JRadioButtonMenuItem addRadioButton (String menuLabel, ActionListener listener ){

			JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(menuLabel);
			menuItem.addActionListener(listener);

			this.add(menuItem);
			_buttonGroup.add(menuItem);
			return menuItem;
		}

		public void removeAllButtons () {
			_buttonGroup = new JButtonGroup();
			// Remove radio buttons from the radio sub menu: 
			removeAll();
		}

		public JButtonGroup getButtonGroup () {
			return _buttonGroup; 
		}

		protected static void refreshMetadataKeysRadioButtonMenu (PhotoSpreadRadioButtonSubMenu rbMenu, final PhotoSpreadCell cell) {

			if (cell == null)
				return;

			rbMenu.removeAllButtons();
			TreeSet<String> allMetadataKeys = cell.getAllMetadataKeys ();

			Iterator<String> it = allMetadataKeys.iterator();

			if (!it.hasNext()) {
				rbMenu.addRadioButton(
						"No objects to sort in cell " +
						cell.getCellAddress() +
						".",
						null);
				return;
			}

			while (it.hasNext()) {
				rbMenu.addRadioButton(
						it.next(),
						new java.awt.event.ActionListener() {

							public void actionPerformed(ActionEvent e) {
								cell.setSortKey(e.getActionCommand());
								cell.sortObjects();
							}
						});
			};
		}
	}


	/****************************************************
	 * PopupListener Inner Class
	 *****************************************************/

	class PopupListener extends MouseInputAdapter implements PopupMenuListener {

		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {

			if (e.isPopupTrigger()) {
				PhotoSpreadContextMenu.this.show(e.getComponent(),
						e.getX(), e.getY());
			}
		}

		public void mouseExited (MouseEvent mouseEnteredEvent) {

		}

		public void popupMenuCanceled(PopupMenuEvent e) {

		}

		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			AllSortKeysMenuItemMouseDragListener.setCouldBeDirty(true);
		}

		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

		}

	}

	/****************************************************
	 * AllSortKeysMenuItemFocusListener Inner Class
	 *****************************************************/

	static class AllSortKeysMenuItemMouseDragListener extends MouseInputAdapter {

		static boolean _couldBeDirty = true;
		PhotoSpreadRadioButtonSubMenu _allKeysRBMenu;
		PhotoSpreadCell _displayedCell;

		public  AllSortKeysMenuItemMouseDragListener(PhotoSpreadRadioButtonSubMenu menu, PhotoSpreadCell cell) {
			_allKeysRBMenu = menu;
			_displayedCell = cell;
		}

		public static void setCouldBeDirty (boolean value) {
			_couldBeDirty = value;
		}

		@Override
		public void mouseEntered(MouseEvent mouseEnteredEvent) {

			// System.out.println("Submenu entered. _couldBeDirty: " + _couldBeDirty);

			if (!_couldBeDirty)
				return;

			PhotoSpreadRadioButtonSubMenu.refreshMetadataKeysRadioButtonMenu (_allKeysRBMenu, _displayedCell);

			_couldBeDirty = false;
		}
	}
}
