package org.dawnsci.boofcv.examples.util;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.ui.IWorkbenchPage;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

public class Utils {

	/**
	 * 
	 * @param viewID
	 * @param plotName
	 * @param img
	 * @throws Throwable
	 */
	public static void showPlotView(final String viewID,
			final String plotName, final IDataset img) throws Throwable {
		EclipseUtils.getPage().showView(viewID);
		EclipseUtils.getPage().setPartState(
				EclipseUtils.getPage().findViewReference(viewID),
				IWorkbenchPage.STATE_MAXIMIZED);
		try {
			SDAPlotter.imagePlot(plotName, img);
		} catch (Exception e) {
			e.printStackTrace();
		}
		EclipseUtils.delay(1000);
	}
}
