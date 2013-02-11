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
			
			//create the persistent file
			IPersistentFile file = persist.createPersistentFile(tmp.getAbsolutePath());
			file.setROIs(rois);
			file.close();
			
			//read the persistent file and retrieve the regions
			file = persist.getPersistentFile(tmp.getAbsolutePath());
			Map<String, ROIBase> roisRead = file.getROIs(null);
			file.close();
			
			//test that the rois are the same
			if(roisRead != null){
				assertEquals(rois.containsKey("rectangle0"), roisRead.containsKey("rectangle0"));
				assertEquals(rois.containsKey("circle0"), roisRead.containsKey("circle0"));
			} else {
				fail("ROIs read are Null.");
			}
		
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			fail("IOException occured while writing/reading ROis");
		} catch (Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Exception occured while writing/reading ROis");
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
			BooleanDataset mask0 = new BooleanDataset(bd0);
			BooleanDataset mask1 = new BooleanDataset(bd1);
			Map<String, BooleanDataset> masks = new HashMap<String, BooleanDataset>();
			masks.put("mask0", mask0);
			masks.put("mask1", mask1);
			
			// create the PersistentService
			IPersistenceService persist = PersistenceServiceCreator.createPersistenceService();
			
			//create the persistent file
			IPersistentFile file = persist.createPersistentFile(tmp.getAbsolutePath());
			file.setMasks(masks);
			file.close();
			
			//read the persistent file and retrieve the regions
			file = persist.getPersistentFile(tmp.getAbsolutePath());
			Map<String, BooleanDataset> masksRead = file.getMasks(null);
			file.close();
			
			//test that the masks are the same
			if(masksRead != null){
				assertEquals(masks.containsKey("mask0"), masksRead.containsKey("mask0"));
				assertEquals(masks.containsKey("mask1"), masksRead.containsKey("mask1"));
			} else {
				fail("ROIs read are Null.");
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			fail("IOException occured while writing/reading Masks");
		} catch (Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Exception occured while writing/reading Masks");
		}
	}

	@Test
	public void testWriteReadData(){
		try {
			final File tmp = File.createTempFile("TestData", ".nxs");
			tmp.deleteOnExit();
			tmp.createNewFile();
			
			// dataset
			final double[] random = new double[100];
			for (int i = 0; i < 100; i++) {
				random[i] = Math.random();
			}
			final double[] axis = new double[2048];
			for (int i = 0; i < 2048; i++) {
				axis[i] = i;
			}
			AbstractDataset da = AbstractDataset.arange(4194304,
					AbstractDataset.FLOAT64);
			da.setName("data");
			da.setShape(2048, 2048);
			DoubleDataset dx = new DoubleDataset(axis);
			dx.setName("X Axis");
			DoubleDataset dy = new DoubleDataset(axis);
			dy.setName("Y Axis");
			List<AbstractDataset> axes = new ArrayList<AbstractDataset>();
			axes.add(dx);
			axes.add(dy);

			// create the PersistentService
			IPersistenceService persist = PersistenceServiceCreator
					.createPersistenceService();

			// create the persistent file
			IPersistentFile file = persist.createPersistentFile(tmp.getAbsolutePath());
			file.setData(da);
			file.setAxes(axes);
			file.close();

			// read the persistent file and retrieve the regions
			file = persist.getPersistentFile(tmp.getAbsolutePath());
			ILazyDataset dataRead = file.getData("data", null);
			List<ILazyDataset> axesRead = file.getAxes("X Axis", "Y Axis", null);
			file.close();

			// test that the data/axes are the same
			assertEquals(da.getName(), dataRead.getName());
			assertEquals(axes.get(0).getName(), axesRead.get(0).getName());
			assertEquals(axes.get(1).getName(), axesRead.get(1).getName());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			fail("IOException occured while writing/reading Masks");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Exception occured while writing/reading Masks");
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
				assertEquals(true, resultException);
				if(!resultException)
					fail("Another error occured");
			}

			try {
				file.getMasks(null);
				fail("Reading Exception not caught");
			} catch (Exception e) {
				boolean resultException = e.getMessage().startsWith("Reading Exception: ");
				assertEquals(true, resultException);
				if(!resultException)
					fail("Another error occured");
			}

			try {
				file.getROIs(null);
				fail("Reading Exception not caught");
			} catch (Exception e) {
				boolean resultException = e.getMessage().startsWith("Reading Exception: ");
				assertEquals(true, resultException);
				if(!resultException)
					fail("Another error occured");
			}
			
		} catch (Exception e1) {
			fail("Could not find test file");
			e1.printStackTrace();
		} finally {
			if(file!= null)
				file.close();
		}
	}

}
