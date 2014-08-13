package org.dawnsci.persistence.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dawb.common.services.IPersistenceService;
import org.dawb.common.services.IPersistentFile;
import org.dawnsci.persistence.PersistenceServiceCreator;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.dataset.Dataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetFactory;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;

public class ReadWriteDataTest extends AbstractThreadTestBase {

	@Test
	public void testWriteReadData(){
		try {
			final File tmp = File.createTempFile("TestData", ".nxs");
			tmp.deleteOnExit();
			tmp.createNewFile();
			
			// dataset
			Dataset  da = createTestData();
			List<IDataset> axes = createTestAxesData();

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
			assertEquals(da.getShape()[0], dataRead.getShape()[0]);
			assertEquals(da.getShape()[1], dataRead.getShape()[1]);
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
			Dataset da  = createTestData();
			List<IDataset> axes = createTestAxesData();
			
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
			assertEquals(da.getShape()[0], dataRead.getShape()[0]);
			assertEquals(da.getShape()[1], dataRead.getShape()[1]);
			assertEquals(axes.get(0).getName(), axesRead.get(0).getName());
			assertEquals(axes.get(1).getName(), axesRead.get(1).getName());
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
		testWriteReadData();
	}

	private Dataset createTestData(){
		// dataset
		final double[] random = new double[100];
		for (int i = 0; i < 100; i++) {
			random[i] = Math.random();
		}
		Dataset da = DatasetFactory.createRange(4194304,
				Dataset.FLOAT64);
		da.setName("data");
		da.setShape(2048, 2048);
		
		return da;
	}

	private List<IDataset> createTestAxesData(){
		final double[] axis = new double[2048];
		for (int i = 0; i < 2048; i++) {
			axis[i] = i;
		}
		DoubleDataset dx = new DoubleDataset(axis);
		dx.setName("X Axis");
		DoubleDataset dy = new DoubleDataset(axis);
		dy.setName("Y Axis");
		List<IDataset> axes = new ArrayList<IDataset>();
		axes.add(dx);
		axes.add(dy);
		return axes;
	}
}
