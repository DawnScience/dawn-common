package org.dawb.common.ui.plot.tool;

/**
 * Used to mark views which can contain tools
 * @author fcp94556
 *
 */
public interface IToolContainer {

	/**
	 * The active tool
	 * @return
	 */
	public IToolPage getActiveTool();
}
