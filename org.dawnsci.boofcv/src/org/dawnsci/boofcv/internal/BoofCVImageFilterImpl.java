/*
 * Copyright (c) 2011-2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.boofcv.internal;

import org.dawnsci.boofcv.converter.ConvertIDataset;
import org.dawnsci.boofcv.util.Utils;
import org.eclipse.dawnsci.analysis.api.image.IImageFilterService;
import org.eclipse.dawnsci.analysis.api.image.ImageThresholdType;
import org.eclipse.january.dataset.BooleanDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.RGBByteDataset;
import org.eclipse.january.dataset.RGBDataset;

import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.GThresholdImageOps;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.filter.blur.BlurImageOps;
import boofcv.alg.filter.blur.GBlurImageOps;
import boofcv.alg.filter.derivative.GImageDerivativeOps;
import boofcv.alg.misc.ImageStatistics;
import boofcv.core.image.GeneralizedImageOps;
import boofcv.core.image.border.BorderType;
import boofcv.struct.ConnectRule;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSInt32;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.image.ImageUInt8;
import boofcv.struct.image.MultiSpectral;

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
		if (input instanceof RGBByteDataset || input instanceof RGBDataset) {
			ImageBase<?> rgbConverted = ConvertIDataset.convertFrom(input, ImageFloat32.class, 3);
			MultiSpectral<ImageFloat32> blurred = new MultiSpectral<ImageFloat32>(ImageFloat32.class, 3);
			ImageFloat32 rBlurred = new ImageFloat32(shape[1], shape[0]);
			ImageFloat32 gBlurred = new ImageFloat32(shape[1], shape[0]);
			ImageFloat32 bBlurred = new ImageFloat32(shape[1], shape[0]);
			blurred.setBands(new ImageFloat32[] {rBlurred, gBlurred, bBlurred});
			BlurImageOps.gaussian((ImageFloat32) ((MultiSpectral<?>)rgbConverted).getBand(0), rBlurred, sigma, radius, null);
			BlurImageOps.gaussian((ImageFloat32) ((MultiSpectral<?>)rgbConverted).getBand(1), gBlurred, sigma, radius, null);
			BlurImageOps.gaussian((ImageFloat32) ((MultiSpectral<?>)rgbConverted).getBand(2), bBlurred, sigma, radius, null);
			return ConvertIDataset.convertTo(blurred, false);
		} else {
			ImageFloat32 converted = ConvertIDataset.convertFrom(input, ImageFloat32.class, 1);
			ImageFloat32 blurred = new ImageFloat32(shape[1], shape[0]);
			BlurImageOps.gaussian(converted, blurred, sigma, radius, null);
			return ConvertIDataset.convertTo(blurred, false);
		}
	}

	@Override
	public IDataset filterDerivativeSobel(IDataset input, boolean isXaxis) {
		int[] shape = Utils.getShape(input);

		ImageSingleBand<?> converted = ConvertIDataset.convertFrom(input, ImageUInt8.class, 1);
		Class<? extends ImageSingleBand<?>> derivType = (Class<? extends ImageSingleBand<?>>) GImageDerivativeOps.getDerivativeType(ImageUInt8.class);
		ImageSingleBand<?> derivX = GeneralizedImageOps.createSingleBand(derivType, shape[1], shape[0]);
		ImageSingleBand<?> derivY = GeneralizedImageOps.createSingleBand(derivType, shape[1], shape[0]);
		// Calculate image's derivative
		GImageDerivativeOps.sobel(converted, derivX, derivY, BorderType.EXTENDED);

		// convert back to IDataset
		if (isXaxis)
			return ConvertIDataset.convertTo(derivX, false);
		else
			return ConvertIDataset.convertTo(derivY, false);
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
	public IDataset extractBlob(IDataset input, int rule) throws Exception {
		if (!(input instanceof BooleanDataset))
			throw new IllegalArgumentException("The input dataset should be a binary image.");
		int[] shape = Utils.getShape(input);

		// convert
		ImageUInt8 binary = ConvertIDataset.convertFrom(input, ImageUInt8.class, 1);
		// Detect blobs inside the image using a rule
		ConnectRule[] rules = ConnectRule.values();
		ConnectRule contourRule = null;
		for (int i = 0; i < rules.length; i++) {
			if (rules[i].getShortName().equals(String.valueOf(rule)))
				contourRule = rules[i];
		}
		if (contourRule == null)
			throw new Exception("Rule parameter can be 4 or 8 only");

		ImageSInt32 label = new ImageSInt32(shape[1], shape[0]);
		BinaryImageOps.contour(binary, contourRule, label);

		return ConvertIDataset.convertTo(label, false);
	}

	@Override
	public IDataset filterMedian(IDataset input, int radius) {
		int[] shape = Utils.getShape(input);
		if (input instanceof RGBByteDataset || input instanceof RGBDataset) {
			ImageBase<?> rgbConverted = ConvertIDataset.convertFrom(input, ImageFloat32.class, 3);
			MultiSpectral<ImageFloat32> median = new MultiSpectral<ImageFloat32>(ImageFloat32.class, 3);
			ImageFloat32 rMedian = new ImageFloat32(shape[1], shape[0]);
			ImageFloat32 gMedian = new ImageFloat32(shape[1], shape[0]);
			ImageFloat32 bMedian = new ImageFloat32(shape[1], shape[0]);
			median.setBands(new ImageFloat32[] {rMedian, gMedian, bMedian});
			BlurImageOps.median((ImageFloat32) ((MultiSpectral<?>)rgbConverted).getBand(0), rMedian, radius);
			BlurImageOps.median((ImageFloat32) ((MultiSpectral<?>)rgbConverted).getBand(1), gMedian, radius);
			BlurImageOps.median((ImageFloat32) ((MultiSpectral<?>)rgbConverted).getBand(2), bMedian, radius);
			return ConvertIDataset.convertTo(median, false);
		} else {
			ImageFloat32 converted = ConvertIDataset.convertFrom(input, ImageFloat32.class, 1);
			ImageFloat32 median = GBlurImageOps.median(converted, null, radius);
			return ConvertIDataset.convertTo(median, false);
		}
	}

	@Override
	public IDataset filterMean(IDataset input, int radius) {
		int[] shape = Utils.getShape(input);
		if (input instanceof RGBByteDataset || input instanceof RGBDataset) {
			ImageBase<?> rgbConverted = ConvertIDataset.convertFrom(input, ImageFloat32.class, 3);
			MultiSpectral<ImageFloat32> mean = new MultiSpectral<ImageFloat32>(ImageFloat32.class, 3);
			ImageFloat32 rMean = new ImageFloat32(shape[1], shape[0]);
			ImageFloat32 gMean = new ImageFloat32(shape[1], shape[0]);
			ImageFloat32 bMean = new ImageFloat32(shape[1], shape[0]);
			mean.setBands(new ImageFloat32[] {rMean, gMean, bMean});
			BlurImageOps.mean((ImageFloat32) ((MultiSpectral<?>)rgbConverted).getBand(0), rMean, radius, null);
			BlurImageOps.mean((ImageFloat32) ((MultiSpectral<?>)rgbConverted).getBand(1), gMean, radius, null);
			BlurImageOps.mean((ImageFloat32) ((MultiSpectral<?>)rgbConverted).getBand(2), bMean, radius, null);
			return ConvertIDataset.convertTo(mean, false);
		} else {
			ImageFloat32 converted = ConvertIDataset.convertFrom(input, ImageFloat32.class, 1);
			ImageFloat32 mean = GBlurImageOps.mean(converted, null, radius, null);
			return ConvertIDataset.convertTo(mean, false);
		}
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
			int multiplier = multiply(converted, max.doubleValue(), min.doubleValue());
			int iMin = (int) (min.doubleValue() * multiplier);
			int iMax = (int) (max.doubleValue() * multiplier);
			GThresholdImageOps.threshold(converted, binary, GThresholdImageOps.computeOtsu(converted, iMin, iMax+1), down);
			break;
		case GLOBAL_ENTROPY:
			min = input.min(true); max = input.max(true);
			multiplier = multiply(converted, max.doubleValue(), min.doubleValue());
			iMin = (int) (min.doubleValue() * multiplier);
			iMax = (int) (max.doubleValue() * multiplier);
			GThresholdImageOps.threshold(converted, binary, GThresholdImageOps.computeEntropy(converted, iMin, iMax+1), down);
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

	/**
	 * If the min and max values are between 0 and 1 then we multiply the
	 * dataset by the needed multiplier
	 * 
	 * @param image
	 * @param maxValue
	 * @param minValue
	 * @return
	 */
	private int multiply(ImageFloat32 image, double maxValue, double minValue) {
		int multiplier = 1;
		if ((minValue < 1 && maxValue <= 1) || ((maxValue - minValue <= 1))) {
			String minformat = String.format("%6.3e", minValue);
			int mindigit = Math.abs(Integer.valueOf(minformat.split("e")[1]));
			String maxformat = String.format("%6.3e", maxValue);
			int maxdigit = Math.abs(Integer.valueOf(maxformat.split("e")[1]));
			int max = maxdigit > mindigit ? maxdigit : mindigit;
			for (int i = 0; i < max; i++)
				multiplier *= 10;
			float[] data = image.data;
			for (int i = 0; i < data.length; i++) {
				data[i] *= multiplier;
			}
		}
		return multiplier;
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
