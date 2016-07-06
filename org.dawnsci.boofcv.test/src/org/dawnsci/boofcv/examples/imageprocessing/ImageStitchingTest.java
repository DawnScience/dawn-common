/*-
 * Copyright (c) 2014-2016 Diamond Light Source Ltd.
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
import org.dawnsci.boofcv.examples.util.Utils;
import org.eclipse.dawnsci.analysis.api.image.IImageStitchingProcess;
import org.eclipse.dawnsci.analysis.api.image.IImageTransform;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.LazyDataset;
import org.eclipse.january.dataset.Slice;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.ImageStackLoader;

public class ImageStitchingTest {

	private ILazyDataset data;
	private IImageStitchingProcess stitcher;
	private IImageTransform transform;

	@Before
	public void init() throws Exception {
		List<String> filenames = new ArrayList<String>();
		String[] files = Utils.getFileNames("resources/82702_UViewImage", true);
		Utils.getArrayAsList(files, filenames);
		ImageStackLoader loader = new ImageStackLoader(filenames, null);
		data = new LazyDataset("test stack", loader.getDtype(), loader.getShape(), loader);
		stitcher = BoofCVImageStitchingProcessCreator.createStitchingProcess();
		transform = BoofCVImageTransformCreator.createTransformService();
	}

	@Test
	public void testImageStitchingProcess() throws Throwable {
		int[] shape = data.getShape();
		// rotated shape
		int[] newShape = new int[] {data.getShape()[0], 512, 512};
		String name = "testImageStitchingProcess.h5";
		ILazyWriteableDataset lazy = TestUtils.createTempLazyFile(newShape, name);
		for (int i = 0; i < shape[0]; i++) {
			IDataset slice = data.getSlice(new Slice(i, shape[0], shape[1])).squeeze();
			IDataset rotated = DatasetUtils.convertToDataset(transform.rotate(slice, 45, true));
			// add rotated image to temp file
			TestUtils.appendDataset(lazy, rotated, i, null);
		}
		IDataset stitched = stitcher.stitch(lazy, 3, 11, 80, new IMonitor.Stub());
		Assert.assertTrue("Expected value is 1268, Actual value is " + stitched.getShape()[0], 1268 == stitched.getShape()[0]);
		Assert.assertTrue("Expected value is 4180, Actual value is " + stitched.getShape()[1], 4180 == stitched.getShape()[1]);
	}

	@Test
	public void testTwoImagesStitching() throws Throwable {
		IDataset image1 = data.getSlice(new Slice(0, data.getShape()[0], data.getShape()[1])).squeeze();
		IDataset image2 = data.getSlice(new Slice(1, data.getShape()[0], data.getShape()[1])).squeeze();
		// translation along the x axis
		double[] translationX = new double[] {0, 512};
		IDataset stitched = stitcher.stitch(image1, image2, translationX);
		Assert.assertEquals(1024, stitched.getShape()[0]);
		Assert.assertEquals(512, stitched.getShape()[1]);

		// translation along the y axis
		double[] translationY = new double[] {512, 0};
		IDataset stitched2 = stitcher.stitch(image1, image2, translationY);
		Assert.assertEquals(512, stitched2.getShape()[0]);
		Assert.assertEquals(1024, stitched2.getShape()[1]);

		// translation along the x and y axes
		double[] translationXY = new double[] { 512, 512 };
		IDataset stitched3 = stitcher.stitch(image1, image2, translationXY);
		Assert.assertEquals(1024, stitched3.getShape()[0]);
		Assert.assertEquals(1024, stitched3.getShape()[1]);
	}
}
