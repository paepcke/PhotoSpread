/**
 * 
 */
package photoSpreadUtilities;

import photoSpreadUtilities.Misc.Pair;

/**
 * @author paepcke
 *
 */

public class CellCoordinates extends Pair<Integer, Integer> {

/**
	 * @param misc
	 * @param obj1
	 * @param obj2
	 */
	public CellCoordinates(Integer obj1, Integer obj2) {
		
		new Misc().super(obj1, obj2);
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
		return Misc.getCellAddress(this);
	}
}
