package edu.stanford.photoSpreadUtilities;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * @author paepcke
 *
 * This glass pane class provides the following services:
 * 	  - Any components added to this pane will float
 * 	    on top of the regular (contentPane's) UI components.
 *      NOTE: Currently only JLabel is supported. Not buttons,
 *            which would not be clickable anyway.
 *    - One component or image may be added to an instance
 *      of this class via setCursorTrailer(). This item
 *      will trail the cursor, offset by a value set 
 *      through setCursorTrailOffset(). This trail will have
 *      transparency (alpha value) set via setAlpha().
 *    - As for the superclass, all events are passed through
 *      to the underlying contentPane. Active elements like
 *      buttons will therefore not work on this pane. They
 *      will only show.
 *      
 *    - Visibility control:
 *    
 *      The cursor trail's visibility can be controlled via
 *      setCursorTrailVisible(boolean). The other components'
 *      visibility is controlled via setComponentVisible(boolean).
 *      
 *      Both cursor trail and component visibility together
 *      are controlled by setVisible(boolean).
 *      
 * An instance of this class must be set as the glass panel
 * of a JPanel (setGlassPane()).
 * 
 * NOTE: for the cursor trail image of a component to show up, 
 *       you must ensure that the component has been laid out.
 *       A way to do this, for instance for a button:
 *       
 *       		contentPane.add(cursorTrailButton);
 *				window.pack();
 *				contentPane.remove(cursorTrailButton);
 *
 *       Where contentPane is a content pane that was obtained via
 *       getContentPane() on a JFrame, or that is a normal
 *       JPanel that is later given to a JFrame via
 *       setContentPane();
 */
public class OsmoticOverlayTracker extends OsmoticGlassPane {
	
	private static final long serialVersionUID = 1L;
	
	private JComponent _cursorTrailComponent = null;
	private Image _cursorTrailImage = null;
	private Dimension _cursorTrailOffset = new Dimension(0,0);
	private float _alphaValue = 0.7f;
	
	private boolean _componentsVisible = true;
	private boolean _cursorTrailVisible = true;
	
	public OsmoticOverlayTracker(JFrame frame) {
		super(frame);
		setLayout(new BorderLayout());
	}

	public void setCursorTrailer (Image cursorTrailImage) {
		
		// Check for zero size:
		if ((cursorTrailImage.getHeight(null) <= 0) ||
			(cursorTrailImage.getWidth(null) <= 0))
			throw new RuntimeException(
					"Labels to use for cursor trail " +
					"images must contain an ImageIcon. " +
			"This label's ImageIcon has zero width and/or height.");

		_cursorTrailImage = cursorTrailImage;
	}
	
	public void setCursorTrailer (JComponent cursorTrailComponent) {
		
		// Ensure that the component that's to be used
		// as a cursor trailer either contains a preferred
		// size, or has non-zero dimensions:
		
		if ((cursorTrailComponent.getPreferredSize() == null) &&
			((cursorTrailComponent.getHeight() <= 0) ||
			 (cursorTrailComponent.getWidth() <= 0)))
			
			throw new RuntimeException(
				"Labels to use for cursor trail " +
				"components must either have a specified " +
				"preferred size, or non-zero with and height. " +
				"This component does not satisfy either condition.");
			
		
		_cursorTrailComponent = cursorTrailComponent;

		// Make sure that an image is created of this
		// component next time paintComponent() is called:
		_cursorTrailImage = null;
	}
	
	/**
	 * @param _alphaValue the _alphaValue to set
	 */
	public void setAlphaValue(float _alphaValue) {
		this._alphaValue = _alphaValue;
	}

	/**
	 * @return the _alphaValue
	 */
	public float getAlphaValue() {
		return _alphaValue;
	}

	public void setCursorTrailOffset(Dimension cursorTrailOffset) {
		this._cursorTrailOffset = cursorTrailOffset;
	}

	public Dimension getCursorTrailOffset() {
		return _cursorTrailOffset;
	}

	/**
	 * @param _componentsVisible the _componentsVisible to set
	 */
	public void setComponentsVisible(boolean componentsVisible) {
		this._componentsVisible = componentsVisible;
	}

	/**
	 * @return the _componentsVisible
	 */
	public boolean isComponentsVisible() {
		return _componentsVisible;
	}
	
	/**
	 * @param _cursorTrailVisible the _cursorTrailVisible to set
	 */
	public void setCursorTrailVisible(boolean cursorTrailVisible) {
		this._cursorTrailVisible = cursorTrailVisible;
	}

	/**
	 * @return the _cursorTrailVisible
	 */
	public boolean isCursorTrailVisible() {
		return _cursorTrailVisible;
	}
	
	public boolean isCursorTrailSet () {
		return ((_cursorTrailImage != null) ||
				(_cursorTrailComponent != null));
	}

	/**
	 * Paint all the regular components, like buttons, that have
	 * been added to the glass pane, plus the special overlay component
	 * that follows the cursor.
	 * 
	 * For an example of drawing a fixed-location rectangle on this
	 * glass pane, and a circle that follows the cursor: see 
	 * the commented out body of the superclass' paintComponent().
	 * 
	 * @see photoSpreadUtilities.OsmoticGlassPane#paintComponent(java.awt.Graphics)
	 */
	protected void paintComponent(Graphics g) {

		// Have all the standard components
		// on this glass pane be drawn. (Like
		// buttons, etc.):

		Graphics2D g2 = (Graphics2D) g;
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _alphaValue));
				
		if (_componentsVisible) {
			
			// Draw all components:
			// ******super.paintComponents(g2);
			
			// Draw labels again:
			Component compToShow;
			JLabel labelComp = null;
			Component[] comps = getComponents();
			for (int i=0; i<getComponentCount(); i++) {
				compToShow = comps[i];
			
				try {
					labelComp = (JLabel) compToShow;
				} catch (ClassCastException e) {
					// Component is not a JLabel
				}
				g2.drawImage(((ImageIcon) labelComp.getIcon()).getImage(), 0, 0, null);
			}
		}

		if (!_cursorTrailVisible)
			return;
		
		// Does the image of the overlay component
		// need to be created? Yes, if the
		// user set an overlay component, but
		// this is the first time that paintComponent()
		// was called since then, and we therefore haven't
		// created an image of the component yet:
		
		if ((_cursorTrailImage == null) &&
		    (_cursorTrailComponent != null)) {
			
			// Create a BufferedImage, get its graphics
			// context, and have the cursor trail component
			// paint itself onto it. So do this we want 
			// the component's preferred size, or if that's
			// unavailable, its current size. We guaranteed in
			// setTrailerComponent() that those are not both
			// zero:
			
			Dimension prefDim = _cursorTrailComponent.getPreferredSize();
			_cursorTrailComponent.setSize(prefDim);
			if (prefDim == null) {
				prefDim = new Dimension (_cursorTrailComponent.getWidth(),
										 _cursorTrailComponent.getHeight());
			}
			int cursorTrailWidth  = prefDim.width;
			int cursorTrailHeight = prefDim.height;
			_cursorTrailImage = new BufferedImage(
					cursorTrailWidth,
					cursorTrailHeight,
					BufferedImage.TYPE_INT_ARGB);
			Graphics2D gComponentImage = ((BufferedImage) _cursorTrailImage).createGraphics();
			_cursorTrailComponent.paint(gComponentImage);
		}

		// If an cursor trail image exists, draw it at
		// offset _overlayOffset from the cursor position:
		
		Point cursorSnapshot = getCursorLoc();
		if ((getCursorLoc() != null) && 			
			(_cursorTrailImage != null)) {
				g2.drawImage(
					_cursorTrailImage,
					// null,			  // No transform operation
					cursorSnapshot.x + _cursorTrailOffset.width, 
					cursorSnapshot.y + _cursorTrailOffset.height,
					null); // no ImageObserver
		}
			
		g2.dispose();
	}

	/****************************************************
	 * Main and/or Testing Methods
	 * @return 
	 *****************************************************/
/*
	private static void createAndShowGUI () {

		JFrame window = new JFrame();

		Dimension emptySpaceDim = new Dimension(50, 50);
		Box.Filler emptySpace1 = new Box.Filler(emptySpaceDim, emptySpaceDim, emptySpaceDim);
		Box.Filler emptySpace2 = new Box.Filler(emptySpaceDim, emptySpaceDim, emptySpaceDim);
		Box.Filler emptySpace3 = new Box.Filler(emptySpaceDim, emptySpaceDim, emptySpaceDim);

		JButton testButtonNorth = new JButton("North");
		JButton testButtonCenter= new JButton("Center");
		JButton testButtonSouth = new JButton("South");
		JButton buttonGlass = new JButton("Glass");
		
		buttonGlass.setBackground(Color.BLUE);

		OsmoticOverlayTracker glassPane = new OsmoticOverlayTracker(window);
		glassPane.setLayout(new BorderLayout());
		
		JPanel contentPane = (JPanel) window.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.setBackground(Color.YELLOW);

		contentPane.add(testButtonNorth, BorderLayout.NORTH);
		// contentPane.add(emptySpace2, BorderLayout.CENTER);
		contentPane.add(testButtonCenter, BorderLayout.CENTER);
		contentPane.add(testButtonSouth, BorderLayout.SOUTH);

		window.setGlassPane(glassPane);
		glassPane.setOpaque(false);
		glassPane.setVisible(true);

		glassPane.add(emptySpace1, BorderLayout.NORTH);
		glassPane.add(buttonGlass, BorderLayout.CENTER);
		glassPane.add(emptySpace2, BorderLayout.SOUTH);

		window.setContentPane(contentPane);

		window.setBounds(1000, 1000, 300, 300);
		window.pack();
		window.setVisible(true);

		// Make a component that will follow the
		// cursor at partial transparency as an
		// image on the glass pane:
		
		JButton xButton = new JButton("XMen");
		xButton.setPreferredSize(new Dimension(100,100));
		
		// Must ensure that the component gets properly sized:
		contentPane.add(xButton);
		window.pack();
		contentPane.remove(xButton);
		
		// Tell glass pane to use this button
		// as the cursor-following overlay: 
		glassPane.setCursorTrailer(xButton);
	}
	

	public static void main (String[] args) {


		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI(); 
			}
		});
		System.out.println("Done");

	}

*/	
}
