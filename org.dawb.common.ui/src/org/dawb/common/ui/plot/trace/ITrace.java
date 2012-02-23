package org.dawb.common.ui.plot.trace;


import org.eclipse.swt.graphics.Color;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

/**
 * A representation of a plotted data set.
 * 
 * 
 * 
 * @author fcp94556
 *
 */
public interface ITrace {
	
	
	/**
	 * Name of trace, matches name of the abstract data set that originally created it.
	 * @return
	 */
	public String getName();
	/**
	 * Name of trace, matches name of the abstract data set that originally created it.
	 * @return
	 */
	public void setName(String name);
	
	
	/**
	 * Call this method to return a plotted data set by this trace. NOTE the plotting system
	 * will likely not be using AbstractDataset as internal data. Instead it will get the
	 * current data of the plot required and construct an AbstractDataset for it. This means
	 * that you can plot int data but get back double data if the graph keeps data internally
	 * as doubles for instance. If the append(...) method has been used, the data returned by
	 * name from here will include the appended points.
	 */
	public AbstractDataset getData();
	

}
