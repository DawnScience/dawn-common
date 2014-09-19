package org.dawnsci.boofcv.examples.imageprocessing;

import org.dawnsci.boofcv.examples.util.Utils;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;

public class ExampleImageStitching {

	public static void showStitchedImage(IDataset stitched) throws Throwable {
		Utils.showPlotView("uk.ac.diamond.scisoft.analysis.rcp.plotView1", "Plot 1", stitched);
		System.out.println("something has been plotted!");
	}
}
