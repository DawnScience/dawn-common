package org.dawnsci.persistence.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.dawb.common.services.IPersistenceService;
import org.dawb.common.services.IPersistentFile;
import org.dawnsci.persistence.PersistenceServiceCreator;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;

public class ReadWriteHistoryTest extends AbstractThreadTestBase {

	@Test
	public void testWriteReadHisto1D() throws Exception {
		try {
			final File tmp = File.createTempFile("TestData", ".nxs");
			tmp.deleteOnExit();
			tmp.createNewFile();
			
			// dataset
			AbstractDataset[]  da = new AbstractDataset[]{createTestData(new int []{512}, 1), 
					                                      createTestData(new int []{256}, 2), 
					                                      createTestData(new int []{128}, 3)};

			// create the PersistentService
			IPersistenceService persist = PersistenceServiceCreator.createPersistenceService();

			// create the persistent file
			IPersistentFile file = null;
			try {
				file = persist.createPersistentFile(tmp.getAbsolutePath());
				file.setHistory(da);
			} catch (Exception e) {
				e.printStackTrace();
				fail("Exception occured while writing the data/axes");
			} finally{
				if(file!= null)
					file.close();
			}

			// read the persistent file and retrieve the regions
			Map<String, ILazyDataset> history = null;
			try {
				file = persist.getPersistentFile(tmp.getAbsolutePath());
				history = file.getHistory(new IMonitor.Stub());
				
			} catch (Exception e) {
				e.printStackTrace();
				fail("Exception occured while reading the data/axes");
			} finally{
				if(file!= null)
					file.close();
			}

			// test that the data/axes are the same
			for (int index = 1; index<=3; index++) {
				ILazyDataset la = history.get("/entry/history/history"+index);
				if (!Arrays.equals(da[index-1].getShape(), la.getShape())) {
					throw new Exception("Incorrect shape read back!");
				}
				index++;
			}

		} catch (IOException e1) {
			e1.printStackTrace();
			fail("IOException occured while creating test file");
		}
	}

	@Test
	public void testWriteReadHistoImage() throws Exception {
		try {
			final File tmp = File.createTempFile("TestData", ".nxs");
			tmp.deleteOnExit();
			tmp.createNewFile();
			
			// dataset
			AbstractDataset  da = createTestData(new int[]{2048, 2048});

			// create the PersistentService
			IPersistenceService persist = PersistenceServiceCreator.createPersistenceService();

			// create the persistent file
			IPersistentFile file = null;
			try {
				file = persist.createPersistentFile(tmp.getAbsolutePath());
				file.setHistory(da);
			} catch (Exception e) {
				e.printStackTrace();
				fail("Exception occured while writing the data/axes");
			} finally{
				if(file!= null)
					file.close();
			}

			// read the persistent file and retrieve the regions
			ILazyDataset dataRead = null;
			try {
				file = persist.getPersistentFile(tmp.getAbsolutePath());
				Map<String, ILazyDataset> history = file.getHistory(new IMonitor.Stub());
				dataRead = history.values().iterator().next();
				
			} catch (Exception e) {
				e.printStackTrace();
				fail("Exception occured while reading the data/axes");
			} finally{
				if(file!= null)
					file.close();
			}

			// test that the data/axes are the same
			assertEquals(da.getName(), dataRead.getName());
			if (!Arrays.equals(da.getShape(), dataRead.getShape())) {
				throw new Exception("Incorrect shape read back!");
			}

		} catch (IOException e1) {
			e1.printStackTrace();
			fail("IOException occured while creating test file");
		}
	}

	@Test
	public void testReWriteHisto() throws Exception {
		try {
			final File tmp = File.createTempFile("TestData", ".nxs");
			tmp.deleteOnExit();
			tmp.createNewFile();
			
			// dataset
			AbstractDataset da  = createTestData(new int[]{2048, 2048});
			
			// create the PersistentService
			IPersistenceService persist = PersistenceServiceCreator.createPersistenceService();

			// create the persistent file
			IPersistentFile file = null;
			try {
				file = persist.createPersistentFile(tmp.getAbsolutePath());
				file.setHistory(da);
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
				file.setHistory(da);
				
			} catch (Exception e) {
				e.printStackTrace();
				fail("Exception occured while rewriting the data/axes");
			} finally{
				if(file!= null)
					file.close();
			}

			// read the persistent file and retrieve the regions
			ILazyDataset dataRead = null;
  		    try {
				file = persist.getPersistentFile(tmp.getAbsolutePath());
				Map<String, ILazyDataset> history = file.getHistory(new IMonitor.Stub());
				dataRead = history.values().iterator().next();

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
			if (!Arrays.equals(da.getShape(), dataRead.getShape())) {
				throw new Exception("Incorrect shape read back!");
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
			fail("Exception occured while writing/reading dataset in threads");
		}
	}

	@Override
	protected void doTestOfDataSet(int index) throws Throwable {
		testWriteReadHistoImage();
	}

	private AbstractDataset createTestData(int[] shape){
        return createTestData(shape, -1);
	}
	private AbstractDataset createTestData(int[] shape, int index){
		// dataset
		final double[] random = new double[100];
		for (int i = 0; i < 100; i++) {
			random[i] = Math.random();
		}
		int size = 1;
		for (int d : shape) size *= d;
		
		AbstractDataset da = AbstractDataset.arange(size, AbstractDataset.FLOAT64);
		if (index>-1) {
			da.setName("history"+index);
		} else {
			da.setName("history");
		}
		da.setShape(shape);
		
		return da;
	}
}
