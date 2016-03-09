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
import java.io.IOException;

import org.dawnsci.persistence.PersistenceServiceCreator;
import org.eclipse.dawnsci.analysis.api.diffraction.DetectorProperties;
import org.eclipse.dawnsci.analysis.api.diffraction.DiffractionCrystalEnvironment;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistentFile;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.DiffractionMetadata;

public class ReadWriteDiffractionTest extends AbstractThreadTestBase {

	@Ignore
	@Test
	public void testWriteReadMeta() {

		try {
			final File tmp = File.createTempFile("TestMask", ".nxs");
			tmp.deleteOnExit();
			tmp.createNewFile();

			IDiffractionMetadata metaIn = new DiffractionMetadata(null, 
					DetectorProperties.getDefaultDetectorProperties(new int[]{1000,1500}),
					DiffractionCrystalEnvironment.getDefaultDiffractionCrystalEnvironment());

			// create the PersistentService
			IPersistenceService persist = PersistenceServiceCreator.createPersistenceService();

			//create the persistent file and set meta
			IPersistentFile file = null;
			try {
				file = persist.createPersistentFile(tmp.getAbsolutePath());
//				file.setDiffractionMetadata(metaIn);
			} catch (Exception e){
				e.printStackTrace();
				fail("Exception occured while writing diffraction metadata");
			} finally {
				if(file != null)
					file.close();
			}
			
			//do the rewrite
			try {
				file = persist.createPersistentFile(tmp.getAbsolutePath());
//				file.setDiffractionMetadata(metaIn);
			} catch (Exception e){
				e.printStackTrace();
				fail("Exception occured while writing diffraction metadata");
			} finally {
				if(file != null)
					file.close();
			}

			IDiffractionMetadata metaOut = null;
			try {
				file = persist.getPersistentFile(tmp.getAbsolutePath());
//				metaOut = file.getDiffractionMetadata(null);
			} catch (Exception e) {
				e.printStackTrace();
				fail("Exception occured while reading diffraction metadata");
			} finally {
				file.close();
			}

			//test that the rois are the same
			if(metaOut != null && metaOut.getDiffractionCrystalEnvironment() != null &&
					metaOut.getDetector2DProperties() != null){

				assertEquals(metaIn.getDiffractionCrystalEnvironment().getWavelength(),
						metaOut.getDiffractionCrystalEnvironment().getWavelength(),0.0);
				assertEquals(metaIn.getDetector2DProperties().getBeamCentreDistance(),
						metaOut.getDetector2DProperties().getBeamCentreDistance(),0.0);
				assertEquals(metaIn.getDetector2DProperties().getBeamCentreCoords()[0],
						metaOut.getDetector2DProperties().getBeamCentreCoords()[0],0.0);
				assertEquals(metaIn.getDetector2DProperties().getBeamCentreCoords()[1],
						metaOut.getDetector2DProperties().getBeamCentreCoords()[1],0.0);
				assertEquals(metaIn.getDetector2DProperties().getNormalAnglesInDegrees()[0],
						metaIn.getDetector2DProperties().getNormalAnglesInDegrees()[0],0.0);
				assertEquals(metaIn.getDetector2DProperties().getNormalAnglesInDegrees()[1],
						metaIn.getDetector2DProperties().getNormalAnglesInDegrees()[1],0.0);
				assertEquals(metaIn.getDetector2DProperties().getNormalAnglesInDegrees()[2],
						metaIn.getDetector2DProperties().getNormalAnglesInDegrees()[2],0.0);

			} else {
				fail("Metadata read is Null.");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			fail("IOException occured while creating the test file");
		}
	}
	
	@Ignore
	@Test
	public void testReWriteData(){ 
		try {
			final File tmp = File.createTempFile("TestMask", ".nxs");
			tmp.deleteOnExit();
			tmp.createNewFile();

			IDiffractionMetadata metaIn = new DiffractionMetadata(null, 
					DetectorProperties.getDefaultDetectorProperties(new int[]{1000,1500}),
					DiffractionCrystalEnvironment.getDefaultDiffractionCrystalEnvironment());

			// create the PersistentService
			IPersistenceService persist = PersistenceServiceCreator.createPersistenceService();

			//create the persistent file and set meta
			IPersistentFile file = null;
			try {
				file = persist.createPersistentFile(tmp.getAbsolutePath());
//				file.setDiffractionMetadata(metaIn);
			} catch (Exception e){
				e.printStackTrace();
				fail("Exception occured while writing diffraction metadata");
			} finally {
				if(file != null)
					file.close();
			}
			
			//create the persistent file and set meta again
			try {
				file = persist.createPersistentFile(tmp.getAbsolutePath());
//				file.setDiffractionMetadata(metaIn);
			} catch (Exception e){
				e.printStackTrace();
				fail("Exception occured while writing diffraction metadata");
			} finally {
				if(file != null)
					file.close();
			}

			IDiffractionMetadata metaOut = null;
			try {
				file = persist.getPersistentFile(tmp.getAbsolutePath());
//				metaOut = file.getDiffractionMetadata(null);
			} catch (Exception e) {
				e.printStackTrace();
				fail("Exception occured while reading diffraction metadata");
			} finally {
				file.close();
			}

			//test that the rois are the same
			if(metaOut != null && metaOut.getDiffractionCrystalEnvironment() != null &&
					metaOut.getDetector2DProperties() != null){

				assertEquals(metaIn.getDiffractionCrystalEnvironment().getWavelength(),
						metaOut.getDiffractionCrystalEnvironment().getWavelength(),0.0);
				assertEquals(metaIn.getDetector2DProperties().getBeamCentreDistance(),
						metaOut.getDetector2DProperties().getBeamCentreDistance(),0.0);
				assertEquals(metaIn.getDetector2DProperties().getBeamCentreCoords()[0],
						metaOut.getDetector2DProperties().getBeamCentreCoords()[0],0.0);
				assertEquals(metaIn.getDetector2DProperties().getBeamCentreCoords()[1],
						metaOut.getDetector2DProperties().getBeamCentreCoords()[1],0.0);
				assertEquals(metaIn.getDetector2DProperties().getNormalAnglesInDegrees()[0],
						metaIn.getDetector2DProperties().getNormalAnglesInDegrees()[0],0.0);
				assertEquals(metaIn.getDetector2DProperties().getNormalAnglesInDegrees()[1],
						metaIn.getDetector2DProperties().getNormalAnglesInDegrees()[1],0.0);
				assertEquals(metaIn.getDetector2DProperties().getNormalAnglesInDegrees()[2],
						metaIn.getDetector2DProperties().getNormalAnglesInDegrees()[2],0.0);

			} else {
				fail("Metadata read is Null.");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			fail("IOException occured while creating the test file");
		}
	}
	
	@Ignore
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
		testWriteReadMeta();
		
	}

}
