package org.dawb.common.ui.plot.axis;

/**
 * Interface for converting between real value and coordinate in plotting system
 * (which is screen pixels).
 * 
 * Each region receives their own copy of ICoordinateSystem and this is
 * disposed when the region is. This avoids memory leaks on any listeners added
 * to the axes.
 * 
 * @author fcp94556
 *
 */
public interface ICoordinateSystem {

	
	/**
	 * The position in pixels of a given value.
	 * @param value
	 * @return
	 */
	public int[] getValuePosition(double... value);
	
	/**
	 * The value for a position in pixels.
	 * @param value
	 * @return
	 */
	public double[] getPositionValue(int... position);


	/**
	 * Listen to the coordinates changing, zoom in or image rotated usually.
	 * @param l
	 */
	public void addCoordinateSystemListener(ICoordinateSystemListener l);
	
	/**
	 * Removing listening to the coordinates changing, zoom in or image rotated usually.
	 * @param l
	 */
	public void removeCoordinateSystemListener(ICoordinateSystemListener l);

	/**
	 * Get x-axis when the orientation is standard.
	 * @return
	 */
	public IAxis getX();
	
	
	/**
	 * Get x-axis when the orientation is standard.
	 * @return
	 */
	public IAxis getY();

	/**
	 * Called when the region is removed. This assumes each ICoordinateSystem instance is unique
	 * to each region.
	 */
	public void dispose();

}
