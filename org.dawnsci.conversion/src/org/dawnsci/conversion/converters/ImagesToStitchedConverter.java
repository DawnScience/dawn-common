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
import java.util.Collections;
import java.util.List;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.util.list.SortNatural;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.image.IImageStitchingProcess;
import org.eclipse.dawnsci.analysis.dataset.impl.Activator;
import org.eclipse.dawnsci.analysis.dataset.impl.LazyDataset;

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

	private List<IDataset> imageStack = new ArrayList<IDataset>();

	private IImageStitchingProcess stitcher;

	public ImagesToStitchedConverter(IConversionContext context)
			throws Exception {
		super(context);
		final File dir = new File(context.getOutputPath());
		dir.mkdirs();
		// We put the many files in one ILazyDataset and set that in the context
		// as an override.
		ILazyDataset set = getLazyDataset();
		context.setLazyDataset(set);
		context.addSliceDimension(0, "all");
	}

	private void createImageStitcher() {
		if (stitcher == null) {
			stitcher = (IImageStitchingProcess) Activator
					.getService(IImageStitchingProcess.class);
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
			ConversionStitchedBean conversionBean = (ConversionStitchedBean)context.getUserObject();
			int rows = conversionBean.getRows();
			int columns = conversionBean.getColumns();
			double angle = conversionBean.getAngle();
			createImageStitcher();
			IDataset stitched = stitcher.stitch(imageStack, rows, columns, angle);
			stitched.setName("stitched");
			final File sliceFile = new File(getFilePath(stitched));

			if (!sliceFile.getParentFile().exists())
				sliceFile.getParentFile().mkdirs();

			// JavaImageSaver likes 33 but users don't
			int bits = getBits();
			if (bits == 32 && getExtension().toLowerCase().startsWith("tif"))
				bits = 33;

			final JavaImageSaver saver = new JavaImageSaver(
					sliceFile.getAbsolutePath(), getExtension(), bits, true);
			final DataHolder dh = new DataHolder();
			dh.addDataset(stitched.getName(), stitched);
			dh.setFilePath(sliceFile.getAbsolutePath());
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
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(angle);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result + columns;
			result = prime * result + rows;
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
			ConversionStitchedBean other = (ConversionStitchedBean) obj;
			if (Double.doubleToLongBits(angle) != Double
					.doubleToLongBits(other.angle))
				return false;
			if (columns != other.columns)
				return false;
			if (rows != other.rows)
				return false;
			return true;
		}
		public double getAngle() {
			return angle;
		}
		public void setAngle(double angle) {
			this.angle = angle;
		}
	}
}
