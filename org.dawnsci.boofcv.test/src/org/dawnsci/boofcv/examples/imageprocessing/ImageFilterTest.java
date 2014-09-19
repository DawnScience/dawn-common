/*
 * Copyright (c) 2014 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.boofcv.examples.imageprocessing;

import java.util.List;

import org.dawb.common.services.ServiceManager;
import org.dawnsci.boofcv.BoofCVImageFilterServiceCreator;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.image.IImageFilterService;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class ImageFilterTest {

	private IImageFilterService service;
	private String dataname = "image-01";
	private IDataHolder holder;
	private IDataset data;

	@BeforeClass
	public static void beforeClass() {
		ServiceManager.setService(IImageFilterService.class, BoofCVImageFilterServiceCreator.createFilterService());
	}

	@Before
	public void before() throws Exception {
		service = (IImageFilterService) ServiceManager.getService(IImageFilterService.class);
		holder = LoaderFactory.getData("resources/particles01.jpg", null);
		data = holder.getDataset(dataname);
	}

	@Test
	public void filterDerivativeSobel() {
		List<IDataset> derivatives = service.filterDerivativeSobel(data);
		Assert.assertEquals("Value of first item is not the expected one", -9, derivatives.get(0).getDouble(0), 0);
	}

	@Test
	public void filterGaussianBlur() {
		IDataset blurred = service.filterGaussianBlur(data, -1, 10);
		Assert.assertEquals("Value of first item is not the expected one", 67.10220336914062, blurred.getDouble(0), 0);
	}

	@Test
	public void filterThreshold() {
		IDataset thresholded = service.filterThreshold(data, 100, true, false);
		Assert.assertEquals("Value of first item is not the expected one", 0, thresholded.getDouble(0), 0);
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
	public void filterContour() {
		IDataset contoured = service.filterContour(data, 8, 0xFFFFFF, 0xFF2020);
		Assert.assertEquals("Value of item is not the expected one", 8224.0, contoured.getDouble(551, 384), 0);
	}
}
