package org.dawb.common.ui.plot.tool;

/**
 * This system allows one to get the page
 * which corresponds to the tool the user would
 * like to use with their plotting system. The
 * page may contain its own plotting or it may
 * create selection regions on the main IPlottingSystem.
 * 
 * @author fcp94556
 *
 */
public interface IToolPageSystem {

	/**
	 * Get the current tool page that the user would like to use.
	 * Fitting, profile, derivative etc. Null if no selection has been made.
	 * @return
	 */
	public IToolPage getCurrentToolPage();
	
	/**
	 * Add a tool change listener. If the user changes preferred tool
	 * this listener will be called so that any views showing the current
	 * tool are updated.
	 * 
	 * @param l
	 */
	public void addToolChangeListener(IToolChangeListener l);
	
	/**
	 * Remove a tool change listener if one has been addded.
	 * @param l
	 */
	public void removeToolChangeListener(IToolChangeListener l);
}
