/*
 * Copyright (c) 2012, 2017 Diamond Light Source Ltd.
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
import org.dawnsci.conversion.ServiceLoader;
import org.dawnsci.conversion.converters.util.LocalServiceManager;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.tree.impl.AttributeImpl;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.LazyDataset;

import uk.ac.diamond.scisoft.analysis.io.ImageStackLoader;

/**
 * This converter converts a directory of images to a stack in HDF5
 * which allows slicing tools to be run in a faster way.
 * 
 * @author Matthew Gerring
 *
 */
public class ImagesToHDFConverter extends AbstractConversion{

	private NexusFile hFile;
	private String name;
	private GroupNode group;

	public ImagesToHDFConverter(IConversionContext context) throws Exception {
		super(context);

		// We open the file here, and create the group.
		hFile   = ServiceLoader.getNexusFileFactory().newNexusFile(context.getOutputPath());
		hFile.createAndOpenToWrite();
		// We make the group
		final String datasetNameStr = context.getDatasetNames().get(0);
		String[]  paths = datasetNameStr.split(Node.SEPARATOR);
		if ("".equals(paths[0]))
			paths = Arrays.copyOfRange(paths, 1, paths.length);
		final String entry = Tree.ROOT + paths[0];
		group = hFile.getGroup(entry, true);
		hFile.addAttribute(group, new AttributeImpl(NexusConstants.NXCLASS, NexusConstants.ENTRY));

		if (paths.length>2) {
			String path = "";
			for (int i = 1; i < paths.length-1; i++) {
				path = path + Node.SEPARATOR + paths[i];
				group = hFile.getGroup(path, true);
				if (i<(paths.length-2))
					hFile.addAttribute(group, new AttributeImpl(NexusConstants.NXCLASS, NexusConstants.ENTRY));
			}
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
		LazyDataset lazyDataset = new LazyDataset("Folder Stack", loader.getDType(), loader.getShape(), loader);
		return lazyDataset;

	}

	private boolean first = true;

	@Override
	protected void convert(IDataset slice) throws Exception {
		final String datasetPath = context.getDatasetNames().get(0);
		Dataset data = DatasetUtils.convertToDataset(slice);
		data.setName(name);
		DataNode d = NexusUtils.appendData(hFile, group, data);
		if (first) {
			hFile.addAttribute(d, new AttributeImpl("original_name", datasetPath));
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
