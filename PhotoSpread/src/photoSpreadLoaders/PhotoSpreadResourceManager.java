/**
 * 
 */
package photoSpreadLoaders;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

/**
 * @author paepcke
 *
 */
public class PhotoSpreadResourceManager extends ClassLoader {

	/**
	 * @param args
	 */
	
	// All root directories of resources (Singleton instance)
	public static ResourcePaths resourceRoots = ResourcePaths.getResourcePaths();
	
	// Quickly recall mapping from an old path root to a new one:
	Hashtable<String, String> oldToNewRoot;
	
	public PhotoSpreadResourceManager() {
		System.out.println("Initialized - No file");
	}

	public PhotoSpreadResourceManager(String propFileName) {	
		System.out.println("Initialized - file:" + propFileName);
	}

	public String mapResourceLocation (String oldResource) {
		/////******
		return "foo";
	}
	
	public String findLongestResourceMatch(String oldResource) {

		String res = "";
		File resFile;
		String resourceRoot;
		File normalizer = new File(oldResource);
		String old = normalizer.getAbsolutePath();
		int sepIndex = 0;
		java.util.Iterator<String> resourceRootIterator = resourceRoots.iterator();
		
		while (resourceRootIterator.hasNext()) { 
			resourceRoot = resourceRootIterator.next();
			while (true) {
				res = resourceRoot + old.substring(sepIndex);
				resFile = new File(res);
				if (resFile.exists())
					return res;
				sepIndex = (old.substring(sepIndex)).indexOf(File.separatorChar);
			}
		}
		return "";
	}
	
	private static File normalizeFileName (String fileName) {
		return new File(fileName);
	}
	
	private static int matchTo (File knownPath, File newPath) {
		
		String knownDir;
		String newDir;
		int knownIndx, newIndx;
		int currMatch = -1;
		char currChar;
		
		if (knownPath.getAbsolutePath().isEmpty() || newPath.getAbsolutePath().isEmpty()) 
			return -1;
		
		knownDir = knownPath.getAbsolutePath();
		newDir = newPath.getAbsolutePath();
		
		knownIndx = knownDir.length() - 1;
		newIndx   = newDir.length() - 1;
		
		for (; knownIndx > -1; knownIndx--) {
			currChar = knownDir.charAt(knownIndx);
			if (currChar != newDir.charAt(newIndx--))
				return currMatch;
			if (currChar == File.separatorChar)
				currMatch = newIndx + 2;
		}
		currMatch = 0; // everything matched
		
		return currMatch;
		
	}
	
	private static void testNormalizeFileName () {
		File knownF = PhotoSpreadResourceManager.normalizeFileName("C:\\Users\\Paepcke\\foo.txt");
		File newF = PhotoSpreadResourceManager.normalizeFileName("C:\\Users\\Paepcke\\foo.txt");
		System.out.println("Known: " + knownF.getAbsolutePath());
		System.out.println("New: " + newF.getAbsolutePath());
		// System.out.println(knownF.getCanonicalPath());
		// System.out.println(knownF.getName());
		
	}
	
	private static void testMatchTo () {

		File knownF = PhotoSpreadResourceManager.normalizeFileName("C:\\Users\\Paepcke\\foo.txt");
		File newF = PhotoSpreadResourceManager.normalizeFileName("C:\\Users\\Paepcke\\foo.txt");
		int matchPoint = PhotoSpreadResourceManager.matchTo(knownF, newF);
		System.out.println("Match up to: " + matchPoint); 
		if (matchPoint > -1)
			System.out.println("Match: " + newF.getAbsolutePath().substring(matchPoint));

		System.out.println();
		knownF = PhotoSpreadResourceManager.normalizeFileName("C:\\Users\\Paepcke\\foo.txt");
		newF = PhotoSpreadResourceManager.normalizeFileName("G:\\Users\\Paepcke\\foo.txt");		
		System.out.println("Known: " + knownF.getAbsolutePath());
		System.out.println("New: " + newF.getAbsolutePath());
		matchPoint = PhotoSpreadResourceManager.matchTo(knownF, newF);
		System.out.println("Match up to: " + matchPoint); 
		if (matchPoint > -1)
			System.out.println("Match: " + newF.getAbsolutePath().substring(matchPoint));
	
		System.out.println();
		knownF = PhotoSpreadResourceManager.normalizeFileName("C:\\Users\\Paepcke\\foo.txt");
		newF = PhotoSpreadResourceManager.normalizeFileName("G:\\boilers\\sunday\\foo.txt");		
		System.out.println("Known: " + knownF.getAbsolutePath());
		System.out.println("New: " + newF.getAbsolutePath());
		matchPoint = PhotoSpreadResourceManager.matchTo(knownF, newF);
		System.out.println("Match up to: " + matchPoint); 
		if (matchPoint > -1)
			System.out.println("Match: " + newF.getAbsolutePath().substring(matchPoint));
	
	}
	
/*	private static void testFindLongestResourceMatch () {
		resourceRoots.add("G:\\Crystal all photos to be edited");
		resourceRoots.add("G:\\Crystal all photos to be edited\\SORTED PHOTOS");
		
	}
*/	
	public static void main(String[] args) throws IOException {

		System.out.println("I Start.");
		
		//System.getProperties().list(System.out);
		testNormalizeFileName();
		testMatchTo();
		

		System.out.println("I stopped.");
	}


}
