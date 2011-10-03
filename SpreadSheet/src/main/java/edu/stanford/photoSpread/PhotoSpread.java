/**
 * 
 */
package edu.stanford.photoSpread;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import javax.naming.InvalidNameException;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import edu.stanford.inputOutput.InputOutput;
import edu.stanford.photoSpread.PhotoSpreadException.IllegalArgumentException;
import edu.stanford.photoSpread.PhotoSpreadException.IllegalPreferenceException;
import edu.stanford.photoSpread.PhotoSpreadException.IllegalPreferenceValueException;
import edu.stanford.photoSpread.PhotoSpreadException.NotImplementedException;
import edu.stanford.photoSpreadObjects.PhotoSpreadTableObject;
import edu.stanford.photoSpreadObjects.photoSpreadComponents.Workspace;
import edu.stanford.photoSpreadTable.PhotoSpreadTableMenu;
import edu.stanford.photoSpreadUtilities.Misc;
import edu.stanford.photoSpreadUtilities.PhotoSpreadProperties;

/**
 * @author paepcke
 * 
 * Main class for starting the application
 * Find user preferences and kick things off.
 * Prefs may come from a preferences file or
 * from command line options. The preferences file
 * is by default expected in:
 * 
 * 		1. The command line as prefsFile=<filePath>
 * 		2. $HOME/_photoSpread/photoSpreadPrefs.cnf
 * 
 * It's OK if no file is found. Defaults take over
 * 
 * To add a new preference to the system:
 * 
 * 		- Add entry to legalPreferenceAttrNames
 * 		- Add a key strings for the preferences properties data structure.
 *        (see all the others. They all end with 'Key'.
 *      - Add a default value for the new preference in initDefaultProperties()
 *      - Add a case statement in validatePref()
 *      
 *  In the program the preferences can be accessed directly
 *  via userPrefs.getProperty(<propKey>). The values are all
 *  strings. Convenience are useful for values that need
 *  to be converted to non-String types:
 *  
 *  	- Misc.getPrefInt(<propKey>)
 */


/**
 * @author paepcke
 *
 */
public class PhotoSpread {

	// NOTE: Change this for new versions:
	public static String version = "V7Alpha4";
	
	public static enum DebugLevel {
		DEBUG, NO_DEBUG
	}
	public static DebugLevel currDebugLevel = DebugLevel.NO_DEBUG;

	private static boolean _DnDInProgress = false;
	private static JFrame _currentSheetWindow = null;
	private static Workspace _currentWorkspace = null;
	private static Component _defaultGlassPane = null; 

	// List of all legal, but currently unimplemented
	// attribute names:

	protected static enum unimplementedPrefs {
		workspaceObjHeight,
		workspaceFoo;

		public static boolean hasMember (String pref) {

			try {
				valueOf(pref);
			} catch (java.lang.IllegalArgumentException e) {
				return false;
			}
			return true;
		}
	}

	// List of all legal --pref attribute names. Used in
	// validatePrefs() to check legality of a commandline
	// or preferences file attr/value pair:

	protected static enum legalPreferenceAttrNames {

		sheetSize,
		sheetNumCols,
		sheetNumRows,
		sheetRowHeightMin,
		sheetColWidthMin,
		sheetObjsInCell,
		sheetCellObjsWidth,

		workspaceSize,
		workspaceNumCols,
		workspaceHGap,
		workspaceVGap,
		workspaceObjWidth,
		workspaceObjHeight,
		workspaceMaxObjWidth,
		workspaceMaxObjHeight,

		editorSize,
		formulaEditorStripSize,
		dragGhostSize,

		resourcePaths,
		csvFieldDelimiter,
		prefsFile,
		metaDataEditorSize,

		lastDirWritten,
		lastDirRead
	}

	// Key strings used for the preferences properties data structure:

	public static final String prefsFileKey = "prefsFile";
	public static final String csvFieldDelimiterKey = "csvFieldDelimiter";
	public static final String lastDirWrittenKey = "lastDirWritten";
	public static final String lastDirReadKey = "lastDirRead";
	public static final String workspaceSizeKey = "workspaceSize";
	public static final String editorSizeKey = "editorSize";
	public static final String sheetSizeKey = "sheetSize";
	public static final String formulaEditorStripSizeKey = "formulaEditorStripSize";
	public static final String dragGhostSizeKey = "dragGhostSize";
	public static final String sheetRowHeightMinKey = "sheetRowHeightMin";
	public static final String sheetColWidthMinKey = "sheetColWidthMin";
	public static final String sheetObjsInCellKey = "sheetObjsInCell";
	public static final String sheetCellObjsWidthKey = "sheetCellObjsWidth";

	public static final String sheetNumColsKey = "sheetNumCols";
	public static final String sheetNumRowsKey = "sheetNumRows";
	public static final String workspaceNumColsKey = "workspaceNumCols";
	public static final String workspaceHGapKey = "workspaceHGap";
	public static final String workspaceVGapKey = "workspaceVGap";
	public static final String workspaceObjWidthKey = "workspaceObjWidth";
	public static final String workspaceObjHeightKey = "workspaceObjHeight";
	public static final String workspaceMaxObjWidthKey = "workspaceMaxObjWidth";
	public static final String workspaceMaxObjHeightKey = "workspaceMaxObjHeight";

	public static String propertySeparator = "=";

	public static String prefsFileExtension = "cnf";

	// Default and user preferences. Specify enough entries that 
	// no rehashing is required. I experienced a bug with Properties
	// when their hashtables rehashed:

	public static PhotoSpreadProperties<String, String> photoSpreadDefaults = 
		new PhotoSpreadProperties<String, String>(100);
	
	public static  PhotoSpreadProperties<String, String> photoSpreadPrefs = 
		new PhotoSpreadProperties<String, String>(photoSpreadDefaults, 100);

	private static String photoSpreadPrefsDir = "_photoSpread" + 
	System.getProperty("file.separator");
	private static String photoSpreadPrefsFileName = "photoSpreadPrefs." + prefsFileExtension;
	private static String prefsFilePath;

	/****************************************************
	 * Class StartupErrorPanel
	 *****************************************************/

	static class StartupErrorPanel extends JFrame {
		
		private static final long serialVersionUID = 1L;

		public StartupErrorPanel (String errMsg) {
			setVisible(true);
			Misc.showErrorMsg(errMsg);
			dispose();
		}
	}

	/****************************************************
	 * Static Getters/Setters 
	 *****************************************************/

	public static Component getDefaultGlassPane() {
		return _defaultGlassPane;
	}

	public static JFrame getCurrentSheetWindow () {
		return _currentSheetWindow;
	}

	public static void setCurrentSheetWindow(JFrame _currentSheetWindow) {
		PhotoSpread._currentSheetWindow = _currentSheetWindow;
	}

	public static void setCurrentWorkspaceWindow (Workspace currWorkspace) {
		_currentWorkspace = currWorkspace;
	}
	
	public static Workspace getCurrentWorkspaceWindow () {
		return _currentWorkspace;
	}
	
	/**
	 * Initialize the user preferences defaults.
	 */
	
	//****** Change back to private after JUnit test is done
	public static void initDefaultProperties() {

		// Set all the defaults in the separate defaults properties.
		// They will be used if the program asks for some property 
		// that's not set:

		photoSpreadDefaults.setProperty(csvFieldDelimiterKey, ",");
		photoSpreadDefaults.setProperty(lastDirWrittenKey, System.getProperty("user.dir"));
		photoSpreadDefaults.setProperty(lastDirReadKey, System.getProperty("user.dir"));
		photoSpreadDefaults.setProperty(editorSizeKey, "830 940");
		photoSpreadDefaults.setProperty(sheetSizeKey, "820 600"); // EDITED by eshieh
		photoSpreadDefaults.setProperty(formulaEditorStripSizeKey, "400 30");
		photoSpreadDefaults.setProperty(dragGhostSizeKey, "50 50");
		photoSpreadDefaults.setProperty(sheetRowHeightMinKey, "60");
		photoSpreadDefaults.setProperty(sheetColWidthMinKey, "80");
		photoSpreadDefaults.setProperty(sheetObjsInCellKey, "10");
		photoSpreadDefaults.setProperty(sheetCellObjsWidthKey, "50");
		photoSpreadDefaults.setProperty(sheetNumColsKey, "10");
		photoSpreadDefaults.setProperty(sheetNumRowsKey, "10"); // TODO: eshieh change number of rows, columns.
		photoSpreadDefaults.setProperty(workspaceNumColsKey, "1");
		photoSpreadDefaults.setProperty(workspaceHGapKey, "2");
		photoSpreadDefaults.setProperty(workspaceVGapKey, "2");
		photoSpreadDefaults.setProperty(workspaceObjWidthKey, "600"); // EDITED by eshieh - originally 500 (as with all below)
		// For now we make all items square:
		photoSpreadDefaults.setProperty(workspaceObjHeightKey,
				photoSpreadDefaults.getProperty(workspaceObjWidthKey));
		//photoSpreadDefaults.setProperty(workspaceObjHeightKey, "500");
		photoSpreadDefaults.setProperty(workspaceMaxObjWidthKey, "600");
		photoSpreadDefaults.setProperty(workspaceMaxObjHeightKey, "600");
		photoSpreadDefaults.setProperty(workspaceSizeKey, "600 600");
	}

	/****************************************************
	 * Getter/Setter(s)
	 *****************************************************/

	public static void setDnDInProgress(boolean dnDInProgress) {
		_DnDInProgress = dnDInProgress;
	}

	public static boolean isDnDInProgress() {
		return _DnDInProgress;
	}


	/****************************************************
	 * Methods (all static)
	 *****************************************************/
	/*
	 * Return false if user just asked for help. Return
	 * true if caller may proceed. 
	 */

	@SuppressWarnings("static-access")
	protected static boolean processCommandLineArgs(String[] args) 
	throws PhotoSpreadException.IllegalArgumentException, PropertyVetoException {

		if ((args == null) || (args.length == 0)) return true;


		final boolean noArg = false;
		CommandLine parsedOptions;

		// Generate an 'Options' instance and 
		// add specifications for each acceptable option.
		// ...addOption(<shortForm>, <longForm>, <has Arg or not>, <usageDescription>

		Options cmdLineOptions = new Options();
		cmdLineOptions.addOption( "h", "help", noArg, "Print usage description.");

		// The following code works well for long options: --Foobar,
		// and you have to use it for options that may occur more than
		// once. But not for single-letter options...

		cmdLineOptions.addOption(OptionBuilder
				.withLongOpt("workspaceSize")
				.withDescription("Initial size of workspace in pixels \"W H\".")
				.hasArgs(2)
				.create()); // create option w/ these above parameters

		cmdLineOptions.addOption(OptionBuilder
				.withLongOpt("editorSize")
				.withDescription("Initial size of editor in pixels: \"W H\".")
				.hasArgs(2)
				.create()); // create option w/ these above parameters

		cmdLineOptions.addOption(OptionBuilder
				.withLongOpt("metaDataEditorSize")
				.withDescription("Initial size in pixels of editor window for individual objects (e.g. photos).")
				.hasArgs(2) // 
				.create()); // create option w/ these above parameters

		cmdLineOptions.addOption(OptionBuilder
				.withLongOpt("resourcePaths")
				.withDescription("Colon-separated paths to PhotoSpread resources (such as photos).")
				.hasArgs(1) // 
				.create()); // create option w/ these above parameters


		// Automatically generate a help string from the Options instance:
		HelpFormatter formatter = new HelpFormatter();

		CommandLineParser parser = new GnuParser();
		try {
			// Parse the commandline options:
			parsedOptions = parser.parse(cmdLineOptions, args);

		} catch (ParseException e) {
			// Bad command line option syntax:
			throw new PhotoSpreadException.IllegalArgumentException("Command line options parsing failed. Reason: " + e.getMessage());
		}

		// Now parsedOptions can be queried for the presence
		// of options on the command line:

		if (parsedOptions.hasOption("help"))  {
			formatter.printHelp("PhotoSpread.jar", cmdLineOptions);
			return false;
		}

		if (parsedOptions.hasOption("resourcePaths")) {
			// TODO: resolve paths here
			// Get colon-separated list of resource path
			// String[] rps = parsedOptions.getOptionValues("r");
		}

		// Go through each option object and pull out
		// the attr/value pairs. Check each for validity:

		@SuppressWarnings("unchecked")
		Iterator<Option> it = (Iterator<Option>) parsedOptions.iterator();
		ArrayList<String> cleanPrefResult;

		while (it.hasNext()) {

			// Get pair [<attrName>, <value>]:
			cleanPrefResult = cleanCommandLineOption(it.next());
			try {

				validatePref(cleanPrefResult.get(0), cleanPrefResult.get(1));

			} catch (PhotoSpreadException.IllegalPreferenceValueException e) {
				new StartupErrorPanel(e.getMessage());
				System.exit(-1);
			} catch (NotImplementedException e) {
				new StartupErrorPanel(e.getMessage());
				System.exit(-1);
			} catch (IllegalPreferenceException e) {
				new StartupErrorPanel(e.getMessage());
				System.exit(-1);
			}

			// We returned; so all OK. Command-line-specified options
			// go into the System properties:
			System.setProperty(cleanPrefResult.get(0), cleanPrefResult.get(1));
		}

		return true;
	}

	protected static ArrayList<String> cleanCommandLineOption(Option op) {

		ArrayList<String> res = new ArrayList<String>();
		String argstr = "";

		String[] opValues = op.getValues();
		res.add(op.getLongOpt());
		if (opValues != null) {
			for (int i=0; i<opValues.length;i++) {
				argstr = argstr + (i==0 ? "" : " ") + op.getValue(i);
			}
			res.add(argstr);
		} else {
			res.add("");
		}
		// new StartupErrorPanel("Validate res[0]: '" + res.get(0)+ "'. res[1] '" + res.get(1) + "'.");
		return res;
	}

	/*
	 * Given an attribute name and an attribute value,
	 * check whether the pair is acceptable.
	 */

	protected static void validatePref(String attrName, String attrValue) 
	throws NotImplementedException, IllegalPreferenceException, IllegalPreferenceValueException {

		try {
			switch (legalPreferenceAttrNames.valueOf(attrName)) {

			// Legal, but unimplemented PhotoSpread preferences:

			case workspaceObjHeight:
				throw new NotImplementedException(
						"Preference '" + 
						attrName + 
				"' is legal, but not implemented. All Workspace items are square right now, with workspaceObjWidth determining the size.");
				// Preferences that require a single character:
			case csvFieldDelimiter:
				if (attrValue.length() != 1)
					throw new IllegalPreferenceValueException("Preference '" + 
							attrName +
					"' requires a single character.");
				break;

				// Preferences that take a file or directory name:
			case lastDirWritten:
			case lastDirRead:
			case prefsFile:
				/* Could check for attrValue being a legal file.
				   But this will happen later when we try to open it.
				 */
				break;
				// Preferences that require one integer:
			case sheetRowHeightMin:
			case sheetColWidthMin:
			case sheetNumRows:
			case sheetNumCols:
			case sheetObjsInCell:
			case sheetCellObjsWidth:
			case workspaceNumCols:
			case workspaceHGap:
			case workspaceVGap:
			case workspaceObjWidth:
			case workspaceMaxObjWidth:
			case workspaceMaxObjHeight:
				// case workspaceObjHeight:   // Not Implemented
				Integer.parseInt(attrValue); // throws NumberFormatException if not number.
				break;
				// Preferences that require two integers:
			case workspaceSize:
			case metaDataEditorSize:
			case editorSize:
			case sheetSize:
			case dragGhostSize:
				// attrValue must be a string of two numbers.
				// Note: the regular expression "[\s]" should work
				// and describe white space. But it doesn't:

				String[] twoNumStrings = attrValue.split("[ \t\n\f\r]");
				if (twoNumStrings.length != 2)
					throw new PhotoSpreadException.IllegalPreferenceValueException(
							"Preference '" + 
							attrName + 
							" requires two ints. Not: '" +
							attrValue + "'");
				Integer.parseInt(twoNumStrings[0].trim()); // throws NumberFormatException if not numbers.
				Integer.parseInt(twoNumStrings[1].trim());
				break;
				// Preferences that require a colon-separated set of paths:
			case resourcePaths:
				// TODO: check syntax: foo:bar/fum:E:/blue/green only.
				break;
			default:
				return;
			// We should have either bombed in the valueOf() above,
			// or we should have covered them all.
			}
		} catch (NumberFormatException e) {
			throw new PhotoSpreadException.IllegalPreferenceValueException("Preference '" + 
					attrName + 
					"' requires integer. Not: '" + 
					attrValue + "'");
		} catch (java.lang.IllegalArgumentException e) {
			/* Thrown by valueOf(attrName) if name is not in the 
			   legalPreferenceAttrNames enum.
			 */
			throw new PhotoSpreadException.IllegalPreferenceException("Preference '" + attrName + "' is unknown.");
		}		
	}

	/*
	 * Once the command line args have been loaded into the
	 * System.properties, we now try to find a preferences
	 * file. 
	 */

	protected static void initPreferences() throws InvalidNameException, IOException, IllegalArgumentException {

		// FileInputStream prefsStream;
		BufferedReader prefsStream;
		
		initDefaultProperties();

		// Was command line arg included to set the preferences file?
		prefsFilePath = System.getProperty(prefsFileKey);
		if (prefsFilePath == null) {
			// If not: If $HOME available: prefs file is like $HOME/.photoSpread/photoSpreadPrefs.cnf
			if (System.getenv("HOME") != null) {
				prefsFilePath = System.getenv("HOME") + 
				System.getProperty("file.separator") +
				photoSpreadPrefsDir + 
				photoSpreadPrefsFileName;
				File prefFile = new File(prefsFilePath);
				if (!prefFile.exists())
					// Final fallback: prefs file in dir that program was started with:
					prefsFilePath = System.getProperty("user.dir") + 
					System.getProperty("file.separator") + 
					photoSpreadPrefsFileName;
			}
		}

		if (prefsFilePath != null) {
			// Clean up the prefs file path:
			prefsFilePath = InputOutput.normalizePath(prefsFilePath);
			photoSpreadPrefs.setProperty(prefsFileKey, prefsFilePath);
			prefsStream = openPrefsFile();

			if (prefsStream != null) {
				try {

					// Stock our in-memory preferences from the prefs file:
					photoSpreadPrefs.load(prefsStream);

				} catch (java.lang.IllegalArgumentException e) {
					throw new PhotoSpreadException.IllegalArgumentException("Malformed Unicode in preference file " + prefsFilePath + ".");
				} catch (IOException e) {
					throw new IOException("Can open, but not read preference file " + prefsFilePath + ".");
				} finally {
					prefsStream.close();
				}
			} // end if
		}

		// Check validity of the options that we can check and exit:
		validatePreferences();
	}

	/**
	 * @return
	 * @throws FileNotFoundException
	 */
	protected static BufferedReader openPrefsFile() throws FileNotFoundException {
		BufferedReader prefsStream;
		// Try to open the preference file:
		try {
			// prefsStream = new FileInputStream(prefsFilePath); 
			prefsStream = new BufferedReader(new FileReader(prefsFilePath));
		} catch (FileNotFoundException e) { 
			// No prefs file found (or permissions):
			prefsStream = null;

			// If user specified a prefs file explicitly in the command line
			// then we print an error msg, else we just quietly stick to the defaults:

			if (System.getProperty(prefsFileKey) != null)
				throw new FileNotFoundException("Cannot open command-line-specified preference file " + 
						System.getProperty(prefsFileKey) + 
				".");
		} // end catch
		return prefsStream;
	}

	public static void savePreferences () throws IOException {

		FileWriter prefsWriter = null;
		String prefsFilePath = photoSpreadPrefs.getProperty(prefsFileKey);

		try {
			prefsWriter = new FileWriter(photoSpreadPrefs.getProperty(prefsFileKey));
		} catch (IOException e) {
			new IOException("Could not write the preference file " + prefsFilePath + ".");
		}
		photoSpreadPrefs.store(prefsWriter, "# Photospread Preferences File\n\n");
	}

	protected static void validatePreferences() {

		String userPrefKey;
		String userPrefValue;

		Enumeration<String> userPrefKeys = photoSpreadPrefs.keys();


		while (userPrefKeys.hasMoreElements()) {
			userPrefKey   = (String) userPrefKeys.nextElement();
			userPrefValue = photoSpreadPrefs.getProperty(userPrefKey);
			if (unimplementedPrefs.hasMember(userPrefKey))
				continue;
			try {

				validatePref(userPrefKey, userPrefValue);

			} catch (NotImplementedException e) {
				new StartupErrorPanel(e.getMessage());
				System.exit(-1);
			} catch (IllegalPreferenceException e) {
				new StartupErrorPanel(e.getMessage());
				System.exit(-1);
			} catch (IllegalPreferenceValueException e) {
				new StartupErrorPanel(e.getMessage());
				System.exit(-1);
			}
		} // end while

			// Check for value errors due to overconstraints:

		// Sheet width inconsistent with column with * number of columns:
		Dimension sheetDim = null;
		try {

			sheetDim = photoSpreadPrefs.getDimension(sheetSizeKey);
			int minColWidth = photoSpreadPrefs.getInt(sheetColWidthMinKey);
			int minRowHeight = photoSpreadPrefs.getInt(sheetRowHeightMinKey);
			int numCols = photoSpreadPrefs.getInt(sheetNumColsKey);
			int numRows = photoSpreadPrefs.getInt(sheetNumRowsKey);


			if ((minColWidth * numCols) > sheetDim.width) {
					new StartupErrorPanel(
						"Preference value error: " +
						sheetColWidthMinKey +
						" * " +
						sheetNumColsKey +
						" must be \nless than or equal to " +
						sheetDim.width +
						" (the requested sheet dimension width). \nThat product is " +
						minColWidth * numCols +
						". The sheet width specified in the preferences is " +
						sheetDim.width +
						"."
				);
				System.exit(-1);
			}

			if ((minRowHeight * numRows) > sheetDim.height) {
				new StartupErrorPanel(
						"Preference value error: " +
						sheetRowHeightMinKey +
						" * " +
						sheetNumRowsKey +
						"\n must be less than or equal to " +
						sheetDim.height +
						" (the height specified in " +
						sheetSizeKey +
						"). \n But that product is " +
						minRowHeight * numRows+
						". The sheet height specified in the preferences is " +
						sheetDim.height +
						"."
				);
				System.exit(-1);
			}
		} catch (NumberFormatException e) {
			// already checked validity
		}
	}
	
	public static void trace (String msg) {
		if (PhotoSpread.currDebugLevel == DebugLevel.DEBUG)
			System.out.println(msg);
	}
	

	/**
	 * Restores the original glass pane into the sheet window
	 */
	public static void restoreGlassPane () {
		_currentSheetWindow.setGlassPane(_defaultGlassPane);
	}

	public static void main (final String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {

				// TODO ****************    Disabled Command Line Arg Checking for JProbe ************
				/*       		try {
        			if (!processCommandLineArgs(args))
        				System.exit(-1);
        		} catch (IllegalArgumentException e) {
        			new StartupErrorPanel(e.getMessage());
        			System.exit(-1);
        		} catch (Exception e) {
        			new StartupErrorPanel(e.getMessage());
        			// e.printStackTrace();
        			System.exit(0);
        		}
				 */        		// End ****************    Disabled Command Line Arg Checking for JProbe ************
				
				// Set up properties file:
				try {
					initPreferences();
				} catch (InvalidNameException e) {
					new StartupErrorPanel(e.getMessage());
					System.exit(-1);
				} catch (IOException e) {
					new StartupErrorPanel(e.getMessage());
					System.exit(-1);
				} catch (IllegalArgumentException e) {
					new StartupErrorPanel(e.getMessage());
					System.exit(-1);
				}

				JFrame app = new JFrame();

				// Make current sheet window JFrame easy to find:
				_currentSheetWindow = app;
				_defaultGlassPane = app.getGlassPane();

				app.setTitle("PhotoSpread Sheet " + PhotoSpread.version + "  (F1 for Help in all Windows)");

				app.setLayout(new BorderLayout());

				// Size of sheet:
				try {
					app.setMinimumSize(photoSpreadPrefs.getDimension(sheetSizeKey));
				} catch (NumberFormatException e) {
					new StartupErrorPanel(e.getMessage());
					System.exit(-1);
				}

				PhotoSpreadTableObject tableObject = new PhotoSpreadTableObject((JFrame) app);
				
				
				
			
				
				
				
				
				JPopupMenu.setDefaultLightWeightPopupEnabled(false);
				PhotoSpreadTableMenu mainMenuBar = new PhotoSpreadTableMenu(tableObject);
				app.setJMenuBar(mainMenuBar);
												
				// Don't know what this size controls:
				try {
					// Don't know what this size controls:
					app.add(tableObject.getObjectComponent(200, 200));

				} catch (NumberFormatException e) {
					new StartupErrorPanel(e.getMessage());
					// e.printStackTrace();
					System.exit(-1);
				}

				// The constructor of the PhotoSpreadTableObject that is invoked
				// above will set up a confirm dialog for exiting when a window
				// closing event is generated by the user clicking the Workspace,
				// or sheet window's X button. The following statement enables these
				// events to be delivered:
				app.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
				
				// Make cnt-shift-w work. It will put up a confirmation
				// dialog within the spreadsheet window (therefore the 'app'
				// parameter in the confirm action creation:
				Misc.bindKey(
						app,
						"control W", 
						new Misc.AppExitWithConfirmAction("Really exit PhotoSpread?", app));

				// Center the window on the screen.
				app.setLocationRelativeTo(null);
				app.setVisible(true);
			}
		});
	}
}		
