/*
 * Copyright (c) 2012, 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.boofcv.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.util.Pair;
import org.dawnsci.boofcv.converter.ConvertIDataset;
import org.dawnsci.boofcv.registration.ImageHessianRegistration;
import org.eclipse.dawnsci.analysis.api.image.HessianRegParameters;
import org.eclipse.dawnsci.analysis.api.image.IImageTransform;
import org.eclipse.dawnsci.analysis.api.image.DetectionAlgoParameters;
import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.hdf5.HDF5Utils;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import boofcv.alg.distort.DistortImageOps;
import boofcv.alg.interpolate.TypeInterpolate;
import boofcv.struct.feature.TupleDesc;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSingleBand;

/**
 * Implementation of IImageTransform<br>
 * 
 * This class is internal and not supposed to be used out of this bundle.
 * 
 * @author Baha El-Kassaby
 *
 */
public class BoofCVImageTransformImpl<T extends ImageSingleBand<?>, TD extends TupleDesc<?>> implements IImageTransform {

	static {
		System.out.println("Starting BoofCV image transform service.");
	}

	private static final Logger logger = LoggerFactory.getLogger(BoofCVImageTransformImpl.class);

	public BoofCVImageTransformImpl() {
		// Important do nothing here, OSGI may start the service more than once.
	}

	@Override
	public IDataset rotate(IDataset data, double angle) throws Exception {
		return rotate(data, angle, true);
	}

	@Override
	public IDataset rotate(IDataset data, double angle, boolean keepShape) throws Exception {
		if (data.getShape().length != 2)
			throw new Exception("Data shape is not 2D");
		ImageFloat32 image = ConvertIDataset.convertFrom(data, ImageFloat32.class, 1);
		int width = 0, height = 0;
		if (keepShape) {
			width = image.width;
			height = image.height;
		} else {
			// calculate resulting bounding box
			double cos = Math.abs(Math.cos(Math.toRadians(angle)));
			double sin = Math.abs(Math.sin(Math.toRadians(angle)));
			width = (int) (image.width * cos + image.height * sin);
			height = (int) (image.height * cos + image.width * sin);
		}
		ImageFloat32 rotated = new ImageFloat32(width, height);

		DistortImageOps.rotate(image, rotated, TypeInterpolate.BILINEAR, (float)Math.toRadians(angle));
		return ConvertIDataset.convertTo(rotated, true);
	}

	@Override
	public List<IDataset> align(List<IDataset> images, IMonitor monitor) throws Exception {
		List<IDataset> alignedList = new ArrayList<IDataset>();
		ImageFloat32 imageA = ConvertIDataset.convertFrom(images.get(0), ImageFloat32.class, 1);
		alignedList.add(images.get(0));
		if (images.get(0).getShape().length != 2)
			throw new Exception("Data shape is not 2D");
		
		for (int i = 1; i < images.size(); i++) {
			if (images.get(i).getShape().length != 2)
				throw new Exception("Data shape is not 2D");
			ImageFloat32 imageB = ConvertIDataset.convertFrom(images.get(i), ImageFloat32.class, 1);
			ImageSingleBand<?> aligned = ImageHessianRegistration.registerHessian(imageA, imageB);
			IDataset alignedData = ConvertIDataset.convertTo(aligned, true);
			alignedData.setName(images.get(i).getName());
			alignedList.add(alignedData);
			if(monitor != null) {
				if (monitor.isCancelled())
					return alignedList;
				monitor.worked(1);
			}
		}
		return alignedList;
	}

	@Override
	public ILazyDataset align(ILazyDataset lazydata, IMonitor monitor) throws Exception {
		return align(lazydata, null, null, monitor);
	}

	@Override
	public ILazyDataset align(ILazyDataset lazydata, DetectionAlgoParameters detectParams, HessianRegParameters hessianParams, IMonitor monitor) throws Exception {
		if (lazydata.getShape().length != 3)
			throw new Exception("Supported Lazy data is 3D, please provide a 3D dataset");
		// save on a temp file
		String path = "/entry/data/";
		String name = "aligned";
		File tmpFile = File.createTempFile("tmp_aligned", ".h5");
		tmpFile.deleteOnExit();
		String file = tmpFile.getAbsolutePath();
		IDataset firstSlice = lazydata.getSlice(new Slice(1)).squeeze();
		ILazyWriteableDataset lazy = HDF5Utils.createLazyDataset(file, path, name, lazydata.getShape(), null,
				lazydata.getShape(), Dataset.FLOAT32, null, false);

		//convert to boofcv data
		ImageFloat32 imageA = ConvertIDataset.convertFrom(firstSlice, ImageFloat32.class, 1);
		// add first image
		appendDataset(lazy, firstSlice, 0, monitor);
		int[] shape = lazydata.getShape();
		if (firstSlice.getShape().length != 2)
			throw new Exception("Data shape is not 2D");
		
		for (int i = 1; i < shape[0]; i++) {
			IDataset slice = lazydata.getSlice(new Slice(i, i + 1)).squeeze();
			if (slice.getShape().length != 2)
				throw new Exception("Data shape is not 2D");
			ImageFloat32 imageB = ConvertIDataset.convertFrom(slice, ImageFloat32.class, 1);
			ImageSingleBand<?> aligned = ImageHessianRegistration.registerHessian(imageA, imageB, detectParams, hessianParams);
			IDataset alignedData = ConvertIDataset.convertTo(aligned, true);
			alignedData.setName(slice.getName());
			// add data to lazy file
			appendDataset(lazy, alignedData, i, monitor);

			if(monitor != null) {
				if (monitor.isCancelled()) {
					// reload file
					lazy.setName("Aligned");
					return getLazyData(file, path + name);
				}
				monitor.worked(1);
			}
		}
		// reload file
		lazy.setName("Aligned");
		return getLazyData(file, path + name);
	}

	@Override
	public IDataset affineTransform(IDataset data, double a11, double a12, double a21, double a22, double dx, double dy) throws Exception {
		return affineTransform(data, a11, a12, a21, a22, dx, dy, false);
	}

	@Override
	public IDataset affineTransform(IDataset data, double a11, double a12, double a21, double a22, double dx, double dy, boolean keepShape) throws Exception {
		if (data.getShape().length != 2)
			throw new Exception("Data shape is not 2D");
		ImageFloat32 image = ConvertIDataset.convertFrom(data, ImageFloat32.class, 1);
		
		// IDataset uses row major ordering, but ImageFloat32 uses column major
		// this is why the the affine transform parameters will be exchanged in what follows next...
		
		int width = 0, height = 0;
		if (keepShape) {
			width = image.width;
			height = image.height;
		} else {
			// calculate resulting bounding box
			Pair<Double, Double> coords00 = affineTransformation(0, 0, a22, a21, a12, a11, dy, dx);
			Pair<Double, Double> coords10 = affineTransformation(0, image.height, a22, a21, a12, a11, dy, dx);
			Pair<Double, Double> coords01 = affineTransformation(image.width, 0, a22, a21, a12, a11, dy, dx);
			Pair<Double, Double> coords11 = affineTransformation(image.width, image.height, a22, a21, a12, a11, dy, dx);
			
			List<Double> coordsx = Arrays.asList(coords00.getFirst(), coords10.getFirst(), coords01.getFirst(), coords11.getFirst());
			List<Double> coordsy = Arrays.asList(coords00.getSecond(), coords10.getSecond(), coords01.getSecond(), coords11.getSecond());
			
			double maxx = Collections.max(coordsx);
			double maxy = Collections.max(coordsy);
			
			height = (int) (maxy);
			width = (int) (maxx);
		}
		
		ImageFloat32 transformed = new ImageFloat32(width, height);

		DistortImageOps.affine(image, transformed, TypeInterpolate.BILINEAR, a22, a21, a12, a11, dy, dx);
		return ConvertIDataset.convertTo(transformed, true);
	}

	private ILazyDataset getLazyData(String filename, String node) {
		ILazyDataset shifted = null;
		try {
			shifted = HDF5Utils.loadDataset(filename, node);
		} catch (ScanFileHolderException e) {
			logger .error("Could not reload the temp h5 file:", e);
		}
		return shifted;
	}

	private static void appendDataset(ILazyWriteableDataset lazy, IDataset data, int idx, IMonitor monitor) throws Exception {
		SliceND ndSlice = new SliceND(lazy.getShape(), new int[] {idx, 0, 0}, new int[] {(idx+1), data.getShape()[0], data.getShape()[1]}, null);
		lazy.setSlice(monitor, data, ndSlice);
	}

	private static Pair<Double, Double> affineTransformation(double x, double y, double a11, double a12, double a21, double a22, double dx, double dy) {
		return new Pair<Double, Double>(x*a11 + y*a12 + dx, x*a21 + y*a22 + dy);
	}
	
}
