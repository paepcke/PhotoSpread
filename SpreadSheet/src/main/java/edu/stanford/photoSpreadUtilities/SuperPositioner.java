/**
 * 
 */
package edu.stanford.photoSpreadUtilities;

import java.awt.Color;
import java.awt.Component;
import java.security.InvalidParameterException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import edu.stanford.photoSpreadTable.PredictableEquiSizedGridLayout;
import edu.stanford.photoSpreadUtilities.Const.Alignment;

/**
 * @author paepcke
 *
 */
public class SuperPositioner extends JPanel {

	/**
	 * Takes a component and optionally an enclosing frame,
	 * and superposes the component onto the frame using its
	 * glass pane.
	 * 
	 */

	public static final int OF_PANEL_WIDTH = 0;
	public static final int OF_PANEL_HEIGHT = 1;
	
	private static final long serialVersionUID = 1L;
	private static int _defaultAlpha = 255/2;
	private JPanel _superposeSubstrate = null;
	private JLayeredPane _superposePane;

	private Component _itemToShow;
	private ComputableDimension _initialItemSize = null;
	private int _initialItemSizePercentage;

	/****************************************************
	 * Constructor(s)
	 *****************************************************/

	// public SuperPositioner(JFrame frame, Component itemToShow, int alpha, String popupTitle) {
	public SuperPositioner(JPanel panelToSuperposeOn, Component itemToShow, int alpha, String popupTitle) {

		if ((panelToSuperposeOn == null) ||
			(itemToShow == null) ||
			(alpha < 0) ||
			(alpha > 255))
			throw new InvalidParameterException("One of 'frame', 'itemToshow', or 'alpha' are null.");

		_itemToShow = itemToShow;
		_superposeSubstrate = panelToSuperposeOn;
		_defaultAlpha = alpha;
		
		// Clients may call setInitialItemSize() later. But for
		// the case that they don't, we need to initialize
		// _initialItemSize to something reasaonble:
		
		if (_initialItemSize == null)
			
			// If we created a default item as part of the constructor,
			// then that item's preferred size will have been set
			// to something reasonable:
			
			if (_itemToShow.getPreferredSize() != null)
				_initialItemSize = new ComputableDimension(_itemToShow.getPreferredSize());
			// Otherwise we make the image 100% of the surrounding frame,
			// assuming a 1/1 item aspect ratio:
			else
				setInitialItemSizePercentage(100, OF_PANEL_WIDTH, (float) 1.0);
				
		if (popupTitle == null)
			popupTitle = "";

		// A LayeredJPane will be the surface onto which
		// we'll place the item to superimpose:
		
		_superposePane = new JLayeredPane();
		_superposePane.setOpaque(false);

		_superposePane.setBackground(new Color (
				Color.GRAY.getRed(),
				Color.GRAY.getGreen(),
				Color.GRAY.getBlue(),
				_defaultAlpha));

		// _superposePane.setLayout(new FlowLayout(FlowLayout.LEADING));
		
		PredictableEquiSizedGridLayout superposeLayout = new PredictableEquiSizedGridLayout();
		_superposePane.setLayout(superposeLayout);
		superposeLayout.setLayoutAlignmentX(Alignment.LEFT_H_ALIGNED);
		superposeLayout.setLayoutAlignmentX(Alignment.TOP_V_ALIGNED);
		
		// Border around the superposition:
		
		TitledBorder superImpositionBorder = BorderFactory.createTitledBorder(popupTitle);
		superImpositionBorder.setTitleColor(Const.superposePaneTitleColor);
		_superposePane.setBorder(superImpositionBorder);
		
		_superposePane.add(_itemToShow);

		// ComputableDimension superposePaneSize = new ComputableDimension (_superposePane.getSize());
		// ComputableDimension borderSize = new ComputableDimension (superImpositionBorder.getMinimumSize(_superposePane));
		// _superposePane.setPreferredSize(superposePaneSize.plus(borderSize));
		_superposePane.setPreferredSize(new ComputableDimension(600,600));

		_superposePane.setLayer(_itemToShow, JLayeredPane.POPUP_LAYER);
		_superposeSubstrate.add(_superposePane);

		// We start invisible, letting the client
		// manipulate quantities like initial size
		// of shown item. The client will then need
		// to call setVisible(true):

		_superposePane.setVisible(false);
		
		// _superposeSubstrate.validate();
	}

	public SuperPositioner  (JPanel panelToSuperposeOn, Component itemToShow, int alpha) {
		this(panelToSuperposeOn, itemToShow, alpha, "");
	}


	public SuperPositioner  (JPanel panelToSuperposeOn, Component itemToShow, String popupTitle) {
		this(panelToSuperposeOn, itemToShow, getDefaultAlpha(), popupTitle);
	}

	public SuperPositioner  (JPanel panelToSuperposeOn, Component itemToShow) {
		this(panelToSuperposeOn, itemToShow, getDefaultAlpha(), "");
	}
	
	public SuperPositioner  (JPanel panelToSuperposeOn, String popupTitle) {
		this(panelToSuperposeOn, prepareSample(panelToSuperposeOn), _defaultAlpha, popupTitle);
	}
	
	public SuperPositioner  (JPanel panelToSuperposeOn) {
	
		this(panelToSuperposeOn, prepareSample(panelToSuperposeOn), _defaultAlpha, "");  // no popup title
	}

	private static Component prepareSample(JPanel panelToSuperposeOn) {
	
		Component sample = new JPanel();
		sample.setBackground(Const.superposeDefaultSampleColor);
		ComputableDimension sampleDim = new ComputableDimension(panelToSuperposeOn.getSize());
		sample.setPreferredSize(sampleDim.div(2));
		sample.setVisible(true);
		return sample;
	}


	/**
	 * Set width/height of how large the superimposed item is
	 * to be shown initially
	 * 
	 * @param initItemSize 
	 */
	public void setInitialItemSize(ComputableDimension initItemSize) {

		_initialItemSize = initItemSize;
		
		if (_itemToShow != null)
			_itemToShow.setPreferredSize(initItemSize);
			resizeItem(initItemSize);
	}
	
	/**
	 * Set width/height of how large the superimposed item is
	 * to be shown initially
	 * 
	 * @param initItemWidth
	 * @param initItemHeight
	 */
	public void setInitialItemSize(int initItemWidth, int initItemHeight) {
		setInitialItemSize (new ComputableDimension(initItemWidth, initItemHeight));
	}
	
	/**
	 * Set width/height of how large the superimposed item is
	 * to be shown initially. The size is passed in as a percentage
	 * of the surrounding panel (which was passed into the constructor
	 * earlier). 
	 * @param initItemSizePercentage Percentage of surrounding panel
	 */
	public void setInitialItemSizePercentage(int initItemSizePercentage) {
		
		_initialItemSizePercentage = initItemSizePercentage;
		
		int newInitialItemWidth = _superposeSubstrate.getWidth() * initItemSizePercentage / 100;
		int newInitialItemHeight = _superposeSubstrate.getHeight() * initItemSizePercentage / 100;
		setInitialItemSize(new ComputableDimension (newInitialItemWidth, newInitialItemHeight));
	}

	/**
	 * Set width/height of how large the superimposed item is 
	 * to be shown initially. The size is computed by either the
	 * width or the height of the surrounding panel. Caller picks
	 * which of these sides is used as the reference by setting
	 * the parameter whichPanelSide to OF_PANEL_WIDTH or 
	 * OF_PANEL_HEIGHT. The initItemSizePercentage is is applied
	 * to the thus specified panel side and taken as the displayed
	 * item's width (height). The remaining item's side is then 
	 * computed from the parameter ratioWidthOverHeight item aspect
	 * ratio. 
	 * 
	 * 
	 * @param initItemSizePercentage Percentage of panel side
	 * @param whichPanelSide Choice of panel side (width/height) to use as reference for the percentage
	 * @param ratioWidthOverHeight Item aspect ratio
	 */
	public void setInitialItemSizePercentage (int initItemSizePercentage, int whichPanelSide, float ratioWidthOverHeight) {
		
		int newInitialItemWidth;
		int newInitialItemHeight;
		
		if (! (
				(whichPanelSide == OF_PANEL_WIDTH) ||
				(whichPanelSide == OF_PANEL_HEIGHT))
		)
			throw new InvalidParameterException ("Panel side spec needs to be OF_PANEL_WIDTH or OF_PANEL_HEIGHT");
				
		if (whichPanelSide == OF_PANEL_WIDTH) {
			newInitialItemWidth = _superposeSubstrate.getWidth() * initItemSizePercentage / 100;
			newInitialItemHeight = ((int) (newInitialItemWidth / ratioWidthOverHeight));
		}
		else {
			newInitialItemHeight = _superposeSubstrate.getHeight() * initItemSizePercentage / 100;
			newInitialItemWidth = ((int) (newInitialItemHeight * ratioWidthOverHeight));
		}
		setInitialItemSize(new ComputableDimension(newInitialItemWidth, newInitialItemHeight));
			
	}

	public ComputableDimension getInitialItemSize() {
		return _initialItemSize;
	}

	public int getInitialItemSizePercentage() {
		return _initialItemSizePercentage;
	}

	/****************************************************
	 * Methods
	 *****************************************************/

	public void dispose () {
		_superposePane.setVisible(false);
		_superposePane.remove(_itemToShow);
		_superposeSubstrate.remove(_superposePane);
		_superposePane = null;
	}

	/**
	 * Allow UI to display this instance of 
	 * superposition. Client needs to call this
	 * method, because instance construction leaves
	 * the superposition hidden. 
	 * 
	 * @param show
	 * @Override
	 */
	public void setVisible(boolean show) {

		_superposePane.setVisible(show);
		super.setVisible(show);
	}
	
	// This method doesn't work yet.
	public void setItemToShow(Component comp) {
		return;
	}

	public ComputableDimension getItemSize() {
		return new ComputableDimension(_itemToShow.getSize());
	}

	
	/**
	 * Resize the item to the given dimension
	 * @param dim ComputationalDimension to use
	 */
	public void resizeItem (ComputableDimension dim)  {
		_itemToShow.setSize(dim);
		_superposePane.invalidate();
	}

	/**
	 * Resize the item to the given width/height
	 * 
	 * @param width
	 * @param height
	 */
	public void resizeItem (int width, int height)  {
		this.resizeItem(new ComputableDimension(width, height));
	}

	/**
	 * Resize the item to a percentage of its initial size
	 * 
	 * @param percentage Percentage of superimposed item's initial size
	 */
	public void resizeItem (int percentage) {
		resizeItem(_initialItemSize.percent(percentage));
	}

	public static void setDefaultAlpha(int newAlpha) {
		if (!((newAlpha >= 0) && (newAlpha <= 255)))
			throw new InvalidParameterException("Alpha values for superimposition must be between 0 and 255, incl.");
		_defaultAlpha = newAlpha;
	}

	public static int getDefaultAlpha() {
		return _defaultAlpha;
	}


	/****************************************************
	 * Main and/or Testing Methods
	 * @throws InterruptedException 
	 *****************************************************/

	public static void main (String[] args) throws InterruptedException {

		JFrame frame = new JFrame("Glass Writing Test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1000,1000);

		JButton contentLayerButton = new JButton("Content Layer");
		JPanel panel = new JPanel();
		panel.setPreferredSize(frame.getSize());
		panel.setBackground(Color.blue);
		panel.add(contentLayerButton);
		frame.add(panel);
		frame.setVisible(true);

		new JButton("Glass Layer");

		// SuperPositioner superPos = new SuperPositioner(panel, glassLayerButton, "Sample image");
		SuperPositioner superPos = new SuperPositioner(panel);
		superPos.setVisible(true);

		for (int i = 0; i < 2; i++) {
			superPos.resizeItem(120);
			superPos.resizeItem(120);
			ComputableDimension currSize = superPos.getItemSize(); 
			superPos.resizeItem(currSize.width, currSize.height + 40);
		} // next i
		
		superPos.resizeItem(70);
		// superPos.dispose();
		
		// JButton newButton = new JButton("New Sample");
		// newButton.setVisible(true);
		// superPos.setItemToShow(newButton);
	}
}
