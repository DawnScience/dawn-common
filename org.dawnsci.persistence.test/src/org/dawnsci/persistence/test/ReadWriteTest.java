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

import org.dawnsci.persistence.PersistenceServiceCreator;
import org.dawnsci.persistence.ServiceLoader;
import org.dawnsci.persistence.internal.PersistenceConstants;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistentFile;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.junit.Before;
import org.junit.Test;

public class ReadWriteTest extends AbstractThreadTestBase {

	@Before
	public void init() {
		// Set factory for test
		ServiceLoader.setNexusFactory(new NexusFileFactoryHDF5());
	}

	@Test
	public void testReadWriteWithThreads(){
		try {
			super.testWithNThreads(3);
		} catch (Throwable e) {
			e.printStackTrace();
			fail("Exception occured while writing/reading data, rois and masks in threads");
		}
	}

	@Test
	public void testReadWriteVersionSite() throws Exception{
		File tmp = File.createTempFile("TestVersionSite", ".nxs");
		tmp.deleteOnExit();
		tmp.createNewFile();
		String version = PersistenceConstants.CURRENT_VERSION;
		String site = "Diamond Light Source";
		// create the PersistentService
		IPersistenceService persist = PersistenceServiceCreator.createPersistenceService();

		// create the persistent file and set the version/site
		IPersistentFile file = null;
		try {
			file = persist.createPersistentFile(tmp.getAbsolutePath());
			file.setSite(site);
		} finally {
			if (file != null)
				file.close();
		}

		String versionRead = "";
		String siteRead = "";
		// read the persistent file and retrieve the version/site
		try {
			file = persist.getPersistentFile(tmp.getAbsolutePath());
			versionRead = file.getVersion();
			siteRead = file.getSite();
		} finally {
			if (file != null)
				file.close();
		}
		assertEquals(version, versionRead);
		assertEquals(site, siteRead);
	}

	@Test
	public void testReadExceptionHDF5File() throws Exception{
		String path = "testfiles/MoKedge_1_15.nxs";

		// create the PersistentService
		IPersistenceService persist = PersistenceServiceCreator.createPersistenceService();
		// read the persistent file and retrieve the regions
		IPersistentFile file = null;
		try {
			file = persist.getPersistentFile(path);

			try {
				file.getData("data", null);
				fail("Reading Exception not caught");
			} catch (Exception e) {
				String classException = e.getClass().getName();
				assertTrue("Exception caught:" + classException, true);
			}

			try {
				file.getMasks(null);
				fail("Reading Exception not caught");
			} catch (Exception e) {
				String msg = e.getMessage();
				boolean resultException = msg.startsWith("Group ") && msg.contains(" does not exist ");
				if(resultException)
					assertTrue("Reading Exception caught", resultException);
				else
					assertTrue("Another exception was caught:"+ e.getMessage(), resultException);
			}

			try {
				file.getROIs(null);
				fail("Reading Exception not caught");
			} catch (Exception e) {
				String msg = e.getMessage();
				boolean resultException = msg.startsWith("Group ") && msg.contains(" does not exist ");
				if(resultException)
					assertTrue("Reading Exception caught", resultException);
				else
					assertTrue("Another exception was caught:"+ e.getMessage(), resultException);
			}
		} finally {
			if(file!= null)
				file.close();
		}
	}

	@Override
	protected void doTestOfDataSet(int index) throws Throwable {
		new ReadWriteROITest().testWriteReadROI();
		new ReadWriteMaskTest().testWriteReadMask();
		new ReadWriteDataTest().testWriteReadData();
	}
}
