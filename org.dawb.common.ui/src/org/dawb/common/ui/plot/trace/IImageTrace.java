package org.dawb.common.ui.plot.trace;

import org.dawb.common.ui.plot.region.RegionBounds;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

public interface IImageTrace extends ITrace {

	/**
	 * Pulls a data set out of the image data for
	 * a give selection. For instance getting the bounds
	 * of a box to slice and return the data.
	 * 
	 * @param bounds
	 * @return
	 */
	AbstractDataset slice(RegionBounds bounds);

}
