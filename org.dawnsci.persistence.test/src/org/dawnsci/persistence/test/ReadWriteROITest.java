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
import java.util.HashMap;
import java.util.Map;

import org.dawb.common.services.IPersistenceService;
import org.dawb.common.services.IPersistentFile;
import org.dawnsci.persistence.PersistenceServiceCreator;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.junit.Test;

public class ReadWriteROITest extends AbstractThreadTestBase {

	//Do not put the annotation as the files needs to be created and closed after each test
	//so it can run with the thread tests
	//Passes value by array
	public IPersistentFile before(File[] tmp) throws Exception {

		tmp[0] = File.createTempFile("TestRoi", ".txt");
		tmp[0].createNewFile();

		// create the PersistentService
		IPersistenceService persist = PersistenceServiceCreator.createPersistenceService();
		IPersistentFile file = persist.createPersistentFile(tmp[0].getAbsolutePath());
		return file;
	}

	public void after(File tmp, IPersistentFile file){
		if (tmp != null)
			tmp.deleteOnExit();

		if(file != null)
			file.close();
	}

	@Test
	public void testWriteReadRectangularROI() throws Exception {
		//create and init files
		File[] tmp = new File[1];
		IPersistentFile file = before(tmp);

		IROI rroi = new RectangularROI();
		Map<String, IROI> rois = new HashMap<String, IROI>();
		rois.put("MyROI", rroi);
		file.setROIs(rois);

		Map<String, IROI> roisRead = null;
		//read the persistent file and retrieve the functions
		roisRead = file.getROIs(null);
	
		//test that the rois are the same
		assertEquals(rois.containsKey("MyROI"), roisRead.containsKey("MyROI"));

		//test the unmarshalling of the JSON String
		IROI resultROI = roisRead.get("MyROI");
		
		assertEquals(rroi, resultROI);

		//close files
		after(tmp[0], file);
	}

	@Test
	public void testWriteReadROI() throws Exception{
		//create and init files
		File[] tmp = new File[1];
		IPersistentFile file = before(tmp);
		// regions
		RectangularROI rroi = new RectangularROI(0, 0, 100, 200, 0);
		CircularROI croi = new CircularROI(50, 100, 100);
		Map<String, IROI> rois = new HashMap<String, IROI>();
		rois.put("rectangle0", rroi);
		rois.put("circle0", croi);

		// create the persistent file and set rois
		file.setROIs(rois);


		Map<String, IROI> roisRead = null;
		// read the persistent file and retrieve the regions
		roisRead = file.getROIs(null);

		// test that the rois are the same
		if (roisRead != null) {
			assertEquals(rois.containsKey("rectangle0"),
					roisRead.containsKey("rectangle0"));
			assertEquals(rois.containsKey("circle0"),
					roisRead.containsKey("circle0"));
		} else {
			fail("ROIs read are Null.");
		}

		//close files
		after(tmp[0], file);
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
