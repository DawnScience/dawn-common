/*
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dawnsci.boofcv.BoofCVImageFilterServiceCreator;
import org.dawnsci.boofcv.BoofCVImageTransformCreator;
import org.dawnsci.boofcv.examples.util.Utils;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.api.dataset.Slice;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.api.image.IImageFilterService;
import org.eclipse.dawnsci.analysis.api.image.IImageTransform;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.dataset.impl.AbstractDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.LazyDataset;
import org.eclipse.dawnsci.hdf5.HDF5Utils;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.ImageStackLoader;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class ImageLazyProcessingTest {

	private IImageFilterService service;
	private ILazyDataset data;
	private IImageTransform transform;

	@Before
	public void before() throws Exception {
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
		String name = "test";
		ILazyWriteableDataset lazy = createTempLazyFile(data, newShape, name);
		ILazyDataset rotatedImages = null;
		try {
			for (int i = 0; i < shape[0]; i++) {
				IDataset slice = data.getSlice(new Slice(i, shape[0], shape[1])).squeeze();
				IDataset rotated = DatasetUtils.convertToDataset(transform.rotate(slice, 45, false));
				// add rotated image to temp file
				appendDataset(lazy, rotated, i, null);
			}
		} finally {
			// read from temp file
			rotatedImages = getTempLazyData(name);
		}
		assertArrayEquals(newShape, rotatedImages.getShape());
		assertEquals(newShape[2], rotatedImages.getShape()[2]);
		IDataset rotatedImage = rotatedImages.getSlice(new Slice(10, shape[0], shape[1])).squeeze();
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

	/**
	 * Method that creates an hdf5 file in the temp directory of the OS
	 * 
	 * @param data
	 * @param name
	 * @return lazy writable dataset on disk
	 */
	private ILazyWriteableDataset createTempLazyFile(ILazyDataset data, int[] newShape, String name) {
		// save on a temp file
		String filepath = System.getProperty("java.io.tmpdir") + File.separator;
		String nodepath = "/entry/data/";
		String file = filepath + "tmp_" + name + ".h5";
		File tmpFile = new File(file);
		if (tmpFile.exists())
			tmpFile.delete();
		IDataset slice = data.getSlice(new Slice(0, data.getShape()[0], data.getShape()[1])).squeeze();
		int dtype = AbstractDataset.getDType(slice);
		return HDF5Utils.createLazyDataset(file, nodepath, name, newShape, null, newShape, dtype, null, false);
	}

	/**
	 * Method that reads a temporary hdf5 file on disk
	 * 
	 * @param name
	 * @return lazydataset
	 * @throws Exception 
	 */
	private ILazyDataset getTempLazyData(String name) throws Exception {
		String filepath = System.getProperty("java.io.tmpdir") + File.separator;
		String nodepath = "/entry/data/";
		String file = filepath + "tmp_" + name + ".h5";
		IDataHolder holder = LoaderFactory.getData(file, false, true, null);
		ILazyDataset shifted = holder.getLazyDataset(nodepath + name);
		return shifted;
	}

	/**
	 * Method that appends a dataset to an existing lazy writable dataset
	 * 
	 * @param lazy
	 * @param data
	 * @param idx
	 * @param monitor
	 * @throws Exception
	 */
	private void appendDataset(ILazyWriteableDataset lazy, IDataset data, int idx, IMonitor monitor) throws Exception {
		SliceND ndSlice = new SliceND(lazy.getShape(), new int[] { idx, 0, 0 },
				new int[] { (idx + 1), data.getShape()[0], data.getShape()[1] }, null);
		lazy.setSlice(monitor, data, ndSlice);
	}
}
