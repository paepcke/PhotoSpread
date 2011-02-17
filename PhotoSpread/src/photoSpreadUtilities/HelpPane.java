/**
 * 
 */
package photoSpreadUtilities;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

import photoSpreadUtilities.Misc.WindowCloseAction;

/**
 * Light-weight help panel creation. Given either a plain text
 * or HTML string, or the pathname to a plain text or HTML file,
 * instances of this class put up a read-only dialog boxes with the
 * text. HTML is rendered, as long as it is very simple. 
 * 
 * The dialogs scroll if required; the user can resize them,
 * cnt-plus/cnt-minus increase/decrease the font, and cnt-w
 * dismisses the information dialogs.
 * 
 * 
 * @author paepcke
 *
 */
public class HelpPane extends JFrame {
	
	//********************   GOES TO Const *********************
	Dimension defaultHelpScreenSize = new Dimension (500, 600);
	float fontEnlargementFactor = 01.25f;
	float fontReductionFactor   = 0.80f;
	//********************   END GOES TO Const *********************

	private static final long serialVersionUID = 1L;
	JHTMLTextArea _textPane = new JHTMLTextArea();
	JScrollPane _scrollPane;
	Insets _matteInsets = new Insets (
			30, // top
			20, // left
			10, // bottom
			20);  // right

	/****************************************************
	 * Constructor(s)
	 *****************************************************/

	/**
	 * @param title Title in dialog title border.
	 * @param helpText HelpText object that wraps help text (plain or HTML).
	 */
	
	public HelpPane (String title, HelpString helpText) {
		doLoad(_textPane, helpText);
		initHelpPane(title, null);
	}
	
	/**
	 * @param title Title in dialog title border.
	 * @param helpText HelpText object that wraps help text (plain or HTML).
	 * @param parentFrame Frame within which the help dialog is to be shown. If null, 
	 * shows centered on screen.
	 */
	public HelpPane (String title, HelpString helpText, JFrame parentFrame) {
		doLoad(_textPane, helpText);
		initHelpPane(title, parentFrame);
	}
	
	/**
	 * @param title Title in dialog title border.
	 * @param helpFilePath File path to file with help text (plain or HTML).
	 * @throws FileNotFoundException 
	 */
	public HelpPane (String title, String helpFilePath) throws FileNotFoundException {
		doLoad(_textPane, helpFilePath);
		initHelpPane(title, null);
	}
	
	/**
	 * @param titled Title in dialog title border.
	 * @param helpFilePath File path to file with help text (plain or HTML).
	 * @param parentFrame Frame within which the help dialog is to be shown. If null, 
	 * shows centered on screen.
	 * @throws FileNotFoundException 
	 */
	public HelpPane (String title, String helpFilePath, JFrame parentFrame) throws FileNotFoundException {
		doLoad(_textPane, helpFilePath);
		initHelpPane(title, parentFrame);
	}
	
	public HelpPane (String title, InputStream helpStream) {
		doLoad(_textPane, helpStream);
		initHelpPane(title, null);
	}

	public HelpPane (String title, InputStream helpStream, JFrame parentFrame) {
		doLoad(_textPane, helpStream);
		initHelpPane(title, parentFrame);
	}
	
	
	private void initHelpPane (String title, JFrame parentFrame) {
		
		setTitle(title);
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		// Center the window on the screen.
		setLocationRelativeTo(parentFrame);
				
		JPanel content = (JPanel) getContentPane();
		content.setLayout(new BorderLayout());
		content.setBorder(new MatteBorder(_matteInsets, Color.BLUE));
		
		_textPane.setBorder(BorderFactory.createRaisedBevelBorder());

		_scrollPane = new JScrollPane(
				_textPane,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		_scrollPane.setWheelScrollingEnabled(true);
		
		content.add(_scrollPane, BorderLayout.CENTER);

		// Make cnt-+/- enlarge/reduce the font:
		FontLargerAction fontLargerAction = new FontLargerAction(_textPane);
		FontSmallerAction fontSmallerAction = new FontSmallerAction(_textPane);
		WindowCloseAction winCloseAction = new Misc().new WindowCloseAction(this);
		
		Misc.bindKey(this, "control PLUS", fontLargerAction);
		Misc.bindKey(this, "control EQUALS", fontLargerAction);
		Misc.bindKey(this, "control shift EQUALS", fontLargerAction);
		
		Misc.bindKey(this, "control MINUS", fontSmallerAction);
		Misc.bindKey(this, "control shift MINUS", fontSmallerAction);

		// Easy way to close window via cnt-w:
		Misc.bindKey(this, "control W", winCloseAction);

		setSize(defaultHelpScreenSize);
		//parentFrame.add(content);
		// setSize(getWidth(), 3*getHeight()/2);
		// setSize(content.getWidth(), content.getHeight());
		// pack();
		setVisible(true);
	}

	/****************************************************
	 * JHTMLTextArea Inner Class
	 *****************************************************/

	/**
	 * Extension of JTextArea that renders HTML or plain text,
	 * and resizes nicely when users resize the enclosing window.
	 *
	 */
	class JHTMLTextArea extends JTextArea {

		private static final long serialVersionUID = 1L;
		private String _text = "";
		private Insets _htmlMargin = new Insets(10,10,10,10);
		private View _stringView = null;
		
		public JHTMLTextArea () {
			super(1,1);
			setEditable(false);
		}

		public JHTMLTextArea (String text) {
			super(text, 1,1);
			setText(text);
		}
		
		public void setText (String text) {
			_text = text;
			_stringView = BasicHTML.createHTMLView(this, text);
		}
		
		public String getText () {
			return _text;
		}
		
		public Dimension getPreferredSize() {
			Dimension prefSize = new Dimension();
			prefSize.width = getAvailableSize().width;
			prefSize.height = defaultHelpScreenSize.height;
			return prefSize;
		}
		
		public Dimension getAvailableSize() {

			Dimension prefSize = getSize(); // of Frame
			prefSize.width -= (
					  _matteInsets.left
					+ _matteInsets.right
					+ _htmlMargin.left
					+ _htmlMargin.right);
			
			prefSize.height -= (
					  _matteInsets.top 
					+ _matteInsets.bottom
					+ _htmlMargin.top
					+ _htmlMargin.bottom);
			
			return prefSize;
		}
		
		protected void paintComponent(Graphics g) {
			
			super.paintComponent(g);
			
			Dimension newDim = getAvailableSize();
			Point origin = new Point (_htmlMargin.left, _htmlMargin.top);
			Rectangle newRenderArea = new Rectangle(origin, newDim);
			_stringView.paint(g, newRenderArea);
		}
	}
	
	/****************************************************
	 * Class HelpString
	 *****************************************************/

	/**
	 * Wrapper class for a string that is used as a 
	 * help text. Purely to make convenient HelpPane
	 * constructors via polymorphism.
	 *
	 */
	public static final class HelpString {
		
		String _theHelpString;
		
		public HelpString (String theHelpString) {
			_theHelpString = theHelpString;
		}
		
		public String getString () {
			return _theHelpString;
		}
	}
	
	/****************************************************
	 * Action(s)
	 *****************************************************/

	public class FontLargerAction extends AbstractAction implements Action, ActionListener{

		private static final long serialVersionUID = 1L;
		private JHTMLTextArea _textArea = null;

		public FontLargerAction () {
			throw new RuntimeException("Font action requires a JHTMLTextArea constructor argument.");
		}

		public FontLargerAction (JHTMLTextArea textArea) {
			_textArea = textArea;
		}

		public void actionPerformed(ActionEvent evt) {
			Font font = _textArea.getFont();
			_textArea.setFont(font.deriveFont(fontEnlargementFactor * font.getSize2D()));
			_textArea.setText(_textArea.getText());
			_textArea.revalidate();
			_textArea.repaint();
		}
	}

	public class FontSmallerAction extends AbstractAction implements Action, ActionListener{

		private static final long serialVersionUID = 1L;
		private JHTMLTextArea _textArea = null;

		public FontSmallerAction () {
			throw new RuntimeException("Font action requires a JHTMLTextArea constructor argument.");
		}

		public FontSmallerAction (JHTMLTextArea textArea) {
			_textArea = textArea;
		}

		public void actionPerformed(ActionEvent evt) {
			Font font = _textArea.getFont();
			_textArea.setFont(font.deriveFont(fontReductionFactor * font.getSize2D()));
			_textArea.setText(_textArea.getText());
			_textArea.revalidate();
			_textArea.repaint();
		}
	}


	/****************************************************
	 * Methods
	 *****************************************************/

	public static void doLoad (JHTMLTextArea textComponent, HelpString helpText) {
		textComponent.setText(helpText.getString());
	}
	
	public static void doLoad (JHTMLTextArea textComponent, InputStream helpTextStream) {
		BufferedReader fileReader = new BufferedReader(new InputStreamReader(helpTextStream));
		doLoad(textComponent, fileReader);
	}

	public static void doLoad (JHTMLTextArea textComponent, String helpTextFilePath) throws FileNotFoundException {
		BufferedReader fileReader = new BufferedReader( new FileReader(helpTextFilePath));
		doLoad(textComponent, fileReader);
	}
	
	public static void doLoad (JHTMLTextArea textComponent, BufferedReader fileReader) {

		String line;
		String fullText = "";

		try {
			line = fileReader.readLine();
			while (line != null) {
				fullText += line;
				line = fileReader.readLine();
			}
			textComponent.setText(fullText);

		} catch (IOException exception) {
			// File-not-found exceptions would have occurred earlier,
			// when the BufferedReader was created. We ignore this one.
		} finally {
			if (fileReader != null) {
				try {
					fileReader.close();
				} catch (IOException ignoredException) {
				}
			}
		}
	}

	/****************************************************
	 * Methods for Testing (Main)
	 *****************************************************/


	public static void main(String args[]) {
		URL helpFileURLs[] = new URL[20];
		String sheetHelpFileSpec = "file:///HelpFiles/sheetHelp.html";
		try {
			
			helpFileURLs[0] = new URL(sheetHelpFileSpec);
			
		} catch (MalformedURLException e) {
			Misc.showErrorMsgAndStackTrace(e, "");
			//e.printStackTrace();
		}
		
		new HelpPane("HTML String Help", new HelpString("<html>Hello <b> there </b>.</html>"));
		new HelpPane("String Help", new HelpString("Hello there again."));
	}
}
