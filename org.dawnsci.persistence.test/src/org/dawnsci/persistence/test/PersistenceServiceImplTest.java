package org.dawnsci.persistence.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawb.common.services.IPersistenceService;
import org.dawb.common.services.IPersistentFile;
import org.dawnsci.persistence.PersistenceServiceCreator;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.roi.CircularROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

public class PersistenceServiceImplTest extends AbstractThreadTest {

	@Test
	public void testWriteReadROI(){
		
		try {
			final File tmp = File.createTempFile("TestRoi", ".nxs");
			tmp.deleteOnExit();
			tmp.createNewFile();
			
			//regions
			RectangularROI rroi = new RectangularROI(0, 0, 100, 200, 0);
			CircularROI croi = new CircularROI(50, 100, 100);
			Map<String, ROIBase> rois = new HashMap<String, ROIBase>();
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

			Map<String, ROIBase> roisRead = null;
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
	public void testWriteReadMask(){
		try {
			final File tmp = File.createTempFile("TestMask", ".nxs");
			tmp.deleteOnExit();
			tmp.createNewFile();
			
			// masks
			boolean[] bd0 = { false, true, false, true, false, true, false, true,
					true, true, false, true };
			boolean[] bd1 = { true, true, true, true, false, true, false, true,
					false, true, false, false };
			boolean[] bd2 = { false, false, true, true, false, true, false, true,
					false, true, true, true };
			BooleanDataset mask0 = new BooleanDataset(bd0);
			BooleanDataset mask1 = new BooleanDataset(bd1);
			BooleanDataset mask2 = new BooleanDataset(bd2);
			Map<String, BooleanDataset> masks = new HashMap<String, BooleanDataset>();
			masks.put("mask3", mask0);
			masks.put("mask4", mask1);
			
			// create the PersistentService
			IPersistenceService persist = PersistenceServiceCreator.createPersistenceService();
			//create the persistent file
			IPersistentFile file = null;
			try{
				file = persist.createPersistentFile(tmp.getAbsolutePath());
				//overwrite mask1 with mask2
				file.addMask("mask0", mask0, null);
				file.addMask("mask1", mask1, null);
				file.addMask("mask1", mask2, null);
				// add another mask
				file.addMask("mask2", mask2, null);
				// add a set of masks
				file.setMasks(masks);

			}catch(Exception e){
				e.printStackTrace();
				fail("Exception occured while writing Masks");
			}finally{
				if (file != null)
					file.close();
			}
			
			//read the persistent file and retrieve the regions
			IPersistentFile fileReader = null;
			Map<String, BooleanDataset> masksRead = null;
			try{
				fileReader = persist.getPersistentFile(tmp.getAbsolutePath());
				masksRead = fileReader.getMasks(null);
			}catch(Exception e){
				e.printStackTrace();
				fail("Exception occured while reading Masks");
			}finally{
				if(fileReader != null)
					fileReader.close();
			}

			//test that masks are saved in the file
			if(masksRead != null){
				assertTrue(masksRead.containsKey("mask0"));
				assertTrue(masksRead.containsKey("mask1"));
				assertTrue(masksRead.containsKey("mask2"));
				
				//check that the rewriting did work
				boolean[] resultData = masksRead.get("mask1").getData();
				for (int i = 0; i < resultData.length; i++) {
					assertEquals(bd2[i], resultData[i]);
				}
			} else {
				fail("ROIs read are Null.");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			fail("IOException occured while creating test file");
		}
	}

	@Test
	public void testWriteReadData(){
		try {
			final File tmp = File.createTempFile("TestData", ".nxs");
			tmp.deleteOnExit();
			tmp.createNewFile();
			
			// dataset
			AbstractDataset da = createTestData();
			List<AbstractDataset> axes = createTestAxesData();

			// create the PersistentService
			IPersistenceService persist = PersistenceServiceCreator
					.createPersistenceService();

			// create the persistent file
			IPersistentFile file = null;
			try {
				file = persist.createPersistentFile(tmp.getAbsolutePath());
				file.setData(da);
				file.setAxes(axes);
			} catch (Exception e) {
				e.printStackTrace();
				fail("Exception occured while writing the data/axes");
			} finally{
				if(file!= null)
					file.close();
			}

			// read the persistent file and retrieve the regions
			ILazyDataset dataRead = null;
			List<ILazyDataset> axesRead = null;
			try {
				file = persist.getPersistentFile(tmp.getAbsolutePath());
				dataRead = file.getData("data", null);
				axesRead = file.getAxes("X Axis", "Y Axis", null);
			} catch (Exception e) {
				e.printStackTrace();
				fail("Exception occured while reading the data/axes");
			} finally{
				if(file!= null)
					file.close();
			}

			// test that the data/axes are the same
			assertEquals(da.getName(), dataRead.getName());
			assertEquals(axes.get(0).getName(), axesRead.get(0).getName());
			assertEquals(axes.get(1).getName(), axesRead.get(1).getName());
		} catch (IOException e1) {
			e1.printStackTrace();
			fail("IOException occured while creating test file");
		}
	}

	@Test
	public void testReWriteData(){
		try {
			final File tmp = File.createTempFile("TestData", ".nxs");
			tmp.deleteOnExit();
			tmp.createNewFile();
			
			// dataset
			AbstractDataset da = createTestData();
			List<AbstractDataset> axes = createTestAxesData();
			
			// create the PersistentService
			IPersistenceService persist = PersistenceServiceCreator
					.createPersistenceService();

			// create the persistent file
			IPersistentFile file = null;
			try {
				file = persist.createPersistentFile(tmp.getAbsolutePath());
				file.setData(da);
				file.setAxes(axes);
			} catch (Exception e) {
				e.printStackTrace();
				fail("Exception occured while writing the data/axes");
			} finally{
				if(file!= null)
					file.close();
			}

			// rewrite the persistent file
			try {
				file = persist.createPersistentFile(tmp.getAbsolutePath());
				file.setData(da);
				file.setAxes(axes);
			} catch (Exception e) {
				e.printStackTrace();
				fail("Exception occured while rewriting the data/axes");
			} finally{
				if(file!= null)
					file.close();
			}

			// read the persistent file and retrieve the regions
			ILazyDataset dataRead = null;
			List<ILazyDataset> axesRead = null;
			try {
				file = persist.getPersistentFile(tmp.getAbsolutePath());
				dataRead = file.getData("data", null);
				axesRead = file.getAxes("X Axis", "Y Axis", null);
			} catch (Exception e) {
				e.printStackTrace();
				fail("Exception occured while reading the data/axes");
			} finally{
				if(file!= null)
					file.close();
			}
			
			file.close();

			// test that the data/axes are the same
			assertEquals(da.getName(), dataRead.getName());
			assertEquals(axes.get(0).getName(), axesRead.get(0).getName());
			assertEquals(axes.get(1).getName(), axesRead.get(1).getName());
		} catch (IOException e1) {
			e1.printStackTrace();
			fail("IOException occured while creating the test file");
		}
	}

	@Test
	public void testReadWriteVersionSite(){
		
		try {
			File tmp = File.createTempFile("TestVersionSite", ".nxs");
			tmp.deleteOnExit();
			tmp.createNewFile();
			String version = "1.0";
			String site = "Diamond Light Source";
			// create the PersistentService
			IPersistenceService persist = PersistenceServiceCreator.createPersistenceService();

			// create the persistent file and set the version/site
			IPersistentFile file = null;
			try {
				file = persist.createPersistentFile(tmp.getAbsolutePath());
				file.setSite(site);
				file.setVersion(version);
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
		testWriteReadROI();
		testWriteReadMask();
		testWriteReadData();
	}

	@Test
	public void testReadExceptionHDF5File(){
		String path = Hdf5TestUtils.getAbsolutePath("src/org/dawnsci/persistence/test/MoKedge_1_15.nxs");

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

	private AbstractDataset createTestData(){
		// dataset
		final double[] random = new double[100];
		for (int i = 0; i < 100; i++) {
			random[i] = Math.random();
		}
		AbstractDataset da = AbstractDataset.arange(4194304,
				AbstractDataset.FLOAT64);
		da.setName("data");
		da.setShape(2048, 2048);
		
		return da;
	}

	private List<AbstractDataset> createTestAxesData(){
		final double[] axis = new double[2048];
		for (int i = 0; i < 2048; i++) {
			axis[i] = i;
		}
		DoubleDataset dx = new DoubleDataset(axis);
		dx.setName("X Axis");
		DoubleDataset dy = new DoubleDataset(axis);
		dy.setName("Y Axis");
		List<AbstractDataset> axes = new ArrayList<AbstractDataset>();
		axes.add(dx);
		axes.add(dy);
		return axes;
	}
}