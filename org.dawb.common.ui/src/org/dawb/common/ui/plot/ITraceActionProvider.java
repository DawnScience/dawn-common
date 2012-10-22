package org.dawb.common.ui.plot;

import org.dawb.common.ui.plot.trace.ITrace;
import org.eclipse.jface.action.IContributionManager;

public interface ITraceActionProvider {
	/**
	 * Creates the trace actions (add line, box etc.) in the IContributionManager
	 * @param toolBarManager
	 * @param imageTrace
	 * @param system
	 */
	public void fillTraceActions(IContributionManager toolBarManager, ITrace imageTrace, IPlottingSystem system);

}
