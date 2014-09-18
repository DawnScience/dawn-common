/*-
 * Copyright (c) 2014 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.boofcv.converter;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSInt16;
import boofcv.struct.image.ImageSInt8;
import boofcv.struct.image.ImageUInt16;
import boofcv.struct.image.ImageUInt8;

public class IDatasetConverterTest {

	private String dataname = "image-01";
	private IDataHolder holder;
	private IDataset data;

	@Before
	public void before() throws Exception {
		holder = LoaderFactory.getData("resources/particles01.jpg", null);
		data = holder.getDataset(dataname);
	}

	@Test
	public void testInt8Conversion() throws Throwable {
		ImageUInt8 unsigned8 = (ImageUInt8) ConvertIDataset.convertFrom(data, ImageUInt8.class, true);
		IDataset result1 = ConvertIDataset.convertTo(unsigned8, false);
		result1.setName(dataname);
		Assert.assertTrue("Converted data is not the same rank as original", data.getRank() == result1.getRank());
		Assert.assertArrayEquals("Converted data is not the same shape as original", data.getShape(), result1.getShape());

		ImageSInt8 signed8 = ConvertIDataset.convertFrom(data, ImageSInt8.class, 1);
		IDataset result2 = ConvertIDataset.convertTo(signed8, false);
		result2.setName(dataname);
		Assert.assertTrue("Converted data is not the same rank as original", data.getRank() == result2.getRank());
		Assert.assertArrayEquals("Converted data is not the same shape as original", data.getShape(), result2.getShape());
	}

	@Test
	public void testInt16Conversion() throws Throwable {
		ImageUInt16 unsigned16 = ConvertIDataset.convertFrom(data, ImageUInt16.class, 1);
		IDataset result1 = ConvertIDataset.convertTo(unsigned16, false);
		result1.setName(dataname);
		Assert.assertTrue("Converted data is not the same rank as original", data.getRank() == result1.getRank());
		Assert.assertArrayEquals("Converted data is not the same shape as original", data.getShape(), result1.getShape());

		ImageSInt16 signed16 = ConvertIDataset.convertFrom(data, ImageSInt16.class, 1);
		IDataset result2 = ConvertIDataset.convertTo(signed16, false);
		result2.setName(dataname);
		Assert.assertTrue("Converted data is not the same rank as original", data.getRank() == result2.getRank());
		Assert.assertArrayEquals("Converted data is not the same shape as original", data.getShape(), result2.getShape());
	}

	@Test
	public void testFloat32Conversion() throws Throwable {
		ImageFloat32 float32 = (ImageFloat32) ConvertIDataset.convertFrom(data, ImageFloat32.class, 1);
		IDataset result = ConvertIDataset.convertTo(float32, false);
		result.setName(dataname);
		Assert.assertTrue("Converted data is not the same rank as original", data.getRank() == result.getRank());
		Assert.assertArrayEquals("Converted data is not the same shape as original", data.getShape(), result.getShape());
	}
}
