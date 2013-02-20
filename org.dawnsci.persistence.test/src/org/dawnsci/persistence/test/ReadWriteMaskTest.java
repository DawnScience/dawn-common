package org.dawnsci.persistence.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.dawb.common.services.IPersistenceService;
import org.dawb.common.services.IPersistentFile;
import org.dawnsci.persistence.PersistenceServiceCreator;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;

public class ReadWriteMaskTest extends AbstractThreadTest{
	
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
	public void testReadWriteWithThreads(){
		try {
			super.testWithNThreads(10);
		} catch (Throwable e) {
			e.printStackTrace();
			fail("Exception occured while writing/reading masks in threads");
		}
	}

	@Override
	protected void doTestOfDataSet(int index) throws Throwable {
		testWriteReadMask();
	}
}