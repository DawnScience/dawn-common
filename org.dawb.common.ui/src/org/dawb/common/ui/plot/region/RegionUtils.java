package org.dawb.common.ui.plot.region;

import java.util.Collection;
import java.util.HashSet;

import org.dawb.common.ui.plot.IPlottingSystem;
import org.eclipse.swt.graphics.Color;

/**
 * Class containing utility methods for regions to avoid duplication 
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

	/**
	 * 
	 * @param plotter
	 * @return
	 */
	public static Color getUnqueColor(IRegion.RegionType type, IPlottingSystem plotter, Collection<Color> colours) {

		final Collection<Color> used = new HashSet<Color>(7);
		for (IRegion reg : plotter.getRegions()) {
			if (reg.getRegionType()!=type) continue;
			used.add(reg.getRegionColor());
		}
        
		for (Color color : colours) {
			if (!used.contains(color)) return color;
		}
		return colours.iterator().next();
	}
}
