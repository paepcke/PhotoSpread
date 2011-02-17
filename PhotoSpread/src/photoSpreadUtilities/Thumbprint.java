/**
 * 
 */
package photoSpreadUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import photoSpread.PhotoSpreadException;
import photoSpread.PhotoSpreadException.FileIOException;


/**
 * Creates a quite likely unique thumbprint of
 * a file. 
 * @author paepcke
 *
 */

public class Thumbprint {

	private static final int firstSamplePlace =  3500;
	private static final int secondSamplePlace =  7000;
	private static final int sampleSize =  400;
	private static final int useAllContentThreshold =  secondSamplePlace + sampleSize + 1;

	private long fileSize = 0;
	private long firstSampleSum = 0;
	private long secondSampleSum = 0;

	/****************************************************
	 * Factories
	 * @throws FileIOException 
	 * @throws photoSpread.PhotoSpreadException.FileNotFoundException 
	 *****************************************************/

	// FileInputStream --> FileChannel --> ByteBuffer --> loop get(int)
	// BufferedImage.getRaster() --> Raster.getDataBuffer() --> DataBuffer 

	public Thumbprint (String fileName) 
	throws photoSpread.PhotoSpreadException.FileNotFoundException, FileIOException {
		computeThumbprint(fileName);
	}

	public Thumbprint (File file) 
	throws photoSpread.PhotoSpreadException.FileNotFoundException, FileIOException {
		computeThumbprint(file);
	}

	/****************************************************
	 * Methods
	 *****************************************************/

	public boolean equals (Thumbprint otherThumbprint) {
		return ((fileSize == otherThumbprint.getFileSize()) &&
				(firstSampleSum == otherThumbprint.getFirstSampleSum()) &&
				(secondSampleSum == otherThumbprint.getSecondSampleSum()));
	}
	
	protected long getFileSize() {
		return fileSize;
	}
	
	protected long getFirstSampleSum () {
		return firstSampleSum;
	}
	
	protected long getSecondSampleSum () {
		return secondSampleSum;
	}
	
	/*
	 * Heavy lifting for thumbprint computation.
	 * @throws FileIOException 
	 * @throws photoSpread.PhotoSpreadException.FileNotFoundException 
	 * @throws FileIOException 

	 */
	private void computeThumbprint (String fileName) 
	throws photoSpread.PhotoSpreadException.FileNotFoundException, FileIOException {
		computeThumbprint(new File(fileName));
	}

	private void computeThumbprint (File file) 
	throws photoSpread.PhotoSpreadException.FileNotFoundException, FileIOException {

		FileInputStream finStream = null;

		try {
			finStream = new FileInputStream(file);

			FileChannel fChannel = finStream.getChannel();
			
			fileSize = fChannel.size();
			if (fChannel.size() <= useAllContentThreshold) {
				addAllBytes(fChannel, file);
				return;
			}
			
			// Get a buffer exactly sampleSize bytes large:
			ByteBuffer byteBuffer = ByteBuffer.allocate(sampleSize);
			
			// File pointer to where first sample is to be taken:
			fChannel.position(firstSamplePlace);
			
			// Read all sampleSize bytes:
			int bytesRead = fChannel.read(byteBuffer);
			if (bytesRead != sampleSize) {
				throw new IOException("Cannot read first sample.");
			}
			
			// Add them up to get the sum of the first sample:
			for (int i=0; i<sampleSize; i++) 
				firstSampleSum += byteBuffer.get(i);

			// Get a fresh sample buffer:
			byteBuffer = ByteBuffer.allocate(sampleSize);

			// File pointer to where second sample is to be taken:
			fChannel.position(secondSamplePlace);
			
			// Read all sampleSize bytes:
			bytesRead = fChannel.read(byteBuffer);
			if (bytesRead != sampleSize) {
				throw new IOException("Cannot read second sample.");
			}
			
			// Add them up to get the sum of the first sample:
			for (int i=0; i<sampleSize; i++) 
				secondSampleSum += byteBuffer.get(i);


		} catch (FileNotFoundException e) {
			throw new PhotoSpreadException.FileNotFoundException("File '" + file.getAbsolutePath() + "' not found.");
			
		} catch (IOException e1) {

			throw new PhotoSpreadException.FileIOException(
					"File read problem with '" + 
					file.getAbsolutePath() + 
					".' " +
					e1.getMessage());
		} finally {
			if (finStream != null)
				try {
					finStream.close();
				} catch (IOException e1) {

					throw new PhotoSpreadException.FileIOException(
							"Cannot close file '" + 
							file.getAbsolutePath() + 
							".' " +
							e1.getMessage());
				}
		}
	}
	
	private void addAllBytes(FileChannel fChannel, File file) throws FileIOException {

		try {
		// Get a buffer exactly the size of the file bytes large:
		ByteBuffer byteBuffer = ByteBuffer.allocate((int) fileSize);

		// Read the entire file, (which is known to be small;
		// otherwise we would have sampled above):
		
		int bytesRead = fChannel.read(byteBuffer);
		if (bytesRead != sampleSize) {
			throw new IOException("Cannot read first sample.");
		}
		
		// Add them up to get the sum of the first sample:
		for (int i=0; i<fileSize; i++) 
			firstSampleSum += byteBuffer.get(i);

		} catch (IOException e1) {

			throw new PhotoSpreadException.FileIOException(
					"File read problem with '" + 
					file.getAbsolutePath() + 
					".' " +
					e1.getMessage());
		}
	}



	/****************************************************
	 * Test Methods --- Main
	 * @throws FileIOException 
	 * @throws photoSpread.PhotoSpreadException.FileNotFoundException 
	 *****************************************************/


	public static void main(String[] args) 
	throws photoSpread.PhotoSpreadException.FileNotFoundException, FileIOException {

		Thumbprint tp1;
		Thumbprint tp2;
		Thumbprint tp1Again;
		@SuppressWarnings("unused")
		Thumbprint tpError;
		
		String f1 = "E:\\Users\\Paepcke\\dldev\\src\\PhotoSpreadTesting\\TestCases\\Photos\\crowdOneFaceClear.jpg";
		File f2 = new File ("E:\\Users\\Paepcke\\dldev\\src\\PhotoSpreadTesting\\TestCases\\Photos\\conventionCenterTwoWomen.jpg");

		tp1 = new Thumbprint(f1);
		tp2 = new Thumbprint(f2);
		tp1Again = new Thumbprint(f1);
		System.out.println("Thumb1 == Thumb2?: " + tp1.equals(tp2));
		System.out.println("Thumb1 == Thumb1?: " + tp1.equals(tp1));
		System.out.println("Thumb1 == Thumb1Again?: " + tp1.equals(tp1Again));
		tpError = new Thumbprint("foo/bar");
	}

}
