package org.dawb.common.ui.plot.trace;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
	public static String getUniqueTrace(final String nameStub, final IPlottingSystem system, final String... usedNames) {
		int i = 1;
		final List used = usedNames!=null ? Arrays.asList(usedNames) : Collections.emptyList();
		while(system.getTrace(nameStub+" "+i)!=null || used.contains(nameStub+" "+i)) {
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
