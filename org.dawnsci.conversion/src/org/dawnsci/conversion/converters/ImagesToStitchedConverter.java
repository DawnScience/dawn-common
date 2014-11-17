/*-
 * Copyright (c) 2014 Diamond Light Source Ltd.
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

import org.dawb.common.services.ServiceManager;
import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.util.list.SortNatural;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.image.IImageStitchingProcess;
import org.eclipse.dawnsci.analysis.dataset.impl.LazyDataset;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.ImageStackLoader;
import uk.ac.diamond.scisoft.analysis.io.JavaImageSaver;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

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

	private void createImageStitcher() {
		if (stitcher == null) {
			try {
				stitcher = (IImageStitchingProcess) ServiceManager.getService(IImageStitchingProcess.class);
			} catch (Exception e) {
				logger.error("Error getting Stitching service:" + e);
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void convert(IDataset slice) throws Exception {

		if (context.getMonitor() != null && context.getMonitor().isCancelled()) {
			throw new Exception(getClass().getSimpleName() + " is cancelled");
		}

		List<String> paths = context.getFilePaths();
		imageStack.add(slice);

		if (imageStack.size() == paths.size()) {
			String outputPath = context.getOutputPath();
			ConversionStitchedBean conversionBean = (ConversionStitchedBean)context.getUserObject();
			int rows = conversionBean.getRows();
			int columns = conversionBean.getColumns();
			double angle = conversionBean.getAngle();
			boolean useFeatureAssociation = conversionBean.isFeatureAssociated();
			double fieldOfView = conversionBean.getFieldOfView();
			double[] translations = conversionBean.getTranslations();
			createImageStitcher();
			IDataset stitched = null;
			IRegion region = conversionBean.getRoi();
			if (region != null) {
				stitched = stitcher.stitch(imageStack, rows, columns, angle, fieldOfView, translations, region.getROI(), useFeatureAssociation);
			} else {
				stitched = stitcher.stitch(imageStack, rows, columns, angle);
			}
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
		final List<String> regexs = context.getFilePaths();
		final List<String> paths = new ArrayList<String>(Math.max(
				regexs.size(), 10));
		for (String regex : regexs) {
			final List<File> files = expand(regex);
			for (File file : files) {
				try {
					ILazyDataset data = LoaderFactory.getData(
							file.getAbsolutePath(), context.getMonitor())
							.getLazyDataset(0);
					if (data.getRank() == 2)
						paths.add(file.getAbsolutePath());
				} catch (Exception ignored) {
					continue;
				}
			}
		}
		Collections.sort(paths, new SortNatural<String>(true));
		ImageStackLoader loader = new ImageStackLoader(paths,
				context.getMonitor());
		LazyDataset lazyDataset = new LazyDataset("Folder Stack",
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
		private double[] translations;
		private IRegion region;

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
		public void setRoi(IRegion region) {
			this.region = region;
		}
		public IRegion getRoi() {
			return region;
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
		public void setTranslations(double[] translations) {
			this.translations = translations;
		}
		public double[] getTranslations() {
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
			result = prime * result
					+ ((region == null) ? 0 : region.hashCode());
			result = prime * result + rows;
			result = prime * result + Arrays.hashCode(translations);
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
			if (region == null) {
				if (other.region != null)
					return false;
			} else if (!region.equals(other.region))
				return false;
			if (rows != other.rows)
				return false;
			if (!Arrays.equals(translations, other.translations))
				return false;
			return true;
		}
	}
}
