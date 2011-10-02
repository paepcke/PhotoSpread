/**
 * 
 */
package edu.stanford.photoSpreadUtilities;

import java.awt.Dimension;
import java.awt.Point;

/**
 * @author paepcke
 * Dimension (width/height) that can be scaled, multiplied, and divided
 */
public class ComputableDimension extends Dimension implements Comparable<Dimension> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

/****************************************************
 * Constructor(s)
 *****************************************************/
	
	/**
	 * Create ComputableDimension with zero width/height.
	 */
	public ComputableDimension() {
		super();
	}

	/**
	 * Create ComputableDimension from another Dimension.
	 * @param d
	 */
	public ComputableDimension(Dimension d) {
		super(d);
	}

	/**
	 * Create ComputableDimension from width and height.
	 * @param width
	 * @param height
	 */
	public ComputableDimension(int width, int height) {
		super(width, height);
	}
	
	/**
	 * Create ComputableDimension from a string that contains two
	 * space-separated integers.
	 * @param widthHeightStr String containing two space-separated integers. Example: "10 20"
	 */
	public ComputableDimension (String widthHeightStr) {
		super(new Dimension(makeIntPairFromString(widthHeightStr).first(), makeIntPairFromString(widthHeightStr).second()));
	}
	
	/**
	 * Create largest ComputableDimension from two Dimensions' widths and heights.
	 * @param d1 First dimension.
	 * @param d2 Second dimension.
	 */
	public ComputableDimension(Dimension d1, Dimension d2) {
		super(Math.max(d1.width, d2.width), Math.max(d1.height, d2.height));
	}

	public ComputableDimension(Point pt) {
		super(pt.x, pt.y);
	}
	
	/****************************************************
	 * Methods
	 *****************************************************/
	
	private static Misc.Pair<Integer, Integer> makeIntPairFromString (String twoIntStr) {
		return Misc.twoIntsFromString (twoIntStr);
	}
	
	/**
	 * Conversion to point
	 * @param dim
	 * @return New Point object with x/y set from width/height 
	 */
	public Point toPoint () {
		return new Point(this.width, this.height);
	}
	
	/**
	 * Adds parameter's value to this ComputableDimension
	 * instance's width and height.
	 * 
	 * @param addant Integer to add to width and height
	 * @return New ComputableDimension of the added-to width/height
	 */
	
	public ComputableDimension plus (int addant) {
		return new ComputableDimension(this.width + addant, this.height + addant);
	}
	
	/**
	 * Adds parameter's value to this ComputableDimension
	 * instance's width and height.
	 * 
	 * @param addant Dimension whose width/height are to be added to
	 * this {@link ComputableDimension} instance's width/height.
	 * @return New ComputableDimension of the added-to width/height
	 */

	public ComputableDimension plus (ComputableDimension addant) {
		return new ComputableDimension(this.width + addant.width, this.height + addant.height);
	}

	/**
	 * Subtracts parameter's value from this ComputableDimension
	 * instance's width and height.
	 * 
	 * @param addant Integer to subtract from width and height
	 * @return New ComputableDimension of the added-to width/height.
	 * Will do negative height/width results.
	 */
	
	public ComputableDimension minus (int subtrahent) {
		return new ComputableDimension(this.width - subtrahent, this.height - subtrahent);
	}

	/**
	 * Subtract parameter's value from this ComputableDimension
	 * instance's width and height.
	 * 
	 * @param subtrahent ComputableDimension whose width/height 
	 * are to be added to this {@link ComputableDimension} instance's width/height.
	 * @return New ComputableDimension of the subtracted-from width/height
	 */

	public ComputableDimension minus (ComputableDimension subtrahent) {
		return new ComputableDimension(this.width + subtrahent.width, this.height + subtrahent.height);
	}
	
	
	/**
	 * Compute given percentage for width and height
	 * 
	 * @param percentage
	 * @return New ComputableDimension with width/height set to 
	 * percentage of current width/height. 
	 */
	
	public ComputableDimension percent(int percentage) {
		return new ComputableDimension (((int)this.getWidth() * percentage / 100), ((int)this.height * percentage / 100));
	}
	
	/**
	 * @param divisor
	 * @return ComputableDimension with width and height divided by divisor
	 */
	
	public ComputableDimension div(int divisor) {
		return new ComputableDimension(((int) this.width/divisor), ((int) this.height/divisor));
	}

	/**
	 * @param factor
	 * @return ComputableDimension with width and height each multiplied by factor.
	 */
	public ComputableDimension times (int factor) {
		return new ComputableDimension(((int) this.width * factor), ((int) this.height * factor));
	}
	
	public Boolean equals(Dimension dim) {
		return ((dim.width == this.width) && (dim.height == this.height));
	}
	
	/**
	 * Implements compareTo for ComputableDimension.
	 * Precedence given to height over width:
	 * 	    this-height < passed-in-height ==> this is smaller
	 * 	    this-height > passed-in-height ==> this is bigger
	 * 
	 * 	    this-width < passed-in-height ==> this is smaller
	 *                ...
	 * @param Dimension or ComputableDimension to compare to 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Dimension thatDim) {
		
		if (this == thatDim) return Const.EQUAL;

		if (this.height < thatDim.height) return Const.SMALLER;
		if (this.height > thatDim.height) return Const.BIGGER;
		
		if (this.width< thatDim.width) return Const.SMALLER;
		if (this.width > thatDim.width) return Const.BIGGER;
		
		assert this.equals(thatDim) : "CompareTo for ComputableDimension inconsistent with equals.";
		return Const.EQUAL;
	}
	
	public int compareWidthTo (Dimension thatDim) {
		
		if (this == thatDim) return Const.EQUAL;

		if (this.width< thatDim.width) return Const.SMALLER;
		if (this.width > thatDim.width) return Const.BIGGER;
		
		assert (this.width == thatDim.width) : "CompareWidthTo for ComputableDimension inconsistent with equals.";
		return Const.EQUAL;
		
	}

	public int compareHeightTo (Dimension thatDim) {
		
		if (this == thatDim) return Const.EQUAL;

		if (this.height < thatDim.height) return Const.SMALLER;
		if (this.height > thatDim.height) return Const.BIGGER;
		
		assert (this.height == thatDim.height) : "CompareHeightTo for ComputableDimension inconsistent with equals.";
		return Const.EQUAL;
	}
	
	/**
	 * Given a second dimension, return the larger of the two widths.
	 * @param thatDim
	 * @return Larger of the widths
	 */
	public int maxWidth (Dimension thatDim) {
		return Math.max(this.width, thatDim.width);
	}
	
	/**
	 * Given a second dimension, return the larger of the two heights.
	 * @param thatDim
	 * @return Larger of the heights
	 */
	public int maxHeight(Dimension thatDim) {
		return Math.max(this.height, thatDim.height);
	}
	
	/**
	 * Given a second dimension, return the smaller of the two widths.
	 * @param thatDim
	 * @return Smaller of the two widths.
	 */
	public int minWidth (Dimension thatDim) {
		return Math.min(this.width, thatDim.width);
	}
	
	/**
	 * Given a second dimension, return the smaller of the two heights.
	 * @param thatDim
	 * @return Smaller of the two heights
	 */
	public int minHeight(Dimension thatDim) {
		return Math.min(this.height, thatDim.height);
	}
	
	/**
	 * Given a second dimension, return the smaller area of the two.
	 * @param thatDim
	 * @return Smaller area of the two dimensions 
	 */
	public int minArea (Dimension thatDim) {
		return Math.min (this.width * this.height, thatDim.width * thatDim.height);
	}

	/**
	 * Given a second dimension, return the larger area of the two.
	 *
	 * @param thatDim
	 * @return Larger area of the two dimensions 
	 */
	public int maxArea (Dimension thatDim) {
		return Math.max (this.width * this.height, thatDim.width * thatDim.height);
	}
	
	/**
	 * Given an array of Dimension instances, return the largest
	 * width and the largest height among those instances. These
	 * quantities may originate from different instances.
	 * @param dims
	 * @return ComputableDimension containing the maximum width and 
	 * maximum height among the passed-in Dimension instances.
	 */
	public static ComputableDimension maxWidthHeight (Dimension dims[]) {
		
		int maxWidth = 0;
		int maxHeight = 0;
		
		for (int i = 0; i < dims.length; i++) {
			if (dims[i].width > maxWidth) maxWidth = dims[i].width;
			if (dims[i].height> maxHeight) maxHeight = dims[i].height;
		}
		
		return new ComputableDimension(maxWidth, maxHeight);
	}
	
}
