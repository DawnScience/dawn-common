package org.dawb.common.ui.plot.region;

import org.dawb.common.ui.plot.region.IRegion.RegionType;


public interface IRegionSystem {

	
	/**
	 * Creates a selection region by type. This does not create any user interface
	 * for the region. You can then call methods on the region to set color and 
	 * position for the selection. Use addRegion(...) and removeRegion(...) to control
	 * if the selection is active on the graph.
	 * 
	 * Usually thread safe.
	 * 
	 * @param name
	 * @param regionType
	 * @return
	 * @throws Exception if name exists already.
	 */
	public IRegion createRegion(final String name, final RegionType regionType) throws Exception;
	
	/**
	 * Add a selection region to the graph. Not thread safe, call from UI thread.
	 * @param region
	 */
	public void addRegion(final IRegion region);
	
	
	/**
	 * Remove a selection region to the graph. Not thread safe, call from UI thread.
	 * @param region
	 */
	public void removeRegion(final IRegion region);
	
	/**
	 * Get a region by name.
	 * @param name
	 * @return
	 */
	public IRegion getRegion(final String name);

	/**
	 * 
	 * @param l
	 */
	public boolean addRegionListener(final IRegionListener l);
	
	/**
	 * 
	 * @param l
	 */
	public boolean removeRegionListener(final IRegionListener l);

	/**
	 * Remove all regions. Not thread safe, call from UI thread.
	 */
	public void clearRegions();

}
