package org.dawb.common.ui.plot.region;

import java.util.EventListener;

public interface IRegionBoundsListener extends EventListener {

	
	/**
	 * Called when the region is being dragged around and
	 * its value is being updated in a live way. Do not do
	 * a lot of work in this callback.
	 * 
	 * @param evt
	 */
	void regionBoundsDragged(RegionBoundsEvent evt);

	/**
	 * Called when the region changes position, and the user has
	 * finished clicking and dragging or when the region position
	 * has been updated programmatically.
	 * 
	 * @param evt
	 */
	void regionBoundsChanged(RegionBoundsEvent evt);
}
