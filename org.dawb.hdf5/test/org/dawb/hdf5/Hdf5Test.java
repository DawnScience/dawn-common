/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.hdf5;

import java.io.File;
import java.util.Random;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.h5.H5Datatype;

import org.junit.Test;


public class Hdf5Test {
	
	private static String FILENAME = "dset.h5";
	private static String DATASETNAME = "dset";
	private static final int DIM_X = 4;
	private static final int DIM_Y = 6;
	
	@Test
	public void writeH5Example() throws Exception {
		
		IHierarchicalDataFile file = null;
		try {
			
			final File tmp = File.createTempFile("Test", "h5");
			tmp.deleteOnExit();
			tmp.createNewFile();
			
			// Throws exception if already writing to this file
			// on another thread. 
			// writeH5Example is synchronized to here to avoid this write
			// (but not others!) interfering.
			file = HierarchicalDataFactory.getWriter(tmp.getAbsolutePath());
					
			final Group myDataGroup = file.group("myData");
			
			final double[] random = new double[2048*2048];
			for (int i = 0; i < 2048*2048; i++) {
				random[i] = Math.random();
			}
			Dataset s = file.createDataset("data", 
					new H5Datatype(Datatype.CLASS_FLOAT, 64/8, Datatype.NATIVE, Datatype.NATIVE), 
					new long[]{2048,2048},
					random, 
					myDataGroup);
			file.setNexusAttribute(s, Nexus.SDS);
			file.close();
			
			file = HierarchicalDataFactory.getReader(tmp.getAbsolutePath());
			final Dataset set = (Dataset)file.getData("myData/data");
			
			set.getMetadata();
			
			final long[] size = set.getDims();
			if (size[0]!=2048 || size[1]!=2048) throw new Exception("Unexpected read data set size!");
			
			final double[] data = (double[])set.getData();
			
			for (int i = 0; i < data.length; i++) {
				if (data[i]!=random[i]) throw new Exception("Unexpected data value!");
			}
			
		} finally {
			if (file!=null) file.close();
		}
	}

	@Test
	public void testOpenFile() throws Exception {
		

		int id = -1;
		try {
			final String path = Hdf5TestUtils.getAbsolutePath("test/org/dawb/hdf5/FeKedge_1_15.nxs");
			id = H5.H5Fopen(path, 
				            HDF5Constants.H5F_ACC_RDWR,
					        HDF5Constants.H5P_DEFAULT);
			
			
			if (id<0) throw new Exception("Cannot open hdf5 file!");
		} finally {
			if (id>-1) H5.H5Fclose(id);
		}
		
		
	}
	
	@Test
	public void testData() throws Exception {
		getData(Hdf5TestUtils.getAbsolutePath("test/org/dawb/hdf5/FeKedge_1_15.nxs"));
	}
	
	private void getData(String path) throws Exception {
		
		IHierarchicalDataFile testFile = null;

		try {
			// open the file and retrieve the file structure
			testFile = HierarchicalDataFactory.getReader(path);
			final Dataset set = (Dataset)testFile.getData("/entry1/counterTimer01/lnI0It");
			final Object  dat = set.read();
			final double[] da = (double[])dat;
			if (da.length<100) throw new Exception("Did not get data from node lnI0It");
		} finally {
			// close file resource
			if (testFile!=null) testFile.close();
		}
	}

	@Test
	public void testShowStructure() throws Exception {
		printStructure(Hdf5TestUtils.getAbsolutePath("test/org/dawb/hdf5/FeKedge_1_15.nxs"));
		//printStructure(/buffer/linpickard1/results/examples/DCT_201006-good.h5");
		//printStructure("/buffer/linpickard1/results/large test files/rhodo_f2_datanorm_.h5");
	}
	
	public void printStructure(final String path) throws Exception {

		IHierarchicalDataFile testFile = null;

		try {
			// open the file and retrieve the file structure
			testFile = HierarchicalDataFactory.getReader(path);
			final Group root = testFile.getRoot();			
			if (root == null) throw new Exception("Did not get root!");
			
			testFile.print();
			
		} finally {
			// close file resource
			if (testFile!=null) testFile.close();
		}

	}

	
	//@Test
	public void testReadWriteDataset() {
		
		int file_id = -1;
		int dataset_id = -1;
		int[][] dset_data = new int[DIM_X][DIM_Y];

		// Initialize the dataset.
		for (int indx = 0; indx < DIM_X; indx++)
			for (int jndx = 0; jndx < DIM_Y; jndx++)
				dset_data[indx][jndx] = indx * 6 + jndx + 1;

		// Open an existing file.
		try {
			file_id = H5.H5Fopen(FILENAME, HDF5Constants.H5F_ACC_RDWR,
					HDF5Constants.H5P_DEFAULT);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		// Open an existing dataset.
		try {
			if (file_id >= 0)
				dataset_id = H5.H5Dopen(file_id, "/" + DATASETNAME);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		// Write the dataset.
		try {
			if (dataset_id >= 0)
				H5.H5Dwrite(dataset_id, HDF5Constants.H5T_NATIVE_INT,
						HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL,
						HDF5Constants.H5P_DEFAULT, dset_data);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		try {
			if (dataset_id >= 0)
				H5.H5Dread(dataset_id, HDF5Constants.H5T_NATIVE_INT,
						HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL,
						HDF5Constants.H5P_DEFAULT, dset_data);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		// Close the dataset.
		try {
			if (dataset_id >= 0)
				H5.H5Dclose(dataset_id);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		// Close the file.
		try {
			if (file_id >= 0)
				H5.H5Fclose(file_id);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}