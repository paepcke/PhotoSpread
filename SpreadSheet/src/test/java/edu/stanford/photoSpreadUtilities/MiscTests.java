package edu.stanford.photoSpreadUtilities;

import static org.junit.Assert.*;

import java.security.InvalidParameterException;

import org.junit.Test;

public class MiscTests {

	@Test
	public void testGetCellAddressToString() {
		String stringCoordsFromInts = Misc.getCellAddress(0, 1);
		assertEquals("Conversion (0,1) returned " + stringCoordsFromInts + ". Expected 'A1'", "A1", stringCoordsFromInts);
		
		String stringCoordsFromCellCoords = Misc.getCellAddress(new CellCoordinates(0, 1));
		assertEquals("Conversion CellCoordinates(0,1) to String returned " + 
						stringCoordsFromCellCoords + 
						". Expected 'A1'", 
						"A1", 
						stringCoordsFromCellCoords);
		
	}

	@Test
	public void testGetCellCoordinatesFromString() {
		
		CellCoordinates coords = Misc.getCellAddress("A1");
		assertEquals("Conversion string 'A1' to CellCoordinates returned " + 
						coords + 
						". Expected CellCoords<A1>", 
					 new CellCoordinates(0,1).toString(),
					 coords.toString());
		
		String excelAddr = "F10";
		CellCoordinates res = Misc.getCellAddress(excelAddr);
		CellCoordinates expected = new CellCoordinates(9, 6);
		assertEquals("Conversion 'F10' to CellCoordinates(9,6) failed. Got '" +
						res.toString(),
					expected.toString(),
					res.toString());
		
		excelAddr = "ZZ1";
		res = Misc.getCellAddress(excelAddr);
		expected = new CellCoordinates(0, 702);
		assertEquals("Conversion 'ZZ1' to CellCoordinates(0,702) failed. Got '" +
						res.toString(),
					expected.toString(),
					res.toString());
	}

	@Test
	public void testIntToExcelCol() {
		assertEquals("Failed col 702 to 'ZZ'.", "ZZ", Misc.intToExcelCol(702));
		try {
			Misc.intToExcelCol(0);
		} catch (InvalidParameterException e) {
			return; // Expected.
		}
		fail("Int to column name did not throw an InvalidParameterException for input 0.");
	}
}
