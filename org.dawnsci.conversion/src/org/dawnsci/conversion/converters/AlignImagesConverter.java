/*-
 * Copyright (c) 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.conversion.converters;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dawb.common.util.list.SortNatural;
import org.dawnsci.conversion.converters.util.LocalServiceManager;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.ImageStackLoader;
import uk.ac.diamond.scisoft.analysis.io.JavaImageSaver;

/**
 * Aligns a stack or directory of images
 * 
 * @author Baha El Kassaby
 * 
 */
public class AlignImagesConverter extends AbstractImageConversion {

	private static final Logger logger = LoggerFactory.getLogger(AlignImagesConverter.class);
	private List<IDataset> alignedImages;
	private int idx = 0;

	public AlignImagesConverter() {
		super(null);
	}
	
	public AlignImagesConverter(IConversionContext context) throws Exception {
		super(context);
		final File dir = new File(context.getOutputPath());
		dir.mkdirs();
		// We put the many files in one ILazyDataset and set that in the context
		// as an override.
		ILazyDataset set = getLazyDataset();
		context.setLazyDataset(set);
		context.addSliceDimension(0, "all");
	}

	@Override
	protected void convert(IDataset slice) throws Exception {
		if (context.getMonitor() != null && context.getMonitor().isCancelled()) {
			throw new Exception(getClass().getSimpleName() + " is cancelled");
		}
		ILazyDataset lazy = context.getLazyDataset();
		// create saving name
		List<String> dataNames = context.getDatasetNames();
		String outputPath = context.getOutputPath();
		File saveFile = new File(dataNames.get(idx));
		String originalFileName = saveFile.getName();
		if (originalFileName.contains("."))
			originalFileName = originalFileName.split("\\.")[0];
		outputPath += File.separator + "aligned_" + originalFileName;

		ConversionAlignBean conversionBean = (ConversionAlignBean) context.getUserObject();
		alignedImages = conversionBean.getAligned();
		saveImage(alignedImages.get(idx), outputPath);
		idx++;
		if (idx == lazy.getShape()[0])
			idx = 0;
		if (context.getMonitor() != null)
			context.getMonitor().worked(1);
	}

	private void saveImage(IDataset data, String outputPath) throws Exception {
		final File outputFile = new File(outputPath);

		if (!outputFile.getParentFile().exists())
			outputFile.getParentFile().mkdirs();

		// JavaImageSaver likes 33 but users don't
		int bits = getBits();
		if (bits == 32 && getExtension().toLowerCase().startsWith("tif"))
			bits = 33;

		final JavaImageSaver saver = new JavaImageSaver(
				outputFile.getAbsolutePath(), getExtension(), bits, true);
		final DataHolder dh = new DataHolder();
		dh.addDataset(data.getName(), data);
		dh.setFilePath(outputFile.getAbsolutePath());
		saver.saveFile(dh);
	}

	private ILazyDataset getLazyDataset() throws Exception {
		List<String> regexs = context.getFilePaths();
		final List<String> paths = new ArrayList<String>(Math.max(
				regexs.size(), 10));
		ILazyDataset lazyDataset = null;
		// if dat file: try to parse it and populate the list of all images paths 
		if (regexs.size() == 1 && regexs.get(0).endsWith(".dat")) {
			IDataHolder holder = LocalServiceManager.getLoaderService().getData(regexs.get(0),null);
			String[] names = holder.getNames();
			for (String name : names) {
				lazyDataset = holder.getLazyDataset(name);
				if (lazyDataset != null && lazyDataset.getRank() == 3) 
					return lazyDataset;
			}
		}
		for (String regex : regexs) {
			final List<File> files = expand(regex);
			for (File file : files) {
				try {
					ILazyDataset data = LocalServiceManager.getLoaderService().getData(
							file.getAbsolutePath(), context.getMonitor())
							.getLazyDataset(0);
					if (data.getRank() == 2)
						paths.add(file.getAbsolutePath());
				} catch (Exception ignored) {
					logger.debug("Exception ignored:"+ignored.getMessage());
					continue;
				}
			}
		}
		if (paths.size() > 0) {
			Collections.sort(paths, new SortNatural<String>(true));
			ImageStackLoader loader = new ImageStackLoader(paths,
					context.getMonitor());
			lazyDataset = loader.createLazyDataset("Folder Stack");
		} else {
			lazyDataset = LocalServiceManager.getLoaderService().getData(regexs.get(0),null).getLazyDataset(0);
		}
		
		return lazyDataset;
	}

	protected String getExtension() {
		if (context.getUserObject() == null)
			return "tif";
		return ((ConversionInfoBean) context.getUserObject()).getExtension();
	}

	private int getBits() {
		if (context.getUserObject() == null)
			return 33;
		return ((ConversionInfoBean) context.getUserObject()).getBits();
	}

	/**
	 * To be used as the user object to convey data about the align conversion.
	 *
	 */
	public static class ConversionAlignBean extends ConversionInfoBean {
		private List<IDataset> aligned;

		public void setAligned(List<IDataset> aligned) {
			this.aligned = aligned;
		}
		public List<IDataset> getAligned() {
			return aligned;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result
					+ ((aligned == null) ? 0 : aligned.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			ConversionAlignBean other = (ConversionAlignBean) obj;
			if (aligned == null) {
				if (other.aligned != null)
					return false;
			} else if (!aligned.equals(other.aligned))
				return false;
			return true;
		}
	}
}
