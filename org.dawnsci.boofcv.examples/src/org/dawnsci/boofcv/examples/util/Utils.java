/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.boofcv.examples.util;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.ui.IWorkbenchPage;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.io.AbstractFileLoader;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

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

	/**
	 * Loads images given a List of paths and returns them as a List of dataset
	 * @param names
	 * @param progressMonitorWrapper
	 * @param pngMode
	 * @return List<Dataset>
	 */
	public static List<IDataset> getImageDatasets(List<String> names, IMonitor progressMonitorWrapper) {
		List<IDataset> data = new ArrayList<IDataset>();
		Class<? extends AbstractFileLoader> loaderClass = LoaderFactory.getLoaderClass("tiff");
		try {
			Constructor<?> constructor = loaderClass.getConstructor(String.class);
			for (String n : names) {
				IDataset a;
				try {
					AbstractFileLoader l = (AbstractFileLoader) constructor.newInstance(n);
					a = l.loadFile(progressMonitorWrapper).getDataset(0);
					if (n.contains("/")) {
						String name = n.substring(n.lastIndexOf("/") + 1);
						a.setName(name);
					} else {
						a.setName(n);
					}
					data.add(a);
					System.out.println("Loaded :" + n);
				} catch (Exception e) {
					System.out.println("Could not load :" + n);
				}
			}
		} catch (NoSuchMethodException e) {
			System.out.println("Could not find constructor for loader");
		}
		return data.size() > 0 ? data : null;
	}

	/**
	 * Returns a string array of file names in a path directory, with or without
	 * the full path
	 * 
	 * @param path
	 * @param withFullPath
	 * @return string array
	 */
	public static String[] getFileNames(String path, boolean withFullPath) {
		File dir = new File(path);
		String[] children = dir.list();
		if (children == null) {
			return null;
		}
		// We filter any files that start with '.' or directory
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				File f = new File(dir.getAbsolutePath()+"/"+name);
				if (f.isDirectory())
					return false;
				return !name.startsWith(".");
			}
		};
		children = dir.list(filter);
		Arrays.sort(children);
		if (withFullPath) {
			for (int i = 0; i < children.length; i++) {
				children[i] = path + "/" + children[i];
			}
		}
		return children;
	}

	/**
	 * Put a String[] in a List of String
	 * 
	 * @param array
	 * @param list
	 */
	public static void getArrayAsList(String[] array, List<String> list) {
		list.clear();
		for (int i = 0; i < array.length; i++) {
			list.add(array[i]);
		}
	}
}
