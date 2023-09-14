/*
 * Copyright (c) 2012, 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.conversion.converters;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IndexIterator;
import org.eclipse.january.dataset.StringDataset;

import uk.ac.diamond.osgi.services.ServiceProvider;

/**
 * This converter creates stacks from 
 * 
 * @author Matthew Gerring
 *
 */
public class CompareConverter extends AbstractConversion{

	private NexusFile hFile;
	private Map<String,String>    groups;
	private Map<String,Boolean>   written;

	public CompareConverter(IConversionContext context) throws Exception {
		super(context);

		// We open the file here, and create the group.
		hFile   = ServiceProvider.getService(INexusFileFactory.class).newNexusFile(context.getOutputPath());
		hFile.createAndOpenToWrite();
		// For each dataset name we make a branch in the conversion file,
		// to store the data.
		final List<String> names = context.getDatasetNames();

		groups  = new HashMap<String, String>(names.size());
		written = new HashMap<String, Boolean>(names.size());
		String group = null;
		for (String datasetNameStr : names) {
			
			String[]  paths = datasetNameStr.split(Node.SEPARATOR);
			if ("".equals(paths[0])) paths = Arrays.copyOfRange(paths, 1, paths.length);
			
			group = Tree.ROOT + paths[0];
			GroupNode groupNode = hFile.getGroup(group, true);
			hFile.addAttribute(groupNode, TreeFactory.createAttribute(NexusConstants.NXCLASS, NexusConstants.ENTRY));
			String path = group;
			if (paths.length>2) {
				for (int i = 1; i < paths.length-1; i++) {
					path = path + Node.SEPARATOR + paths[i];
					groupNode = hFile.getGroup(path, true);
					if (i<(paths.length-2))
						hFile.addAttribute(groupNode, TreeFactory.createAttribute(NexusConstants.NXCLASS, NexusConstants.ENTRY));
				}
				try {
					groupNode = hFile.getGroup(path, true);
					hFile.addAttribute(groupNode, TreeFactory.createAttribute(NexusConstants.NXCLASS, NexusConstants.DATA));
				} catch (Exception ignored) {
					continue;
				}
			}
			groups.put(datasetNameStr, path);
			written.put(datasetNameStr, false);
		}
	}


	private Map<String, int[]> requiredShapes;
	
	@Override
	protected void convert(IDataset slice) throws Exception {
		final String datasetPath = slice.getName(); // Slice must be named the same as the path it will write to

		final String name = datasetPath.substring(datasetPath.lastIndexOf(Node.SEPARATOR) + 1);
		final String group = groups.get(datasetPath);

		Dataset abs = DatasetUtils.convertToDataset(slice).squeeze();

		// Each dataset must come through as the same shape, even if it is not.
		// We keep the requiredShapes and force the current dataset to be the same
		// as previous shapes.
		int[] requiredShape = getRequiredShape(datasetPath, abs.getShape());
		abs = resize(abs, requiredShape);
		abs.setName(name);
		GroupNode groupNode = hFile.getGroup(group, true);
		DataNode dNode = NexusUtils.appendData(hFile, groupNode, abs);
		if (!written.get(datasetPath)) {
			hFile.addAttribute(dNode, TreeFactory.createAttribute("original_name", datasetPath));
			written.put(datasetPath, true);
		}
		if (context.getMonitor() != null && context.getMonitor().isCancelled()) {
			hFile.close();
			throw new Exception("Conversion is cancelled!");
		}
	}

	public static Dataset resize(final Dataset a, final int... shape) {

		if (a instanceof StringDataset)
			return a;
		int size = a.getSize();
		Dataset rdata = DatasetFactory.zeros(a);
		IndexIterator it = rdata.getIterator();
		while (it.hasNext()) {
			rdata.setObjectAbs(it.index, it.index < size ? a.getObjectAbs(it.index) : Double.NaN);
		}

		return rdata;
	}
	
	private int[] getRequiredShape(String path, int[] defaultShape) {
		if (requiredShapes == null)
			requiredShapes = new HashMap<String, int[]>();
		int[] shape = context.getUserObject() != null
				? ((ConversionInfoBean) context.getUserObject()).getRequiredShape(path)
				: null;
		if (shape == null)
			shape = requiredShapes.get(path);
		if (shape == null) {
			shape = defaultShape;
			requiredShapes.put(path, defaultShape);
		}
		return shape;
	}

	public void close(IConversionContext context) throws Exception {
		hFile.close();
		super.close(context);
	}

	/**
	 * To be used as the user object to convey data about the conversion.
	 * 
	 * @author Matthew Gerring
	 *
	 */
	public static final class ConversionInfoBean {

		private Map<String, int[]> requiredShapes = new HashMap<String, int[]>();

		private int[] getRequiredShape(String path) {
			if (requiredShapes == null)
				return null;
			return requiredShapes.get(path);
		}

		private void addRequiredShape(String path, int[] shape) {
			requiredShapes.put(path, shape);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((requiredShapes == null) ? 0 : requiredShapes.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ConversionInfoBean other = (ConversionInfoBean) obj;
			if (requiredShapes == null) {
				if (other.requiredShapes != null)
					return false;
			} else if (!requiredShapes.equals(other.requiredShapes))
				return false;
			return true;
		}

	}
}
