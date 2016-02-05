/*-
 * Copyright (c) 2014 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.boofcv.examples.imageprocessing;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.boofcv.BoofCVImageStitchingProcessCreator;
import org.dawnsci.boofcv.BoofCVImageTransformCreator;
import org.dawnsci.boofcv.examples.util.ImageLoaderJob;
import org.dawnsci.boofcv.examples.util.Utils;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.image.IImageStitchingProcess;
import org.eclipse.dawnsci.analysis.api.image.IImageTransform;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.junit.Assert;
import org.junit.Test;

public class ImageStitchingPluginTest {

	@Test
	public void testImageStitchingProcess() throws Throwable {
		List<String> filenames = new ArrayList<String>();
		String[] files = Utils.getFileNames("resources/82702_UViewImage", true);
		Utils.getArrayAsList(files, filenames);
		List<IDataset> data = loadData(filenames);

		IImageStitchingProcess stitcher = BoofCVImageStitchingProcessCreator.createStitchingProcess();
		IImageTransform transform = BoofCVImageTransformCreator.createTransformService();
		List<IDataset> rotated = new ArrayList<IDataset>(data.size());
		for (int i = 0; i < data.size(); i++) {
			rotated.add(transform.rotate(data.get(i), 45, true));
		}
		IDataset stitched = stitcher.stitch(rotated, 3, 11, 80, new IMonitor.Stub());
		IDataset plottedStitched = ExampleImageStitching.showStitchedImage(stitched);
		Assert.assertEquals(stitched.getShape()[0], plottedStitched.getShape()[0]);
		Assert.assertEquals(stitched.getShape()[1], plottedStitched.getShape()[1]);
		Assert.assertTrue("the Stiched image does not have the expected width of 1108", 1108 == plottedStitched.getShape()[0]);
		Assert.assertTrue("the Stiched image does not have the expected height of 2428", 2428 == plottedStitched.getShape()[1]);

	}

	private List<IDataset> loadData(List<String> filenames) {
		ImageLoaderJob imageLoader = new ImageLoaderJob(filenames);
		imageLoader.schedule();
		try {
			imageLoader.join();
			return imageLoader.getData();
		} catch (InterruptedException e) {
			System.err.println("Error loading the images" + e.getMessage());
			return null;
		}
	}
}
