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
import org.dawnsci.conversion.converters.util.LocalServiceManager;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.image.IImageStitchingProcess;
import org.eclipse.dawnsci.analysis.api.image.IImageTransform;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.impl.Image;
import org.eclipse.dawnsci.analysis.dataset.impl.LazyDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.ImageStackLoader;
import uk.ac.diamond.scisoft.analysis.io.JavaImageSaver;

/**
 * Converts a directory of images to a stitched image
 * 
 * @author Baha El Kassaby
 * 
 */
public class ImagesToStitchedConverter extends AbstractImageConversion {

	private static final Logger logger = LoggerFactory.getLogger(ImagesToStitchedConverter.class);
	private List<IDataset> imageStack = new ArrayList<IDataset>();

	private static IImageStitchingProcess stitcher;
	private static IImageTransform transformer;

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
	
	/**
	 * OSGI Calls this
	 * @param s
	 */
	public static void setImageSticher(IImageStitchingProcess s) {
		stitcher = s;
	}

	/**
	 * OSGI Calls this
	 * @param s
	 */
	public static void setImageTransform(IImageTransform its) {
		transformer = its;
	}

	@Override
	protected void convert(IDataset slice) throws Exception {

		if (context.getMonitor() != null && context.getMonitor().isCancelled()) {
			throw new Exception(getClass().getSimpleName() + " is cancelled");
		}
		ConversionStitchedBean conversionBean = (ConversionStitchedBean)context.getUserObject();
		ILazyDataset lazy = context.getLazyDataset();
		// Rotate each image by angle degrees
		double angle = conversionBean.getAngle();
		IDataset rotated = transformer.rotate(slice, angle);
		// crop each image given an elliptical roi
		IROI roi = conversionBean.getRoi();
		if (roi != null) {
			IDataset cropped = Image.maxRectangleFromEllipticalImage(rotated, roi);
			imageStack.add(cropped);
		} else {
			imageStack.add(rotated);
		}
		int stackSize = lazy.getShape()[0];
		if (imageStack.size() == stackSize) {
			String outputPath = context.getOutputPath();
			int rows = conversionBean.getRows();
			int columns = conversionBean.getColumns();
			boolean useFeatureAssociation = conversionBean.isFeatureAssociated();
			boolean isInputDatFile = conversionBean.isInputDatFile();
			double fieldOfView = conversionBean.getFieldOfView();
			List<double[]> translations = conversionBean.getTranslations();
			// stitch the stack of images
			IDataset stitched = stitcher.stitch(imageStack, rows, columns, fieldOfView, translations, useFeatureAssociation, isInputDatFile);

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
		if (context.getMonitor() != null)
			context.getMonitor().worked(1);
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

	@Override
	public void close(IConversionContext context) {

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
	 * To be used as the user object to convey data about the stitched conversion.
	 *
	 */
	public static class ConversionStitchedBean extends ConversionInfoBean {
		private int rows = 3;
		private int columns = 3;
		private double angle = 49;
		private double fieldOfView = 50;
		private boolean featureAssociated;
		private boolean isInputDatFile = false;
		private List<double[]> translations;
		private IROI roi;

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
		public boolean isInputDatFile() {
			return isInputDatFile;
		}
		public void setInputDatFile(boolean isInputDatFile) {
			this.isInputDatFile = isInputDatFile;
		}
		public void setTranslations(List<double[]> translations) {
			this.translations = translations;
		}
		public List<double[]> getTranslations() {
			return translations;
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
			result = prime * result + (isInputDatFile ? 1231 : 1237);
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
			if (isInputDatFile != other.isInputDatFile)
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
