/**
 * 
 */
package edu.stanford.photoSpreadUtilities;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;
import java.text.ParseException;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import edu.stanford.photoSpread.PhotoSpread;
import edu.stanford.photoSpreadLoaders.CSVFileFilter;
import edu.stanford.photoSpreadUtilities.HelpPane.HelpString;

/**
 * @author paepcke
 * 
 * Useful utilities of various sorts.
 *
 */
public final class Misc {

	static String reportToAdminMsg = "\nPlease copy/paste and send to the poor schlemiel who fixes this stuff:";
	// The JFileChooser() is very slow. Known problem on the Web
	// TODO: Switch file choosing to SWT:
	static final JFileChooser fc = new JFileChooser();
	
	/**
	 * @author paepcke
	 * Inner class to construct a Pair of two
	 * items that may be of differing type.
	 * Access the two parts as foo.first()
	 * and foo.second();
	 *
	 * @param <T1> Any item of type T
	 * @param <T2> Any item of type T
	 */

	/****************************************************
	 * Pair Inner Class
	 *****************************************************/


	public class Pair<T1, T2> {

		T1 _firstItem;
		T2 _secondItem;

		public Pair (T1 obj1, T2 obj2) {
			_firstItem= obj1;
			_secondItem = obj2;
		}

		public T1 first () {
			return _firstItem;
		}

		public T2 second() {
			return _secondItem;
		}

	}

	/****************************************************
	 * WindowCloseAction Inner Class
	 *****************************************************/

	public class WindowCloseAction extends AbstractAction implements Action, ActionListener{

		private static final long serialVersionUID = 1L;

		private JFrame _win = null;

		public WindowCloseAction () {

		}

		public WindowCloseAction (JFrame frameToClose) {
			_win = frameToClose;
		}

		public void actionPerformed(ActionEvent e) {

			try {
				if (_win == null)
					_win =  (JFrame) SwingUtilities.getWindowAncestor((Component) e.getSource());
			} catch (Exception exc) {
				return;
			}

			if (_win != null) {
				_win.dispose();
			}
		}
	}

	/****************************************************
	 * ShowHelpAction Inner Class
	 *****************************************************/

	/**
	 * @author paepcke
	 *
	 * Given the name of a help file, this class can display 
	 * a help panel. Help files are in a directory called HelpFiles,
	 * whose parent directory must be on the classpath. 
	 */
	public static class ShowHelpAction extends AbstractAction implements Action, ActionListener{

		private static final long serialVersionUID = 1L;
		private String _winTitle = null;
		private JFrame _parentFrame = null;
		private InputStream _helpFileStream = null;
		private HelpString _helpText= null;
		
		/**
		 * Given a relative path, like "HelpFiles/foo.html",
		 * return an input stream that points to the respective resource.
		 * 'Relative' is with respect to the location of the
		 * main method's class location.
		 * 
		 * This method will work within Eclipse, and also
		 * when the resources are included in an executable
		 * jar file (as files, not within their own nested jar).
		 * 
		 * To read from a text file resource: 
		 *       BufferedReader fileReader = new BufferedReader(new InputStreamReader(helpTextStream));
		 *       String line = fileReader.readLine();
		 *	     while (line != null) {
		 *		   fullText += line;
		 *		   line = fileReader.readLine();
		 * 	     }
		 *
		 * 
		 * @param pathName
		 * @return InputStream from the resource, or null.
		 * @throws java.io.FileNotFoundException 
		 */
		public static InputStream getResource(String pathName) throws java.io.FileNotFoundException {
			
			File resFileObj = new File(pathName);
			if (resFileObj.isAbsolute())
				return new FileInputStream(resFileObj);
			
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			return loader.getResourceAsStream(pathName);
		}

		public ShowHelpAction (String winTitle, String helpFilePath) {
			_winTitle = winTitle;
			try {
				_helpFileStream = getResource(helpFilePath);
			} catch (java.io.FileNotFoundException e) {
				showErrorMsg("Failed to find help file '" + helpFilePath + "'.");
			}
		}

		public ShowHelpAction (String winTitle, HelpString helpText) {
			_winTitle = winTitle;
			_helpText = helpText;
		}

		public ShowHelpAction (String winTitle, String helpFilePath, JFrame parentFrame) {
			_winTitle = winTitle;
			try {
				_helpFileStream = getResource(helpFilePath);
			} catch (java.io.FileNotFoundException e) {
				showErrorMsg("Failed to find help file '" + helpFilePath + "'.");
			}
			_parentFrame = parentFrame;
		}

		public ShowHelpAction (String winTitle, HelpString helpText, JFrame parentFrame) {
			_winTitle = winTitle;
			_helpText = helpText;
			_parentFrame = parentFrame;
		}

		public void makeHelpPaneVisible() {
			if (_helpFileStream == null) 
				new HelpPane(_winTitle, _helpText, _parentFrame);
			else
				new HelpPane(_winTitle, _helpFileStream, _parentFrame);
		}
		
		public void actionPerformed(ActionEvent e) {
			makeHelpPaneVisible();
		}
	}

	/****************************************************
	 * AppExitWithConfirmAction Inner Class
	 *****************************************************/

	/**
	 * @author paepcke
	 * Action that asks user's OK to exit the entire program.
	 * Instances of this action can be bound to keys, such
	 * as cnt-shift-w
	 */
	public static class AppExitWithConfirmAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;
		String _exitConfirmation = "";
		JFrame _frameToShowIn = null;

		
		public AppExitWithConfirmAction (String confirmMsg) {
			_exitConfirmation = confirmMsg;
		}

		public AppExitWithConfirmAction (String confirmMsg, JFrame frameToShowIn) {
			this(confirmMsg);
			_frameToShowIn = frameToShowIn;
		}
		
		public void actionPerformed (ActionEvent e) {
			exitIfUserWants(_exitConfirmation, _frameToShowIn);
		}
	}

	/****************************************************
	 * SpaceFiller Inner Class
	 *****************************************************/
	
	public static class SpaceFiller extends Box.Filler {
		
		private static final long serialVersionUID = 1L;

		boolean _isTransparent;
		Dimension _dim;
		
		public SpaceFiller (Dimension dim, boolean isTransparent) {
			super(dim, dim, dim);
			_isTransparent = isTransparent;
			_dim = dim;
			if (_isTransparent)
				setOpaque(false);
			else
				setOpaque(true);
		}
		
		public void paintComponent(Graphics g) {
			if (_isTransparent)
				return;
			super.paintComponent(g);
		}
	}
	
	/****************************************************
	 * Methods
	 *****************************************************/
	
	/**
	 * Given filename string, make sure it has
	 * the specified extension. If another extension
	 * exists, it is replaced. Throws error if
	 * filename is a directory.
	 * 
	 * @param fileName
	 * @param desiredExtension
	 * @return
	 * @throws ParseException
	 */
	public static String ensureFileExtension(String fileName, String desiredExtension) throws ParseException {

		// If user passed in a directory, throw error:

		if (denotesDirectory(fileName))
			throw new ParseException("Cannot add extension '" + 
					desiredExtension + 
					"' to '" + fileName + 
					"', which is a directory.", 0);

		// Make sure the passed-in extension
		// doesn't have dots:

		desiredExtension = Misc.trim(desiredExtension, '.');
		int dotPos = fileName.lastIndexOf(".");

		// If file is without extension, just append the desired one:
		if (dotPos == -1) return fileName + '.' + desiredExtension;

		// If file name already has the right extension, great:
		String currExtension = fileName.substring(dotPos + 1);
		if (currExtension.equals((String) desiredExtension)) return fileName;


		return fileName.substring(0, dotPos + 1) + desiredExtension; 
	}

	public static boolean denotesDirectory(String path) {
		if (path.endsWith("/") || path.endsWith("\\"))
			return true;
		else
			return false;
	}

	/**
	 * Given a File object, returns its path's extension, if present.
	 * 
	 * @param f
	 * @return Extension of given File object's path, or null.
	 */
	public static String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 &&  i < s.length() - 1) {
			ext = s.substring(i+1).toLowerCase();
		}
		return ext;
	}
	
	/**
	 * Reads a file storing intermediate data into a list if file is just too,
	 * too big.
	 * 
	 * @param fileName the file to be read
	 * @return Byte array with file contents
	 * @throws IOException
	 */
	public static byte[] readBigFile(String fileName) throws IOException {

		final int BUF_LEN = 10000 * 1024; // 10MB

		InputStream in = null;
		final int noOffset = 0;
		final byte[] buf = new byte[BUF_LEN];
		// List of bufLen pieces of the read data:
		List<byte[]> chunkList = new ArrayList<byte[]>();

		try {
			in = new BufferedInputStream(new FileInputStream(fileName));
			int lenRead = 0;
			while ((lenRead = in.read(buf, noOffset, BUF_LEN)) != -1) {
				// If all the file's data fit into the first
				// chunk, then we're done:
				if ((lenRead < BUF_LEN) && chunkList.isEmpty()) {
					return buf;
				}
				// Not done reading, or we already read at least
				// one chunk. Create a new chunk and copy the just-read
				// data into it:
				byte[] tmp = new byte[lenRead];
				System.arraycopy(buf, noOffset, tmp, noOffset, lenRead); // still
																			// need
																			// to
																			// do
																			// copy
				// Append to our growing list of chunks:
				chunkList.add(tmp);
			}
			/*
			 * This part is optional. This method could instead return the
			 * arrayList of data chunks. But here we consolidate everything into
			 * one big buffer:
			 */

			// If only one chunk was read, return it.
			// This would only happen if the file is exactly
			// bufLen bytes long:
			if (chunkList.size() == 1)
				return (byte[]) chunkList.get(0);

			int totalLength = 0;
			for (int i = 0; i < chunkList.size(); i++)
				totalLength += ((byte[]) chunkList.get(i)).length;
			byte[] resBuf = new byte[totalLength]; // Final output buffer

			int totalBytesCopied = 0;
			for (int i = 0; i < chunkList.size(); i++) {
				byte[] oneChunk = (byte[]) chunkList.get(i);
				System.arraycopy(oneChunk, noOffset, resBuf, totalBytesCopied,
						oneChunk.length);
				totalBytesCopied += oneChunk.length;
			}
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (Exception e) {
				}
		}
		return buf;
	}
	
	
	public static ArrayList<File> flattenFiles(ArrayList<File> files) {
		
		ArrayList<File> flatFiles = new ArrayList<File>();
		
		for (File f : files) {
			if(f.isDirectory()){
				flatFiles.addAll(flattenFiles(f.listFiles()));
			}
			else{
				flatFiles.add(f);
			}
		}
		return flatFiles;
	}

	public static ArrayList<File> flattenFiles(File[] files){
		
		ArrayList<File> flatFiles = new ArrayList<File>();
		for(int i = 0; i < files.length; i++){
			File f = files[i];
			if(f.isDirectory()){
				flatFiles.addAll(flattenFiles(f.listFiles()));
			}
			else{
				flatFiles.add(f);
			}
		}
		return flatFiles;
	}
	
	/**
	 * Given a string, like C3 or AY390, return 
	 * a corresponding CellCoordinates object, or null if
	 * error.
	 * @param cellSpec String of a cell name
	 * @return CellCoordinates corresponding to given name, or null if parsing error.
	 */
	public static CellCoordinates getCellAddress(String cellSpec) {
		
		String colName;
		int rowInt = 0;
		int pos = -1;
		for (char c : cellSpec.toCharArray()) {
			if (Character.getType(c) == Character.UPPERCASE_LETTER) {
				pos++;
				continue;
			}
		}
		if (pos < 0)
			return null;
		
		colName = new String(cellSpec.substring(0, pos + 1));
		
		for (char c : cellSpec.substring(pos + 1).toCharArray())
			if (Character.getType(c) != Character.DECIMAL_DIGIT_NUMBER)
				return null;
		
		try {
			rowInt = new Integer(cellSpec.substring(pos + 1));
		} catch (Exception e) {
			return null;
		}
		
		if (rowInt < 1)
			return null;
		
		// Make row 0-based:
		return(new CellCoordinates(--rowInt, excelColToInt(colName)));
	}

	public static String getCellAddress(CellCoordinates coords) {
		return getCellAddress(coords.row(), coords.column());
	}

	public static String getCellAddress(int row, int col) {
		if (col == 0) return "";
		return intToExcelCol(col) + (++row);
	}

	/**
	 * Given an int between 1 and 701, return string "A" through "ZZ"
	 * as per Excel spreadsheet column names.
	 * 
	 * @param col
	 * @return
	 * @throws InvalidParameterException
	 */
	public static String intToExcelCol(int col) throws InvalidParameterException {
		if (col < 1)
			throw new InvalidParameterException("Only numbers above zero can be converted to Excel column names.");
		return intToExcelColHelper(--col);
	}

	public static String intToExcelColHelper(int col) {

		final int radix = 26;
		char excelDigit;

		if (col < radix) {
			excelDigit = (char)(((int)'A') + col);
			return String.valueOf(excelDigit);
		}
		else
			return intToExcelColHelper((int)Math.floor(col/radix) - 1) + intToExcelColHelper(col % radix);
	}

	/**
	 * Given an Excel column name, return the corresponding
	 * column number. Column A is 1.
	 * 
	 * @param colName
	 * @return Integer corresponding to the Excel name (A<==>1)
	 */
	public static int excelColToInt(String colName) {

		int res = 0;
		int radix   = 26;
		StringCharacterIterator it = new StringCharacterIterator(colName); 

		for(char excelDigit = it.first(); excelDigit != StringCharacterIterator.DONE; excelDigit = it.next()) {
			res = (res*radix) + excelDigit - (int)'A' + 1;
		}
		return res;
	}

	/**
	 * Given a string and a char, trim all occurrences of
	 * the char from both the front and back of the string.
	 * 
	 * @param str
	 * @param trash
	 * @return Cleaned string.
	 */
	public static String trim(String str, char trash) {
		
		while (str.startsWith(String.valueOf(trash)))
			str = str.substring(1);
		while (str.endsWith(String.valueOf(trash)))
			str = str.substring(0, str.length() - 1);

		return str;
	}
	
	public static String trim(String str, String trash) {
		
		while (str.startsWith(trash)) {
			str = str.substring(trash.length());
		}
		
		while (str.endsWith(trash)) {
			str = str.substring(0, str.length() - trash.length());
		}
		
		return str;
	}
	
	/**
	 * Determines whether a value is PhotoSpread's special null.
	 * @param arg
	 * @return True if argument is a PhotoSpread null, else false
	 */
	public boolean isNull(Object arg) {
		try {
			return ((String) arg).equals(Const.NULL_VALUE_STRING);
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Given a string, return a string with all XML special
	 * characters escaped.
	 * @param strToConvert
	 * @return XML-safe string.
	 */
	public static String escapeXMLSpecials(String strToConvert) {
	
	    StringBuffer buffer = new StringBuffer();
	    for(int i = 0;i < strToConvert.length();i++)
	    {
	       char c = strToConvert.charAt(i);
	       if(c == '<')
	          buffer.append("&lt;");
	       else if(c == '>')
	          buffer.append("&gt;");
	       else if(c == '&')
	          buffer.append("&amp;");
	       else if(c == '"')
	          buffer.append("&quot;");
	       else if(c == '\'')
	          buffer.append("&apos;");
	       else
	          buffer.append(c);
	    }
	    return buffer.toString();
	}

	/**
	 * Bind a key to an action on a particular component. 
	 * The binding will be active while the surrounding 
	 * window is selected. Binding keys to a container,
	 * like JFrame needs to use the root pane. The
	 * polymorphic bindKey(JFrame) takes care of that.
	 * 
	 * @param comp The component to which the action will be attached.
	 * @param keyDescription A string describing the key as
	 * per KeyStroke.getKeyStroke(String). Ex: "alt A" or "ctrl UP" (for up-arrow). 
	 * Key names are the <keyName> part in VK_<keyName>
	 * @param action Action object to invoke when key is pressed.
	 */
	public static void bindKey (JComponent comp, String keyDescription, Action action) {

		InputMap keyMap = comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = comp.getActionMap();

		keyMap.put(KeyStroke.getKeyStroke(keyDescription), keyDescription);
		actionMap.put(keyDescription, action);
	}

	public static void bindKey (JFrame frame, String keyDescription, Action action) {
		bindKey(frame.getRootPane(), keyDescription, action);
	}
	
	/**
	 * Given a string with two integers, produce a Pair object
	 * with the two integers. Error checking is performed on
	 * the string.
	 * 
	 * @param strOfTwoSpaceSeparatedInts
	 * @return Pair object containing two ints.
	 */

	public static Pair<Integer, Integer> twoIntsFromString (String twoIntsStr) {

		String strOfTwoSpaceSeparatedInts = ((String) twoIntsStr);

		// Partition the string by whitespace. The 3 tells the
		// split to return at most 3 elements: the first two
		// (hopefully) numbers, and any rest of the string. The
		// latter we ignore:

		String[] intStrArray = strOfTwoSpaceSeparatedInts.split("[ \t\n\f\r]", 3);
		if (intStrArray.length < 2) {
			throw new NumberFormatException("Expected a string with two integers. Was given '" + 
					strOfTwoSpaceSeparatedInts + "'.");
		}

		int first, second = 0;

		try {
			first = Integer.parseInt(intStrArray[0]);
			second = Integer.parseInt(intStrArray[1]);

		} catch (NumberFormatException e) {
			throw new RuntimeException(new NumberFormatException("Expected a string with two integers. Was given '" + 
					strOfTwoSpaceSeparatedInts + "'."));
		}

		return new Misc().new Pair<Integer, Integer>(first, second);
	}
	

	/**
	 * Create an ImageIcon from a file name. 
	 * @param path File containing a Java-understandable file (.gif, .jpg, .png)
	 * @param description Description of image for assistive technologies.
	 * @return ImageIcon or null if file not found.
	 */
	public static ImageIcon createImageIcon(String path, String description) {
	    java.net.URL imgURL = ClassLoader.getSystemResource(path);
	    if (imgURL != null) {
	        return new ImageIcon(imgURL, description);
	    } else {
	    	return null;
	    	// throw new PhotoSpreadException.FileIOException("Image file " + path + " not found.");
	    }
	}

	/**
	 * Create an ImageIcon from a file name. 
	 * @param path File containing a Java-understandable file (.gif, .jpg, .png)
	 * @param description Description of image for assistive technologies.
	 * @return ImageIcon or null if file not found.
	 */
	public static ImageIcon createImageIcon(String path) {
		return createImageIcon(path, "");
	}
	
	public static ImageIcon createImageIcon(String path, Dimension dim) {
		
		ImageIcon imgIcon = createImageIcon(path, "");
		if (imgIcon == null)
			return null;
		BufferedImage buffImg = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = buffImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(imgIcon.getImage(), 0, 0, dim.width, dim.height, null);
        g2.dispose();
        return new ImageIcon(buffImg);
	}
	
    public static Image getScaledImage(ImageIcon srcImgIcon, int h, int w){
        
        Image srcImg = srcImgIcon.getImage();
      
        Double scale = new Double(srcImgIcon.getIconHeight()) / new Double(srcImgIcon.getIconWidth());
        int scaledHeight = new Double(w * scale).intValue();
        
        BufferedImage resizedImg = new BufferedImage(w,   scaledHeight, BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, 0, 0, w, scaledHeight, null);
        g2.dispose();
        
        
        return resizedImg;
    }

	  public static final Component getVisibleChildAt(Container container, Point p) {
	    for (int i = 0; i < container.getComponentCount(); i++) {
	      Component c = container.getComponent(i);
	      if (c.isVisible() && c.contains(p.x - c.getX(), p.y - c.getY()))
	        return c;
	    }

	    return null;
	  }
   

	/**
	 * Use for error message dialog boxes that just call for clicking OK.
	 * @param msg Message to display in the dialog box
	 */

	public static void showErrorMsg(String msg) {
		showErrorMsg(msg, null);
	}
	
	public static void showErrorMsgOnSheetWindow(String msg) {
		showErrorMsg(msg, PhotoSpread.getCurrentSheetWindow());
	}
	
	/**
	 * For multi-line error messages. Use for error message 
	 * dialog boxes that just call for clicking OK.
	 * @param msgs ArrayList of message strings to display in the dialog box, 
	 * one line per ArrayList item.
	 */

	public static void showErrorMsgs(ArrayList<String> msgs) {
		showErrorMsgArray(msgs, null);

	}
	
	/**
	 * For multi-line error messages. Use for error message 
	 * dialog boxes that just call for clicking OK.
	 * Displays the box on top of the current sheet window.
	 * @param msgs ArrayList of message strings to display in the dialog box, 
	 * one line per ArrayList item.
	 */

	public static void showErrorMsgsOnSheetWindow(ArrayList<String> msgs) {
		showErrorMsgArray(msgs, PhotoSpread.getCurrentSheetWindow());
	}
	
	
	public static void showErrorMsg(Exception e, String msg) {
		String fullMsg = msg + " (" + e.getClass().getName() + "): " + e.getMessage();
		showErrorMsg(fullMsg);
	}
	
	public static void showErrorMsgAndStackTrace(Exception e) {
		ArrayList<String> msgs = new ArrayList<String>();
		msgs.add(e.getMessage());
		msgs.add(reportToAdminMsg);
		msgs.addAll(prepareStackTraceOutput(e));
		
		showErrorMsgs(msgs);
		/*showErrorMsg(
				e.getMessage() +
				reportToAdminMsg +
				prepareStackTraceOutput(e));
				*/
	}

	public static void showErrorMsgAndStackTrace(Exception e, String msg) {

		ArrayList<String> fullMsg = new ArrayList<String>();
		
		fullMsg.add(msg);
		fullMsg.add(e.getMessage());
		fullMsg.add(reportToAdminMsg);
		fullMsg.addAll(prepareStackTraceOutput(e));	
		showErrorMsgs(fullMsg);
	}
	
	private static ArrayList<String> prepareStackTraceOutput(Exception e) {
		String oneStackTraceLine = "";
		ArrayList<String> fullMsg = new ArrayList<String>();
		
		for (StackTraceElement traceEntry : e.getStackTrace()) {
			oneStackTraceLine = 
				"" + "at " + 
				traceEntry.getFileName() + 
				"(" + 
				traceEntry.getMethodName() +
				":" +
				traceEntry.getLineNumber() + 
				")";
			fullMsg.add(oneStackTraceLine);
		}
		return fullMsg;
	}
	
	/**
	 * Use for error message dialog boxes that just call for clicking OK.
	 * @param msg Message to display in the dialog box
	 * @param frameToShowIn Frame in which the dialog box is to be placed. If null, 
	 * then a new frame is created.
	 */

	public static void showErrorMsg(String msg, Component frameToShowIn) {
		JOptionPane.showMessageDialog(
				frameToShowIn,
				msg,
				"PhotoSpread Error",
				JOptionPane.ERROR_MESSAGE);
	}
	
	public static File getCSVFileNameFromUser() {
		return getFileNameFromUser(
				new CSVFileFilter(),
				JFileChooser.FILES_AND_DIRECTORIES);
	}
	
	public static File getFileNameFromUser() {
		return getFileNameFromUser(
				null, // no file filter
				JFileChooser.FILES_AND_DIRECTORIES);
	}
	
	public static File getFileNameFromUser(int whatToShow) {
		return getFileNameFromUser(
				null, // no file filter
				whatToShow);
	}
	
	/**
	 * @return
	 */
	public static File getFileNameFromUser(
			FileFilter browseFileTypeFilter,
			int whatToShow) {
		
		File importFile = null;
		String priorReadDir = PhotoSpread.photoSpreadPrefs.getProperty(PhotoSpread.lastDirReadKey);

		fc.setCurrentDirectory(new File(priorReadDir));
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if ((whatToShow == JFileChooser.DIRECTORIES_ONLY) ||
				(whatToShow == JFileChooser.FILES_ONLY) ||
				(whatToShow == JFileChooser.FILES_AND_DIRECTORIES))
			fc.setFileSelectionMode(whatToShow);
		// Should browser offer only files? Only directories? Both, etc.
		// Value of null shows all files:
		fc.setFileFilter(browseFileTypeFilter);

		fc.setMultiSelectionEnabled(false);
		
		int returnVal = fc.showOpenDialog(PhotoSpread.getCurrentSheetWindow());
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return null;

		importFile = fc.getSelectedFile();
		PhotoSpread.photoSpreadPrefs.setProperty(PhotoSpread.lastDirReadKey, importFile.getParent());
		
		return importFile;
	}

	
	/**
	 * Use for error message dialog boxes that just call for clicking OK.
	 * @param msgs Array of messages to display in the dialog box as a 'vertical stack'
	 * @param frameToShowIn Frame in which the dialog box is to be placed. If null, 
	 * then a new frame is created.
	 */
	public static void showErrorMsgArray(ArrayList<String> msgs, Component frameToShowIn) {
		JOptionPane op = new JOptionPane(msgs, JOptionPane.ERROR_MESSAGE) {
			private static final long serialVersionUID = 1L;
			public int getMaxCharactersPerLineCount() {
		        return 80;
		    }
		};
		op.setVisible(true);
		/*
		op.showMessageDialog(
				frameToShowIn,
				msgs,
				"PhotoSpread Error",
				JOptionPane.ERROR_MESSAGE);
				*/
	}
	
	/**
	 * Use for yes/no dialog boxes.
	 * @param msg Message to display in the dialog box
	 * @return True if user clicked Yes. Else returns false;
	 */
	public static boolean showConfirmMsg(String msg) {
		return showConfirmMsg(msg, null);
	}

	/**
	 * Use for yes/no dialog boxes.
	 * @param msg Message to display in the dialog box
	 * @param frameToShowIn Frame in which the dialog box is to be placed. If null, 
	 * then a new frame is created.
	 * @return True if user clicked Yes. Else returns false;
	 */
	public static boolean showConfirmMsg(String msg, Component frameToShowIn) {
		int res = JOptionPane.showConfirmDialog(
				frameToShowIn,
				msg, 
				"PhotoSpread Confirmation",
				JOptionPane.YES_NO_OPTION);
		if (res == JOptionPane.YES_OPTION)
			return true;
		else
			return false;
	}
	
	/**
	 * Shows a purely informational message with an OK button.
	 * @param msg The message to show the user.
	 * @param frameToShowIn The frame within which to show the 
	 * message dialog. Null causes display in center of screen.
	 */
	
	public static void showInfoMsg (String msg, Component frameToShowIn) {
		JOptionPane.showMessageDialog(
				frameToShowIn, 
				msg, 
				"PhotoSpread Information", 
				JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * Shows a purely informational message with an OK button.
	 * The message will display in the center of the screen.
	 * 
	 * @param msg The message to show the user.
	 */
	
	public static void showInfoMsg (String msg) {
		showInfoMsg(msg, null);
	}
	
	/**
	 * Puts up a Yes/No question. If user answers yes,
	 * exits the program (and therefore never returns).
	 * Else returns false. Dialog box will be in center 
	 * of the screen.
	 * @param confirmMsg
	 * @return If the user responded No to confirmMsg, returns false. Else won't return.
	 */
	public static boolean exitIfUserWants (String confirmMsg) {

		if (showConfirmMsg(confirmMsg))
			System.exit(0);
		return false;
	}

	/**
	 * Puts up a Yes/No question. If user answers yes,
	 * exits the program (and therefore never returns).
	 * Else returns false. Dialog box will be inside
	 * the given frame.
	 * @param confirmMsg
	 * @return If the user responded No to confirmMsg, returns false. Else won't return.
	 */
	public static boolean exitIfUserWants (String confirmMsg, JFrame frameToShowIn) {

		if (showConfirmMsg(confirmMsg, frameToShowIn))
			System.exit(0);
		return false;
	}

	/****************************************************
	 * Inner Class ByteArrayConverter: Convert byte array to string 
	 *****************************************************/
    
	public static class ByteArrayConverter {

		static final byte[] HEX_CHAR_TABLE = { (byte) '0', (byte) '1',
				(byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6',
				(byte) '7', (byte) '8', (byte) '9', (byte) 'a', (byte) 'b',
				(byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f' };

		public static String getHexString(byte[] raw) {
			
			byte[] hex = new byte[2 * raw.length];
			int index = 0;
			String res = null;
			
			for (byte b : raw) {
				int v = b & 0xFF;
				hex[index++] = HEX_CHAR_TABLE[v >>> 4];
				hex[index++] = HEX_CHAR_TABLE[v & 0xF];
			}
			try {				
				res = new String(hex, "US-ASCII");
			}
			catch (UnsupportedEncodingException e) {
				// Every Java implementation is required to support US-ASCII==>do nothing
			}
			return res;
		}
	}
	
	/****************************************************
	 * Main and/or Testing Methods
	 *****************************************************/

	public static void main(final String[] args) {

		try {
			//System.out.println("Current directory: " + System.getProperty("user.dir"));
			//String path = "HelpFiles/sheetHelp.html";
			// File f = Misc.ShowHelpAction.getResource(path);
			// System.out.println(f);

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
