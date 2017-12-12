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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawnsci.conversion.ServiceLoader;
import org.dawnsci.conversion.converters.util.LocalServiceManager;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.tree.impl.AttributeImpl;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.january.dataset.AggregateDataset;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.NexusTreeUtils;

public class Convert1DtoND extends AbstractConversion {
	
	private final static Logger logger = LoggerFactory.getLogger(Convert1DtoND.class);
	
	Map<String, List<ILazyDataset>> dataMap = new HashMap<String, List<ILazyDataset>>();

	public Convert1DtoND(IConversionContext context) throws Exception {
		super(context);
		
		File file = new File(context.getOutputPath());
		
		//overwrite if exists
		if (file.exists()) {
			file.delete();
		} else {
			file.getParentFile().mkdirs();
		}
		
		//test file can be opened
		NexusFile fileH = ServiceLoader.getNexusFileFactory().newNexusFile(context.getOutputPath());
		fileH.createAndOpenToWrite();
		fileH.close();
		
	}

	@Override
	protected void convert(IDataset slice) throws Exception {
		if (!dataMap.containsKey(slice.getName()))
			dataMap.put(slice.getName(), new ArrayList<ILazyDataset>());
		dataMap.get(slice.getName()).add(slice);

	}
	
	@Override
	public void close(IConversionContext context) throws Exception {

		try (NexusFile file = ServiceLoader.getNexusFileFactory().newNexusFile(context.getOutputPath())) {
			file.openToWrite(true);
			IDataset axis = null;
			int axisLength = -1;
			String axisName = context.getAxisDatasetName();
			
			if (axisName != null) {
				axis = LocalServiceManager.getLoaderService().getDataset(context.getFilePaths().get(0),axisName,null);
				axisLength = axis.getShape()[0];
				
			}

			for (String key : dataMap.keySet()) {
				List<ILazyDataset> out = dataMap.get(key);
				String[] paths = getNexusPathAndNameFromKey(key);

				String entry = Tree.ROOT + paths[0];
				GroupNode groupNode = file.getGroup(entry, true);
				file.addAttribute(groupNode, new AttributeImpl(NexusFile.NXCLASS, "NXentry"));

				if (paths.length>2) {
					String path = entry;
					for (int i = 1; i < paths.length-1; i++) {
						path += Node.SEPARATOR + paths[i];
						groupNode = file.getGroup(path, true);
						if (i<(paths.length-1))
							file.addAttribute(groupNode, new AttributeImpl(NexusFile.NXCLASS, "NXentry"));
					}
				}
				
				if (context.getUserObject() == null ||
					!(context.getUserObject() instanceof Convert1DInfoBean)) {
					saveTo2DStack(file, groupNode, out, paths, key,axisLength);
				} else {
					Convert1DInfoBean bean = (Convert1DInfoBean)context.getUserObject();
					if (bean.fastAxis*bean.slowAxis != out.size()) {
						saveTo2DStack(file, groupNode, out, paths, key, axisLength);
					} else {
						saveTo3DStack(file, groupNode, out, paths, key, bean,axisLength);
					}
				}
			}
			if (axis != null) {
				String[] paths = getNexusPathAndNameFromKey(axisName);
				String entry = Tree.ROOT + paths[0];
				GroupNode groupNode = file.getGroup(entry, true);
				file.addAttribute(groupNode, new AttributeImpl(NexusFile.NXCLASS, "NXentry"));
				
				if (paths.length>2) {
					String path = entry;
					for (int i = 1; i < paths.length-1; i++) {
						path += Node.SEPARATOR + paths[i];
						groupNode = file.getGroup(path, true);
						if (i<(paths.length-1))
							file.addAttribute(groupNode, new AttributeImpl(NexusFile.NXCLASS, "NXentry"));
					}
				}
				saveAxis(file, groupNode, axis, paths);
			}
			file.close();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		super.close(context);
	}
	
	private void saveAxis(NexusFile file, GroupNode group, IDataset out, String[] paths) throws Exception {
		
		String name = paths[paths.length-1];
		
		out.setName(name);
		DataNode dNode = file.createData(group, out);
		
		
		file.addAttribute(dNode, new AttributeImpl(NexusTreeUtils.NX_AXIS, "1"));

	}
	
	private void saveTo2DStack(NexusFile file, GroupNode group, List<ILazyDataset> out,String[] paths,String key, int axisLength) throws Exception{
		
		String name = paths[paths.length-1];
		
		Dataset first = DatasetUtils.convertToDataset(out.get(0).getSlice());
		first.setName(name);
		DataNode dNode = NexusUtils.appendData(file, group, first);
		
		if (first.getShape()[0] == axisLength)
			file.addAttribute(dNode, new AttributeImpl(NexusTreeUtils.NX_SIGNAL, "1"));
		file.addAttribute(dNode, new AttributeImpl("original_name", key));
		
		for (int i = 1; i < out.size(); i++) {
			Dataset a = DatasetUtils.convertToDataset(out.get(i).getSlice());
			a.setName(name);
			dNode = NexusUtils.appendData(file, group, a);
		}
	}
	
	private void saveTo3DStack(NexusFile file, GroupNode group, List<ILazyDataset> out,String[] paths,String key, Convert1DInfoBean bean,int axisLength) throws Exception{
		
		ILazyDataset[] lz = new ILazyDataset[bean.fastAxis];
		String name = paths[paths.length-1];
		
		IDataset first = out.get(0).getSlice();
		DataNode dataNode = null;
		for (int i = 0; i < bean.slowAxis; i++) {
			
			for (int j = 0; j < bean.fastAxis; j++) {
				lz[j] = out.get(i*bean.fastAxis + j);
			}
			
			ILazyDataset ds = new AggregateDataset(true, lz);
			Dataset a = DatasetUtils.convertToDataset(ds.getSlice());
			a.setName(name);
			dataNode = NexusUtils.appendData(file, group, a);
		}
		if (dataNode != null)
			file.addAttribute(dataNode, new AttributeImpl("original_name", key));

		if (first.getShape()[0] == axisLength && dataNode != null)
			file.addAttribute(dataNode, new AttributeImpl(NexusTreeUtils.NX_SIGNAL, "1"));
	}
	
	private String[] getNexusPathAndNameFromKey(String key) {
		String[]  paths = key.split(Node.SEPARATOR);
		
		if (paths.length == 1) {
			return new String[] {"entry1", paths[0]};
		}
		
		if ("".equals(paths[0])) paths = Arrays.copyOfRange(paths, 1, paths.length);
		return paths;
	}
	
	public static final class Convert1DInfoBean {
	 public int fastAxis = 0;
	 public int slowAxis = 0;
	}

}
