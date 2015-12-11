package org.dawnsci.boofcv.examples.imageprocessing;

import org.dawnsci.boofcv.examples.util.Utils;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;

public class ExampleImageStitching {

	public static IDataset showStitchedImage(IDataset stitched) throws Throwable {
		return Utils.showPlotView("uk.ac.diamond.scisoft.analysis.rcp.plotView1", "Plot 1", stitched);
	}
}
