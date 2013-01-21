package org.dawb.common.ui.plot.trace;

import java.util.List;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

/**
 * DO NOT USE YET IPlottingSystem does not yet support 3D.
 * 
 * THIS INTERFACE IS A WORK IN PROGRESS (If you do use it it will not
 * work but your code will be backwardsly compatible when the 3D API
 * is finished.)
 * 
 * @author fcp94556
 *
 */
public interface ISurfaceTrace extends IPaletteTrace {

	/**
	 * Set the data of the plot, will replot if called on an active plot.
	 * @param data
	 * @param axes
	 * @throws Exception
	 */
	public void setData(final AbstractDataset data, final List<AbstractDataset> axes);
	
	/**
	 * The 3D set of axes, may contain nulls (z is often null for intensity).
	 * @return
	 */
	public List<AbstractDataset> getAxes();
	
	/**
	 * Data for the surface, a 2D dataset
	 */
	public AbstractDataset getData();
	
	/**
	 * Labels for the axes. The data set name of the axis used if not set.
	 * Should be size 3 but may have nulls.
	 * @return
	 */
	public List<String> getAxesNames();
	
	/**
	 * 
	 * @param axesNames
	 */
	public void setAxesNames(List<String> axesNames);
	
	/**
	 * 
	 * @return true if plot is currently plotting.
	 */
	public boolean isActive();

}
