/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.boofcv.examples.imageprocessing;

import java.util.List;

import org.dawnsci.boofcv.converter.ConvertIDataset;
import org.dawnsci.boofcv.examples.util.Utils;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.misc.ImageStatistics;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSInt32;
import boofcv.struct.image.ImageUInt8;

/**
 * Demonstrates how to create binary images by thresholding, applying binary morphological operations, and
 * then extracting detected features by finding their contours.
 *
 */
public class ExampleBinaryOps {

	/**
	 * 
	 * @throws Throwable
	 */
	public static void runExample() throws Throwable {
		String dataname = "image-01";
		IDataHolder holder = LoaderFactory.getData("resources/particles01.jpg", null);
//		IDataHolder holder = LoaderFactory.getData("resources/pow_M99S5_1_0001.cbf", null);

		IDataset data = holder.getDataset(dataname);
		Utils.showPlotView("uk.ac.diamond.scisoft.analysis.rcp.plotViewDP", "Dataset Plot", data);

		// convert into BoofCV format
		ImageFloat32 input = ConvertIDataset.convertFrom(data, ImageFloat32.class, 1);
		ImageUInt8 binary = new ImageUInt8(input.width,input.height);
		ImageSInt32 label = new ImageSInt32(input.width,input.height);

		// the mean pixel value is often a reasonable threshold when creating a binary image
		double mean = ImageStatistics.mean(input);

		// create a binary image by thresholding
		ThresholdImageOps.threshold(input,binary,(float)mean,true);

		// remove small blobs through erosion and dilation
		// The null in the input indicates that it should internally declare the work image it needs
		// this is less efficient, but easier to code.
		ImageUInt8 filtered = BinaryImageOps.erode8(binary, null);
		filtered = BinaryImageOps.dilate8(filtered, null);

		// Detect blobs inside the image using an 8-connect rule
		List<Contour> contours = BinaryImageOps.contour(filtered, 8, label);

		// colors of contours
		int colorExternal = 0xFFFFFF;
		int colorInternal = 0xFF2020;

		// Convert back to IDataset
		IDataset visualBinary = ConvertIDataset.convertTo(binary, true);
		IDataset visualFiltered = ConvertIDataset.convertTo(filtered, true);
		IDataset visualLabel = ConvertIDataset.convertTo(label, true);
		IDataset visualContour = ConvertIDataset.contourImageToIDataset(contours, colorExternal, colorInternal, input.width, input.height);

		visualBinary.setName("Binary Original");
		visualFiltered.setName("Binary Filtered");
		visualLabel.setName("Labeled Blobs");
		visualLabel.setName("Contours");

		Utils.showPlotView("uk.ac.diamond.scisoft.analysis.rcp.plotView1", "Plot 1", visualBinary);
		Utils.showPlotView("uk.ac.diamond.scisoft.analysis.rcp.plotView1", "Plot 1", visualFiltered);
		Utils.showPlotView("uk.ac.diamond.scisoft.analysis.rcp.plotView1", "Plot 1", visualLabel);
		Utils.showPlotView("uk.ac.diamond.scisoft.analysis.rcp.plotView1", "Plot 1", visualContour);
	}
}
