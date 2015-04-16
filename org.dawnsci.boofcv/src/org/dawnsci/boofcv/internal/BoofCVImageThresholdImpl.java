/*-
 * Copyright (c) 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.boofcv.internal;

import org.dawnsci.boofcv.converter.ConvertIDataset;
import org.dawnsci.boofcv.util.Utils;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.image.IImageThreshold;

import boofcv.alg.filter.binary.GThresholdImageOps;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.misc.ImageStatistics;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageUInt8;

/**
 * Implementation of IImageTheshold<br>
 * 
 * This class is internal and not supposed to be used out of this bundle.
 * 
 * @author Baha El Kassaby
 *
 */
public class BoofCVImageThresholdImpl implements IImageThreshold {

	static {
		System.out.println("Starting BoofCV image threshold service.");
	}

	public BoofCVImageThresholdImpl() {
		// Important do nothing here, OSGI may start the service more than once.
	}

	enum ThresholdType {
		GLOBAL,
		MEAN,
		OTSU,
		ENTROPY,
		SQUARE,
		GAUSSIAN,
		SAUVOLA;
	}

	private IDataset threshold(ThresholdType type, IDataset input, float threshold, int radius, boolean down, boolean isBinary) {
		int[] shape = Utils.getShape(input);

		ImageFloat32 converted = ConvertIDataset.convertFrom(input, ImageFloat32.class, 1);
		ImageUInt8 binary = new ImageUInt8(shape[1], shape[0]);

		switch (type) {
		case GLOBAL:
			// create a binary image by thresholding
			ThresholdImageOps.threshold(converted, binary, threshold, down);
			break;
		case MEAN:
			GThresholdImageOps.threshold(converted, binary, ImageStatistics.mean(converted), down);
			break;
		case OTSU:
			GThresholdImageOps.threshold(converted, binary, GThresholdImageOps.computeOtsu(converted, 0, 256), down);
			break;
		case ENTROPY:
			GThresholdImageOps.threshold(converted, binary, GThresholdImageOps.computeEntropy(converted, 0, 256), down);
			break;
		case SQUARE:
			GThresholdImageOps.adaptiveSquare(converted, binary, radius, 0, down, null, null);
			break;
		case GAUSSIAN:
			GThresholdImageOps.adaptiveGaussian(converted, binary, radius, 0, down, null, null);
			break;
		case SAUVOLA:
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
		return threshold(ThresholdType.GLOBAL, input, threshold, 0, down, isBinary);
	}

	@Override
	public IDataset globalMeanThreshold(IDataset input, boolean down, boolean isBinary) {
		return threshold(ThresholdType.MEAN, input, 0, 0, down, isBinary);
	}

	@Override
	public IDataset globalOtsuThreshold(IDataset input, boolean down, boolean isBinary) {
		return threshold(ThresholdType.OTSU, input, 0, 0, down, isBinary);
	}

	@Override
	public IDataset globalEntropyThreshold(IDataset input, boolean down, boolean isBinary) {
		return threshold(ThresholdType.ENTROPY, input, 0, 0, down, isBinary);
	}

	@Override
	public IDataset adaptiveSquareThreshold(IDataset input, int radius, boolean down, boolean isBinary) {
		return threshold(ThresholdType.SQUARE, input, 0, radius, down, isBinary);
	}

	@Override
	public IDataset adaptiveGaussianThreshold(IDataset input, int radius, boolean down, boolean isBinary) {
		return threshold(ThresholdType.GAUSSIAN, input, 0, radius, down, isBinary);
	}

	@Override
	public IDataset adaptiveSauvolaThreshold(IDataset input, int radius, boolean down, boolean isBinary) {
		return threshold(ThresholdType.SAUVOLA, input, 0, radius, down, isBinary);
	}
}
