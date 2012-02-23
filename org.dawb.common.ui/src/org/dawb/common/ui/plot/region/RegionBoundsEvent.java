package org.dawb.common.ui.plot.region;

import java.util.EventObject;

public class RegionBoundsEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5892437380421200585L;
	private RegionBounds regionBounds;

	public RegionBoundsEvent(Object source, RegionBounds regionBounds) {
		super(source);
		this.regionBounds = regionBounds;
	}

	public RegionBounds getRegionBounds() {
		return regionBounds;
	}

}
