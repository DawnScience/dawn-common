package org.dawnsci.persistence.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.dawb.common.services.IPersistenceService;
import org.dawb.common.services.IPersistentFile;
import org.dawnsci.persistence.PersistenceServiceCreator;
import org.dawnsci.persistence.internal.PersistenceConstants;
import org.junit.Test;

public class ReadWriteTest extends AbstractThreadTestBase {

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
	public void testReadWriteVersionSite(){
		
		try {
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
			} catch (Exception e) {
				e.printStackTrace();
				fail("Exception occured while writing the version/site");
			} finally {
				if (file!= null)
					file.close();
			}
			
			String versionRead = "";
			String siteRead = "";
			// read the persistent file and retrieve the version/site
			try {
				file = persist.getPersistentFile(tmp.getAbsolutePath());
				String str = file.getVersion();
				versionRead = str.substring(1, str.length()-1); //we get rid of the first and last character
				str = file.getSite();
				siteRead = str.substring(1, str.length()-1); //we get rid of the first and last character
			} catch (Exception e) {
				e.printStackTrace();
				fail("Exception occured while reading the version/site");
			}finally{
				if (file != null)
					file.close();
			}
			assertEquals(version, versionRead);
			assertEquals(site, siteRead);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException occured while creationg test file");;
		}
	}

	@Test
	public void testReadExceptionHDF5File(){
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
				boolean resultException = e.getMessage().startsWith("Reading Exception: ");
				if(resultException)
					assertTrue("Reading Exception caught", resultException);
				else
					assertTrue("Another exception was caught:"+ e.getMessage(), resultException);
			}

			try {
				file.getMasks(null);
				fail("Reading Exception not caught");
			} catch (Exception e) {
				boolean resultException = e.getMessage().startsWith("Reading Exception: ");
				if(resultException)
					assertTrue("Reading Exception caught", resultException);
				else
					assertTrue("Another exception was caught:"+ e.getMessage(), resultException);
			}

			try {
				file.getROIs(null);
				fail("Reading Exception not caught");
			} catch (Exception e) {
				boolean resultException = e.getMessage().startsWith("Reading Exception: ");
				if(resultException)
					assertTrue("Reading Exception caught", resultException);
				else
					assertTrue("Another exception was caught:"+ e.getMessage(), resultException);
			}
			
		} catch (Exception e1) {
			fail("Could not find test file");
			e1.printStackTrace();
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
