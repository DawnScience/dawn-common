package org.dawb.common.ui.plot.region;

import org.dawb.common.ui.plot.IPlottingSystem;

/**
 * Class containing untility methods for regions to avoid duplication 
 * @author fcp94556
 *
 */
public class RegionUtils {

	/**
	 * Call to get a unique region name 
	 * @param nameStub
	 * @param system
	 * @return
	 */
	public static String getUniqueName(final String nameStub, final IPlottingSystem system) {
		int i = 1;
		while(system.getRegion(nameStub+" "+i)!=null) {
			++i;
			if (i>10000) break; // something went wrong!
		}
		return nameStub+" "+i;
	}
}
