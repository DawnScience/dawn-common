/*
 * Copyright (c) 2012-2016 Diamond Light Source Ltd.
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

import org.dawnsci.persistence.PersistenceServiceCreator;
import org.dawnsci.persistence.ServiceLoader;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistentFile;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RingROI;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
import org.eclipse.dawnsci.json.MarshallerService;
import org.junit.Test;

public class ReadWriteROITest extends AbstractThreadTestBase {

	//Do not put the annotation as the files needs to be created and closed after each test
	//so it can run with the thread tests
	//Passes value by array
	public IPersistentFile before(File[] tmp) throws Exception {

		tmp[0] = File.createTempFile("TestRoi", ".txt");
		tmp[0].createNewFile();

		// create the PersistentService
		// and check that ServiceLoader.getJSONMarshaller() != null
		if (ServiceLoader.getJSONMarshallerService() == null)
			ServiceLoader.setJSONMarshallerService(new MarshallerService());
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
		RectangularROI rroi = new RectangularROI(0, 0, 100, 200, 0);
		rroi.setName("MyRectangularROI");
		testWriteReadROI(rroi);
	}

	@Test 
	public void testWriteReadCircularROI() throws Exception {
		CircularROI croi = new CircularROI(50, 100, 100);
		croi.setName("MyCircularROI");
		testWriteReadROI(croi);
	}

	@Test
	public void testWriteReadSectorROI() throws Exception {
		SectorROI sroi = new SectorROI();
		sroi.setDpp(10);
		sroi.setPoint(new double[] {100, 100});
		sroi.setName("MySectorROI");
		testWriteReadROI(sroi);
	}

	@Test
	public void testWriteReadRingROI() throws Exception {
		RingROI rroi = new RingROI();
		rroi.setPoint(new double[] { 100, 100 });
		rroi.setRadii(new double[] { 20, 30 });
		rroi.setName("MyRingROI");
		testWriteReadROI(rroi);
	}

	@Test 
	public void testWriteReadLinearROI() throws Exception {
		LinearROI lroi = new LinearROI(new double[]{10, 15}, new double[]{150, 200});
		lroi.setName("MyLinearROI");
		testWriteReadROI(lroi);
	}

	private void testWriteReadROI(IROI roi) throws Exception {
		// create and init files
		File[] tmp = new File[1];
		IPersistentFile file = before(tmp);

		
		Map<String, IROI> rois = new HashMap<String, IROI>();
		rois.put(roi.getName(), roi);
		file.setROIs(rois);

		Map<String, IROI> roisRead = null;
		// read the persistent file and retrieve the functions
		roisRead = file.getROIs(null);

		// test that the rois are the same
		assertEquals(rois.containsKey(roi.getName()), roisRead.containsKey(roi.getName()));

		// test the unmarshalling of the JSON String
		IROI resultROI = roisRead.get(roi.getName());

		assertEquals(roi, resultROI);

		// close files
		after(tmp[0], file);
	}

	/**
	 * Run all writeRead of tested ROIs
	 * 
	 * @throws Exception
	 */
	public void testWriteReadROI() throws Exception {
		testWriteReadLinearROI();
		testWriteReadCircularROI();
		testWriteReadRectangularROI();
		testWriteReadRingROI();
		testWriteReadSectorROI();
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
		testWriteReadRectangularROI();
	}
}
