package org.dawb.common.ui.svg;

import org.eclipse.draw2d.geometry.Translatable;

public interface IMapMode {

	/**
	 * Convert a Logical Unit into a Device Unit
	 *  
	 * @param logicalUnit the value to be converted
	 * @return the value represented in device units
	 */
	public int LPtoDP(int logicalUnit);

	/**
	 * Convert a Device Unit into a Logical Unit
	 *  
	 * @param deviceUnit the value to be converted
	 * @return the value represented in logical units
	 */
	public int DPtoLP(int deviceUnit);
	
	/**
	 * Convert a <code>Translatable</code> to Device Units (pixels)
	 * 
	 * @param t the <code>Translatable</code> to convert
	 * @return the parameter <code>t</code> that was scaled for convenience.
	 */
	public Translatable LPtoDP( Translatable t );
	
	/**
	 * Convert a Translatable to Logical Units (i.e. Hi-Metrics)
	 * @param t the Translatable to convert
	 * @return the parameter <code>t</code> that was scaled for convenience.
	 */
	public Translatable DPtoLP( Translatable t );

}