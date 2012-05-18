package org.dawb.common.ui.plot.trace;

import org.dawb.common.ui.plot.IPlottingSystem;

/**
 * Class containing utility methods for regions to avoid duplication 
 * @author fcp94556
 *
 */
public class TraceUtils {

	/**
	 * Call to get a unique region name 
	 * @param nameStub
	 * @param system
	 * @return
	 */
	public static String getUniqueTrace(final String nameStub, final IPlottingSystem system) {
		int i = 1;
		while(system.getTrace(nameStub+" "+i)!=null) {
			++i;
			if (i>10000) break; // something went wrong!
		}
		return nameStub+" "+i;
	}

	/**
	 * Removes a trace of this name if it is already there.
	 * @param plottingSystem
	 * @param string
	 * @return
	 */
	public static final ILineTrace replaceCreateLineTrace(IPlottingSystem system, String name) {
		if (system.getTrace(name)!=null) {
			system.removeTrace(system.getTrace(name));
		}
		return system.createLineTrace(name);
	}
}
