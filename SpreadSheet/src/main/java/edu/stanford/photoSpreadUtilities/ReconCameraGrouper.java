/**
 * 
 */
package edu.stanford.photoSpreadUtilities;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.photoSpread.PhotoSpreadException;
import edu.stanford.photoSpread.PhotoSpreadException.IllegalArgumentException;
import edu.stanford.photoSpreadObjects.PhotoSpreadImage;
import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;

/**
 * @author paepcke
 *
 *	Special purpose class for Eric Abelson.
 *	Given two cells, A and B. Assume A has many
 *  Recon Camera photos with metadata. Cell B
 *  contains a subset of those photos. This class
 *  goes through each photo b in B and adds all 
 *  photos from A that were taken in the same
 *  15-shot series as photo b.
 *  
 *  This grouping is determined by comparing three
 *  pieces of metadata:
 *  	- The serial number of the camera
 *  	- The group ID as extracted from the filename
 *  	- The shot date/time
 *  
 *  
 *  
 */
public class ReconCameraGrouper {

	private static long   _interShotTime = 60000; // milliseconds
	private static int    _maxPhotosInReconyxGroup = 15;
	
	// Column names for relevant Reconyx records:
	private static String _filenameKey = 
		Const.permanentMetadataAttributeNames[Const.FILENAME_METADATA_ATTR_NAME];
	private static String _serialNumberKey = "sn"; // ***from "SN"
	private static String _dateKey = "imageDate"; // ***from "Image Date"
	private static String _timeKey = "imageTime"; // ***from "Image Time"
	
	// Support metadata we add to each photo in the
	// full-set cell:
	
	private static String _shotGroupNumKey = "@shotGroupNum";
	private static String _shotNumKey = "@shotNum";
	private static String _dateSecsKey = "@dateSecs";
	
	private static ArrayList<String> _uniquifyingKeyset =
		new ArrayList<String>();

	private static String _dateTimePattern = "mm/dd/yyy kk:mm:ss";

	MetadataIndexer _metadataIndexer;
	PhotoSpreadCell _fullSetCell = null;
	PhotoSpreadCell _subsetCell = null;
	PhotoSpreadCell _resultSetCell = null;
	
	/****************************************************
	 * Constructors
	 * @throws IllegalArgumentException 
	 *****************************************************/

	public ReconCameraGrouper (
			PhotoSpreadCell fullSetCell,
			PhotoSpreadCell subsetCell,
			PhotoSpreadCell resultSetCell) throws IllegalArgumentException {
		
		Iterator<PhotoSpreadObject> fullSetIterator = null;
		PhotoSpreadObject photo = null;
		
		_fullSetCell   = fullSetCell;
		_subsetCell    = subsetCell;
		_resultSetCell = resultSetCell;
		
		_uniquifyingKeyset.add(_shotGroupNumKey);
		_uniquifyingKeyset.add(_dateSecsKey);
		_uniquifyingKeyset.add(_serialNumberKey);
		
		// Ensure that the fullSetCell has
		// an indexer for the metadata fields 
		// that we care about (the ones that
		// together make a Reconyx photo unique):
		
		ensureAllKeysIndexed(_fullSetCell);

		// Same for subset cell:
		
		ensureAllKeysIndexed(_subsetCell);
		
		// Now check whether at least one of the fullSet
		// cell's objects have one of our special metadata
		// keys (shot group number or dateSecs). If so, we
		// assume that all objects in the fullset cell
		// have those Reconyx-specific metadata fields set.
		// If not, we add those fields to the cell's
		// objects, which will also update the index to
		// include the new fields.
		// We only want to do this once,
		// of course, therefore the prior check:
		
		fullSetIterator = _fullSetCell.getObjectsIterator();
		if (!fullSetIterator.hasNext())
			// Full-set cell is empty. Nothing to do:
			return;
		
		photo = fullSetIterator.next();
		if (photo.getMetaData(_shotGroupNumKey) == Const.NULL_VALUE_STRING)
			addSignaturesToCellObjs(fullSetCell);

		return;
	}
	
	
	/****************************************************
	 * Inner class PhotoSignature
	 *****************************************************/

	public class PhotoSignature {

		private String _cameraSN     = "";
		private int	   _shotGroupNum = -1;
		private int    _shotNum      = -1;
		private long   _dateSecs     = -1;

		/****************************************************
		 * Constructor Inner class PhotoSignature
		 *****************************************************/

		public PhotoSignature (
				String cameraSN,
				int    shotGroupNum,
				int	   shotNum,
				long   dateSecs) {
			_cameraSN = cameraSN;
			_shotGroupNum = shotGroupNum;
			_shotNum = shotNum;
			_dateSecs = dateSecs;
		}

		/****************************************************
		 * Getters/Setters Inner class PhotoSignature
		 *****************************************************/

		void setCameraSN(String cameraSN) {
			this._cameraSN = cameraSN;
		}

		String getCameraSN() {
			return _cameraSN;
		}

		void setShotGroup(int _shotGroup) {
			this._shotGroupNum = _shotGroup;
		}

		int getShotGroup() {
			return _shotGroupNum;
		}

		void setShotNum(int _shotNum) {
			this._shotNum = _shotNum;
		}

		int getShotNum() {
			return _shotNum;
		}

		void setDateSecs(long _dateSecs) {
			this._dateSecs = _dateSecs;
		}

		long getDateSecs() {
			return _dateSecs;
		}

		/****************************************************
		 * Methods Inner class PhotoSignature
		 *****************************************************/
		
	} // end class PhotoSignature

	
	/****************************************************
	 * Methods
	 * @throws IllegalArgumentException 
	 * @throws IllegalArgumentException 
	 *****************************************************/

	public TreeSetRandomSubsetIterable<PhotoSpreadObject> expandToGroups () 
	throws IllegalArgumentException {
		return expandToGroups(_fullSetCell, _subsetCell, _resultSetCell);
	}

	public TreeSetRandomSubsetIterable<PhotoSpreadObject> expandToGroups (
			PhotoSpreadCell fullSetCell,
			PhotoSpreadCell subsetCell,
			PhotoSpreadCell resultSetCell) 
			throws IllegalArgumentException {

		TreeSetRandomSubsetIterable<PhotoSpreadObject> res = 
			new TreeSetRandomSubsetIterable<PhotoSpreadObject>();
		res.setIndexer(new PhotoSpreadObjIndexerFinder());
		
		HashSet<PhotoSpreadObject> sameGroupObjects; 
		
		PhotoSpreadObject photoGroup[] = 
			new PhotoSpreadObject[_maxPhotosInReconyxGroup];
		
		PhotoSignature subsetPhotoSig; 
		long fullPhotoTime;
		int fullPhotoShotNum;
		
		// Initialize all photoGroup[] entries to null:
		for (int i=0; i<_maxPhotosInReconyxGroup; i++)
			photoGroup[i] = null;
		
		for (PhotoSpreadObject photo : subsetCell.getObjects()) {

			// Get the photo' signature:
			subsetPhotoSig = makePhotoSignature((PhotoSpreadImage) photo);
			
			// Find in the full set cell all the objects
			// with the same shot group number:
			sameGroupObjects = fullSetCell.find(
					_shotGroupNumKey, 
					((Integer) subsetPhotoSig.getShotGroup()).toString());
			
			// For each photo in the same shot group, get its
			// shot time in seconds and its shot number key. Then
			// see whether camera id and shot time agree to the
			// specified tolerance (of _interShotTime seconds).
			// From the above search we already know that the group
			// numbers agree:
			for (PhotoSpreadObject candidate : sameGroupObjects) {

				try {
					fullPhotoTime = Long.parseLong(candidate.getMetaData(_dateSecsKey));
				} catch (NumberFormatException e) {
					throw new RuntimeException("No proper time/date available " +
							"in seconds format from photo:\n'" +
							photo +
							"'");
				}
				try {
				fullPhotoShotNum = Integer.parseInt(candidate.getMetaData(_shotNumKey));
				} catch (NumberFormatException e) {
					throw new RuntimeException("No proper shot number available " +
							"from photo:\n'" +
							photo +
							"'");
				}
				
				if ((candidate.getMetaData(_serialNumberKey).equals(
						subsetPhotoSig.getCameraSN())) &&
					(timeDiff(fullPhotoTime, subsetPhotoSig.getDateSecs()) <= 
						_interShotTime))
					
					// The 'candidate' photo from the full set does
					// belong to the same group as the current 'photo'
					// from the subset cell. Put the candidate into 
					// our array of photos of one group:
					
					photoGroup[fullPhotoShotNum - 1] = candidate;
			} // end collect all photos of one group into an array
			
			// Now we have one Reconyx shot group of photos, sorted,
			// in photoGroup[]. We add that whole group to our result
			// set in order:
			
			for (int i=0; i<_maxPhotosInReconyxGroup; i++) {

				if (photoGroup[i] == null)
					continue;

				res.add(photoGroup[i]);

				// Clear the entry in the array for the next
				// object whose group we'll find (in the outer loop):
				photoGroup[i] = null;
			} // end transfer one shot group to final result set
		} // look at one photo in the subset cell
		
		_resultSetCell.addObjects(res);
		_resultSetCell.getTableModel().fireTableCellUpdated(
				resultSetCell.getRow(), resultSetCell.getColumn());
		
		return res;
	}

	public PhotoSignature makePhotoSignature (PhotoSpreadImage photo) 
	throws PhotoSpreadException.IllegalArgumentException {

		// Camera serial number:
		String serialNumCamera = photo.getMetaData(_serialNumberKey);
		if (serialNumCamera.equals(Const.NULL_VALUE_STRING))
			throw new PhotoSpreadException.IllegalArgumentException(
					"Photo has no camera serial number: " +
					photo.getFilePath());

		// Shot group ID: (The top n-2 decimal digits of the numeric
		// part of the filename. And shot ID: the last two digits.
		// Ex.: M0000103: group ID: 1; shot id: 3 

		Integer fullShotID = extractShotNumberFromFilename(photo);
		if (fullShotID == null)
			throw new PhotoSpreadException.IllegalArgumentException(
					"Photo has no proper shot number: " +
					photo.getFilePath());

		int groupID = fullShotID/100;
		int shotID  = fullShotID % 100;
		
		// Photo date and time:

		String shotDate = photo.getMetaData(_dateKey);
		if (shotDate.equals(Const.NULL_VALUE_STRING))
			throw new PhotoSpreadException.IllegalArgumentException(
					"Photo has no proper date field: " +
					photo.getFilePath());

		String shotTime = photo.getMetaData(_timeKey);
		if (shotTime.equals(Const.NULL_VALUE_STRING))
			throw new PhotoSpreadException.IllegalArgumentException(
					"Photo has no proper time field: " +
					photo.getFilePath());

		// Wrap a Date instance around the data and the time;

		Date thisPhotoDateTime;
		try {
			thisPhotoDateTime = makeDateTimeInstance(shotDate, shotTime);
		} catch (ParseException e) {
			throw new PhotoSpreadException.IllegalArgumentException(
					"Cannot convert photo date or time into proper format: " +
					photo.getFilePath());

		}
		
		return new PhotoSignature(
				serialNumCamera,
				groupID,
				shotID,
				thisPhotoDateTime.getTime());
	}
	
	@SuppressWarnings("unused")
	private boolean areGroupSiblings(PhotoSignature sig1, PhotoSignature sig2) {
		
		if (sig1.getCameraSN() != sig2.getCameraSN())
			return false;

		if (sig1.getShotGroup() != sig2.getShotGroup())
			return false;
		
		if (timeDiff(sig1.getDateSecs(), sig2.getDateSecs()) > _interShotTime)
			return false;

		return true;
	}

	private Integer extractShotNumberFromFilename (PhotoSpreadImage photo) {

		String fileName = photo.getMetaData(_filenameKey);
		if (fileName.equals(Const.NULL_VALUE_STRING))
			return null;

		Integer res = 0;
		File photoFile  = new File(fileName);

		// Grab 7 consecutive digits exactly:
		Pattern pattern = Pattern.compile("(\\d\\d\\d\\d\\d\\d\\d)");

		// Only want the file name itself, not whole path:
		String baseName = photoFile.getName();
		Matcher matcher = pattern.matcher(baseName);

		if (!matcher.find())
			return null;
		try {
			res = Integer.parseInt(matcher.group());

		} catch (IllegalStateException e) {
			return null;
		} catch (NumberFormatException e) {
			return null;
		}

		return res;
	}

	private Date makeDateTimeInstance (String dataStr, String timeStr) 
	throws ParseException {

		SimpleDateFormat dateTimeParser = new SimpleDateFormat(_dateTimePattern);
		Date dateTime;

		dateTime = dateTimeParser.parse(dataStr + " " + timeStr);
		return dateTime;
	}

	@SuppressWarnings("unused")
	private long timeDiff(Date date1, Date date2) {
		long diffMsecs = Math.abs(date1.getTime() - date2.getTime());
		long diffSecs  = (long) Math.ceil(diffMsecs / 1000.0);
		return diffSecs;
	}
	
	private long timeDiff(long timeInSecs1, long timeInSecs2) {
		return Math.abs(timeInSecs1 - timeInSecs2);
	}
	
	private void ensureAllKeysIndexed(PhotoSpreadCell cell) {
		
		MetadataIndexer indexer = cell.getMetadataIndexer();
		
		if (indexer == null) {
			indexer = new MetadataIndexer(_uniquifyingKeyset);
			cell.setMetadataIndexer(indexer);
		}
		else // ensure that the existing indexer indexes all of
			 // the special metadata items we need (group shot ID, etc.):
			indexer.addMetadataKeysToIndex(_uniquifyingKeyset);
	}
	
	/**
	 * Given a cell with photos that are assumed to have the
	 * standard Reconyx metadata, ensure that all photos also
	 * have all the metadata fields that are derived from the
	 * standard Reconyx photos: the shot group number, the
	 * shot time in seconds since the beginning of time,
	 * and the shot number within the group.
	 * @param cell
	 * @throws IllegalArgumentException
	 */
	private void addSignaturesToCellObjs (PhotoSpreadCell cell) 
	throws IllegalArgumentException {
		
		PhotoSignature sig = null;
		
		for (PhotoSpreadObject photo : cell.getObjects()) {
			
			sig = makePhotoSignature((PhotoSpreadImage) photo);
			photo.setMetaData(
					_shotGroupNumKey, 
					((Integer) sig.getShotGroup()).toString());
			photo.setMetaData(
					_dateSecsKey,
					((Long) sig.getDateSecs()).toString());
			photo.setMetaData(
					_shotNumKey,
					((Integer) sig.getShotNum()).toString());
		}
	}


	/****************************************************
	 * Main for Testing
	 *****************************************************/
/*
	public static void main(final String[] args) {
		ReconCameraGrouper test = new ReconCameraGrouper();
		Date testDate1 = null;
		Date testDate2 = null;
		Date testDate3 = null;
		// Integer shotNum;

		try {

			testDate1 = test.makeDateTimeInstance("5/11/2007", "18:58:58");
			testDate2 = test.makeDateTimeInstance("5/11/2007", "18:58:59");
			testDate3 = test.makeDateTimeInstance("5/11/2007", "21:58:59");

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("D1 diff D2: " + test.timeDiff(testDate1, testDate2) + "sec");
		System.out.println("D2 diff D1: " + test.timeDiff(testDate2, testDate1) + "sec");
		System.out.println("D1 diff D3: " + test.timeDiff(testDate1, testDate3) + "sec");

		//shotNum = test.extractShotNumberFromFilename("C:/foo/M0001234.jpg");
		//System.out.println("shotNum 00001234: " + shotNum);
		//shotNum = test.extractShotNumberFromFilename("C:/foo/M1234.jpg");
		//System.out.println("shotNum 00001234: " + shotNum);
	}
*/	
}