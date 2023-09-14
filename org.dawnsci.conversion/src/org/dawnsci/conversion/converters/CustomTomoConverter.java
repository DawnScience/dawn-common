/*
 * Copyright (c) 2012-2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.conversion.converters;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.IFindInTree;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.api.tree.TreeUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Slice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.osgi.services.ServiceProvider;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.JavaImageSaver;

/**
 * Custom converter for tomography data.
 * 
 * Manages the conversion of tomography nexus files containing NXtomo into image files
 * 
 * @author Baha El Kassaby - Removal of IHierchicalDataFile and HObject usage
 */
public class CustomTomoConverter extends AbstractConversion {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(CustomTomoConverter.class);

	private static final String DEF = "definition";
	private static final String NXTOMO = "nxtomo";
	private static final String DATA_LOCATION = "instrument/detector/data";
	private static final String KEY_LOCATION = "instrument/detector/image_key";
	private int counter;
	private int nImages;
	
	public CustomTomoConverter(IConversionContext context) {
		super(context);
	}

	@Override
	protected ILazyDataset getLazyDataset(final File                   path, 
							              final String               dsPath,
							              final IConversionContext   context) throws Exception {
		
		//Overriding getLazyDataset allows us to process the tomography bean before calling the super
		if (context.getUserObject() != null && context.getUserObject() instanceof TomoInfoBean) {
			processTomoInfoBeanContext(path, context);
		} else {
			throw new IllegalArgumentException("Not a recognised tomography file");
		}
		counter = 0;
		nImages = ((TomoInfoBean)context.getUserObject()).getNumberOfImages();
		return super.getLazyDataset(path, dsPath, context);	
	}

	@Override
	protected void convert(IDataset slice) throws Exception {
		if (context.getMonitor()!=null && context.getMonitor().isCancelled()) {
			throw new Exception(getClass().getSimpleName()+" is cancelled");
		}

		String filename = ((TomoInfoBean)context.getUserObject()).getNextFileName();
		int nBits = ((TomoInfoBean)context.getUserObject()).getBits();

		File file = new File(filename);
		file.getParentFile().mkdirs();

		final JavaImageSaver saver = new JavaImageSaver(filename, "tiff", nBits, true);
		final DataHolder     dh    = new DataHolder();
		dh.addDataset(slice.getName(), slice);
		saver.saveFile(dh);

		if (nImages < 101 || counter%(nImages/100) == 0) {
			if (context.getMonitor()!=null) context.getMonitor().worked((100)/(nImages));
		}

		counter++;
	}

	private void processTomoInfoBeanContext(File path, IConversionContext context) throws Exception {
		if (findGroupContainingDefinition(path.getAbsolutePath()) == null) {
			throw new IllegalArgumentException("Not a recognised tomography file");
		}
		
		TomoInfoBean bean = (TomoInfoBean)context.getUserObject();

		if( context.getOutputPath() != null) bean.setOutputPath(context.getOutputPath() + File.separator + getFileNameNoExtension(path));

		IDataset key = getImageKey(bean, path);
		if (key != null) bean.setImageKey(key);
		else throw new IllegalArgumentException("Tomography file does not contain image key");
		
		counter = 0;
		bean.resetCounters();
	}

	private IDataset getImageKey(TomoInfoBean bean, File path) {
		try {
			IDataHolder dh = ServiceProvider.getService(ILoaderService.class).getData(path.getAbsolutePath(), null);
			String nodepath = bean.tomoPath + KEY_LOCATION;
			//path should start with /
			nodepath = !nodepath.startsWith("/") ? "/" + nodepath : nodepath;
			NodeLink link = dh.getTree().findNodeLink(nodepath);
			DataNode datanode = (DataNode) link.getDestination();
			ILazyDataset lazydata = datanode.getDataset();
			return lazydata.getSlice(new Slice(0, lazydata.getShape()[0], 1)).squeeze();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String findGroupContainingDefinition(String path) throws Exception {
		IDataHolder dh = ServiceProvider.getService(ILoaderService.class).getData(path, null);
		try {
			IFindInTree treefinder = new IFindInTree() {
				@Override
				public boolean found(NodeLink node) {
					if (node.getDestination() instanceof DataNode dataNode) {
						ILazyDataset dataset = dataNode.getDataset();
						String name = dataset.getName();
						if (DEF.equalsIgnoreCase(name)) {
							String value = dataNode.getString();
							if (value != null && value.toLowerCase().contains(NXTOMO))
								return true;
						}
					}
					return false;
				}
				
			};
			Tree tree = dh.getTree();
			GroupNode root = tree.getGroupNode();
			Map<String, NodeLink> outmap = TreeUtils.treeBreadthFirstSearch(root, treefinder, true, null);
			
			if (!outmap.isEmpty()) {
				final String detectorPath = outmap.keySet().iterator().next();
				int idx = detectorPath.lastIndexOf("/");
				return detectorPath.substring(0, idx);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Bean to handle the custom aspects of converting tomography data
	 *
	 */
	public static final class TomoInfoBean {
		private IDataset imageKey;
		//paths contain %s for path to full fill (-minus ext)
		//also contain %xd for number position and width
		private String darkPath, flatPath, projectionPath;
		private String outputPath,tomoPath,filePath;
		private String extension = "tiff";
		private int dark,flat,projection = 0;
		private int nBits = 8;
		
		/**
		 * Method to produce a full file path when given the path and
		 * a filename containing %s and %0xd where x is integer
		 */
		public static String convertToFullPath(String path, String filename) {
			
			String output = createFullPath(path,filename,123);
			
			return output;
		}
		
		/**
		 * Method to check is a valid tomography file (returns true)
		 * and populate some internal data
		 * @throws Exception 
		 *
		 */
		public boolean setTomographyDefinition(String path) throws Exception {
			
			String ob = findGroupContainingDefinition(path);
			
			if (ob == null) return false;
			filePath = path;
			tomoPath = ob;
			// tomo Path should end with /
			if (!tomoPath.endsWith("/")) tomoPath = tomoPath+"/";
			if (!tomoPath.startsWith("/")) tomoPath = "/" + tomoPath;
			return true;
			
		}
		
		public void resetCounters() {
			dark = 0;
			flat = 0;
			projection = 0;
		}
		
		public int getNumberOfImages() {
			
			if (imageKey != null) {
				return imageKey.getShape()[0];
			}
			return -1;
		}
		
		public void setImageKey(IDataset imageKey) {
			this.imageKey = imageKey;
		}
		
		public IDataset getImageKey() {
			return imageKey;
		}
		
		public void setOutputPath(String path){
			this.outputPath = path;
		}
		
		public String getOutputPath(){
			return this.outputPath;
		}
		
		public String getTomoDataName() {
			if (tomoPath == null) return null;
			return tomoPath + DATA_LOCATION;
		}
		
		public void setDarkFieldPath(String path){
			this.darkPath = path;
		}
		
		public void setFlatFieldPath(String path) {
			this.flatPath = path;
		}
		
		public void setProjectionPath(String path) {
			this.projectionPath = path;
		}
		
		public String getNextFileName() {
			int sum = dark+flat+projection;
			
			switch (imageKey.getInt(sum)) {
			case 0:
				return getNextProjectionName();
			case 1:
				return getNextFlatFieldName();
			case 2:
				return getNextDarkFieldName();
			}
			
			return null;
		}
		
		private String getNextFlatFieldName() {
			String path = buildPath(flatPath,flat);
			flat++;
			return path;
		}
		
		private String getNextDarkFieldName() {
			String path = buildPath(darkPath,dark);
			dark++;
			return path;
		}
		
		private String getNextProjectionName() {
			String path = buildPath(projectionPath,projection);
			projection++;
			return path;
		}
		
		private String buildPath(String path, int number) {
			
			if (outputPath == null) {
				int index = filePath.lastIndexOf(".");
				outputPath = filePath.substring(0, index);
			}
			
			return createFullPath(outputPath,path, number);
		}
		
		private static String createFullPath(String path, String name, int number) {
			String output = name.replace("%s", path);
			
			Pattern p = Pattern.compile("%0\\d+d");
			Matcher m = p.matcher(name);
			
			while (m.find()) {
				String sub = name.substring(m.start(), m.end());
				String result = String.format(sub, number);
				output = output.replace(sub, result);
			}
			
			return output;
		}
		

		public void setExtension(String imageFormat) {
			extension = imageFormat;
			
		}
		
		public String getExtension() {
			return extension;
			
		}

		public void setBits(int bitDepth) {
			nBits = bitDepth;
			
		}
		
		public int getBits() {
			return nBits;
			
		}
	}
}
