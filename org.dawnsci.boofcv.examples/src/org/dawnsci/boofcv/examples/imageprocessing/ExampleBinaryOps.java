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

import java.util.List;

import org.dawnsci.boofcv.converter.Converter;
import org.dawnsci.boofcv.examples.util.Utils;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.io.IDataHolder;
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
		IDataset data = holder.getDataset(dataname);
		Utils.showPlotView("uk.ac.diamond.scisoft.analysis.rcp.plotViewDP", "Dataset Plot", data);

		// convert into BoofCV format
		Converter cvt = new Converter();
		ImageFloat32 input = cvt.convertFrom(data);
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
		IDataset visualBinary = cvt.convertTo(binary, true);
		IDataset visualFiltered = cvt.convertTo(filtered, true);
		IDataset visualLabel = cvt.imageToIDataset(label, contours.size());
		IDataset visualContour = cvt.contourImageToIDataset(contours, colorExternal, colorInternal, input.width, input.height);
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
