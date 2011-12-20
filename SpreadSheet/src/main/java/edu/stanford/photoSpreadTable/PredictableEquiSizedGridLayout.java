/**
 * 
 */
package edu.stanford.photoSpreadTable;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.util.ArrayList;
import java.util.Iterator;

import javax.management.RuntimeErrorException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicGraphicsUtils;

import edu.stanford.photoSpreadObjects.photoSpreadComponents.DraggableLabel;
import edu.stanford.photoSpreadUtilities.ComputableDimension;
import edu.stanford.photoSpreadUtilities.Const;
import edu.stanford.photoSpreadUtilities.Const.Alignment;

/**
 * @author paepcke
 * 
 */
public class PredictableEquiSizedGridLayout implements LayoutManager2 {

	protected int _topMargin;
	protected int _bottomMargin;
	protected int _leftMargin;
	protected int _rightMargin;
	protected int _vGap;
	protected int _hGap;

	protected int _numCols;
	protected int _numRows;

	protected boolean _isValid = false;
	protected boolean _lockLayoutAction = false;

	// Const.ALIGNMENT.LEFT_ALIGNED, Const.ALIGNMENT.CENTER_ALIGNED,
	// Const.ALIGNMENT.RIGHT_ALIGNED:
	protected Alignment _hAlignment = Alignment.CENTER_H_ALIGNED;
	// Alignment.TOP_V_ALIGNED, Const.Alignment.CENTER_V_ALIGNED,
	// Const.Alignment.BOTTOM_V_ALIGNED,
	protected Alignment _vAlignment = Alignment.CENTER_V_ALIGNED;

	protected ArrayList<Integer> _maxWidthInCol = new ArrayList<Integer>();
	protected ArrayList<Integer> _maxHeightInRow = new ArrayList<Integer>();

	protected final static int _defaultTopMargin = 5;
	protected final static int _defaultBottomMargin = 5;
	protected final static int _defaultLeftMargin = 5;
	protected final static int _defaultRightMargin = 5;
	protected final static int _defaultVGap = 3;
	protected final static int _defaultHGap = 3;
	protected final static int _defaultNumRows = 1;
	protected final static int _defaultNumCols = 1;

	protected static enum SizeType {
		PREFERRED_SIZE, MAX_SIZE, MIN_SIZE
	};

	/****************************************************
	 * Constructor(s)
	 *****************************************************/

	public PredictableEquiSizedGridLayout(int topMargin, int bottomMargin,
			int leftMargin, int rightMargin, int vGap, int hGap, int numRows,
			int numCols) {

		_topMargin = topMargin;
		_bottomMargin = bottomMargin;
		_leftMargin = leftMargin;
		_rightMargin = rightMargin;
		_vGap = vGap;
		_hGap = hGap;
		_numRows = numRows;
		_numCols = numCols;
	}

	public PredictableEquiSizedGridLayout() {
		this(_defaultTopMargin, _defaultBottomMargin, _defaultLeftMargin,
				_defaultRightMargin, _defaultVGap, _defaultHGap,
				_defaultNumRows, _defaultNumCols);
	}

	public PredictableEquiSizedGridLayout(int numCols, int hGap, int vGap) {
		this(_defaultTopMargin, _defaultBottomMargin, _defaultLeftMargin,
				_defaultRightMargin, vGap, hGap, _defaultNumRows, numCols);
	}

	public PredictableEquiSizedGridLayout(int numCols) {
		this(_defaultTopMargin, _defaultBottomMargin, _defaultLeftMargin,
				_defaultRightMargin, _defaultVGap, _defaultHGap,
				_defaultNumRows, numCols);
	}

	/****************************************************
	 * Getters/Setters
	 *****************************************************/

	public void setColumns(int numCols) {
		_numCols = numCols;
		// _numRows = Const.INVALID;
	}

	public void setColumnsRevalidate(int numCols, Container surroundingContainer) {
		setColumns(numCols);
		layoutContainer(surroundingContainer);
	}

	public void setHGap(int hGap) {
		_hGap = hGap;
		// _numRows = Const.INVALID;
	}

	public void setHGapRevalidate(int hGap, Container surroundingContainer) {
		setHGap(hGap);
		layoutContainer(surroundingContainer);
	}

	public void setVGap(int vGap) {
		_vGap = vGap;
		// _numRows = Const.INVALID;
	}

	public void setVGapRevalidate(int vGap, Container surroundingContainer) {
		setVGap(vGap);
		layoutContainer(surroundingContainer);
	}

	public int getRows() {
		return Math.max(1, _numRows);
	}

	public int getColumns() {
		return Math.max(1, _numCols);
	}

	public int getHGap() {
		return _hGap;
	}

	public int getVGap() {
		return _vGap;
	}

	/****************************************************
	 * Private (Inner) Classes
	 *****************************************************/

	@SuppressWarnings("unchecked")
	private class ContainerIterator<T> implements Iterator<T> {

		int _pos = 0;
		int _numOfComponents;
		Container _container;

		public ContainerIterator(Container container) {
			_container = container;
			_numOfComponents = container.getComponentCount();
		}

		public boolean hasNext() {
			return (_pos < _numOfComponents);
		}

		public T next() {
			return (T) _container.getComponent(_pos++);
		}

		@SuppressWarnings("unused")
		public int size() {
			return _container.getComponentCount();
		}

		public void remove() {
			throw new UnsupportedOperationException(
					"The ContainerIterator does not support the remove() method.");
		}
	}

	/****************************************************
	 * Methods
	 *****************************************************/

	public boolean isValid() {
		return _isValid;
	}

	/**
	 * Recomputes layout without redrawing anything. Every component in the
	 * surrounding container is told where it should be, so that the next
	 * container redraw or window pack() will show its components laid out
	 * according to all the current settings in this layout manager.
	 * 
	 * @param surroundingContainer
	 *            Container to which this layout manager instance provides
	 *            service.
	 */
	public void validate(Container surroundingContainer) {
		validateNumRows(surroundingContainer);
		layoutContainer(surroundingContainer);
		_isValid = true;
	}

	/**
	 * Computes the number of rows that result from the number of components in
	 * the surrounding container, and this layout manager's number of columns
	 * (i.e. value of _numCols)
	 * 
	 * @see Also: validateNumRows(int)
	 * @param surroundingContainer
	 *            Container to which this layout manager instance provides
	 *            service.
	 */
	public void validateNumRows(Container surroundingContainer) {
		validateNumRows(surroundingContainer.getComponentCount());
	}

	/**
	 * Computes the number of rows that result from the number of components in
	 * the surrounding container, and this layout manager's number of columns
	 * (i.e. value of _numCols)
	 * 
	 * @see Also: validateNumRows(int)
	 * @param surroundingContainer
	 *            Container to which this layout manager instance provides
	 *            service.
	 */
	public void validateNumRows(int componentsToLayout) {

		if (_numCols == Const.INVALID)
			_numCols = _defaultNumCols;

		//_numRows = (int) Math.ceil(componentsToLayout / _numCols);
		_numRows = (int) Math.ceil(((double)componentsToLayout) / ((double)_numCols));
	}

	/**
	 * Do main work of laying out the surrounding container.
	 * 
	 * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
	 */
	public void layoutContainer(Container surroundingContainer) {

		if (_lockLayoutAction)
			return;
		try {
			_lockLayoutAction = true;
			// Get the distance of the layour from the surrounding
			// container. These are usually determined by the
			// prevailing Look-and-Feel.
			Insets insets = surroundingContainer.getInsets();

			// x-Pos top left corner of first component:
			int x0 = insets.left + _leftMargin;
			int y0 = insets.top + _topMargin;
			int currX = x0;
			int currY = y0;
			int xOffsetWithinCell = 0;
			int yOffsetWithinCell = 0;

			Component comp;
			ComputableDimension prefSize = new ComputableDimension(0, 0);

			// Initialize _maxHeightInRow and _maxWidthInCol, as well
			// as each component's preferred size if it's not set:
			laidOutSize(surroundingContainer, SizeType.PREFERRED_SIZE);

			// Loop through all components that are currently in the
			// container and give them a position (relative to
			// the surrounding container that we're laying out),
			// and a size:

			ContainerIterator<Component> components = new ContainerIterator<Component>(
					surroundingContainer);

			for (int currRow = 0; currRow < _numRows; currRow++) {
				if (!components.hasNext())
					break;
				for (int currCol = 0; currCol < _numCols; currCol++) {
					if (!components.hasNext())
						break;

					comp = components.next();
					if (comp.isPreferredSizeSet())
						prefSize = new ComputableDimension(comp
								.getPreferredSize());
					else
						prefSize = new ComputableDimension(comp.getSize());

					// Compute spaces for horizontal and vertical
					// alignment of the present component within
					// its cell (currRow, currCol). This space depends
					// on the AlignmentX and AlignmentY values:

					switch (_hAlignment) {
					case LEFT_H_ALIGNED:
						xOffsetWithinCell = 0;
						break;
					case CENTER_H_ALIGNED:
						xOffsetWithinCell = ((int) ((0.5 * _maxWidthInCol
								.get(currCol)) - (0.5 * prefSize.width)));
						break;
					case RIGHT_H_ALIGNED:
						xOffsetWithinCell = ((int) (_maxWidthInCol.get(currCol) - prefSize.width));
						break;
					}

					switch (_vAlignment) {
					case TOP_V_ALIGNED:
						yOffsetWithinCell = 0;
						break;
					case CENTER_V_ALIGNED:
						yOffsetWithinCell = ((int) ((0.5 * _maxHeightInRow
								.get(currRow)) - (0.5 * prefSize.height)));
						break;
					case BOTTOM_V_ALIGNED:
						yOffsetWithinCell = ((int) (_maxHeightInRow
								.get(currRow) - prefSize.height));
						break;
					}

					comp.setBounds(currX + xOffsetWithinCell, currY
							+ yOffsetWithinCell, prefSize.width,
							prefSize.height);

					currX += _maxWidthInCol.get(currCol) + _hGap;
				}
				currX = x0;
				currY += _maxHeightInRow.get(currRow) + _vGap;
			}
		} finally {
			_lockLayoutAction = false;
		}
	}

	/**
	 * Compute total width/height of the current layout for the given
	 * container's components. Rows and columns may have differing widths and
	 * heights. Each column is as wide as the widest component in that column.
	 * Each row is as high as the highest component in that row.
	 * 
	 * <b>Also:</b> this method leaves the _maxHeightInRow ArrayList initialized
	 * to the height of each row: [HighestComponentRow0, HighestComponentRow1,
	 * ...]
	 * 
	 * It leaves the _maxWidthInCol ArrayList initialized to the widest
	 * component size in the columns: [WidestComponentCol0, WidestComponentCol1,
	 * ...]
	 * 
	 * @param surroundingContainer
	 *            Container for which we are to compute the total width and
	 *            height of the layout.
	 * @param sizeType
	 *            : PREFERRED_SIZE, MIN_SIZE, or MAX_SIZE. Controls which size
	 *            assumption to use for the components.
	 * @return Width and height of the total layout.
	 */
	protected ComputableDimension laidOutSize(Container surroundingContainer,
			SizeType sizeType) {

		Insets insets = surroundingContainer.getInsets();
		ComputableDimension compDim = new ComputableDimension();
		Component comp;
		
		// Make sure that the _numRows variable is up to date:
		// validateNumRows(surroundingContainer);

		// Result: initialize to the Look/Feel distance
		// on all four sides. We'll add component dimensions
		// and inter-component distances one component at
		// a time:

		ComputableDimension totalLayoutSize = new ComputableDimension(
				insets.left + insets.right + _leftMargin + _rightMargin,
				insets.top + insets.bottom + _topMargin + _bottomMargin);

		ContainerIterator<Component> components = new ContainerIterator<Component>(
				surroundingContainer);

		_maxWidthInCol.clear();
		_maxHeightInRow.clear();
		// Initialize arrays to zero:
		for (int col = 0; col < _numCols; col++)
			_maxWidthInCol.add(0);

		validateNumRows(surroundingContainer);

		for (int row = 0; row < _numRows; row++)
			_maxHeightInRow.add(0);

		for (int currRow = 0; currRow < _numRows; currRow++) {
			if (!components.hasNext())
				break;
			for (int currCol = 0; currCol < _numCols; currCol++) {
				if (!components.hasNext())
					continue;
				comp = components.next();
				
				// We only want to consider images:
				if (!(comp instanceof DraggableLabel))
					continue;
				
				switch (sizeType) {
				case PREFERRED_SIZE:
					compDim = new ComputableDimension(comp.getPreferredSize());
					break;
				case MIN_SIZE:
					compDim = new ComputableDimension(comp.getMinimumSize());
					break;
				case MAX_SIZE:
					compDim = new ComputableDimension(comp.getMaximumSize());
					break;
				}

				if (compDim.height > _maxHeightInRow.get(currRow))
					_maxHeightInRow.set(currRow, compDim.height);

				if (compDim.width > _maxWidthInCol.get(currCol))
					_maxWidthInCol.set(currCol, compDim.width);
			}
		}
		for (int colWidth : _maxWidthInCol) {
			totalLayoutSize.width += colWidth + _hGap;
		}
		for (int rowHeight : _maxHeightInRow) {
			totalLayoutSize.height += rowHeight + _vGap;
		}

		// During the last run through the nested loop we
		// added _hGap to total width, and _vGap to total
		// height in anticipation of another row/column.
		// Since neither of those came, subtract them again:

		totalLayoutSize.width -= _hGap;
		totalLayoutSize.height -= _vGap;

		return totalLayoutSize;
	}

	/**
	 * Cause horizontal alignment of components with their cells to be left,
	 * center, or right. Automatically cause an internal update of the layout so
	 * that next time the surrounding container is redrawn, the new alignment
	 * will be in effect.
	 * 
	 * @param al
	 *            Alignment: Const.Alignment.{LEFT_H_ALIGNED | CENTER_H_ALIGNED
	 *            | RIGHT_H_ALIGNED}.
	 * @param surroundingContainer
	 *            The container whose layout this LayoutManager is managing.
	 */
	public void setLayoutAlignmentXRevalidate(Alignment al,
			Container surroundingContainer) {
		setLayoutAlignmentX(al);
		validate(surroundingContainer);
	}

	/**
	 * Cause horizontal alignment of components with their cells to be left,
	 * center, or right.
	 * 
	 * @param al
	 *            Alignment: Const.Alignment.{LEFT_H_ALIGNED | CENTER_H_ALIGNED
	 *            | RIGHT_H_ALIGNED} See also the
	 *            {@link #setLayoutAlignmentX(Float) setLayoutAlignmentX(Float)}
	 *            method.
	 */
	public void setLayoutAlignmentX(Alignment al) {
		_hAlignment = al;
	}

	/**
	 * Cause horizontal alignment of components with their cells to be left,
	 * center, or right.
	 * 
	 * @param al
	 *            Alignment code: 0, 0.5, 1 for left, centered, right,
	 *            respectively.
	 */
	public void setLayoutAlignmentX(Float al) {
		int integerizedAlignmentFloat = (int) Math.floor(al * 10);
		switch (integerizedAlignmentFloat) {
		case 0:
			setLayoutAlignmentX(Alignment.LEFT_H_ALIGNED);
			break;
		case 5:
			setLayoutAlignmentX(Alignment.CENTER_H_ALIGNED);
			break;
		case 10:
			setLayoutAlignmentX(Alignment.RIGHT_H_ALIGNED);
			break;
		default:
			throw new RuntimeErrorException(null,
					"Only values 0, 0.5, and 1 are allowed as horizontal alignment values.");
		}
	}

	/**
	 * Cause vertical alignment of components with their cells to be top,
	 * center, or bottom. Automatically cause an internal update of the layout
	 * so that next time the surrounding container is redrawn, the new alignment
	 * will be in effect.
	 * 
	 * @param al
	 *            Alignment: Const.Alignment.{TOP_V_ALIGNED | CENTER_V_ALIGNED |
	 *            BOTTOM_V_ALIGNED}.
	 * @param surroundingContainer
	 *            The container whose layout this LayoutManager is managing.
	 */
	public void setLayoutAlignmentYRevalidate(Alignment al,
			Container surroundingContainer) {
		setLayoutAlignmentY(al);
		validate(surroundingContainer);
	}

	/**
	 * Cause vertical alignment of components with their cells to be top,
	 * center, or bottom.
	 * 
	 * @param al
	 *            Alignment: Const.Alignment.{TOP_V_ALIGNED | CENTER_V_ALIGNED |
	 *            BOTTOM_V_ALIGNED} See also the
	 *            {@link #setLayoutAlignmentY(Float) setLayoutAlignmentY(Float)}
	 *            method.
	 */

	public void setLayoutAlignmentY(Alignment al) {
		_vAlignment = al;
	}

	/**
	 * Cause vertical alignment of components with their cells to be top,
	 * center, or bottom.
	 * 
	 * @param al
	 *            Alignment code: 0, 0.5, 1 for top, centered, bottom,
	 *            respectively.
	 */

	public void setLayoutAlignmentY(Float al) {
		int integerizedAlignmentFloat = (int) Math.floor(al * 10);
		switch (integerizedAlignmentFloat) {
		case 0:
			setLayoutAlignmentY(Alignment.TOP_V_ALIGNED);
			break;
		case 5:
			setLayoutAlignmentY(Alignment.CENTER_V_ALIGNED);
			break;
		case 10:
			setLayoutAlignmentY(Alignment.BOTTOM_V_ALIGNED);
			break;
		default:
			throw new RuntimeErrorException(null,
					"Only values 0, 0.5, and 1 are allowed as vertical alignment values.");
		}
	}

	/**
	 * Returns 0, 0.5, or 1.0 depending on whether the horizontal alignment of
	 * components is left, center, or right aligned within each layout cell.
	 * 
	 * @see java.awt.LayoutManager2#getLayoutAlignmentX(java.awt.Container)
	 */
	public float getLayoutAlignmentX(Container arg0) {
		// Horizontal alignment:
		switch (_hAlignment) {
		case LEFT_H_ALIGNED:
			return 0.0f;
		case CENTER_H_ALIGNED:
			return 0.5f;
		case RIGHT_H_ALIGNED:
			return 1.0f;
		default:
			return 0.5f;
		}
	}

	/**
	 * Returns LEFT_H_ALIGNED, CENTER_H_ALIGNED, or RIGHT_H_ALIGNED depending on
	 * whether the horizontal alignment of components is left, center, or right
	 * aligned within each layout cell.
	 */

	public Alignment getLayoutAlignmentX() {
		return _hAlignment;
	}

	/**
	 * Returns 0, 0.5, or 1.0 depending on whether the horizontal alignment of
	 * components is left, center, or right aligned within each layout cell.
	 * 
	 * @see java.awt.LayoutManager2#getLayoutAlignmentY(java.awt.Container)
	 */
	public float getLayoutAlignmentY(Container arg0) {
		// Vertical alignment:
		switch (_vAlignment) {
		case TOP_V_ALIGNED:
			return 0.0f;
		case CENTER_V_ALIGNED:
			return 0.5f;
		case BOTTOM_V_ALIGNED:
			return 1.0f;
		default:
			return 0.5f;
		}
	}

	/**
	 * Returns TOP_V_ALIGNED, CENTER_V_ALIGNED, or BOTTOM_V_ALIGNED depending on
	 * whether the vertical alignment of components is top, center, or bottom
	 * aligned within each layout cell.
	 */

	public Alignment getLayoutAlignmentY() {
		return _vAlignment;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.LayoutManager2#maximumLayoutSize(java.awt.Container)
	 */
	public Dimension maximumLayoutSize(Container target) {
		return laidOutSize(target, SizeType.MAX_SIZE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
	 */
	public Dimension minimumLayoutSize(Container target) {
		return laidOutSize(target, SizeType.MIN_SIZE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
	 */
	public Dimension preferredLayoutSize(Container target) {
		return laidOutSize(target, SizeType.PREFERRED_SIZE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.LayoutManager2#invalidateLayout(java.awt.Container)
	 */
	public void invalidateLayout(Container target) {
		_isValid = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.LayoutManager#removeLayoutComponent(java.awt.Component)
	 */
	public void removeLayoutComponent(Component arg0) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.LayoutManager2#addLayoutComponent(java.awt.Component,
	 * java.lang.Object)
	 */
	public void addLayoutComponent(Component arg0, Object arg1) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.LayoutManager#addLayoutComponent(java.lang.String,
	 * java.awt.Component)
	 */
	public void addLayoutComponent(String arg0, Component arg1) {

	}

	/**
	 * Compute number of rows that fit into a given container under the special
	 * assumption that all components are of equal height. All other
	 * computations in this class look at the actual components that are stored
	 * in a surrounding container. This method does not. This method changes
	 * nothing in the layout. It simply performs the calculation and returns the
	 * result.
	 * 
	 * @param surroundingContainer
	 *            Container that contains this layout
	 * @param compHeight
	 *            Height in pixels of the the equi-height components.
	 * @return Number of rows with specified equi-height components that would
	 *         fit the specified container.
	 */

	public int getNumRowsAllSameHeightComponents(
			Container surroundingContainer, int compHeight) {

		int containerHeight = surroundingContainer.getHeight();
		int trueCompHeight = compHeight + _vGap;
		int heightOverhead = 0;
		int numRows = 0;

		// Prepare to subtract top/bottom white space from
		// container height:

		heightOverhead += surroundingContainer.getInsets().top;
		heightOverhead += surroundingContainer.getInsets().bottom;
		heightOverhead += _topMargin;
		heightOverhead += _bottomMargin;

		// Num of rows is container height after vertical white space
		// has been subtracted, divided by the height of the equi-height
		// components that the parameter gives us (trueCompHeight includes
		// the vertical gaps between rows. The addition of _vGap in the
		// numerator accounts for the absent vGap after the last row.

		numRows = (int) Math.floor(((float) containerHeight
				- (float) heightOverhead + (float) _vGap)
				/ ((float) trueCompHeight));

		return numRows;
	}

	/****************************************************
	 * Main and/or Testing Methods
	 *****************************************************/

	public static void main(String[] argv) {

		final int BUTTON_TEXT_INSET = 2;

		JFrame window = new JFrame();

		// Buttons for a 2*2 test layout:
		JButton butt1 = new JButton("Button1");
		butt1.setPreferredSize(BasicGraphicsUtils.getPreferredButtonSize(butt1,
				BUTTON_TEXT_INSET));
		JButton butt2 = new JButton("Button2 Longer");
		butt2.setPreferredSize(BasicGraphicsUtils.getPreferredButtonSize(butt2,
				BUTTON_TEXT_INSET));
		JButton butt3 = new JButton("B3");
		butt3.setPreferredSize(BasicGraphicsUtils.getPreferredButtonSize(butt3,
				BUTTON_TEXT_INSET));
		JButton butt4 = new JButton("Butt4");
		butt4.setPreferredSize(BasicGraphicsUtils.getPreferredButtonSize(butt4,
				BUTTON_TEXT_INSET));

		PredictableEquiSizedGridLayout layoutDefault = new PredictableEquiSizedGridLayout();
		// PredictableEquiSizedGridLayout layout2by2 = new
		// PredictableEquiSizedGridLayout(2,2);

		// Layout should be one column as wide as "Button2 Longer":
		JPanel panel = new JPanel(layoutDefault);
		panel.add(butt1);
		panel.add(butt2);
		panel.add(butt3);
		panel.add(butt4);

		panel.validate();
		window.add(panel);

		panel.setVisible(true);
		window.pack();
		window.setVisible(true);

		layoutDefault.setLayoutAlignmentXRevalidate(Alignment.CENTER_H_ALIGNED,
				panel);
		layoutDefault.setLayoutAlignmentXRevalidate(Alignment.LEFT_H_ALIGNED,
				panel);
		layoutDefault.setLayoutAlignmentXRevalidate(Alignment.RIGHT_H_ALIGNED,
				panel);

		layoutDefault.setColumnsRevalidate(2, panel);
		window.pack();
		layoutDefault.setLayoutAlignmentXRevalidate(Alignment.CENTER_H_ALIGNED,
				panel);
		layoutDefault.setLayoutAlignmentXRevalidate(Alignment.LEFT_H_ALIGNED,
				panel);
	}
}
