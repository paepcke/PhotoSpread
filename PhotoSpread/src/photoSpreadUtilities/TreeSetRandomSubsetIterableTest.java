package photoSpreadUtilities;

import java.io.File;
import java.util.ArrayList;

import junit.framework.TestCase;
import photoSpreadObjects.PhotoSpreadDoubleObject;
import photoSpreadObjects.PhotoSpreadImage;
import photoSpreadObjects.PhotoSpreadObject;
import photoSpreadObjects.PhotoSpreadStringObject;
import photoSpreadObjects.PhotoSpreadTextFile;
import photoSpreadTable.PhotoSpreadCell;
import photoSpreadTable.PhotoSpreadTable;
import photoSpreadTable.PhotoSpreadTableModel;

public class TreeSetRandomSubsetIterableTest extends TestCase {

	TreeSetRandomSubsetIterable<PhotoSpreadObject> _tree;
	PhotoSpreadObjIndexerFinder _index;
	
	PhotoSpreadTableModel _model;
	PhotoSpreadCell _cell;

	PhotoSpreadStringObject _str1Obj;
	PhotoSpreadStringObject _str2Obj;
	
	PhotoSpreadDoubleObject _double9Obj;
	PhotoSpreadDoubleObject _double11Obj;
	
	String _unixFileName = "/usr/local/bin/foo.txt";
	String _winFileName  = "C:\\Program Files\\FooApp\\bin\\happyHour.exe";
	String _imgFileName  = "dc00000002.jpg";
	PhotoSpreadTextFile _unixTextFilePSObj;
	PhotoSpreadTextFile _winTextFilePSObj;
	PhotoSpreadImage _imgFilePSObj;
	File _unixJavaIOFileObj;
	File _winJavaIOFileObj;
	File _imgJavaIOFileObj;
	
	PhotoSpreadTable _table1;

	protected void setUp() throws Exception {
		super.setUp();
	
		//_model = new PhotoSpreadTableModel();
		_model = null;
		_cell  = new PhotoSpreadCell(_model, 0, 1);
		
		
		_tree = new TreeSetRandomSubsetIterable<PhotoSpreadObject>();
		_index = new PhotoSpreadObjIndexerFinder();
		_tree.setIndexer(_index);
		
		_str1Obj = new PhotoSpreadStringObject(_cell, "Str1Obj"); 
		_str2Obj = new PhotoSpreadStringObject(_cell, "Str2Obj"); 

		_double9Obj = new PhotoSpreadDoubleObject(_cell, 9.0); 
		_double11Obj = new PhotoSpreadDoubleObject(_cell, 11.0);
	
		_unixJavaIOFileObj = new File(_unixFileName);
		_winJavaIOFileObj  = new File(_winFileName);
		_imgJavaIOFileObj  = new File(_imgFileName);

		_unixTextFilePSObj = new PhotoSpreadTextFile(_cell, _unixFileName);
		_winTextFilePSObj = new PhotoSpreadTextFile(_cell, _winFileName);
		_imgFilePSObj = new PhotoSpreadImage(_cell, _imgFileName);
		
		//_table1 = new PhotoSpreadTable (_model, new JFrame()); 
		_table1 = null;
	}

	public void testAddE() {
		
		assertTrue("Add string obj.", _tree.add(_str1Obj));
		assertEquals("Index retrieves string obj1.", _str1Obj, _index.find(_str1Obj.valueOf()));
		assertEquals("Tree retrieves string obj1.", _str1Obj, _tree.find(_str1Obj.valueOf()));
		assertNull("Different string properly retrieves null", _tree.find(_str2Obj.valueOf()));
		
		assertTrue("Add second string obj.", _tree.add(_str2Obj));
		assertEquals("Index retrieves second string obj.", _str2Obj, _index.find(_str2Obj.valueOf()));
		assertEquals("Tree retrieves second string obj.", _str2Obj, _tree.find(_str2Obj.valueOf()));
		assertEquals("Tree still retrieves first string obj.", _str1Obj, _tree.find(_str1Obj.valueOf()));

		assertTrue("Add Double obj.", _tree.add(_double9Obj));
		assertEquals("Index retrieves Double obj 9.", _double9Obj, _index.find(_double9Obj.valueOf()));
		assertEquals("Tree retrieves Double obj 9.", _double9Obj, _tree.find(_double9Obj.valueOf()));
		assertNull("Different Double properly retrieves null", _tree.find(_double11Obj.valueOf()));
	}

	public void testClear() {
		_tree.clear();
		assertTrue("Indexer should be empty", _index.isEmpty());
		assertEquals("Size should be 0.", 0, _index.size());
		assertNull("Retrieving anything should be null", _tree.find(10.0));
	}

	public void testAddAllCollection() {
		
		ArrayList<PhotoSpreadObject> coll = new ArrayList<PhotoSpreadObject>();
		
		coll.add(_str1Obj);
		coll.add(_str2Obj);
		coll.add(_double9Obj);
		coll.add(_double11Obj);

		
		assertTrue("addAll an ArrayList.", _tree.addAll(coll));
		assertEquals("After addAll: Tree retrieves string obj1.", _str1Obj, _tree.find(_str1Obj.valueOf()));
		assertEquals("After addAll: Tree retrieves string obj2.", _str2Obj, _tree.find(_str2Obj.valueOf()));
		assertEquals("After addAll: Tree retrieves Double obj 9.", _double9Obj, _tree.find(_double9Obj.valueOf()));
		assertEquals("After addAll: Tree retrieves Double obj 11.", _double11Obj, _tree.find(_double11Obj.valueOf()));
	}

	public void testRemoveObject() {
		
		// Set the indexer up to contain all objects:
		fillIndexer ();
		assertTrue("Remove first string obj.", _tree.remove(_str1Obj));
		assertNull("After remove first obj: strObj1 no longer there.", _tree.find(_str1Obj.valueOf()));
		assertEquals("After remove first obj: strObj2 still there.", _str2Obj, _tree.find(_str2Obj.valueOf()));
	}

	public void testFindFile() {
		_tree.add(_unixTextFilePSObj);
		_tree.add(_winTextFilePSObj);
		_tree.add(_imgFilePSObj);
		
		assertEquals("Unix file object retrieval.", _unixTextFilePSObj, _tree.find(_unixJavaIOFileObj));
		assertEquals("Windows file object retrieval.", _winTextFilePSObj, _tree.find(_winJavaIOFileObj));
		assertEquals("Image file object retrieval.", _imgFilePSObj, _tree.find(_imgJavaIOFileObj));
	}

/*	public void testFindPhotoSpreadTable() {
		fail("Not yet implemented");
	}
*/
	
	private void fillIndexer () {
		ArrayList<PhotoSpreadObject> coll = new ArrayList<PhotoSpreadObject>();
		
		coll.add(_str1Obj);
		coll.add(_str2Obj);
		coll.add(_double9Obj);
		coll.add(_double11Obj);

		_tree.addAll(coll);
	}
}
