/**
 * 
 */
package edu.stanford.photoSpreadUtilities;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;

import junit.framework.TestCase;
import edu.stanford.photoSpread.PhotoSpread;
import edu.stanford.photoSpreadObjects.PhotoSpreadObject;
import edu.stanford.photoSpreadObjects.PhotoSpreadStringObject;
import edu.stanford.photoSpreadTable.PhotoSpreadCell;
import edu.stanford.photoSpreadTable.PhotoSpreadTableModel;
import edu.stanford.photoSpreadUtilities.PhotoSpreadComparatorFactory.MetadataComparator;
import edu.stanford.photoSpreadUtilities.PhotoSpreadComparatorFactory.UUIDComparator;

/**
 * @author paepcke
 *
 */
public class PhotoSpreadComparatorFactoryTest extends TestCase {

	PhotoSpreadStringObject psStrObj1;
	PhotoSpreadStringObject psStrObj2;
	PhotoSpreadStringObject psStrObj3;
	PhotoSpreadStringObject psStrObj4;
	PhotoSpreadStringObject psStrObj5;
	PhotoSpreadStringObject psStrObj6;
	
	UUIDComparator compUUID = 
		(UUIDComparator) PhotoSpreadComparatorFactory.createPSUUIDComparator();
	MetadataComparator compMetadata = 
		(MetadataComparator) PhotoSpreadComparatorFactory.createPSMetadataComparator();
	
	TreeSetRandomSubsetIterable<PhotoSpreadObject>  objsUUIDTree;
	TreeSetRandomSubsetIterable<PhotoSpreadObject>  objsMetadataTree;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		
		super.setUp();
		
		PhotoSpread.initDefaultProperties();

		// The out-of-context creation of PhotoSpread table cells and models causes 
		// uninteresting errors. Just ignore them:
		
		try {
			psStrObj1 = new PhotoSpreadStringObject(new PhotoSpreadCell(new PhotoSpreadTableModel(), 0, 0), "Obj1");
			psStrObj2 = new PhotoSpreadStringObject(new PhotoSpreadCell(new PhotoSpreadTableModel(), 0, 1), "Obj2");
			psStrObj3 = new PhotoSpreadStringObject(new PhotoSpreadCell(new PhotoSpreadTableModel(), 0, 2), "Obj3");
			psStrObj4 = new PhotoSpreadStringObject(new PhotoSpreadCell(new PhotoSpreadTableModel(), 0, 3), "Obj4");
			psStrObj5 = new PhotoSpreadStringObject(new PhotoSpreadCell(new PhotoSpreadTableModel(), 0, 4), "Obj5");
			psStrObj6 = new PhotoSpreadStringObject(new PhotoSpreadCell(new PhotoSpreadTableModel(), 0, 5), "Obj6");
		} catch (Exception e) {
			
		}
					
		objsUUIDTree = new TreeSetRandomSubsetIterable<PhotoSpreadObject>(compUUID);
		objsMetadataTree = new TreeSetRandomSubsetIterable<PhotoSpreadObject>(compMetadata);
		
		objsUUIDTree.add(psStrObj3);
		objsUUIDTree.add(psStrObj4);
		objsUUIDTree.add(psStrObj1);
		objsUUIDTree.add(psStrObj5);
		objsUUIDTree.add(psStrObj2);
	}

	public void testCompareUUID() {
		
		assertEquals("Identity", Const.EQUAL, compUUID.compare(psStrObj1, psStrObj1));
		assertEquals("Earlier-created < Later-Created", Const.SMALLER, compUUID.compare(psStrObj1, psStrObj2));
		assertEquals("Later-created > Earlier-Created", Const.BIGGER, compUUID.compare(psStrObj2, psStrObj1));
		
		assertEquals("First from TreeSetRandomSubsetIterable", psStrObj1, objsUUIDTree.first());
		assertEquals("Last from TreeSetRandomSubsetIterable", psStrObj5, objsUUIDTree.last());
		
		SortedSet<PhotoSpreadObject> head = objsUUIDTree.headSet(psStrObj3);
		
		assertEquals("Size of head set for for objsUUIDTree.headSet(<3rd element>)", 2, head.size());
		assertEquals("Contains psStrObj4?", false, head.contains(psStrObj4));
		assertEquals("Contains psStrObj2?", true, head.contains(psStrObj2));
		assertEquals("Does NOT contain psStrObj6", false, head.contains(psStrObj6));
		
		Iterator<PhotoSpreadObject> it=head.iterator();
		assertEquals("First in head set for objsUUIDTree.headSet(<3rd element>)", it.next(), psStrObj1); 
		assertEquals("Second in head set for objsUUIDTree.headSet(<3rd element>)", it.next(), psStrObj2);
		try {
			assertEquals("Third in head set for objsUUIDTree.headSet(<3rd element>)", it.next(), psStrObj3);
			fail("Expected NoSuchElementException.");
		} catch (NoSuchElementException e) {
			// expected
		}
	}
	
	public void testCompareMetadata() {
		
		// Copy items from UUID comparison set to Metadata comparison set:
		objsMetadataTree.addAll(objsUUIDTree);
		
		// A person obj:
		psStrObj1.setMetaData("Name", "Eric");
		psStrObj1.setMetaData("Age", "25");
		psStrObj1.setMetaData("DOB", "1975-01-30");

		// Identical metadata in different objects:
		psStrObj2.setMetaData("Name", "Eric");
		psStrObj2.setMetaData("Age", "25");
		psStrObj2.setMetaData("DOB", "1975-01-30");
		
		// Different case for key and value; else same:
		psStrObj3.setMetaData("name", "eric");
		psStrObj3.setMetaData("Age", "25");
		psStrObj3.setMetaData("DOB", "1975-01-30");

		// Like obj1 in level 1 (Name), but different in level 2:
		psStrObj4.setMetaData("Name", "Eric");
		psStrObj4.setMetaData("Age", "26");
		psStrObj4.setMetaData("DOB", "1975-01-30");
		
		// Missing Level 2
		psStrObj5.setMetaData("Name", "eric");
		psStrObj5.setMetaData("DOB", "1975-01-30");
		
		assertEquals("Identity", Const.EQUAL, compMetadata.compare(psStrObj1, psStrObj1));
		assertEquals("No sort keys; compare by UUID as default", Const.SMALLER, compMetadata.compare(psStrObj2, psStrObj3));

		// Add first-tier metadata key 'Name':
		compMetadata.addMetadataSortKey("Name");
		
		assertEquals("Equal by first key: Name", Const.EQUAL, compMetadata.compare(psStrObj1, psStrObj2));
		assertEquals("Case is ignored", Const.EQUAL, compMetadata.compare(psStrObj1, psStrObj3));
		
		// Add second-tier metadata key 'Age':
		compMetadata.addMetadataSortKey("Age");

		assertEquals("Comparing numbers in 2nd tier", true, compMetadata.compare(psStrObj1, psStrObj6) < 0);
		
		// Remove Age from sort keys:
		compMetadata.removeMetadataSortKey("Age");
		
		
		assertEquals(
				"Equal when unrelated search key missing in one obj ('Age'). Only 'Name' left.", 
				Const.EQUAL, 
				compMetadata.compare(psStrObj1, psStrObj5));
		
		// Test obj metadata comparison when one or both objects are
		// missing a value for the metadata attribute:
		
		compMetadata.clearMetadataSortKeys();
		compMetadata.addMetadataSortKey("Foo");
		
		psStrObj1.setMetaData("Foo", "bar");

		assertEquals(
				"First obj has field, second does not", 
				Const.SMALLER,  // Obj1 < Obj2, making obj1 appear earlier in a sort 
				compMetadata.compare(psStrObj1, psStrObj2));
		
		assertEquals(
				"First obj does not have field, second does", 
				Const.BIGGER,  // Obj2 < Obj1, making obj2 appear earlier in a sort 
				compMetadata.compare(psStrObj2, psStrObj1));

		assertEquals(
				"Neither obj has field. Should default to comparing UUIDs", 
				Const.SMALLER,  // Obj3 < Obj4, making obj3 appear earlier in a sort. (Obj3 created ealier) 
				compMetadata.compare(psStrObj3, psStrObj4));
	}
}

