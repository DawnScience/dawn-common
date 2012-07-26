package org.dawb.common.ui.plot.axis;

/**
 * Interface for converting between real value and coordinate in plotting system
 * (which is screen pixels).
 * 
 * @author fcp94556
 *
 */
public interface ICoordinate {

	
	/**
	 * The position in pixels of a given value.
	 * @param value
	 * @return
	 */
	public int getValuePosition(double value);
	
	/**
	 * The value for a position in pixels.
	 * @param value
	 * @return
	 */
	public double getPositionValue(int position);


}
