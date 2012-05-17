package org.dawb.common.ui.plot.trace;


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
	 * Call this method to return a plotted data set by this trace.
	 */
	public AbstractDataset getData();
	
	/**
	 * True if visible
	 * @return
	 */
	public boolean isVisible();

	/**
	 * True if visible
	 * @return
	 */
	public void setVisible(boolean isVisible);
	
	/**
	 * True if user trace (normally is)
	 * @return
	 */
	public boolean isUserTrace();

	/**
	 * True if visible
	 * @return
	 */
	public void setUserTrace(boolean isUserTrace);

}
