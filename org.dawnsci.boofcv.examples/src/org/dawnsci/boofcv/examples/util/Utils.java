/*-
 * Copyright (c) 2014 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
