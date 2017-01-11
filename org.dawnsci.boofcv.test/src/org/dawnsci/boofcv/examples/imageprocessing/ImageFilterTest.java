/*
 * Copyright (c) 2014 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.boofcv.examples.imageprocessing;

import org.dawnsci.boofcv.BoofCVImageFilterServiceCreator;
import org.eclipse.dawnsci.analysis.api.image.IImageFilterService;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.january.dataset.IDataset;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class ImageFilterTest {

	private IImageFilterService service;
	private String dataname = "image-01";
	private IDataHolder holder;
	private IDataset data;

	@Before
	public void before() throws Exception {
		if (service == null)
			service = BoofCVImageFilterServiceCreator.createFilterService();
		holder = LoaderFactory.getData("resources/particles01.jpg", null);
		data = holder.getDataset(dataname);
	}

	@Test
	public void filterDerivativeSobel() {
		IDataset derivativeX = service.filterDerivativeSobel(data, true);
		Assert.assertEquals("Value of first item is not the expected one", -9, derivativeX.getDouble(0, 0), 0);
	}

	@Test
	public void filterGaussianBlur() {
		IDataset blurred = service.filterGaussianBlur(data, -1, 10);
		Assert.assertEquals("Value of first item is not the expected one", 67.0, blurred.getDouble(0, 0), 0);
	}

	@Test
	public void filterThreshold() {
		IDataset thresholded = service.globalThreshold(data, 100, true, false);
		Assert.assertEquals("Value of first item is not the expected one", 0, thresholded.getDouble(0, 0), 0);
	}

	@Test
	public void filterErode() {
		IDataset eroded = service.filterErode(data, false);
		Assert.assertEquals("Value of item is not the expected one", 1.0, eroded.getDouble(46, 3), 0);
	}

	@Test
	public void filterErodeAndDilate() {
		IDataset erodedAndDilated = service.filterErodeAndDilate(data, false);
		Assert.assertEquals("Value of first item is not the expected one", 1.0, erodedAndDilated.getDouble(45, 2), 0);
	}


	@Test
	public void filterContour() throws Exception {
		IDataset thresholded = service.globalThreshold(data, 100, true, true);
		IDataset contoured = service.extractBlob(thresholded, 8);
		Assert.assertEquals("Value of item is not the expected one", 727, contoured.getDouble(479, 637), 0);
	}

	@Test
	public void testFirstValueOfThreshold() {
		IDataset thresholded = service.globalThreshold(data, 20, true, true);
		Assert.assertEquals("Value of first item is not the expected one", false, thresholded.getBoolean(0, 0));
	}

	@Test
	public void testFirstValueOfMeanThreshold() {
		IDataset thresholded = service.globalMeanThreshold(data, true, true);
		Assert.assertEquals("Value of first item is not the expected one", false, thresholded.getBoolean(0, 0));
	}

	@Test
	public void testFirstValueOfOtsuThreshold() {
		IDataset thresholded = service.globalOtsuThreshold(data, true, true);
		Assert.assertEquals("Value of first item is not the expected one", false, thresholded.getBoolean(0, 0));
	}

	@Test
	public void testFirstValueOfEntropyThreshold() {
		IDataset thresholded = service.globalEntropyThreshold(data, true, true);
		Assert.assertEquals("Value of first item is not the expected one", false, thresholded.getBoolean(0, 0));
	}

	@Test
	public void testFirstValueOfAdaptiveSquareThreshold() {
		IDataset thresholded = service.adaptiveSquareThreshold(data, 15, true, true);
		Assert.assertEquals("Value of first item is not the expected one", false, thresholded.getBoolean(0, 0));
	}

	@Test
	public void testFirstValueOfAdaptiveGaussianThreshold() {
		IDataset thresholded = service.adaptiveGaussianThreshold(data, 15, true, true);
		Assert.assertEquals("Value of first item is not the expected one", false, thresholded.getBoolean(0, 0));
	}

	@Test
	public void testFirstValueOfAdaptiveSauvolaThreshold() {
		IDataset thresholded = service.adaptiveSauvolaThreshold(data, 15, true, true);
		Assert.assertEquals("Value of first item is not the expected one", false, thresholded.getBoolean(0, 0));
	}
}
