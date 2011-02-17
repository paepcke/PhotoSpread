/**
 *	@ 2003 Daniel C. Tofan
 *	daniel@danieltofan.org
 */
package photoSpreadUtilities;

import java.util.*;
import javax.swing.*;

/** Extends <code>javax.swing.ButtonGroup</code> to provide methods 
 *  that allow working with button references instead of button models.
 *	@author Daniel Tofan
 *	@version 1.0	April 2003
 *	@see ButtonGroup
 */
public class JButtonGroup extends ButtonGroup {
	
	private static final long serialVersionUID = 1L;

	/**
	 *	Stores a reference to the currently selected button in the group
	 */
	private AbstractButton selectedButton;

	/** 
	 *	Creates an empty <code>JButtonGroup</code>
	 */
	public JButtonGroup() {
		super();
	}

	/** 
	 *	Creates a <code>JButtonGroup</code> object from an array of buttons and adds the buttons to the group
	 *	No button will be selected initially.
	 *	@param buttons an array of <code>AbstractButton</code>s
	 */
	public JButtonGroup(AbstractButton[] buttons) {
		add(buttons);
	}

	/**	
	 *	Adds a button to the group
	 *	@param button an <code>AbstractButton</code> reference
	 */
	public void add(AbstractButton button) {
		if (button == null || buttons.contains(button)) return;
		super.add(button);
		if (getSelection() == button.getModel()) selectedButton = button;
	}

	/** 
	 *	Adds an array of buttons to the group
	 *	@param buttons an array of <code>AbstractButton</code>s
	 */
	public void add(AbstractButton[] buttons) {
		if (buttons == null) return;
		for (int i=0; i<buttons.length; i++) {
			add(buttons[i]);
		}
	}

	/**	
	 *	Removes a button from the group
	 *	@param button the button to be removed
	 */
	public void remove(AbstractButton button) {
		if (button != null)	{
			if (selectedButton == button) selectedButton = null;
			super.remove(button);
		}
	}

	/**	
	 *	Removes all the buttons in the array from the group
	 *	@param buttons an array of <code>AbstractButton</code>s
	 */
	public void remove(AbstractButton[] buttons) {
		if (buttons == null) return;
		for (int i=0; i<buttons.length; i++) {
			remove(buttons[i]);
		}
	}

	/** 
	 *	Sets the selected button in the group
	 *	Only one button in the group can be selected
	 *	@param button an <code>AbstractButton</code> reference
	 *	@param selected an <code>boolean</code> representing the selection state of the button
	 */
	public void setSelected(AbstractButton button, boolean selected) {
		if (button != null && buttons.contains(button))	{
			setSelected(button.getModel(), selected);
			if (getSelection() == button.getModel()) selectedButton = button;
		}
	}

	/** 
	 *	Sets the selected button model in the group
	 *	@param model a <code>ButtonModel</code> reference
	 *	@param selected an <code>boolean</code> representing the selection state of the button
	 */
	public void setSelected(ButtonModel model, boolean selected) {
		AbstractButton button = getButton(model);
		if (buttons.contains(button)) super.setSelected(model, selected);
	}

	/** 
	 *	Returns the <code>AbstractButton</code> whose <code>ButtonModel</code> is given.
	 *	If the model does not belong to a button in the group, returns null.
	 *	@param model a <code>ButtonModel</code> that should belong to a button in the group
	 *	@return an <code>AbstractButton</code> reference whose model is <code>model</code> if the button belongs to the group, <code>null</code>otherwise
	 */
	public AbstractButton getButton(ButtonModel model) {
		Iterator<AbstractButton> it = buttons.iterator();
		while (it.hasNext()) {
			AbstractButton ab = (AbstractButton)it.next();
			if (ab.getModel() == model) return ab;
		}
		return null;
	}

	/** 
	 *	Returns the selected button in the group.
	 *	@return a reference to the currently selected button in the group or <code>null</code> if no button is selected
	 */
	public AbstractButton getSelected()
	{
		return selectedButton;
	}

	/** 
	 *	Returns whether the button is selected
	 *	@param button an <code>AbstractButton</code> reference
	 *	@return <code>true</code> if the button is selected, <code>false</code> otherwise
	 */
	public boolean isSelected(AbstractButton button) {
		return button == selectedButton;
	}

	/** 
	 *	Returns the buttons in the group as a <code>List</code>
	 *	@return a <code>List</code> containing the buttons in the group, in the order they were added to the group
	 */
	public List<AbstractButton> getButtons() {
		return Collections.unmodifiableList(buttons);
	}

	/**
	 *	Checks whether the group contains the given button
	 *	@return <code>true</code> if the button is contained in the group, <code>false</code> otherwise
	 */
	public boolean contains(AbstractButton button) {
		return buttons.contains(button);
	}
}
