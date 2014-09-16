/*-
 * Copyright (c) 2014 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.boofcv.internal;

import java.util.ArrayList;
import java.util.List;

import org.dawb.common.services.IImageFilterService;
import org.dawnsci.boofcv.converter.ConvertIDataset;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.filter.blur.BlurImageOps;
import boofcv.alg.filter.blur.GBlurImageOps;
import boofcv.alg.filter.derivative.GImageDerivativeOps;
import boofcv.alg.misc.ImageStatistics;
import boofcv.core.image.GeneralizedImageOps;
import boofcv.core.image.border.BorderType;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.image.ImageUInt8;

/**
 * Implementation of IImageFilterService<br>
 * 
 * This class is internal and not supposed to be used out of this bundle.
 * 
 * @author wqk87977
 *
 */
public class BoofCVImageFilterImpl implements IImageFilterService {

	static {
		System.out.println("Starting BoofCV image filter service.");
	}

	public BoofCVImageFilterImpl() {
		// Important do nothing here, OSGI may start the service more than once.
	}

	@Override
	public IDataset filterGaussianBlur(IDataset input, double sigma, int radius) {
		int[] shape = getShape(input);
		ImageFloat32 converted = ConvertIDataset.convertFrom(input, ImageFloat32.class, 1);
		ImageFloat32 blurred = new ImageFloat32(shape[1], shape[0]);
		BlurImageOps.gaussian(converted, blurred, sigma, radius, null);
		return ConvertIDataset.convertTo(blurred, false);
	}

	@Override
	public List<IDataset> filterDerivativeSobel(IDataset input) {
		int[] shape = getShape(input);

		ImageSingleBand<?> converted = ConvertIDataset.convertFrom(input, ImageUInt8.class, 1);

		Class<? extends ImageSingleBand<?>> derivType = GImageDerivativeOps.getDerivativeType(ImageUInt8.class);
		ImageSingleBand<?> derivX = GeneralizedImageOps.createSingleBand(derivType, shape[1], shape[0]);
		ImageSingleBand<?> derivY = GeneralizedImageOps.createSingleBand(derivType, shape[1], shape[0]);

		// Calculate image's derivative
		GImageDerivativeOps.sobel(converted, derivX, derivY, BorderType.EXTENDED);

		//convert back to IDataset
		List<IDataset> output = new ArrayList<IDataset>(2);
		output.add(ConvertIDataset.convertTo(derivX, false));
		output.add(ConvertIDataset.convertTo(derivY, false));

		return output;
	}

	@Override
	public IDataset filterThreshold(IDataset input, float threshold, boolean down, boolean isBinary) {
		int[] shape = getShape(input);

		ImageFloat32 converted = ConvertIDataset.convertFrom(input, ImageFloat32.class, 1);
		ImageUInt8 binary = new ImageUInt8(shape[1], shape[0]);

		// create a binary image by thresholding
		ThresholdImageOps.threshold(converted, binary, threshold, down);

		//convert back to IDataset
		return ConvertIDataset.convertTo(binary, isBinary);
	}

	@Override
	public IDataset filterErode(IDataset input, boolean isBinary) {
		ImageUInt8 converted = ConvertIDataset.convertFrom(input, ImageUInt8.class, 1);

		ImageUInt8 filtered = BinaryImageOps.erode8(converted, null);

		return ConvertIDataset.convertTo(filtered, isBinary);
	}

	@Override
	public IDataset filterDilate(IDataset input, boolean isBinary) {
		ImageUInt8 converted = ConvertIDataset.convertFrom(input, ImageUInt8.class, 1);

		ImageUInt8 filtered = BinaryImageOps.dilate8(converted, null);

		return ConvertIDataset.convertTo(filtered, isBinary);
	}

	@Override
	public IDataset filterErodeAndDilate(IDataset input, boolean isBinary) {
		ImageUInt8 converted = ConvertIDataset.convertFrom(input, ImageUInt8.class, 1);
		ImageUInt8 eroded = BinaryImageOps.erode8(converted, null);
		ImageUInt8 delated = BinaryImageOps.dilate8(eroded, null);
		return ConvertIDataset.convertTo(delated, isBinary);
	}

	@Override
	public IDataset filterContour(IDataset input, int rule, int colorExternal, int colorInternal) {
		int[] shape = getShape(input);
		
		ImageFloat32 converted = ConvertIDataset.convertFrom(input, ImageFloat32.class, 1);

		// the mean pixel value is often a reasonable threshold when creating a binary image
		double mean = ImageStatistics.mean(converted);

		ImageUInt8 binary = new ImageUInt8(shape[1], shape[0]);
		// create a binary image by thresholding
		ThresholdImageOps.threshold(converted, binary, (float) mean, true);

		// Detect blobs inside the image using a rule
		List<Contour> contours = BinaryImageOps.contour(binary, rule, null);

		return ConvertIDataset.contourImageToIDataset(contours, colorExternal, colorInternal, shape[1], shape[0]);
	}

	private int[] getShape(IDataset input) {
		int[] shape = input.getShape();
		if (shape.length != 2)
			throw new IllegalArgumentException("The input data must be of dimension 2");
		return shape;
	}

	@Override
	public IDataset filterMedian(IDataset input, int radius) {
		ImageFloat32 converted = ConvertIDataset.convertFrom(input, ImageFloat32.class, 1);
		ImageFloat32 median = GBlurImageOps.median(converted, null, radius);
		return ConvertIDataset.convertTo(median, false);
	}

	@Override
	public IDataset filterMean(IDataset input, int radius) {
		ImageFloat32 converted = ConvertIDataset.convertFrom(input, ImageFloat32.class, 1);
		ImageFloat32 mean = GBlurImageOps.mean(converted, null, radius, null);
		return ConvertIDataset.convertTo(mean, false);
	}

	@Override
	public IDataset filterMin(IDataset input, int radius) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDataset filterMax(IDataset input, int radius) {
		// TODO Auto-generated method stub
		return null;
	}
}
