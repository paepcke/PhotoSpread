package photoSpreadUtilities;

/**
 * @author paepcke
 *
 */

import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;

import javax.swing.ImageIcon;

import photoSpread.PhotoSpread;
import photoSpread.PhotoSpreadException;
import photoSpread.PhotoSpreadException.BadUUIDStringError;
import photoSpread.PhotoSpreadException.CannotLoadImage;
import photoSpreadLoaders.PhotoSpreadFileImporter;
import photoSpreadObjects.PhotoSpreadImage;
import photoSpreadObjects.PhotoSpreadTextFile;

import com.planetj.math.rabinhash.RabinHashFunction64;


/**
 * @author paepcke
 *
 * The UUID class produces (hopefully) globally unique
 * identifiers for a number of Java build-in, and PhotoSpread
 * specific objects. We use an implementation of the Rabin hash
 * function. This method is fast, and offers a probability
 * of collision bounded by:
 *                          n*m^2
 *                          -----
 *                          2^64
 * where n is the number of binary strings hashed, and
 * m is the length of the longest string.
 * 
 * See the constructors for the hashing strategies used
 * for different objects. In particular, image files can
 * be hashed using only a sampling of the files, speeding
 * up the process.
 */
public class UUID {

	// For calls to UUID(File) and UUID(PhotoSpreadImage):
	public static enum FileHashMethod {
		USE_FILE_SAMPLING, 
		USE_WHOLE_FILE,
		AUTOMATIC
	}
	
	private long theUUID = 0;
	private RabinHashFunction64 hasher = RabinHashFunction64.DEFAULT_HASH_FUNCTION;	

	/*      Parameters that Determine File Sampling         
	     NOTE: if you change these parameters, then files
	           will produce *different* UUIDs. Therefore,
	           previously computed UUIDs for a given file will
	           be different from the UUIDs computed from the same
	           file after the parameters were changed.
	*/
	// Number of bytes to sample at each sampling site within the file:
	private final int numBytesToSample = 100;
	// Percentages of bytes into the file were samples are to be taken.
	// (i.e. the sampling sites where the samplings start):
	private final int[] sampleLocPercentages = new int[] { 20, 40, 60 }; // Percentages into the file
	private final int MAX_SAMPLE_BYTES = numBytesToSample
			* sampleLocPercentages.length;
	private final byte[] allSamples = new byte[MAX_SAMPLE_BYTES];
	private final double[] sampleLocMultipliers = new double[sampleLocPercentages.length];
	private boolean samplingInitialized = false;
	
	/**
	 * Create a UUID from the stringification of a UUID instance that existed in the past. 
	 * That is, given a UUID A, one can: aStr = A.toString(). In a different session one
	 * could then UUID A' = createFromUUIDString(aStr) to receive a UUID instance that
	 * fingerprints the same item as A did.
	 * 
	 * Note that if the original UUID is still around, you'll have two distinct UUID objects that
	 * fingerprint the same item. Try not to do that. 
	 * 
	 * @param stringifiedUUID Result of toString() of a UUID instance that existed in the past.
	 * @return New UUID instance that is equal() to the UUID that produced the parameter.
	 * 
	 * @throws BadUUIDStringError If the passed-in string is not recognizable as a
	 * stringified UUID.
	 */
	public static UUID createFromUUIDString(String stringifiedUUID) throws BadUUIDStringError {
		UUID newUUID = new UUID();
		try {
			newUUID.theUUID = Long.parseLong(stringifiedUUID);
		} catch (NumberFormatException e) {
			throw new BadUUIDStringError("Attempt to create UUID from '" + 
					stringifiedUUID +
					"', which was not created by a previous UUID instance."); 
		}
		return newUUID;
	}
	
	private UUID() {
		// Create just a stub in which theUUID is 
		// not yet initialized. This by itself does
		// not make a valid UUID. This constructor is
		// only a helper for some of the other constructors.
	}
	
	/**
	 * Given an ImageIcon, which wraps an image create a UUID.
	 * NOTE: A UUID computed from in-memory pixels is different
	 *       from a UUID computed from the file from which the
	 *       image was loaded.
	 * 
	 * @param imgIcon
	 * @throws CannotLoadImage
	 */
	public UUID(ImageIcon imgIcon) throws CannotLoadImage {
		// Make error reporting a as reasonable as we
		// can, given that we're not given the file name
		// from which the image was/is being loaded:
		this(imgIcon, "Filename unknown");
	}
	
	/**
	 * Given an ImageIcon, which usually wraps an image that was
	 * taken from the file system, create a UUID.
	 * 
	 * NOTE: A UUID computed from in-memory pixels is different
	 *       from a UUID computed from the file from which the
	 *       image was loaded.
	 * 
	 * @param imgIcon
	 * @param fileName This file name is only passed to improve error messages in case of failure.
	 * @throws CannotLoadImage
	 */
	public UUID(ImageIcon imgIcon, String fileName) throws CannotLoadImage {
		
		final int imgID = 0;
		final int maxImgLoadWaitTime = 1000; // msec
		final int zeroX = 0;
		final int zeroY = 0;
		
		Image theImage = imgIcon.getImage();
		int imgWidth  = theImage.getWidth(imgIcon.getImageObserver());
		int imgHeight = theImage.getHeight(imgIcon.getImageObserver());
		
		// Make sure the image has loaded:
		if ((imgWidth < 0) || (imgHeight < 0)) {
			MediaTracker tracker = new MediaTracker(PhotoSpread.getCurrentSheetWindow());
			tracker.addImage(theImage, imgID);
			try {
				if (!tracker.waitForID(imgID, maxImgLoadWaitTime)) {
						throw new PhotoSpreadException.CannotLoadImage(
								"Timed out while loading image '" +
								fileName + "'.");
				} else {
					// Image loading is done, get the size again:
					imgWidth  = theImage.getWidth(imgIcon.getImageObserver());
					imgHeight = theImage.getHeight(imgIcon.getImageObserver());
				}
			} catch (InterruptedException e) {
				throw new PhotoSpreadException.CannotLoadImage(
								"Timed out while loading image '" +
								fileName + "'. Loading interrupted by outside forces.");
			}
		}
		
		int[] pixels = new int[imgWidth * imgHeight];
		int scansize = imgWidth;
	
		PixelGrabber grabber = new PixelGrabber(
				theImage, 
				zeroX, 
				zeroY, 
				imgWidth, 
				imgHeight,
				pixels,
				0,           // offset
				scansize);

		try {
			// Get all the pixels into our pixels[] buffer
			grabber.grabPixels();
		} catch (InterruptedException e) {
			throw new PhotoSpreadException.CannotLoadImage("Interrupted waiting for image to load (" + fileName + ").");
		}
		/* ******
		System.out.println("Pixels size: " + pixels.length);
		boolean foundNonZeroPixel = false;
		for (int i=0; i<pixels.length; i++) {
			if (pixels[i] != 0) {
				foundNonZeroPixel = true;
				break;
			}
		}
		if (foundNonZeroPixel) System.out.println("Found some non-zero pixels");
		else System.out.println("No non-zero pixels found");
		****** */
		theUUID = hasher.hash(pixels);
	}

	/**
	 * Create a unique UUID for a given string.
	 * @param a string
	 * The Rabin hash method does not distinguish
	 * well between strings that only differ by 
	 * very few characters. So we use Java's string
	 * hash method, which is unique per string, and
	 * computed the same across sessions.
	 */
	public UUID(String str) {
		theUUID = str.hashCode();
	}

	/**
	 * Create a unique UUID for a file, given a file object that wraps it.
	 * We offer two methods for doing the hashing: We can use the contents
	 * of the entire file to compute the Rabin hash, or we can just use
	 * a sample, which is computed in a prescribed manner (see class documentation).
	 * For text files FileHashMethod.USE_WHOLE_FILE is recommended, b/c their content can be
	 * very similar for two files. For image files, FileHashMethod.USE_FILE_SAMPLING is much
	 * faster.
	 *  
	 * The method can be asked to decide on its own which of these two methods to use.
	 * This choice is requested by setting method to FileHashMethod.AUTOMATIC.
	 * The decision is based on the file path extension. Any image file extension
	 * triggers sampling. Other file types are hashed by its full contents.
	 * 
	 * @param fileObj The file whose content is to be hashed
	 * @param method is either FileHashMethod.USE_FILE_SAMPLING, USE_WHOLE_FILE, or FileHashMethod.AUTOMATIC
	 * @throws java.io.FileNotFoundException
	 * @throws IOException
	 */
	public UUID(File fileObj, FileHashMethod method) throws java.io.FileNotFoundException, IOException {
		if (method == FileHashMethod.USE_WHOLE_FILE)
			theUUID = hasher.hash(fileObj);
		else if (method == FileHashMethod.USE_FILE_SAMPLING) 
			theUUID = hasher.hash(getFileSample(fileObj.getAbsolutePath()));
		else { // decide based on file extension:
			if (PhotoSpreadFileImporter.isImage(fileObj))
				theUUID = hasher.hash(getFileSample(fileObj.getAbsolutePath()));
			else
				theUUID = hasher.hash(fileObj);
		}
	}
	
	public UUID(PhotoSpreadImage photoSpreadImgObj) throws FileNotFoundException, IOException {
		this(new File(photoSpreadImgObj.getFilePath()), FileHashMethod.USE_FILE_SAMPLING);
	}
	
	public UUID(PhotoSpreadTextFile photoSpreadTxtFileObj) throws FileNotFoundException, IOException {
		this(new File(photoSpreadTxtFileObj.getFilePath()), FileHashMethod.USE_WHOLE_FILE);
	}
	
	/*
	public UUID(URI uri) {
		theUUID = hasher.hash(uri);
	}
	*/
	
	public UUID (byte[] allBytes) {
		theUUID = hasher.hash(allBytes);	
	}
	
	public UUID(Serializable obj) {
		theUUID = hasher.hash(obj);
	}
	
	public UUID(Double num) {
		this(num.doubleValue());
	}
	
	public UUID(double num) {
		// Convert the Double into a long first.
		// Then get the hash:
		theUUID = hasher.hash(Double.doubleToRawLongBits(num));
	}

	public Boolean equals(UUID obj) {
		return theUUID == obj.theUUID;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 * I could use (int)theUUID as a hash code.
	 * But I'm not sure how unique theUUID is
	 * after conversion from long to int. So
	 * I do the more expensive turning the long into 
	 * a string, and returning that string's hash code.
	 */
	public int hashCode() {
		return toString().hashCode();
	}

	public String toString () {
		return Long.toString(theUUID);
	}

	/**
	 * Compares this UUID object to another UUID object. 
	 * 
	 * @param otherUUID
	 * @return The value 0 if this UUID is equal to the argument UUID; 
	 * a value less than 0 if this UUID is numerically less than the argument UUID; 
	 * and a value greater than 0 if this UUID is numerically greater 
	 * than the argument UUID.
	 */
	public int compareTo(UUID otherUUID) {
		int res = 0;
		if (theUUID < otherUUID.theUUID)
			res = -1;
		if (theUUID > otherUUID.theUUID)
			res = 1;
		return res;
	}

	/**
	 * Sample bytes from a file. The resulting byte[] of
	 * the concatenated samples is then used by the caller
	 * to compute a UUID. All constants involved
	 * are defined at the top of this class definition.
	 * 
	 * NOTE: If this sampling is changed in any way,
	 *       previously computed UUIDs for files will
	 *       not be the same as those computed with the
	 *       modified code.
	 * 
	 * @param Path of file to sample
	 * @return A byte[] of the concatenated samples.
	 * @throws IOException
	 */
	public byte[] getFileSample(String fileName) throws IOException {

		RandomAccessFile randFd = new RandomAccessFile(fileName, "r");
		long fileLen = randFd.length();
		int samplesRead = 0;
		int readRes;
		
		if (!samplingInitialized) {
			initSamplingParameters();
			samplingInitialized = true;
		}
		// Go to sampleLocMultipliers.length different locations
		// in the file. At each location, collect numBytesToSample
		// bytes as one sample:
		try {
			for (int sampleSeq = 0; sampleSeq < sampleLocMultipliers.length; sampleSeq++) {

				randFd.seek(Math.round(fileLen
						* sampleLocMultipliers[sampleSeq]));
				readRes = randFd
						.read(allSamples, samplesRead, numBytesToSample);
				if (readRes > -1)
					samplesRead += readRes;
				else
					break;
			}
		} finally {
			randFd.close();
		}

		int bytesToPad = MAX_SAMPLE_BYTES - samplesRead;
		if (bytesToPad <= 0)
			return allSamples;
		for (int i = samplesRead; i < MAX_SAMPLE_BYTES; i++) {
			allSamples[i] = 0;
		}
		return allSamples;
	}
	
	/**
	 * Pre-compute multipliers for indexing into files
	 * to sampling sites. Just for speed. 
	 */
	private void initSamplingParameters() {
		for (int i = 0; i < sampleLocMultipliers.length; i++)
			sampleLocMultipliers[i] = ((double) sampleLocPercentages[i]) / 100.;
	}
	
	
	
}	
	
	

/*	
 * 
 * 
	// Testing:
  	public static void main(String[] args) {
		UUIDTypeDependent tString = 
			new UUIDTypeDependent("00000000-0000-002a-0000-00000000002a");
		tString.doIt();
		System.out.println("String printing: " + tString);

		UUIDTypeDependent tPhotoSpread = 
			new UUIDTypeDependent(new PhotoSpreadImage());
		System.out.println("String printing: " + tPhotoSpread);

		System.out.println("Equals non-image to itself:" + 
				tString.equals(tString));
		System.out.println("Equals image to itself:" + 
				tPhotoSpread.equals(tPhotoSpread));
		System.out.println("Equals image to non-image:" + 
				tPhotoSpread.equals(tString));
		System.out.println("Equals image to different image:" + 
				tPhotoSpread.equals(new PhotoSpreadImage()));
		System.out.println("Equals non-image to different non-image:" + 
				tString.equals(new UUIDTypeDependent("00000000-0000-002b-0000-00000000002b")));
	}
	
public class PhotoSpreadImage {
}
*/
