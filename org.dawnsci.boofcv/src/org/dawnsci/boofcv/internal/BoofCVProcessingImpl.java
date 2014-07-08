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

import org.dawb.common.services.IBoofCVProcessingService;
import org.dawnsci.boofcv.converter.ConvertIDataset;

import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.filter.blur.BlurImageOps;
import boofcv.alg.filter.derivative.GImageDerivativeOps;
import boofcv.alg.misc.ImageStatistics;
import boofcv.core.image.GeneralizedImageOps;
import boofcv.core.image.border.BorderType;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.image.ImageUInt8;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

/**
 * Implementation of IBoofCVProcessingService<br>
 * 
 * This class is internal and not supposed to be used out of this bundle.
 * 
 * @author wqk87977
 *
 */
public class BoofCVProcessingImpl implements IBoofCVProcessingService {

	@Override
	public IDataset filterGaussianBlur(IDataset input, double sigma, int radius) {
		int[] lengths = getLengths(input);

		//convert to boofcv image
		ImageUInt8 converted = new ImageUInt8(lengths[0], lengths[1]);
		ConvertIDataset.datasetToImage(input, converted);

		// apply gaussian blur filter
		ImageUInt8 blurred = new ImageUInt8(lengths[0], lengths[1]);
		BlurImageOps.gaussian(converted, blurred, sigma, radius, null);

		//convert back to IDataset
		return ConvertIDataset.imageToIDataset(blurred, false);
	}

	@Override
	public List<IDataset> filterDerivativeSobel(IDataset orig) {
		int[] lengths = getLengths(orig);

		ImageUInt8 converted = new ImageUInt8(lengths[0], lengths[1]);
		ConvertIDataset.datasetToImage(orig, converted);

		
		Class<? extends ImageSingleBand<?>> derivType = GImageDerivativeOps.getDerivativeType(ImageUInt8.class);

		ImageSingleBand<?> blurred = GeneralizedImageOps.createSingleBand(ImageUInt8.class, lengths[0], lengths[1]);
		ImageSingleBand<?> derivX = GeneralizedImageOps.createSingleBand(derivType, lengths[0], lengths[1]);
		ImageSingleBand<?> derivY = GeneralizedImageOps.createSingleBand(derivType, lengths[0], lengths[1]);

		// Calculate image's derivative
		GImageDerivativeOps.sobel(blurred, derivX, derivY, BorderType.EXTENDED);

		//convert back to IDataset
		List<IDataset> output = new ArrayList<IDataset>(2);
		output.add(ConvertIDataset.convertTo(derivX, false));
		output.add(ConvertIDataset.convertTo(derivY, false));

		return output;
	}

	@Override
	public IDataset filterThreshold(IDataset input, float threshold, boolean down, boolean isBinary) {
		int[] lengths = getLengths(input);

		ImageFloat32 converted = ConvertIDataset.convertFrom(input);
		ImageUInt8 binary = new ImageUInt8(lengths[0], lengths[1]);

		// the mean pixel value is often a reasonable threshold when creating a binary image
		double mean = ImageStatistics.mean(converted);

		// create a binary image by thresholding
		ThresholdImageOps.threshold(converted, binary, (float)mean, down);

		//convert back to IDataset
		return ConvertIDataset.convertTo(binary, isBinary);
	}

	@Override
	public IDataset filterErode(IDataset input, boolean isBinary) {
		int[] lengths = getLengths(input);

		ImageUInt8 converted = new ImageUInt8(lengths[0], lengths[1]);
		ConvertIDataset.datasetToImage(input, converted);

		ImageUInt8 filtered = BinaryImageOps.erode8(converted, null);

		return ConvertIDataset.convertTo(filtered, isBinary);
	}

	@Override
	public IDataset filterDilate(IDataset input, boolean isBinary) {
		int[] lengths = getLengths(input);

		ImageUInt8 converted = new ImageUInt8(lengths[0], lengths[1]);
		ConvertIDataset.datasetToImage(input, converted);

		ImageUInt8 filtered = BinaryImageOps.dilate8(converted, null);

		return ConvertIDataset.convertTo(filtered, isBinary);
	}

	@Override
	public IDataset filterErodeAndDilate(IDataset input, boolean isBinary) {
		int[] lengths = getLengths(input);
		ImageUInt8 converted = new ImageUInt8(lengths[0], lengths[1]);
		ConvertIDataset.datasetToImage(input, converted);
		ImageUInt8 eroded = BinaryImageOps.erode8(converted, null);
		ImageUInt8 delated = BinaryImageOps.dilate8(eroded, null);
		return ConvertIDataset.convertTo(delated, isBinary);
	}

	@Override
	public IDataset filterContour(IDataset input, int rule, int colorExternal, int colorInternal) {
		int[] lengths = getLengths(input);

		ImageUInt8 converted = new ImageUInt8(lengths[0], lengths[1]);
		ConvertIDataset.datasetToImage(input, converted);
		// Detect blobs inside the image using a rule
		List<Contour> contours = BinaryImageOps.contour(converted, rule, null);

		return ConvertIDataset.contourImageToIDataset(contours, colorExternal, colorInternal, lengths[0], lengths[1]);
	}

	private int[] getLengths(IDataset input) {
		if (input.getShape().length != 2)
			throw new IllegalArgumentException("The input data must be of dimension 2");
		int lengths[] = { input.getShape()[0], input.getShape()[1] };
		return lengths;
	}
}
