package edu.stanford.photoSpreadUtilities;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.TransferHandler;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

/**
 * @author paepcke
 * 
 *         Collected globally used constants for PhotoSpread
 */
public final class Const {

	/****************************************************
	 * Universal Life Constants
	 *****************************************************/

	public static final short NUM_BYTES_IN_ONE_LONG = 8;
	public static final short NUM_BYTES_IN_ONE_INT = 4;
	public static final short NUM_BYTES_IN_ONE_SHORT = 2;
	public static final short NUM_BYTES_IN_ONE_BYTE = 1;
	public static final short NUM_BYTES_IN_ONE_DOUBLE = 8;
	public static final short NUM_BITS_IN_ONE_BYTE = 8;
	public static final short NUM_BYTES_IN_ONE_CHAR = 1;

	/****************************************************
	 * Boolean/Numeric/String Constants Named for Readability
	 *****************************************************/

	public static final Boolean WRITE_THROUGH_TO_EXIF_DEFAULT = true;
	
	public static final Boolean DO_EVAL = true;
	public static final Boolean DONT_EVAL = false;
	public static final Boolean DO_REDRAW = true;
	public static final Boolean DONT_REDRAW = false;

	public static final Boolean DO_STOP_CELL_EDITING = true;
	public static final Boolean DONT_STOP_CELL_EDITING = false;

	public static final Boolean SELECTED = true;
	public static final Boolean NOT_SELECTED = false;

	public static final Boolean ADD_CELL_CONTEXT_MENU = true;
	public static final Boolean DONT_ADD_CELL_CONTEXT_MENU = false;

	public static final Boolean EXPAND_WINDOW = true;
	public static final Boolean DONT_EXPAND_WINDOW = false;

	public static final Boolean MODAL = true;
	public static final Boolean NOT_MODAL = false;

	public static final Boolean ADD_INDEXER = true;
	public static final Boolean NO_INDEXER = false;

	public static final int BEFORE = -1;
	public static final int EQUAL = 0;
	public static final int AFTER = 1;

	public static final int SMALLER = -1;
	public static final int BIGGER = 1;

	public static final Cursor USE_DEFAULT_CURSOR = null;

	public static final int FORCE = TransferHandler.LINK;
	public static final int COPY = TransferHandler.COPY;
	public static final int COPY_FORCE = TransferHandler.COPY | FORCE;
	public static final int MOVE = TransferHandler.MOVE;
	public static final int MOVE_FORCE = TransferHandler.MOVE | FORCE;
	public static final int COPY_OR_MOVE = TransferHandler.COPY_OR_MOVE;
	public static final int COPY_OR_MOVE_FORCE_OR_NOT = TransferHandler.COPY_OR_MOVE
			| FORCE;

	public static enum Direction {
		INDETERMINATE, FORWARD, BACKWARD
	}

	public static enum CursorMoveEffect {
		ON_LAST_PAGE, SAME_PAGE, FLIPPED_PAGE, ON_FIRST_PAGE
	}

	public static enum CursorOffScreen {
		NO, LEFT, RIGHT
	}

	public static enum ObjMovements {
		MOVE, COPY
	}

	public static final int UNCHANGED = -1;

	public static final boolean HAS_FOCUS = true;
	public static final boolean NOT_IN_FOCUS = false;
	public static final boolean IS_EDITOR = true;
	public static final boolean IS_NOT_EDITOR = false;
	public static final boolean IS_SELECTED = true;
	public static final boolean IS_NOT_SELECTED = false;
	public static final boolean UPDATE_LAST_CLICKED = true;
	public static final boolean DONT_UPDATE_LAST_CLICKED = false;
	public static final boolean IS_TRANSPARENT = true;
	public static final boolean IS_NOT_TRANSPARENT = false;

	// Table selections:
	public static final boolean DONT_TOGGLE_SELECTION = false;
	public static final boolean DONT_EXTEND_SELECTION = false;

	// Spawning magnification windows:
	public static final int ALL = -1;
	public static final int LAST_CLICKED = 0;

	// For overloading JPanel's getComponentCount()
	// when the panel isn't directly holding the
	// objects:
	public static final boolean DELEGATE = true;

	// Drag/Drop argument to dropComplete(<success>):
	public static final boolean DROP_FAILED = false;
	public static final boolean DROP_SUCCEEDED = true;

	public static final int NOT_FOUND = -1; // returned from indexOf()

	// PredictableEquiSizedGridLayout: row count invalid:
	public static final int INVALID = -1;

	// Width vs Height:
	public static enum DimensionSide {
		WIDTH, HEIGHT
	}
	
	// Convenient collection of alignment
	// specifiers:
	public static enum Alignment {
		LEFT_H_ALIGNED, CENTER_H_ALIGNED, RIGHT_H_ALIGNED, TOP_V_ALIGNED, CENTER_V_ALIGNED, BOTTOM_V_ALIGNED
	}

	// Default alpha for AlphaCapableLabel instances:

	public static final float defaultLabelAlpha = 0.7f;

	// Coordinates of the cell that serves as trashcan:
	public static final CellCoordinates trashCanCellCoords = new CellCoordinates(
			0, 0);

	// Key under which to store user drag-and-drop key shortcuts
	// in the user preferences property list:

	public static final String DND_KEY_BINDINGS_PROP_KEY = "dndKeyBindings";
	public static final String NULL_VALUE_STRING = "@<null>";

	// Strings used in managing simple object collections in cells, like sets of
	// photos:
	public static final String OBJECTS_COLLECTION_INTERNAL_TOKEN = "_/Objects/_";
	public static final String OBJECTS_COLLECTION_PUBLIC_TOKEN = "(Item Collection)";

	/****************************************************
	 * Sizes
	 *****************************************************/

	// Thickness of selection border in DraggablePanel:
	public static final int labelSelectionBorderWidth = 4;

	// Percent of their max size that objects are displayed
	// in the Workspace:
	public static final int defaultInitialImageSizePercentage = 100;

	// Sheets: margins (empty space) between table cells:
	public static final int SPACE_BETWEEN_TABLE_CELLS_HOR = 2;
	public static final int SPACE_BETWEEN_TABLE_CELLS_VER = 2;

	// Empty space in nav panel at bottom of Workspace between
	// the Zoom button and the navigation buttons:
	public static final int WorkspaceNavBarButtonGroupSpace = 50;

	// When multiple Zoom windows are put up on the screen at the
	// same time, we offset them each a bit so that the user
	// sees that they are all there.
	public static final ComputableDimension ZoomWindowsOffset = new ComputableDimension(
			5, 5);

	// Row resizing: Height of bordered panels that make up the row resize
	// handles:
	public static final int rowResizeHandles = 2;
	// Row resizing: Sensitivity: number of pixels to resize per pixels moved
	// with mouse:
	public static final int motionSensitivity = 2;

	// Empty space around JPanels (not sure they do
	// anything):
	public static final Insets tableCellInsets = new Insets(2, // Top
			2, // Left
			2, // Bottom
			2); // Right

	// Empty space around JPanels (not sure they do
	// anything):
	public static final Insets draggableLabelInsets = new Insets(2, // Top
			2, // Left
			2, // Bottom
			2); // Right

	public static final Insets workspaceInsets = new Insets(30, // Top
			2, // Left
			2, // Bottom
			2); // Right

	// Metadata fields that are automatically set for
	// each image. The ID is set for all items:
	public static short UUID_METADATA_ATTR_NAME = 0;
	public static short FILENAME_METADATA_ATTR_NAME = 1;
	public static String[] permanentMetadataAttributeNames = new String[] {
			"@ID", "@filename" };
	public static String DEFAULT_SORT_KEY = permanentMetadataAttributeNames[FILENAME_METADATA_ATTR_NAME];

	/****************************************************
	 * Fonts
	 *****************************************************/

	/*
	 * Tables (Metadata editor, key bindings editor, ...) ---------------
	 */

	public static final int TABLE_FONT_SIZE = 16;
	public static final Font TABLE_FONT = new Font("Sans-Serif", Font.BOLD,
			TABLE_FONT_SIZE);

	/****************************************************
	 * Colors
	 *****************************************************/

	public static final Color CHRISTINE_LIGHT_BLUE = new Color(221, 235, 248);
	public static final Color CHRISTINE_BEIGE = new Color(220, 215, 206);
	public static final Color CHRISTINE_DARK_GRAY = new Color(85, 66, 64);
	public static final Color CHRISTINE_MILK_COFFEE = new Color(199, 178, 153);
	public static final Color MAROON = new Color(199, 128, 155);
	public static final Color OCRE = new Color(241, 235, 187);
	public static final Color GHOST_LIGHT_BLUE = new Color(139, 226, 234);
	public static final Color GHOST_DARK_BLUE = new Color(241, 235, 187);
	public static final Color GHOST_DIRT = new Color(156, 152, 122);

	/*
	 * Sheets ---------------
	 */

	public static final Color activeCellBackgroundColor = Color.GRAY;
	public static final Color inactiveCellBackgroundColor = CHRISTINE_MILK_COFFEE; // All
																					// inactive
																					// cells
																					// ==>
																					// most
																					// prominent
																					// Sheet
																					// color.
	public static final Color dndGhostBackgroundColor1 = GHOST_LIGHT_BLUE;
	public static final Color dndGhostBackgroundColor2 = OCRE;
	public static final Color dndGhostBorderHighlightColor1 = GHOST_DARK_BLUE;
	public static final Color dndGhostBorderHighlightColor2 = GHOST_DIRT;
	public static final Color dndGhostBorderShadowColor = Color.BLACK;
	public static final Color cellBorderColor = Color.WHITE;

	/*
	 * Workspace ---------------
	 */

	public static final Color workspaceBackgroundColor = Color.BLACK;
	public static final Color superposeDefaultSampleColor = Color.ORANGE;
	public static final Color superposePaneTitleColor = Color.WHITE;
	public static final Color activeCellFrameColor = CHRISTINE_LIGHT_BLUE;

	/*
	 * Labels: images/strings/... ---------------
	 */

	// public static final Color labelHighlightBorderColor = new Color(30, 50,
	// 100);
	// public static final Color labelHighlightBorderColor = Color.WHITE;
	public static final Color labelHighlightBorderColor = Color.RED;

	/*
	 * Metadata Editor ---------------
	 */

	// Background color for metadata editor table background
	// Green:
	// public static final Color metaDataEditorBackGroundColor = new Color(151,
	// 192, 175);
	// Dark brown
	public static final Color metaDataEditorBackGroundColor = new Color(107,
			96, 82);

	// Foreground color for metadata editor table:
	// Very light brown
	public static final Color metaDataEditorForeGroundColor = new Color(255,
			225, 195);

	/****************************************************
	 * Borders
	 *****************************************************/

	public static final Border cellBorder = new BevelBorder(BevelBorder.RAISED,
			Const.labelHighlightBorderColor, Color.white, Color.black,
			Color.gray);

	public static final Border cellHighlightBorder = new BevelBorder(
			BevelBorder.RAISED, Const.labelHighlightBorderColor, Color.GREEN,
			Color.black, Color.gray);

	public static final Border labelHighlightBorder = new BevelBorder(
			BevelBorder.RAISED, Const.labelHighlightBorderColor, Color.white,
			Color.black, Color.gray);

}
