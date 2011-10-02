/**
 * 
 */
package edu.stanford.photoSpreadLoaders;

import java.io.File;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * @author paepcke
 *
 */
public class ResourcePaths extends LinkedList<String> {

	/**
	 *   To make serializability happy (I don't know what
	 *   this instance var means. Was inserted automatically. 
	 */

	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */

	private static ResourcePaths _singletonInstance = null;
	
	private ResourcePaths () {
		// Declared private to prevent instantiation from outside.
	}
	
	public static ResourcePaths getResourcePaths() {

	    if (_singletonInstance == null)
	        // It's ok, we can call this constructor.
	    	// The 'Collections.synchronizedList makes
	    	// this single instance thread safe. That's
	    	// provided by the Collections package.
	    	//_singletonInstance = (ResourcePaths) Collections.synchronizedList(new ResourcePaths());		
	    	_singletonInstance = new ResourcePaths();		
	    return _singletonInstance;
	}
	

    /*	
       For the rare problem of someone creating
       a second instance via cloning.
       */

	public Object clone()  {
		 return null;
		 // I should definitely throw an exception
		 // instead of just returning NULL!!!. But
		 // when I try to do that (throw new CloneNotSupportedException();)
		 // then compiler complains that the super's 
		 // method (LinkedList.clone() does not throw
		 // this exception, so I can't either. Don't
		 // know what to do here.
	 }	
	
	public boolean add(String path) {
		File normalizer = new File (path);
		String normalPath = normalizer.getAbsolutePath();
		if (normalPath.charAt(normalPath.length()-1) == File.separatorChar)
			return super.add(normalPath);
		else
			return super.add(normalPath + File.separatorChar);
	}
	
	private void testListAll() {
		
		ResourcePaths paths = getResourcePaths();
		ListIterator<String> pathIterator = paths.listIterator(0);
		
		System.out.println("Start listing paths.");
		while (pathIterator.hasNext())
			System.out.println(pathIterator.next());
		System.out.println("Done listing paths.");
	}
	
	private void testAdd () {
		ResourcePaths paths = getResourcePaths();
		
		paths.add("/users/paepcke/");
		testListAll();
		paths.add("C:\\users\\paepcke");
		testListAll();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ResourcePaths paths = getResourcePaths();
		paths.testListAll();
		paths.testAdd();
	}

}
