/*-
 * Copyright (c) 2011-2013, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dawnsci.boofcv.examples.imageprocessing;

import org.dawnsci.boofcv.converter.ConvertIDataset;
import org.dawnsci.boofcv.examples.util.Utils;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.io.IDataHolder;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import boofcv.abst.filter.blur.BlurFilter;
import boofcv.abst.filter.derivative.ImageGradient;
import boofcv.alg.filter.blur.BlurImageOps;
import boofcv.alg.filter.blur.GBlurImageOps;
import boofcv.alg.filter.derivative.GImageDerivativeOps;
import boofcv.alg.filter.derivative.GradientSobel;
import boofcv.core.image.GeneralizedImageOps;
import boofcv.core.image.border.BorderType;
import boofcv.core.image.border.FactoryImageBorderAlgs;
import boofcv.factory.filter.blur.FactoryBlurFilter;
import boofcv.factory.filter.derivative.FactoryDerivative;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSInt16;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.image.ImageUInt8;

/**
 * An introductory example designed to introduce basic BoofCV concepts. Each
 * function shows how to perform basic filtering and display operations using
 * different techniques.
 * 
 */
public class ExampleImageFilter {

	private static int blurRadius = 10;

	/**
	 * 
	 * @throws Throwable
	 */
	public static void runExample() throws Throwable {
		String dataname = "image-01";
		IDataHolder holder = LoaderFactory.getData("resources/lena512.bmp", null);
//		IDataHolder holder = LoaderFactory.getData("resources/pow_M99S5_1_0001.cbf", null);
		
		IDataset image = holder.getDataset(dataname);
		Utils.showPlotView("uk.ac.diamond.scisoft.analysis.rcp.plotViewDP", "Dataset Plot", image);

		int width = image.getShape()[0];
		int height = image.getShape()[1];
		// produces the same results
		ImageUInt8 procedural = new ImageUInt8(width, height);
		ImageUInt8 generalized = new ImageUInt8(width, height);
		ImageUInt8 filter = new ImageUInt8(width, height);
		ImageUInt8 nogenerics = new ImageUInt8(width, height);
		ImageFloat32 generalized32 = new ImageFloat32(width, height);

		ConvertIDataset.datasetToImage(image, procedural);
		ConvertIDataset.datasetToImage(image, generalized);
		ConvertIDataset.datasetToImage(image, filter);
		ConvertIDataset.datasetToImage(image, nogenerics);
		ConvertIDataset.datasetToImage(image, generalized32);

		procedural(procedural);
		generalized(generalized);
		filter(filter);
		nogenerics(nogenerics);

		// try another image input type
		generalized(generalized32);
	}

	private static void procedural(ImageUInt8 input) throws Throwable {
		ImageUInt8 blurred = new ImageUInt8(input.width, input.height);
		ImageSInt16 derivX = new ImageSInt16(input.width, input.height);
		ImageSInt16 derivY = new ImageSInt16(input.width, input.height);

		// Gaussian blur: Convolve a Gaussian kernel
		BlurImageOps.gaussian(input, blurred, -1, blurRadius, null);

		// Calculate image's derivative
		GradientSobel.process(blurred, derivX, derivY, FactoryImageBorderAlgs.extend(input));

		// display the results
		IDataset outputImage = ConvertIDataset.colorizeSign(derivX, -1);
		outputImage.setName("Procedural Fixed Type");
		Utils.showPlotView("uk.ac.diamond.scisoft.analysis.rcp.plotView1", "Plot 1", outputImage);

	}

	private static <T extends ImageSingleBand, D extends ImageSingleBand> void generalized(
			T input) throws Throwable {
		Class<T> inputType = (Class<T>) input.getClass();
		Class<D> derivType = GImageDerivativeOps.getDerivativeType(inputType);

		T blurred = GeneralizedImageOps.createSingleBand(inputType, input.width, input.height);
		D derivX = GeneralizedImageOps.createSingleBand(derivType, input.width, input.height);
		D derivY = GeneralizedImageOps.createSingleBand(derivType, input.width, input.height);

		// Gaussian blur: Convolve a Gaussian kernel
		GBlurImageOps.gaussian(input, blurred, -1, blurRadius, null);

		// Calculate image's derivative
		GImageDerivativeOps.sobel(blurred, derivX, derivY, BorderType.EXTENDED);

		// display the results
		IDataset outputImage = ConvertIDataset.colorizeSign(derivX, -1);
		outputImage.setName("Generalized " + inputType.getSimpleName());
		Utils.showPlotView("uk.ac.diamond.scisoft.analysis.rcp.plotView1", "Plot 1", outputImage);
	}

	private static <T extends ImageSingleBand<?>, D extends ImageSingleBand> void filter(
			T input) throws Throwable {
		Class<T> inputType = (Class<T>) input.getClass();
		Class<D> derivType = GImageDerivativeOps.getDerivativeType(inputType);

		T blurred = GeneralizedImageOps.createSingleBand(inputType, input.width, input.height);
		D derivX = GeneralizedImageOps.createSingleBand(derivType, input.width, input.height);
		D derivY = GeneralizedImageOps.createSingleBand(derivType, input.width, input.height);

		// declare image filters
		BlurFilter<T> filterBlur = FactoryBlurFilter.gaussian(inputType, -1, blurRadius);
		ImageGradient<T, D> gradient = FactoryDerivative.sobel(inputType, derivType);

		// process the image
//		filterBlur.process(input,blurred);
		gradient.process(blurred, derivX, derivY);

		// display the results
		IDataset outputImage = ConvertIDataset.colorizeSign(derivX, -1);
		outputImage.setName("Filter " + inputType.getSimpleName());
		Utils.showPlotView("uk.ac.diamond.scisoft.analysis.rcp.plotView1", "Plot 1", outputImage);
	}

	private static void nogenerics(ImageSingleBand<?> input) throws Throwable {
		Class inputType = input.getClass();
		Class derivType = GImageDerivativeOps.getDerivativeType(inputType);

		ImageSingleBand<?> blurred = GeneralizedImageOps.createSingleBand(
				inputType, input.width, input.height);
		ImageSingleBand<?> derivX = GeneralizedImageOps.createSingleBand(
				derivType, input.width, input.height);
		ImageSingleBand<?> derivY = GeneralizedImageOps.createSingleBand(
				derivType, input.width, input.height);

		// Gaussian blur: Convolve a Gaussian kernel
		GBlurImageOps.gaussian(input, blurred, -1, blurRadius, null);

		// Calculate image's derivative
		GImageDerivativeOps.sobel(blurred, derivX, derivY, BorderType.EXTENDED);

		//TODO convert back to IDataset
		// display the results
		IDataset outputImage = ConvertIDataset.colorizeSign(derivX, -1);
		outputImage.setName("Generalized " + inputType.getSimpleName());
		Utils.showPlotView("uk.ac.diamond.scisoft.analysis.rcp.plotView1", "Plot 1", outputImage);
	}
}
