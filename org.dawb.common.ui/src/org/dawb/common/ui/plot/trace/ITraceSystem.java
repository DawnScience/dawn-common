package org.dawb.common.ui.plot.trace;

import java.util.Collection;

public interface ITraceSystem {
	
	
	/**
	 * Creates a line trace used for 1D plotting. This does not add the trace
	 * or give it any data.
	 * 
	 * @param traceName
	 * @return
	 */
	public IImageTrace createImageTrace(String traceName);

	/**
	 * Creates a line trace used for 1D plotting. This does not add the trace
	 * or give it any data.
	 * 
	 * @param traceName
	 * @return
	 */
	public ILineTrace createLineTrace(String traceName);
	
	/**
	 * Adds and plots the trace.
	 * @param trace
	 */
	public void addTrace(ITrace trace);

	/**
	 * Adds and plots the trace.
	 * @param trace
	 */
	public void removeTrace(ITrace trace);

    /**
     * return a plotted trace by name.
     * @param name
     * @return
     */
	public ITrace getTrace(String name);
	
	/**
	 * Call this method to retrieve what is currently plotted.
	 * See all ITraceListener.
	 * 
	 * @return
	 */
	public Collection<ITrace> getTraces();

	

	/**
	 * Add a listener to be notified of new traces plotted
	 * @param l
	 */
	public void addTraceListener(final ITraceListener l);
	
	/**
	 * Remove listener to avoid memory leaks
	 * @param l
	 */
	public void removeTraceListener(final ITraceListener l);
	

}
