package org.dawnsci.persistence.test;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import org.dawb.common.services.IPersistenceService;
import org.dawb.common.services.IPersistentFile;
import org.dawnsci.persistence.PersistenceServiceCreator;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.diffraction.DetectorProperties;
import org.eclipse.dawnsci.analysis.api.diffraction.DiffractionCrystalEnvironment;
import org.eclipse.dawnsci.analysis.api.diffraction.IPowderCalibrationInfo;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.junit.Assert;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.DiffractionMetadata;

public class ReadWritePowderCalibration {

	@Test
	public void testWriteReadMeta() {

		try {
			final File tmp = File.createTempFile("TestPowderCal", ".nxs");
			//tmp.deleteOnExit();
			tmp.createNewFile();

			IDiffractionMetadata metaIn = new DiffractionMetadata(null, 
					DetectorProperties.getDefaultDetectorProperties(new int[]{1000,1500}),
					DiffractionCrystalEnvironment.getDefaultDiffractionCrystalEnvironment());
			metaIn.getDetector2DProperties().setNormalAnglesInDegrees(new double[]{3,4,5});
			
			Matrix3d orient = metaIn.getDetector2DProperties().getOrientation();
			Vector3d origin = metaIn.getDetector2DProperties().getOrigin();
			
			metaIn.getDetector2DProperties().getNormalAnglesInDegrees();

			// create the PersistentService
			IPersistenceService persist = PersistenceServiceCreator.createPersistenceService();

			IDataset ds = DatasetFactory.ones(new int[] {1000, 1500}, Dataset.INT16);
			
			IPowderCalibrationInfo info = new IPowderCalibrationInfo() {
				
				@Override
				public IDataset getUsedDSpaceIndexValues() {
					return DatasetUtils.linSpace(0, 3, 4, Dataset.INT16);
				}
				
				@Override
				public String getMethodDescription() {
					return "We basically made these values up";
				}
				
				@Override
				public String getDetectorName() {
					return "IAmPilatus";
				}
				
				@Override
				public String getCalibrationImagePath() {
					return "/Some/Random/image.tiff";
				}
				
				@Override
				public String getCalibrantName() {
					return "TestCalibrant";
				}
				
				@Override
				public IDataset getCalibrantDSpaceValues() {
					return DatasetUtils.linSpace(1, 4, 4, Dataset.INT16);
				}

				@Override
				public double getResidual() {
					return 0;
				}

				@Override
				public String[] getCitationInformation() {
					return new String[]{"description", "doi","endnote","bibtex"};
				}
			};
			
			//create the persistent file and set meta
			IPersistentFile file = null;
			try {
				file = persist.createPersistentFile(tmp.getAbsolutePath());
				file.setPowderCalibrationInformation(ds, metaIn, info);
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
				metaOut = file.getDiffractionMetadata(null);
				
				if (metaOut == null) {
					Assert.fail("Persistance of meta data returns null");
					return;
				}
				
				Matrix3d orientO = metaOut.getDetector2DProperties().getOrientation();
				Vector3d originO = metaOut.getDetector2DProperties().getOrigin();
				
				double[] inOt = new double[3];
				double[] outOt = new double[3];
				double[] inOn =  new double[3];
				double[] outOn = new double[3];
				
				origin.get(inOn);
				originO.get(outOn);
				
				Assert.assertArrayEquals(inOn, outOn, 0.000001);
				
				orient.getRow(0, inOt);
				orientO.getRow(0, outOt);
				Assert.assertArrayEquals(inOt, outOt, 0.000001);
				orient.getRow(1, inOt);
				orientO.getRow(1, outOt);
				Assert.assertArrayEquals(inOt, outOt, 0.000001);
				orient.getRow(2, inOt);
				orientO.getRow(2, outOt);
				Assert.assertArrayEquals(inOt, outOt, 0.000001);
				
			} catch (Exception e) {
				e.printStackTrace();
				fail("Exception occured while reading diffraction metadata");
			} finally {
				file.close();
			}
			

		} catch (IOException e1) {
			e1.printStackTrace();
			fail("IOException occured while creating the test file");
		}
	}
	
}
