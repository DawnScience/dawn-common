/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.boofcv.examples.util;

import java.util.ArrayList;
import java.util.List;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.ui.IWorkbenchPage;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.io.ImageStackLoader;

public class Utils {

	/**
	 * 
	 * @param viewID
	 * @param plotName
	 * @param img
	 * @throws Throwable
	 */
	public static IDataset showPlotView(final String viewID,
			final String plotName, final IDataset img) throws Throwable {
		EclipseUtils.getPage().showView(viewID);
		EclipseUtils.getPage().setPartState(
				EclipseUtils.getPage().findViewReference(viewID),
				IWorkbenchPage.STATE_MAXIMIZED);
		IDataset data = null;
		try {
			SDAPlotter.imagePlot(plotName, img);
			EclipseUtils.delay(1000);
			IPlottingSystem<Object> system = PlottingFactory.getPlottingSystem(plotName);
			IImageTrace trace = (IImageTrace)system.getTraces().iterator().next();
			data = trace.getData();
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return data;
		}
	}

	/**
	 * Loads images given a List of paths and returns them as a List of dataset
	 * @param names
	 * @param progressMonitorWrapper
	 * @param pngMode
	 * @return List<Dataset>
	 */
	public static List<IDataset> getImageDatasets(List<String> names, IMonitor progressMonitorWrapper) {
		List<IDataset> data = new ArrayList<IDataset>();
		try {
			ImageStackLoader loader = new ImageStackLoader(names, progressMonitorWrapper);
			ILazyDataset lazyStack = loader.createLazyDataset("Folder Stack");
			int[] shape = lazyStack.getShape();
			for (int i = 0; i < shape[0]; i++) {
				IDataset image = lazyStack.getSlice(new Slice(i, shape[0], shape[1])).squeeze();
				data.add(image);
			}
			return data;
		} catch (Exception e1) {
			System.out.println("Could not load image stack:" + e1.getMessage());
			return null;
		}
	}

}
