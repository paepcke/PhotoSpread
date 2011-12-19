/**
 * 
 */
package edu.stanford.photoSpreadUtilities;

import edu.stanford.photoSpreadUtilities.Misc.Pair;

/**
 * @author paepcke
 *
 */

public class CellCoordinates extends Pair<Integer, Integer> {

/**
	 * @param rowNum: zero-based
	 * @param colNum: 1-based
	 */
	public CellCoordinates(Integer rowNum, Integer colNum) {
		
		new Misc().super(rowNum, colNum);
	}

	public int row() {
		return this.first();
	}

	public int column() {
		return this.second();
	}
	
	public boolean equals (CellCoordinates c1, CellCoordinates c2) {
		
		if (c1 == null)
			return (c2 == null);
		if (c2 == null)
			return (c1 == null);
		
		return ((c1.row() == c2.row()) &&
				(c1.column() == c2.column()));
	}
	
	public boolean equals(CellCoordinates otherCell) {
		if (otherCell == null)
			return false;
		return ((row() == otherCell.row()) &&
				(column() == otherCell.column()));
		
	}
	
	public String toString () {
		return "CellCoordinates<" + Misc.getCellAddress(this) + ">";
	}
}
