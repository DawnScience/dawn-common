package org.dawb.common.ui.plot.tool;

import org.dawb.common.ui.plot.IPlottingSystem;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * This class represents a page in a page book view which
 * is associated with a specific tool in the plotting system.
 * A given part may implement an adaptable to 
 * 
 * @author fcp94556
 *
 */
public interface IToolPage extends IPageBookViewPage {

	/**
	 * the title for the tool.
	 * @return
	 */
	public String getTitle();
	
	/**
	 * This title will be show in the part displaying this tool
	 * @param title
	 */
	public void setTitle(final String title);
	
	/**
	 * Called when the tool is read from extension for a given 
	 * plotting system instance, used internally.
	 * 
	 * @param system
	 */
	public void setPlottingSystem(IPlottingSystem system);
	
	/**
	 * returns the main plotting system that the tool is
	 * acting on - not the plotting system that this tool
	 * may be showing.
	 * 
	 * @return
	 */
	public IPlottingSystem getPlottingSystem();
	
	/**
	 * The tool system that this page is active within.
	 * @return
	 */
	public IToolPageSystem getToolSystem();
	
	/**
	 * Set the IToolPageSystem, used internally
	 * @param system
	 */
	public void setToolSystem(IToolPageSystem system);
}
