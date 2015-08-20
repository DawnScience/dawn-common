/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.conversion.converters;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.dawb.common.util.list.SortNatural;
import org.dawnsci.conversion.converters.util.LocalServiceManager;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.dataset.impl.LazyDataset;
import org.eclipse.dawnsci.hdf5.HierarchicalDataFactory;
import org.eclipse.dawnsci.hdf5.IHierarchicalDataFile;
import org.eclipse.dawnsci.hdf5.Nexus;

import uk.ac.diamond.scisoft.analysis.io.ImageStackLoader;

/**
 * This converter converts a directory of images to a stack in HDF5
 * which allows slicing tools to be run in a faster way.
 * 
 * @author Matthew Gerring
 *
 */
public class ImagesToHDFConverter extends AbstractConversion{
	

	private IHierarchicalDataFile hFile;
	private String                group;
	private String                name;

	public ImagesToHDFConverter(IConversionContext context) throws Exception {
		
		super(context);
		
		// We open the file here, and create the group.
		hFile   = HierarchicalDataFactory.getWriter(context.getOutputPath());

		// We make the group
		final String datasetNameStr = context.getDatasetNames().get(0);
		String[]  paths = datasetNameStr.split("/");
		if ("".equals(paths[0])) paths = Arrays.copyOfRange(paths, 1, paths.length);
 		final String entry = hFile.group(paths[0]);
 		hFile.setNexusAttribute(entry, Nexus.ENTRY);

		group = entry;
		if (paths.length>2) {
			for (int i = 1; i < paths.length-1; i++) {
				final String path = paths[i];
				group = hFile.group(path, group);
				if (i<(paths.length-2)) hFile.setNexusAttribute(group, Nexus.ENTRY);
			}
			hFile.setNexusAttribute(group, Nexus.DATA);
		}
		name = paths[paths.length-1];

		// We put the many files in one ILazyDataset and set that in the context as an override.
		ILazyDataset set = getLazyDataset();
		context.setLazyDataset(set);
		
		context.addSliceDimension(0, "all");
		
	}

	private ILazyDataset getLazyDataset() throws Exception {

		final List<String> regexs = context.getFilePaths();
		final List<String> paths = new ArrayList<String>(Math.max(regexs.size(),10));
		for (String regex : regexs) {
			final List<File>   files = expand(regex);
			for (File file : files) {
				try {
					ILazyDataset data = LocalServiceManager.getLoaderService().getData(file.getAbsolutePath(), context.getMonitor()).getLazyDataset(0);
					if (data.getRank()==2) paths.add(file.getAbsolutePath());
				} catch (Exception ignored) {
					continue;
				}
				
				if (context.getMonitor()!=null && context.getMonitor().isCancelled()) {
					hFile.close();
					throw new Exception("Conversion is cancelled!");
				}
			}
		}
		
		Collections.sort(paths, new SortNatural<String>(true));
		ImageStackLoader loader = new ImageStackLoader(paths, context.getMonitor());
		LazyDataset lazyDataset = new LazyDataset("Folder Stack", loader.getDtype(), loader.getShape(), loader);
		return lazyDataset;

	}

    private boolean first = true;
	@Override
	protected void convert(IDataset slice) throws Exception {
		final String datasetPath = context.getDatasetNames().get(0);
		
		String d = hFile.appendDataset(name, slice, group);
		if (first) {
			hFile.setNexusAttribute(d, Nexus.SDS);
			hFile.setAttribute(d, "original_name", datasetPath);
			first = false;
		}
		
		IMonitor mon = context.getMonitor();
		if (mon != null) {
			if (mon.isCancelled()) {
				hFile.close();
				throw new Exception("Conversion is cancelled!");
			} else {
				mon.worked(1);
			}
		}
	}
	
	
	public void close(IConversionContext context) throws Exception{
		hFile.close();
		super.close(context);
	}

}
