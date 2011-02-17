/**
 * 
 */
package photoSpreadUtilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import photoSpreadUtilities.Misc.ShowHelpAction;
import photoSpreadUtilities.Misc.WindowCloseAction;

/**
 * @author paepcke
 *
 */
public class Zoomer extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private int _magnificationIncrement = 10; // percent
	protected int _currentZoom = 100;
	private static final String _titleBarStartTxt = "PhotoSpread Zoom";
	
	private ZoomMouseClickAndWheelListener _mouseHandler = null;
	
	JPanel _panel = null;
	JScrollPane _scrollPane = null;
	JLabel _label = null;

	ImageIcon _fullSizeImgIcon = null;
		
	/****************************************************
	 * Constructor(s)
	 *****************************************************/
	
	public Zoomer (ImageIcon icon) {
		
		initZoomer(icon);
	}
	
	public Zoomer (String path) throws IOException {
		
		File file = new File(path);
		Image img = ImageIO.read(file);
		initZoomer(new ImageIcon(img));
	}
	
	private void initZoomer (ImageIcon imgIcon) {
		
		_fullSizeImgIcon = imgIcon;
		
		this.setTitle(_titleBarStartTxt + 
				": Initial Size (" +
				imgIcon.getIconWidth() +
				"x" +
				imgIcon.getIconHeight() +
				"). Full size: " +
				imgIcon.getImage().getWidth(null) +
				"x" +
				imgIcon.getImage().getHeight(null) +
				".");
		
		_panel = new JPanel();
		_panel.setBackground(Color.BLACK);
		_panel.setLayout(new BorderLayout());
		
		_label = new JLabel(_fullSizeImgIcon);
		_panel.add(_label, BorderLayout.CENTER);
		_mouseHandler = new ZoomMouseClickAndWheelListener();
		_panel.addMouseWheelListener(_mouseHandler);
		_panel.addMouseListener(_mouseHandler);
		_panel.addMouseMotionListener(_mouseHandler);
		
		// _panel.setSize(...
		
		// Make sure resources are deallocated 
		// when window is closed: 
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		_scrollPane = new JScrollPane(_panel);
		_scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		_scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.add(_scrollPane);

		ZoomAction zoomUpAction = new ZoomUpAction();
		ZoomAction zoomDownAction = new ZoomDownAction();
		WindowCloseAction winCloseAction = new Misc().new WindowCloseAction(this);
		
		Misc.bindKey(_panel, "control PLUS", zoomUpAction);
		Misc.bindKey(_panel, "control EQUALS", zoomUpAction);
		Misc.bindKey(_panel, "control shift EQUALS", zoomUpAction);
		
		Misc.bindKey(_panel, "control MINUS", zoomDownAction);
		Misc.bindKey(_panel, "control shift MINUS", zoomDownAction);
		
		Misc.bindKey(this, "control W", winCloseAction);
		Misc.bindKey(this, "F1", new ShowHelpAction(
				"To do in Zoomer Window", 
				"HelpFiles/zoomerHelp.html", 
				this));
		
		// Center the window on the screen.
		this.setLocationRelativeTo(null);
		
		this.setVisible(true);
		pack();
		addWindowListener(new ZoomWindowListener());
		requestFocus();

	}
	
	/****************************************************
	 * Actions
	 *****************************************************/
	
	abstract class ZoomAction extends AbstractAction implements ActionListener {
		
		private static final long serialVersionUID = 1L;

		protected boolean isShiftDown(ActionEvent e) {
			return ((e.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK);
		}
		
		public abstract void actionPerformed(ActionEvent e);
	}
	
	class ZoomUpAction extends ZoomAction {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {

			_currentZoom += _magnificationIncrement;
			if (isShiftDown(e))
				percentScaleImage(_currentZoom, Const.EXPAND_WINDOW);
			else
				percentScaleImage(_currentZoom, Const.DONT_EXPAND_WINDOW);
		}
	}

	class ZoomDownAction extends ZoomAction {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {

			_currentZoom -= _magnificationIncrement;
			if (isShiftDown(e))
				percentScaleImage(_currentZoom, Const.EXPAND_WINDOW);
			else
				percentScaleImage(_currentZoom, Const.DONT_EXPAND_WINDOW);
		}
	}

	
	/****************************************************
	 * ZoomMouseClickAndWheelListener (Inner) Class
	 *****************************************************/
	
	private class ZoomMouseClickAndWheelListener extends MouseAdapter {
		
		Point _cursorDownPoint = null;
		
		private boolean isShiftDown (MouseWheelEvent e) {
			return ((e.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK);
		}
		
		public void mouseWheelMoved (MouseWheelEvent e) {
			
			int notches = e.getWheelRotation();
			if (notches > 0) 
				_currentZoom += _magnificationIncrement;
			else
				_currentZoom -= _magnificationIncrement;
			
			if (isShiftDown(e))
				percentScaleImage(_currentZoom, Const.EXPAND_WINDOW);
			else
				percentScaleImage(_currentZoom, Const.DONT_EXPAND_WINDOW);
		}
		
		public void mousePressed(MouseEvent e) {
			
			if (e.getClickCount() == 2) {
				resetImage();
				return;
			}
			_cursorDownPoint = e.getPoint();
 		}
		
		public void mouseReleased(MouseEvent e) {
			_cursorDownPoint = null;
		}
		
		/* (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseDragged(java.awt.event.MouseEvent)
		 */
		public void mouseDragged (MouseEvent e) {
			
			if (_cursorDownPoint == null)
				return;
			
			Point currCursorLoc = e.getPoint();
			
			int deltaX = currCursorLoc.x - _cursorDownPoint.x;  
			int deltaY = currCursorLoc.y - _cursorDownPoint.y;
			
			int currImgX = _label.getLocation().x;
			int currImgY = _label.getLocation().y;
			
			_label.setLocation(currImgX + deltaX, currImgY + deltaY);
			_cursorDownPoint = currCursorLoc;
		}
		
	}
	
	public class ZoomWindowListener extends WindowAdapter implements WindowListener {

		/* (non-Javadoc)
		 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
		 */
		@Override
		public void windowActivated(WindowEvent arg0) {
			// _panel.requestFocus();
			
		}

/*		 (non-Javadoc)
		 * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
		 
		@Override
		public void windowDeiconified(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}

		 (non-Javadoc)
		 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
		 
		@Override
		public void windowIconified(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}

		 (non-Javadoc)
		 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
		 
		@Override
		public void windowOpened(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}
*/		
	}
	
	/****************************************************
	 * Methods
	 *****************************************************/
	
	/**
	 * Bind a key to an action. The binding will be
	 * active while the Workspace window is selected.
	 * 
	 * @param keyDescription A string describing the key as
	 * per KeyStroke.getKeyStroke(String). Ex: "alt A" or "ctrl UP" (for up-arrow). 
	 * Key names are the <keyName> part in VK_<keyName>
	 * @param action Action object to invoke when key is pressed.
	 */
	public void bindKey (String keyDescription, Action action) {
		
		InputMap keyMap = _panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = _panel.getActionMap();
		
		keyMap.put(KeyStroke.getKeyStroke(keyDescription), keyDescription);
		actionMap.put(keyDescription, action);
	}


	
	private static Double getAspectRatio (ImageIcon imgIcon) {
        return new Double(imgIcon.getIconHeight()) / new Double(imgIcon.getIconWidth());
	}
	
	
    /**
     * Given an image icon and a target width,
     * return a new image icon with an image of
     * the specified width, and proportionately 
     * scaled height.
     * @param srcImgIcon
     * @param newWidth Target width in pixels.
     * @return
     */
    private static Image getScaledImage(ImageIcon srcImgIcon, int newWidth){
        
        Image srcImg = srcImgIcon.getImage();
      
        Double aspectRatio = getAspectRatio(srcImgIcon);
        int newHeight = new Double(newWidth * aspectRatio).intValue();
        
        BufferedImage resizedImg = new BufferedImage(newWidth,   newHeight, BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, 0, 0, newWidth, newHeight, null);
        g2.dispose();
        
        return resizedImg;
    }
    
    /**
     * Scale image to the given percentage while keeping the window size constant.
     * @param targetPercentage Desired final percentage of image's original size.
     */
    public void percentScaleImage (int targetPercentage) {
    	percentScaleImage(targetPercentage, Const.DONT_EXPAND_WINDOW);
    }
    
    /**
     * Scale image to the given percentage. Caller chooses whether
     * enclosing window is to grow with the resize, or not.
     * @param targetPercentage Desired final percentage of image's original size.
     * @param expandWindow If true then the surrounding window grows/shrinks with the
     * enlargement/reduction
     */
    public void percentScaleImage (int targetPercentage, boolean expandWindow) {
        	
    	Image img = _fullSizeImgIcon.getImage();
    	
    	ImageIcon imgIcon = new ImageIcon(img);
    	int width = imgIcon.getIconWidth();

    	int newWidth = new Double (width * targetPercentage / 100f).intValue();
    	imgIcon.setImage(getScaledImage(imgIcon, newWidth));
    	
    	_panel.remove(_label);
    	_label = new JLabel(imgIcon);
    	_panel.add(_label);
    
    	if (expandWindow)
    		pack();
    	else
    		validate();
    	
    	setTitle(_titleBarStartTxt + 
				": " +
				targetPercentage +
				"% of " +
				_fullSizeImgIcon.getImage().getWidth(null) +
				"x" +
				_fullSizeImgIcon.getImage().getHeight(null) +
				" (" +
				imgIcon.getImage().getWidth(null) +
				"x" +
				imgIcon.getImage().getHeight(null) +
				").");
    }
    
    private void resetImage () {
    	percentScaleImage(100);
    	_label.setLocation(0,0);
    	pack();
    }
    

	/****************************************************
	 * Testing Methods
	 *****************************************************/
    
/*	public static void main (String args[]) {
		
		ImageIcon icon = new ImageIcon(
				"E:\\Users\\Paepcke\\dldev\\src\\PhotoSpreadTesting\\TestCases\\Photos\\airplaneInteriorWithEvacueesFacing.jpg");
		
		Zoomer zoomer = new Zoomer(icon);
	}*/
}
