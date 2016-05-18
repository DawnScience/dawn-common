/*-
 * Copyright (c) 2011, 2015 Diamond Light Source Ltd.
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
import org.dawnsci.conversion.ServiceLoader;
import org.dawnsci.conversion.converters.util.LocalServiceManager;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.impl.AbstractDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Image;
import org.eclipse.dawnsci.analysis.dataset.impl.LazyDataset;
import org.eclipse.dawnsci.hdf5.HDF5Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.ImageStackLoader;
import uk.ac.diamond.scisoft.analysis.io.JavaImageSaver;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.utils.FileUtils;

/**
 * Converts a directory of images to a stitched image
 * 
 * @author Baha El Kassaby
 * 
 */
public class ImagesToStitchedConverter extends AbstractImageConversion {

	private static final Logger logger = LoggerFactory.getLogger(ImagesToStitchedConverter.class);
	private ILazyWriteableDataset lazyfile;

	public ImagesToStitchedConverter() {
		super(null);
	}
	
	public ImagesToStitchedConverter(IConversionContext context) throws Exception {
		super(context);
		final File dir = new File(context.getOutputPath());
		dir.mkdirs();
		// We put the many files in one ILazyDataset and set that in the context
		// as an override.
		ILazyDataset set = getLazyDataset();
		context.setLazyDataset(set);
		context.addSliceDimension(0, "all");
	}

	private int idx = 0;

	@Override
	protected void convert(IDataset slice) throws Exception {
		if (context.getMonitor() != null && context.getMonitor().isCancelled()) {
			throw new Exception(getClass().getSimpleName() + " is cancelled");
		}
		ConversionStitchedBean conversionBean = (ConversionStitchedBean)context.getUserObject();
		ILazyDataset lazy = context.getLazyDataset();
		// Rotate each image by angle degrees
		double angle = conversionBean.getAngle();
		IDataset rotatedSlice = ServiceLoader.getImageTransform().rotate(slice, angle);

		if (lazyfile == null)
			lazyfile = createTempLazyFile(lazy.getShape(), "stitchedconverter.h5");
		
		// crop each image given an elliptical roi
		IROI roi = conversionBean.getRoi();
		if (roi != null) {
			IDataset cropped = Image.maxRectangleFromEllipticalImage(rotatedSlice, roi);
			//save to temp file
			appendDataset(lazyfile, cropped, idx, context.getMonitor());
		} else {
			//save to temp file
			appendDataset(lazyfile,rotatedSlice, idx, context.getMonitor());
		}
		
		int stackSize = lazy.getShape()[0];
		if (idx == stackSize-1) {
			
			String outputPath = context.getOutputPath();
			int rows = conversionBean.getRows();
			int columns = conversionBean.getColumns();
			boolean useFeatureAssociation = conversionBean.isFeatureAssociated();
			double fieldOfView = conversionBean.getFieldOfView();
			double[][][] translationsArray = conversionBean.getTranslationsArray();
			
			//read from temp file
			ILazyDataset rotatedImages = getTempLazyData("stitchedconverter.h5", context.getMonitor());

			// stitch the stack of images
			IDataset stitched = ServiceLoader.getImageStitcher().stitch(rotatedImages, rows, columns, fieldOfView, translationsArray, useFeatureAssociation, lazy.getShape(), context.getMonitor());

			stitched.setName("stitched");
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
			dh.addDataset(stitched.getName(), stitched);
			dh.setFilePath(outputFile.getAbsolutePath());
			saver.saveFile(dh);
		}
		idx++;
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
		Collections.sort(paths, new SortNatural<String>(true));
		ImageStackLoader loader = new ImageStackLoader(paths,
				context.getMonitor());
		lazyDataset = new LazyDataset("Folder Stack",
				loader.getDtype(), loader.getShape(), loader);
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
	 * Method that reads a temporary hdf5 file on disk
	 * 
	 * @param name
	 * @return lazydataset
	 * @throws Exception 
	 */
	private ILazyDataset getTempLazyData(String name, IMonitor monitor) throws Exception {
		String nodepath = "/entry/data/";
		String file = FileUtils.getTempFilePath(name);
		IDataHolder holder = LoaderFactory.getData(file, false, true, monitor);
		ILazyDataset shifted = holder.getLazyDataset(nodepath + name);
		return shifted;
	}

	/**
	 * Method that creates an hdf5 file in the temp directory of the OS
	 * 
	 * @param name
	 * @return lazy writable dataset on disk
	 */
	private ILazyWriteableDataset createTempLazyFile(int[] newShape, String name) {
		// save on a temp file
		String nodepath = "/entry/data/";
		String file = FileUtils.getTempFilePath(name);
		File tmpFile = new File(file);
		if (tmpFile.exists())
			tmpFile.delete();
		return HDF5Utils.createLazyDataset(file, nodepath, name, newShape, null, newShape, AbstractDataset.FLOAT32, null, false);
	}

	/**
	 * Method that appends a dataset to an existing lazy writable dataset
	 * 
	 * @param lazy
	 * @param data
	 * @param idx
	 * @param monitor
	 * @throws Exception
	 */
	private void appendDataset(ILazyWriteableDataset lazy, IDataset data, int idx, IMonitor monitor) throws Exception {
		SliceND ndSlice = new SliceND(lazy.getShape(), new int[] { idx, 0, 0 },
				new int[] { (idx + 1), data.getShape()[0], data.getShape()[1] }, null);
		lazy.setSlice(monitor, data, ndSlice);
	}

	/**
	 * To be used as the user object to convey data about the stitched conversion.
	 *
	 */
	public static class ConversionStitchedBean extends ConversionInfoBean {
		private int rows = 3;
		private int columns = 3;
		private double angle = 49;
		private double fieldOfView = 50;
		private boolean featureAssociated;
		private List<double[]> translations;
		private IROI roi;
		private double[][][] translArray;

		public int getRows() {
			return rows;
		}
		public void setRows(int rows) {
			this.rows = rows;
		}
		public int getColumns() {
			return columns;
		}
		public void setColumns(int columns) {
			this.columns = columns;
		}
		public double getAngle() {
			return angle;
		}
		public void setAngle(double angle) {
			this.angle = angle;
		}
		public void setRoi(IROI roi) {
			this.roi = roi;
		}
		public IROI getRoi() {
			return roi;
		}
		public void setFieldOfView(double fieldOfView) {
			this.fieldOfView = fieldOfView;
		}
		public double getFieldOfView() {
			return fieldOfView;
		}
		public boolean isFeatureAssociated() {
			return featureAssociated;
		}
		public void setFeatureAssociated(boolean featureAssociated) {
			this.featureAssociated = featureAssociated;
		}
		public void setTranslations(List<double[]> translations) {
			this.translations = translations;
		}
		public void setTranslations(double[][][] translations) {
			this.translArray = translations;
		}
		public List<double[]> getTranslations() {
			return translations;
		}
		public double[][][] getTranslationsArray() {
			return translArray;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			long temp;
			temp = Double.doubleToLongBits(angle);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result + columns;
			result = prime * result + (featureAssociated ? 1231 : 1237);
			temp = Double.doubleToLongBits(fieldOfView);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result
					+ ((roi == null) ? 0 : roi.hashCode());
			result = prime * result + rows;
			result = prime * result
					+ ((translations == null) ? 0 : translations.hashCode());
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
			ConversionStitchedBean other = (ConversionStitchedBean) obj;
			if (Double.doubleToLongBits(angle) != Double
					.doubleToLongBits(other.angle))
				return false;
			if (columns != other.columns)
				return false;
			if (featureAssociated != other.featureAssociated)
				return false;
			if (Double.doubleToLongBits(fieldOfView) != Double
					.doubleToLongBits(other.fieldOfView))
				return false;
			if (roi == null) {
				if (other.roi != null)
					return false;
			} else if (!roi.equals(other.roi))
				return false;
			if (rows != other.rows)
				return false;
			if (translations == null) {
				if (other.translations != null)
					return false;
			} else if (!translations.equals(other.translations))
				return false;
			return true;
		}
	}
}
