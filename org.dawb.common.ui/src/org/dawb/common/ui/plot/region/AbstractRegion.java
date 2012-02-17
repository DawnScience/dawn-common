package org.dawb.common.ui.plot.region;

import java.util.Collection;
import java.util.HashSet;


public abstract class AbstractRegion implements IRegion {

	private Collection<IRegionBoundsListener> regionBoundsListeners;
	private boolean regionEventsActive = true;

	/**
	 * Add a listener which is notified when this region is resized or
	 * moved.
	 * 
	 * @param l
	 */
	public boolean addRegionBoundsListener(final IRegionBoundsListener l) {
		if (regionBoundsListeners==null) regionBoundsListeners = new HashSet<IRegionBoundsListener>(11);
		return regionBoundsListeners.add(l);
	}
	
	/**
	 * Remove a RegionBoundsListener
	 * @param l
	 */
	public boolean removeRegionBoundsListener(final IRegionBoundsListener l) {
		if (regionBoundsListeners==null) return false;
		return regionBoundsListeners.remove(l);
	}
	
	protected void clearListeners() {
		if (regionBoundsListeners==null) return;
		regionBoundsListeners.clear();
	}
	
	protected void fireRegionBoundsDragged(RegionBounds bounds) {
		
		if (regionBoundsListeners==null) return;
		if (!regionEventsActive) return;
		
		final RegionBoundsEvent evt = new RegionBoundsEvent(this, bounds);
		for (IRegionBoundsListener l : regionBoundsListeners) {
			l.regionBoundsDragged(evt);
		}
	}
	
	protected void fireRegionBoundsChanged(RegionBounds bounds) {
		
		if (regionBoundsListeners==null) return;
		if (!regionEventsActive) return;
		
		final RegionBoundsEvent evt = new RegionBoundsEvent(this, bounds);
		for (IRegionBoundsListener l : regionBoundsListeners) {
			l.regionBoundsChanged(evt);
		}
	}
	
	protected RegionBounds regionBounds;
	
	public RegionBounds getRegionBounds() {
		return regionBounds;
	}

	public void setRegionBounds(RegionBounds bounds) {
		this.regionBounds = bounds;
		fireRegionBoundsChanged(bounds);
	}

	/**
	 * Updates the position of the region, usually called
	 * when items have been created and the position of the 
	 * region should be updated. Does not fire events.
	 */
	protected void updateRegionBounds() {
		if (regionBounds!=null) {
			try {
				this.regionEventsActive = false;
				setRegionBounds(regionBounds);
			} finally {
				this.regionEventsActive = true;
			}
		}
	}
}
