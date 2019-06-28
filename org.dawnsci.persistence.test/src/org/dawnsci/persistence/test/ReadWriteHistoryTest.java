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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.dawnsci.persistence.PersistenceServiceCreator;
import org.dawnsci.persistence.ServiceLoader;
import org.dawnsci.persistence.internal.PersistenceConstants;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistentFile;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.ILazyDataset;
import org.junit.Before;
import org.junit.Test;

public class ReadWriteHistoryTest extends AbstractThreadTestBase {

	@Before
	public void init() {
		// Set factory for test
		new ServiceLoader().setNexusFactory(new NexusFileFactoryHDF5());
	}

	public void after(File tmp, IPersistentFile file){
		if (tmp != null)
			tmp.deleteOnExit();

		if(file != null)
			file.close();
	}

	@Test
	public void testWriteReadHisto1D() throws Exception {
		try {
			// create and init files
			File tmp = File.createTempFile("TestHistory", ".nxs");
			tmp.createNewFile();
			tmp.deleteOnExit();
			// create the PersistentService
			// and check that ServiceLoader.getJSONMarshaller() != null
			if (ServiceLoader.getJSONMarshallerService() == null)
				new ServiceLoader().setJSONMarshallerService(new MarshallerService());
			IPersistenceService persist = PersistenceServiceCreator.createPersistenceService();
			IPersistentFile file = persist.createPersistentFile(tmp.getAbsolutePath());
			
			// dataset
			Dataset[]  da = new Dataset[]{createTestData(new int []{512}, 1), 
					                                      createTestData(new int []{256}, 2), 
					                                      createTestData(new int []{128}, 3)};

			try {
				file.setHistory(da);
			} catch (Exception e) {
				e.printStackTrace();
				fail("Exception occured while writing history:" + e.getMessage());
			} finally{
				if (file != null)
					file.close();
			}

			file = persist.createPersistentFile(tmp.getAbsolutePath());
			// read the persistent file and retrieve the regions
			Map<String, ILazyDataset> history = null;
			try {
				history = file.getHistory(new IMonitor.Stub());
				
			} catch (Exception e) {
				e.printStackTrace();
				fail("Exception occured while reading history:" + e.getMessage());
			} finally{
				if (file != null)
					file.close();
			}

			// test that the data/axes are the same
			for (int index = 1; index<=3; index++) {
				ILazyDataset la = history.get("/entry/history/history"+index);
				if (!Arrays.equals(da[index-1].getShape(), la.getShape())) {
					throw new Exception("Incorrect shape read back!");
				}
				index++;
			}

		} catch (IOException e1) {
			e1.printStackTrace();
			fail("IOException occured while creating test file");
		}
	}

	@Test
	public void testWriteReadHistoImage() throws Exception {
		// create and init files
		File tmp = File.createTempFile("TestHistory", ".nxs");
		tmp.createNewFile();
		tmp.deleteOnExit();
		// create the PersistentService
		// and check that ServiceLoader.getJSONMarshaller() != null
		if (ServiceLoader.getJSONMarshallerService() == null)
			new ServiceLoader().setJSONMarshallerService(new MarshallerService());
		IPersistenceService persist = PersistenceServiceCreator.createPersistenceService();
		IPersistentFile file = persist.createPersistentFile(tmp.getAbsolutePath());
		try {
			// dataset
			Dataset  da = createTestData(new int[]{2048, 2048});

			try {
				file.setHistory(da);
			} catch (Exception e) {
				e.printStackTrace();
				fail("Exception occured while writing the data/axes");
			}

			// read the persistent file and retrieve the regions
			ILazyDataset dataRead = null;
			try {
				Map<String, ILazyDataset> history = file.getHistory(new IMonitor.Stub());
				dataRead = history.values().iterator().next();
				
			} catch (Exception e) {
				e.printStackTrace();
				fail("Exception occured while reading the data/axes");
			}

			// test that the data/axes are the same
			assertEquals(da.getName(), dataRead.getName());
			if (!Arrays.equals(da.getShape(), dataRead.getShape())) {
				throw new Exception("Incorrect shape read back!");
			}

		} catch (IOException e1) {
			e1.printStackTrace();
			fail("IOException occured while creating test file");
		} finally{
			if (file != null)
				file.close();
		}
	}

	@Test
	public void testReWriteHisto() throws Exception {
		try {
			// create and init files
			File tmp = File.createTempFile("TestHistory", ".nxs");
			tmp.createNewFile();
			tmp.deleteOnExit();
			// create the PersistentService
			// and check that ServiceLoader.getJSONMarshaller() != null
			if (ServiceLoader.getJSONMarshallerService() == null)
				new ServiceLoader().setJSONMarshallerService(new MarshallerService());
			IPersistenceService persist = PersistenceServiceCreator.createPersistenceService();
			IPersistentFile file = persist.createPersistentFile(tmp.getAbsolutePath());
			// dataset
			Dataset da  = createTestData(new int[]{2048, 2048});

			try {
				file.setHistory(da);
			} catch (Exception e) {
				e.printStackTrace();
				fail("Exception occured while writing history:" + e.getMessage());
			} finally{
				if (file!= null)
					file.close();
			}

			file = persist.createPersistentFile(tmp.getAbsolutePath());
			// rewrite the persistent file
			try {
				file.setHistory(da);
				
			} catch (Exception e) {
				assertTrue(e.getMessage().startsWith("Object already exists at specified location"));
			} finally{
				try {
					// change name
					da.setName(da.getName() + "0");
					// write
					file.setHistory(da);
				} catch (Exception e) {
					fail("Exception occured while writing history:" + e.getMessage());
				} finally {
					if (file != null)
						file.close();
				}
			}

			file = persist.createPersistentFile(tmp.getAbsolutePath());
			// read the persistent file and retrieve the regions
			ILazyDataset dataRead = null;
  		    try {
				Map<String, ILazyDataset> history = file.getHistory(new IMonitor.Stub());
				dataRead = history.get(PersistenceConstants.HISTORY_ENTRY + "/" + da.getName());

			} catch (Exception e) {
				e.printStackTrace();
				fail("Exception occured while reading the data/axes");
			} finally{
				if (file != null)
					file.close();
			}

			// test that the data/axes are the same
			assertEquals(da.getName(), dataRead.getName());
			if (!Arrays.equals(da.getShape(), dataRead.getShape())) {
				throw new Exception("Incorrect shape read back!");
			}

		} catch (IOException e1) {
			e1.printStackTrace();
			fail("IOException occured while creating the test file");
		}
	}

	@Test
	public void testReadWriteWithThreads(){
		try {
			super.testWithNThreads(10);
		} catch (Throwable e) {
			e.printStackTrace();
			fail("Exception occured while writing/reading dataset in threads");
		}
	}

	@Override
	protected void doTestOfDataSet(int index) throws Throwable {
		testWriteReadHistoImage();
	}

	private Dataset createTestData(int[] shape){
        return createTestData(shape, -1);
	}
	private Dataset createTestData(int[] shape, int index){
		// dataset
		final double[] random = new double[100];
		for (int i = 0; i < 100; i++) {
			random[i] = Math.random();
		}
		int size = 1;
		for (int d : shape) size *= d;
		
		Dataset da = DatasetFactory.createRange(size);
		if (index>-1) {
			da.setName("history"+index);
		} else {
			da.setName("history");
		}
		da.setShape(shape);
		
		return da;
	}
}
