package photoSpreadUtilities;

import java.awt.AWTEvent;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

// import photoSpread.PhotoSpread;
// import photoSpreadObjects.photoSpreadComponents.ObjectsPanel;

/**
 * @author Alexander Potochkin
 * @author Modified by Andreas Paepcke
 * 
 * A glass pane that is truly transparent to
 * all events, and that honors cursor replacement.
 * This abstract class takes care of the event
 * transparency. Painting must be done in a
 * paintComnponent() override of a subclass.
 * Known subclasses: OsmoticOverlayTracker.
 * 
 *

/**
 * Modified from:
 * http://weblogs.java.net/blog/alexfromsun/
 * <p/>
 * This GlassPane is transparent for MouseEvents,
 * and respects any underlying components' cursors.
 * It is also friendly to other users who install 
 * mouse listeners.
 *
 */
public abstract class OsmoticGlassPane extends JPanel implements AWTEventListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final JFrame _frame;
	private Point _cursorLoc = new Point();

	/****************************************************
	 * Constructor(s)
	 *****************************************************/
	
	public OsmoticGlassPane(JFrame frame) {
		super(null);
		this._frame = frame;
		setOpaque(false);
		getToolkit().addAWTEventListener(this, ALLBITS);
	}

/*	
	/**
	 * @param _cursorLoc the _cursorLoc to set
	 *//*
	protected void setCursorLoc(Point cursorLoc) {
		this._cursorLoc = cursorLoc;
	}
*/
	/**
	 * @return the _cursorLoc
	 */
	protected Point getCursorLoc() {
		return _cursorLoc;
	}

	
	protected abstract void paintComponent(Graphics g);
	
/*	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.GREEN.darker());
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
		int d = 22;
		g2.fillRect(getWidth() - d, 0, d, d);
		if (_cursorLoc != null) {
			g2.fillOval(_cursorLoc.x + d, _cursorLoc.y + d, d, d);
		}
		g2.dispose();
	}
*/

	public void eventDispatched(AWTEvent event) {
		if (event instanceof MouseEvent) {
			MouseEvent me = (MouseEvent) event;
			if (!SwingUtilities.isDescendingFrom(me.getComponent(), _frame)) {
				return;
			}
			if (me.getID() == MouseEvent.MOUSE_EXITED && me.getComponent() == _frame) {
				_cursorLoc = null;
			} else {
				MouseEvent converted = SwingUtilities.convertMouseEvent(
						me.getComponent(), 
						me, 
						_frame.getGlassPane());
				_cursorLoc = converted.getPoint();
			}
			repaint();
		}
	}

	/**
	 * If someone added a mouseListener to the JPanel,
	 * or has set a new cursor, then we expect that 
	 * he knows what he is doing, and we return the 
	 * super.contains(x, y) result.
	 * Otherwise we return false to respect the cursors
	 * of the components underneath the glass pane
	 */
	public boolean contains(int x, int y) {
		if (getMouseListeners().length == 0 && getMouseMotionListeners().length == 0
				&& getMouseWheelListeners().length == 0
				&& getCursor() == Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) {
			return false;
		}
		return super.contains(x, y);
	}

	/****************************************************
	 * Main and/or Testing Methods
	 * @return 
	 *****************************************************/

/*	private static void createAndShowGUI () {

		JFrame window = new JFrame();
		// window.setPreferredSize(new Dimension(200,200));
		JButton button1 = new JButton("Test Button");
		button1.setPreferredSize(new Dimension(50, 20));

		OsmoticGlassPane glassPane = new OsmoticGlassPane(window);
		// glassPane.setLayout(new BorderLayout());

		JPanel contentPane = (JPanel) window.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(button1, BorderLayout.NORTH);

		window.pack();
		window.setBounds(1000, 1000, 300, 300);
		window.setVisible(true);

		window.setGlassPane(glassPane);
		// JButton floatButton = new JButton ("On Glass");
		// glassPane.add(floatButton, BorderLayout.SOUTH);
		glassPane.setVisible(true);

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
