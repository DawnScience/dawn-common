/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.persistence.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dawnsci.persistence.PersistenceServiceCreator;
import org.dawnsci.persistence.ServiceLoader;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistentFile;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.junit.Before;
import org.junit.Test;

public class ReadWriteDataTest extends AbstractThreadTestBase {

	@Before
	public void init() {
		// Set factory for test
		new ServiceLoader().setNexusFactory(new NexusFileFactoryHDF5());
	}

	// Do not put the annotation as the files needs to be created and closed
	// after each test
	// so it can run with the thread tests
	// Passes value by array
	public IPersistentFile before(File[] tmp) throws Exception {
		// Set factory for test
		new ServiceLoader().setNexusFactory(new NexusFileFactoryHDF5());

		tmp[0] = File.createTempFile("TestData", ".nxs");
		tmp[0].createNewFile();

		// create the PersistentService
		IPersistenceService persist = PersistenceServiceCreator.createPersistenceService();
		IPersistentFile file = persist.createPersistentFile(tmp[0].getAbsolutePath());
		return file;
	}

	public void after(File tmp, IPersistentFile file) {
		if (tmp != null)
			tmp.deleteOnExit();
		if (file != null)
			file.close();
	}

	@Test
	public void testWriteReadData() throws Exception {
		// create and init files
		File[] tmp = new File[1];
		IPersistentFile file = before(tmp);

		// dataset
		Dataset da = createTestData();
		List<IDataset> axes = createTestAxesData();

		// create the PersistentService
		IPersistenceService persist = PersistenceServiceCreator.createPersistenceService();

		try {
			file = persist.createPersistentFile(tmp[0].getAbsolutePath());
			file.setData(da);
			file.setAxes(axes);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception occured while writing the data/axes");
		}

		// read the persistent file and retrieve the data
		ILazyDataset dataRead = null;
		List<ILazyDataset> axesRead = null;
		try {
			file = persist.getPersistentFile(tmp[0].getAbsolutePath());
			dataRead = file.getData("data", null);
			axesRead = file.getAxes("X Axis", "Y Axis", null);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception occured while reading the data/axes");
		}

		// test that the data/axes are the same
		assertEquals(da.getShape()[0], dataRead.getShape()[0]);
		assertEquals(da.getShape()[1], dataRead.getShape()[1]);
		assertEquals(axes.get(0).getName(), axesRead.get(0).getName());
		assertEquals(axes.get(1).getName(), axesRead.get(1).getName());

		// close files
		after(tmp[0], file);
	}

	@Test
	public void testReWriteData() throws Exception {
		// create and init files
		File[] tmp = new File[1];
		IPersistentFile file = before(tmp);

		// dataset
		Dataset da = createTestData();
		List<IDataset> axes = createTestAxesData();

		// create the PersistentService
		IPersistenceService persist = PersistenceServiceCreator.createPersistenceService();

		try {
			file = persist.createPersistentFile(tmp[0].getAbsolutePath());
			file.setData(da);
			file.setAxes(axes);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception occured while writing the data/axes");
		}

		// write the persistent file with a new data name
		try {
			file = persist.createPersistentFile(tmp[0].getAbsolutePath());
			da.setName("data1");
			file.setData(da);
			
			file.setAxes(axes);
		} catch (Exception e) {
			assertEquals("Object already exists at specified location", e.getMessage());
		}

		// read the persistent file and retrieve the regions
		ILazyDataset dataRead = null;
		List<ILazyDataset> axesRead = null;
		try {
			file = persist.getPersistentFile(tmp[0].getAbsolutePath());
			dataRead = file.getData("data", null);
			axesRead = file.getAxes("X Axis", "Y Axis", null);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception occured while reading the data/axes");
		}

		// test that the data/axes are the same
		assertEquals(da.getShape()[0], dataRead.getShape()[0]);
		assertEquals(da.getShape()[1], dataRead.getShape()[1]);
		assertEquals(axes.get(0).getName(), axesRead.get(0).getName());
		assertEquals(axes.get(1).getName(), axesRead.get(1).getName());

		// close files
		after(tmp[0], file);
	}

	@Test
	public void testReadWriteWithThreads() {
		try {
			super.testWithNThreads(10);
		} catch (Throwable e) {
			e.printStackTrace();
			fail("Exception occured while writing/reading dataset in threads");
		}
	}

	@Override
	protected void doTestOfDataSet(int index) throws Throwable {
		testWriteReadData();
	}

	private Dataset createTestData() {
		// dataset
		final double[] random = new double[100];
		for (int i = 0; i < 100; i++) {
			random[i] = Math.random();
		}
		Dataset da = DatasetFactory.createRange(4194304, Dataset.FLOAT64);
		da.setName("data");
		da.setShape(2048, 2048);

		return da;
	}

	private List<IDataset> createTestAxesData() {
		final double[] axis = new double[2048];
		for (int i = 0; i < 2048; i++) {
			axis[i] = i;
		}
		Dataset dx = DatasetFactory.createFromObject(axis);
		dx.setName("X Axis");
		Dataset dy = DatasetFactory.createFromObject(axis);
		dy.setName("Y Axis");
		List<IDataset> axes = new ArrayList<IDataset>();
		axes.add(dx);
		axes.add(dy);
		return axes;
	}
}
