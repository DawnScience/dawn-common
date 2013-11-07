package org.dawnsci.persistence.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.dawb.common.services.IPersistenceService;
import org.dawb.common.services.IPersistentFile;
import org.dawnsci.persistence.PersistenceServiceCreator;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.roi.CircularROI;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

public class ReadWriteROITest extends AbstractThreadTestBase {

	@Test
	public void testWriteReadROI(){
		
		try {
			final File tmp = File.createTempFile("TestRoi", ".nxs");
			tmp.deleteOnExit();
			tmp.createNewFile();
			
			//regions
			RectangularROI rroi = new RectangularROI(0, 0, 100, 200, 0);
			CircularROI croi = new CircularROI(50, 100, 100);
			Map<String, IROI> rois = new HashMap<String, IROI>();
			rois.put("rectangle0", rroi);
			rois.put("circle0", croi);
			
			// create the PersistentService
			IPersistenceService persist = PersistenceServiceCreator.createPersistenceService();
			
			//create the persistent file and set rois
			IPersistentFile file = null;
			try {
				file = persist.createPersistentFile(tmp.getAbsolutePath());
				file.setROIs(rois);
			} catch (Exception e){
				e.printStackTrace();
				fail("Exception occured while writing ROis");
			} finally {
				if(file != null)
					file.close();
			}

			Map<String, IROI> roisRead = null;
			//read the persistent file and retrieve the regions
			try {
				file = persist.getPersistentFile(tmp.getAbsolutePath());
				roisRead = file.getROIs(null);
			} catch (Exception e) {
				e.printStackTrace();
				fail("Exception occured while reading ROis");
			} finally {
				file.close();
			}

			//test that the rois are the same
			if(roisRead != null){
				assertEquals(rois.containsKey("rectangle0"), roisRead.containsKey("rectangle0"));
				assertEquals(rois.containsKey("circle0"), roisRead.containsKey("circle0"));
			} else {
				fail("ROIs read are Null.");
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
			fail("Exception occured while writing/reading ROIs in threads");
		}
	}

	@Override
	protected void doTestOfDataSet(int index) throws Throwable {
		testWriteReadROI();
	}
}
