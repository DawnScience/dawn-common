/*-
 * Copyright (c) 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.boofcv.examples.imageprocessing;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.boofcv.BoofCVImageFilterServiceCreator;
import org.dawnsci.boofcv.BoofCVImageTransformCreator;
import org.dawnsci.boofcv.examples.util.Utils;
import org.eclipse.dawnsci.analysis.api.image.IImageFilterService;
import org.eclipse.dawnsci.analysis.api.image.IImageTransform;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.LazyDataset;
import org.eclipse.january.dataset.Slice;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.ImageStackLoader;

public class ImageLazyProcessingTest {

	private IImageFilterService service;
	private ILazyDataset data;
	private IImageTransform transform;

	@Before
	public void init() throws Exception {
		if (service == null)
			service = BoofCVImageFilterServiceCreator.createFilterService();
		String[] files = Utils.getFileNames("resources/82702_UViewImage", true);
		List<String> filenames = new ArrayList<String>();
		Utils.getArrayAsList(files, filenames);
		ImageStackLoader loader = new ImageStackLoader(filenames, null);
		data = new LazyDataset("test stack", loader.getDtype(), loader.getShape(), loader);
		transform = BoofCVImageTransformCreator.createTransformService();
	}

	@Test
	public void testLazyRotation() throws Exception {
		int[] shape = data.getShape();
		// rotated shape
		int[] newShape = new int[] {data.getShape()[0], 724, 724};
		String name = "testLazyRotation.h5";
		ILazyWriteableDataset lazy = TestUtils.createTempLazyFile(newShape, name);
		for (int i = 0; i < shape[0]; i++) {
			IDataset slice = data.getSlice(new Slice(i, shape[0], shape[1])).squeeze();
			IDataset rotated = DatasetUtils.convertToDataset(transform.rotate(slice, 45, false));
			// add rotated image to temp file
			TestUtils.appendDataset(lazy, rotated, i, null);
		}
		assertArrayEquals(newShape, lazy.getShape());
		assertEquals(newShape[2], lazy.getShape()[2]);
		IDataset rotatedImage = lazy.getSlice(new Slice(10, shape[0], shape[1])).squeeze();
		assertEquals(newShape[2], rotatedImage.getShape()[1]);
	}

	@Test
	public void testLazyAlignment() throws Exception {
		int[] shape = data.getShape();
		ILazyDataset alignedImages = null;
		// read from temp file
		alignedImages =transform.align(data, null);
		assertArrayEquals(shape, alignedImages.getShape());
		assertEquals(shape[2], alignedImages.getShape()[2]);
	}

}
