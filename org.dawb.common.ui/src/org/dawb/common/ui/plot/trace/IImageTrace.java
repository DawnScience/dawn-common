package org.dawb.common.ui.plot.trace;

import org.dawb.common.ui.plot.region.RegionBounds;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

public interface IImageTrace extends ITrace {

	public enum ImageOrigin {
		TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT;
	}

	/**
	 * Pulls a data set out of the image data for
	 * a given selection. For instance getting the bounds
	 * of a box to slice and return the data.
	 * 
	 * @param bounds
	 * @return
	 */
	AbstractDataset slice(RegionBounds bounds);

	/**
	 * Default is TOP_LEFT unlike normal plotting
	 * @return
	 */
	public ImageOrigin getImageOrigin();
	
	/**
	 * Repaints the axes and the image to the new origin.
	 * @param origin
	 */
	public void setImageOrigin(final ImageOrigin origin);
}
