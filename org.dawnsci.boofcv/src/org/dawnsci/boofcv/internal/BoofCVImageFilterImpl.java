/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.boofcv.internal;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.boofcv.converter.ConvertIDataset;
import org.dawnsci.boofcv.util.Utils;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.image.IImageFilterService;
import org.eclipse.dawnsci.analysis.api.image.ImageThresholdType;

import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.alg.filter.binary.GThresholdImageOps;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.filter.blur.BlurImageOps;
import boofcv.alg.filter.blur.GBlurImageOps;
import boofcv.alg.filter.derivative.GImageDerivativeOps;
import boofcv.alg.misc.ImageStatistics;
import boofcv.core.image.GeneralizedImageOps;
import boofcv.core.image.border.BorderType;
import boofcv.struct.ConnectRule;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.image.ImageUInt8;

/**
 * Implementation of IImageFilterService<br>
 * 
 * This class is internal and not supposed to be used out of this bundle.
 * 
 * @author Baha El Kassaby
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
		int[] shape = Utils.getShape(input);
		ImageFloat32 converted = ConvertIDataset.convertFrom(input, ImageFloat32.class, 1);
		ImageFloat32 blurred = new ImageFloat32(shape[1], shape[0]);
		BlurImageOps.gaussian(converted, blurred, sigma, radius, null);
		return ConvertIDataset.convertTo(blurred, false);
	}

	@Override
	public List<IDataset> filterDerivativeSobel(IDataset input) {
		int[] shape = Utils.getShape(input);

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
	public IDataset filterErode(IDataset input, boolean isBinary) {
		ImageUInt8 converted = ConvertIDataset.convertFrom(input, ImageUInt8.class, 1);

		ImageUInt8 filtered = BinaryImageOps.erode8(converted, 1, null);

		return ConvertIDataset.convertTo(filtered, isBinary);
	}

	@Override
	public IDataset filterDilate(IDataset input, boolean isBinary) {
		ImageUInt8 converted = ConvertIDataset.convertFrom(input, ImageUInt8.class, 1);

		ImageUInt8 filtered = BinaryImageOps.dilate8(converted, 1, null);

		return ConvertIDataset.convertTo(filtered, isBinary);
	}

	@Override
	public IDataset filterErodeAndDilate(IDataset input, boolean isBinary) {
		ImageUInt8 converted = ConvertIDataset.convertFrom(input, ImageUInt8.class, 1);
		ImageUInt8 eroded = BinaryImageOps.erode8(converted, 1, null);
		ImageUInt8 delated = BinaryImageOps.dilate8(eroded, 1, null);
		return ConvertIDataset.convertTo(delated, isBinary);
	}

	@Override
	public IDataset filterContour(IDataset input, int rule, int colorExternal, int colorInternal) throws Exception {
		int[] shape = Utils.getShape(input);
		
		ImageFloat32 converted = ConvertIDataset.convertFrom(input, ImageFloat32.class, 1);

		// the mean pixel value is often a reasonable threshold when creating a binary image
		double mean = ImageStatistics.mean(converted);

		ImageUInt8 binary = new ImageUInt8(shape[1], shape[0]);
		// create a binary image by thresholding
		ThresholdImageOps.threshold(converted, binary, (float) mean, true);

		// Detect blobs inside the image using a rule
		ConnectRule[] rules = ConnectRule.values();
		ConnectRule contourRule = null;
		for (int i = 0; i < rules.length; i++) {
			if (rules[i].getShortName().equals(String.valueOf(rule)))
				contourRule = rules[i];
		}
		if (contourRule == null)
			throw new Exception("Rule parameter can be 4 or 8 only");
		List<Contour> contours = BinaryImageOps.contour(binary, contourRule, null);

		return ConvertIDataset.contourImageToIDataset(contours, colorExternal, colorInternal, shape[1], shape[0]);
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

	private IDataset threshold(ImageThresholdType type, IDataset input, float threshold, int radius, boolean down, boolean isBinary) {
		int[] shape = Utils.getShape(input);

		ImageFloat32 converted = ConvertIDataset.convertFrom(input, ImageFloat32.class, 1);
		ImageUInt8 binary = new ImageUInt8(shape[1], shape[0]);

		switch (type) {
		case GLOBAL_CUSTOM:
			// create a binary image by thresholding
			ThresholdImageOps.threshold(converted, binary, threshold, down);
			break;
		case GLOBAL_MEAN:
			GThresholdImageOps.threshold(converted, binary, ImageStatistics.mean(converted), down);
			break;
		case GLOBAL_OTSU:
			Number min = input.min(true), max = input.max(true);
			GThresholdImageOps.threshold(converted, binary, GThresholdImageOps.computeOtsu(converted, min.intValue(), max.intValue()+1), down);
			break;
		case GLOBAL_ENTROPY:
			min = input.min(true); max = input.max(true);
			GThresholdImageOps.threshold(converted, binary, GThresholdImageOps.computeEntropy(converted, min.intValue(), max.intValue()+1), down);
			break;
		case ADAPTIVE_SQUARE:
			GThresholdImageOps.adaptiveSquare(converted, binary, radius, 0, down, null, null);
			break;
		case ADAPTIVE_GAUSSIAN:
			GThresholdImageOps.adaptiveGaussian(converted, binary, radius, 0, down, null, null);
			break;
		case ADAPTIVE_SAUVOLA:
			GThresholdImageOps.adaptiveSauvola(converted, binary, radius, 0.30f, down);
			break;
		default:
			break;
		}
		//convert back to IDataset
		return ConvertIDataset.convertTo(binary, isBinary);
	}

	@Override
	public IDataset globalThreshold(IDataset input, float threshold, boolean down, boolean isBinary) {
		return threshold(ImageThresholdType.GLOBAL_CUSTOM, input, threshold, 0, down, isBinary);
	}

	@Override
	public IDataset globalMeanThreshold(IDataset input, boolean down, boolean isBinary) {
		return threshold(ImageThresholdType.GLOBAL_MEAN, input, 0, 0, down, isBinary);
	}

	@Override
	public IDataset globalOtsuThreshold(IDataset input, boolean down, boolean isBinary) {
		return threshold(ImageThresholdType.GLOBAL_OTSU, input, 0, 0, down, isBinary);
	}

	@Override
	public IDataset globalEntropyThreshold(IDataset input, boolean down, boolean isBinary) {
		return threshold(ImageThresholdType.GLOBAL_ENTROPY, input, 0, 0, down, isBinary);
	}

	@Override
	public IDataset adaptiveSquareThreshold(IDataset input, int radius, boolean down, boolean isBinary) {
		return threshold(ImageThresholdType.ADAPTIVE_SQUARE, input, 0, radius, down, isBinary);
	}

	@Override
	public IDataset adaptiveGaussianThreshold(IDataset input, int radius, boolean down, boolean isBinary) {
		return threshold(ImageThresholdType.ADAPTIVE_GAUSSIAN, input, 0, radius, down, isBinary);
	}

	@Override
	public IDataset adaptiveSauvolaThreshold(IDataset input, int radius, boolean down, boolean isBinary) {
		return threshold(ImageThresholdType.ADAPTIVE_SAUVOLA, input, 0, radius, down, isBinary);
	}
}
