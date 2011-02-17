/**
 * 
 */
package photoSpreadObjects.photoSpreadComponents;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;

import photoSpreadUtilities.Const;
import photoSpreadUtilities.Misc;
import photoSpreadUtilities.Const.CursorMoveEffect;
import photoSpreadUtilities.Const.CursorOffScreen;
import photoSpreadUtilities.Const.Direction;

/**
 * @author paepcke
 * 
 * Class to handle the selection of items in the Workspace
 * using keyboard and mouse wheel.
 */

public class WorkspaceSelector {

	static private Workspace _workspace;
	
	static private ArrayList<PhotoSpreadAddable> _labels;
	static private DraggableLabel _labelWithCursor = null;
	
	static private int _displayedLabelIndex = 0;
	static private int _indexAtXactionStart = 0;
	static private int _pageAtXactionStart = 0;
	
	static private Direction _direction = Direction.INDETERMINATE;
	private static CursorOffScreen _cursorVisibility = CursorOffScreen.NO;

	
	static private boolean _inSelectionXaction = false;

	
	/****************************************************
	 * Constructor(s)
	 *****************************************************/
	
	public static void init (Workspace theWorkspace) {
		
		_workspace = theWorkspace;
		_labels = getDisplayedLabels();
		
		Misc.bindKey(_workspace.getWorkspacePanel(), "shift RIGHT", extendSelectionToRight);
		Misc.bindKey(_workspace.getWorkspacePanel(), "shift LEFT", extendSelectionToLeft);
		Misc.bindKey(_workspace.getWorkspacePanel(), "shift UP", extendSelectionUp);
		Misc.bindKey(_workspace.getWorkspacePanel(), "shift DOWN", extendSelectionDown);
		Misc.bindKey(_workspace.getWorkspacePanel(), "RIGHT", cursorToRight);
		Misc.bindKey(_workspace.getWorkspacePanel(), "LEFT", cursorToLeft);
		Misc.bindKey(_workspace.getWorkspacePanel(), "UP", cursorUp);
		Misc.bindKey(_workspace.getWorkspacePanel(), "DOWN", cursorDown);
	}
	
	/****************************************************
	 * Actions
	 *****************************************************/
	
	static Action extendSelectionToRight = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			execExtendSelectionToRight();
		}
	};

	static Action extendSelectionToLeft= new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			execExtendSelectionToLeft();
		}
	};

	static Action extendSelectionUp= new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			execExtendSelectionUp();
		}
	};

	static Action extendSelectionDown= new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			execExtendSelectionDown();
		}
	};
	
	
	static Action cursorToRight = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			execCursorToRight ();
		}
	};

	static Action cursorToLeft = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			execCursorToLeft ();
		}
	};

	static Action cursorUp = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			execCursorUp();
		}
	};

	static Action cursorDown = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			execCursorDown();
		}
	};


	
	
	
	/****************************************************
	 * Methods
	 *****************************************************/
	
	/**
	 * Called from Next/Prev/Home/Last button handlers
	 * to warn of user's manual page changes. We handle
	 * those gracefully, not closing the selection transaction.
	 * 
	 * @param pageFlipDirection Direction of page flip: Direction.FORWARD
	 * or Direction.BACKWARD.
	 */
	public static void userChangedPage (Direction pageFlipDirection) {
		
		if ((!_inSelectionXaction) ||
			(_direction == Direction.INDETERMINATE))
			return;
		
		if (pageFlipDirection == Direction.FORWARD) {
			_displayedLabelIndex = 0;
			_cursorVisibility = CursorOffScreen.NO;
		}
		else {
			_displayedLabelIndex = _labels.size() - 1;
			_cursorVisibility = CursorOffScreen.NO;
		}
	}
	
	
	/**
	 * Do what all selection extensions do at the outset.
	 * We follow the Windows File Explorer Thumbnail View
	 * example, which first de-selects everything. 
	 */
	
	private static void startSelectionXaction(Direction direction) {
		
			_workspace.deSelectAll();
			_labels = new ArrayList<PhotoSpreadAddable> (_workspace.getDrawnLabels().values());
			
			PhotoSpreadAddable label = _workspace.getLastLabelClicked();
			
			if (label == null)

				_displayedLabelIndex = 0;

			// A previously clicked label exists:
			else {
				_displayedLabelIndex = _labels.indexOf(label);
				
				// HOWEVER: when pages are flipped objects get
				// reloaded. So above indexOf() call can fail.
				// Don't think this should happen given that
				// the next/prev buttons call userChangedPage().
				// But just in case: do something reasonable:
				
				if ((_displayedLabelIndex < 0) &&
					(direction == Direction.FORWARD))
					_displayedLabelIndex = 0;
				
				if ((_displayedLabelIndex < 0) &&
						(direction == Direction.BACKWARD))
					_displayedLabelIndex = _labels.size() - 1;
			}
			
			_indexAtXactionStart = _displayedLabelIndex;
			_pageAtXactionStart = _workspace.getPage();
			_cursorVisibility = CursorOffScreen.NO;
			_workspace.selectObject(_labels.get(_displayedLabelIndex), true);
			_direction = direction;
			
			if (_direction == Direction.FORWARD)
				incDisplayLabelIndex();
			else
				decDisplayLabelIndex();
			
			_inSelectionXaction = true;
	}
	
	protected static void endSelectionXaction() {
		setCursorMark(Const.NOT_SELECTED);
		_inSelectionXaction = false;
	}
	
	private static void execExtendSelectionToRight() {
		
		if (!_inSelectionXaction) {
			startSelectionXaction(Direction.FORWARD);
		}

		// If sitting on last obj of last page, do nothing:

		if (_cursorVisibility == CursorOffScreen.RIGHT)
			return;

		// Did user just change direction?
		
		if (_direction == Direction.BACKWARD) {
			
			_direction = Direction.FORWARD;
			if (incDisplayLabelIndex() == CursorMoveEffect.ON_LAST_PAGE)
				return;
		}
		
		setCursorMark(false);
		if ((_displayedLabelIndex == _indexAtXactionStart) &&
			(_workspace.getPage() ==_pageAtXactionStart)) {
			
			_workspace.selectObject(_labels.get(_displayedLabelIndex), true);

			if (incDisplayLabelIndex() == CursorMoveEffect.ON_LAST_PAGE)
				return;
		}
		
		if (_displayedLabelIndex >= _labels.size())
			incDisplayLabelIndex();
		_workspace.flipObjectSelection(_labels.get(_displayedLabelIndex), true);
		incDisplayLabelIndex();
	}


	private static void execExtendSelectionToLeft() {

		if (!_inSelectionXaction) {
			startSelectionXaction(Direction.BACKWARD);
		}
		
		// If cursor is already off screen on
		// the left, do nothing:
		
		if (_cursorVisibility == CursorOffScreen.LEFT)
			return;
		
		// Did user just change direction from moving
		// forward (right-arrow key presses) to backward?
		
		if (_direction == Direction.FORWARD) {
			
			_direction = Direction.BACKWARD;
			if (decDisplayLabelIndex() == CursorMoveEffect.ON_FIRST_PAGE)
				return;
		}

		setCursorMark(false);
		
		// Obj where selection transaction started is special:
		// It always stays selected:
		
		if ((_displayedLabelIndex == _indexAtXactionStart) &&
			(_workspace.getPage() ==_pageAtXactionStart)) {
				
			_workspace.selectObject(_labels.get(_displayedLabelIndex), true);
			if (decDisplayLabelIndex() == CursorMoveEffect.ON_FIRST_PAGE)
				return;
		}

		if (_displayedLabelIndex <= -1)
			decDisplayLabelIndex();
		_workspace.flipObjectSelection(_labels.get(_displayedLabelIndex), true);
		decDisplayLabelIndex();
	}


	private static void execExtendSelectionUp() {
		
		for (int i = 0; i < _workspace.getWorkspacePanel().getColumns(); i++) {
			execExtendSelectionToLeft();
		}
	}

	private static void execExtendSelectionDown() {

		for (int i = 0; i < _workspace.getWorkspacePanel().getColumns(); i++) {
			execExtendSelectionToRight();
		}
	}
	
	private static void execCursorToRight () {

		if (!_inSelectionXaction) {
			startSelectionXaction(Direction.FORWARD);
		}

		// If sitting on last obj of last page, do nothing:

		if (_cursorVisibility == CursorOffScreen.RIGHT)
			return;

		// Did user just change direction?
		
		if (_direction == Direction.BACKWARD) {
			
			_direction = Direction.FORWARD;
			// Need to move forward by two, b/c
			// the _displayedLabelIndex is always one 
			// ahead during left-movement:

			if (_cursorVisibility != CursorOffScreen.LEFT)
				incDisplayLabelIndex();
			incDisplayLabelIndex();

		}
		
		if (_displayedLabelIndex >= _labels.size())
			incDisplayLabelIndex();

		setCursorMark(Const.SELECTED);
		incDisplayLabelIndex();
	}
	
	private static void execCursorToLeft() {

		if (!_inSelectionXaction) {
			startSelectionXaction(Direction.BACKWARD);
		}
		
		// If cursor is already off screen on
		// the left, do nothing:
		
		if (_cursorVisibility == CursorOffScreen.LEFT)
			return;
		
		// Did user just switch direction?
		
		if (_direction == Direction.FORWARD) {
			_direction = Direction.BACKWARD;
			
			// Need to backtrack by one, b/c
			// the _displayedLabelIndex is always one 
			// ahead:
			if (_cursorVisibility != CursorOffScreen.RIGHT)
				decDisplayLabelIndex();
			decDisplayLabelIndex();
		}
		
		if (_displayedLabelIndex <= -1)
			decDisplayLabelIndex();
		
		setCursorMark(Const.SELECTED);
		decDisplayLabelIndex();
	}

	private static void execCursorUp() {
		
		for (int i = 0; i < _workspace.getWorkspacePanel().getColumns(); i++) {
			execCursorToLeft();
		}
	}

	private static void execCursorDown() {

		for (int i = 0; i < _workspace.getWorkspacePanel().getColumns(); i++) {
			execCursorToRight();
		}
	}
	
	/**
	 * Increments ptr to currently targeted label in the Workspace.
	 * Attempts to flip page if necessary.
	 * @return Enum CursorMoveEffect: SAME_PAGE if no page flipping was required.
	 * FLIPPED_PAGE if Workspace was made to advance one page, or ON_LAST_PAGE
	 * if page advance would have been required, but Workspace was on last page.
	 */
	private static CursorMoveEffect incDisplayLabelIndex () {

		if (_cursorVisibility == CursorOffScreen.RIGHT)
				return CursorMoveEffect.ON_LAST_PAGE;
		
		if ((_workspace.getPage() == _workspace.getLastPage()) &&
			(_displayedLabelIndex >= _labels.size())) {
			
			_displayedLabelIndex = _labels.size() - 1;
			_cursorVisibility = CursorOffScreen.RIGHT;
			return CursorMoveEffect.ON_LAST_PAGE;
		}

		_displayedLabelIndex++;
		
		// Cursor now definitely not off left edge:
		if (_cursorVisibility == CursorOffScreen.LEFT)
			_cursorVisibility = CursorOffScreen.NO;
		

		if (_displayedLabelIndex > _labels.size()) {
			boolean pageFlipHappened = _workspace.nextPage();
			if (pageFlipHappened) {
				_labels = getDisplayedLabels();
				_displayedLabelIndex = 0;
				return CursorMoveEffect.FLIPPED_PAGE;
			}
			else {
				// Remember that cursor now off to the right:
				_cursorVisibility = CursorOffScreen.RIGHT;
				// Just keep pointing to the last label on that last page:
				_displayedLabelIndex = _labels.size() - 1;
				return CursorMoveEffect.ON_LAST_PAGE;
			}
		}
		return CursorMoveEffect.SAME_PAGE;
	}
	
	/**
	 * Decrements ptr to currently targeted label in the Workspace.
	 * Attempts to flip page if necessary.
	 * @return Enum CursorMoveEffect: SAME_PAGE if no page flipping was required.
	 * FLIPPED_PAGE if Workspace was successfully made to go back one page, or ON_FIRST_PAGE
	 * if page decrement would have been required, but Workspace was on first page.
	 */
	private static CursorMoveEffect decDisplayLabelIndex () {
		
		
		if (_cursorVisibility == CursorOffScreen.LEFT)
			return CursorMoveEffect.ON_FIRST_PAGE; 
				
        if ((_workspace.getPage() == 0) &&
			(_displayedLabelIndex == 0)) {
        	
        	_cursorVisibility = CursorOffScreen.LEFT;
        	return CursorMoveEffect.ON_FIRST_PAGE;
        }
				
		
		_displayedLabelIndex--;
		
		// Cursor now definitely not off right edge:
		if (_cursorVisibility == CursorOffScreen.RIGHT)
			_cursorVisibility = CursorOffScreen.NO;

		if (_displayedLabelIndex < -1) {
			boolean pageFlipHappened = _workspace.prevPage();
			if (pageFlipHappened) {
				_labels = getDisplayedLabels();
				_displayedLabelIndex = _labels.size() - 1;
				return CursorMoveEffect.FLIPPED_PAGE;
			}
			else {
				// Remember that cursor is now off left edge:
				_cursorVisibility = CursorOffScreen.LEFT;
				// Just keep pointing to the first label on that first page:
				_displayedLabelIndex = 0;
				return CursorMoveEffect.ON_FIRST_PAGE;
			}
		}
		return CursorMoveEffect.SAME_PAGE;
	}	
	
	private static void setCursorMark (boolean setTo) {
	
		DraggableLabel label = null;
		
		// If we have previously set another label to be the one
		// with the cursor, then erase the cursor-marking border:
		
		if (_labelWithCursor != null) {
			_labelWithCursor.setBorder(null);
			
			// If that formerly cursor-marked label was selected
			// before we changed its border to the cursor border,
			// then restore the selection highlight on that label:
			
			if (_workspace.isObjectSelected(_labelWithCursor))
				_labelWithCursor.highlight();

			_labelWithCursor.repaint();
		}
		
		if (setTo == Const.SELECTED) {

			// Set the border of the current label to indicate
			// that the cursor is on this label now:
			label = (DraggableLabel) _labels.get(_displayedLabelIndex);
			label.setBorder(BorderFactory.createLineBorder(Const.activeCellFrameColor));
			label.repaint();
		}

		_labelWithCursor = label;
	}
	
	private static ArrayList<PhotoSpreadAddable> getDisplayedLabels () {
		return new ArrayList<PhotoSpreadAddable> (_workspace.getDrawnLabels().values());
	}
}