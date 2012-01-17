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

import ncsa.hdf.object.FileFormat;

public class HierarchicalDataFactory {

	public static boolean canOpen(final String absolutePath) throws Exception {
		return HierarchicalDataFile.isWriting(absolutePath);
	}
	
	/**
	 * Call this method to get a reference to a HierarchicalDataFile
	 * opened for reading use.
	 * 
	 * @param absolutePath
	 * @return
	 * @throws Exception
	 */
	public static IHierarchicalDataFile getReader(final String absolutePath) throws Exception {
		return HierarchicalDataFile.open(absolutePath, FileFormat.READ);
	}
	
	
	/**
	 * Call this method to get a reference to a HierarchicalDataFile
	 * opened for writing use.
	 * 
	 * @param absolutePath
	 * @return
	 * @throws Exception
	 */
	public static IHierarchicalDataFile getWriter(final String absolutePath) throws Exception {
		if (!(new File(absolutePath)).exists()) {
			create(absolutePath);
		}
		return HierarchicalDataFile.open(absolutePath, FileFormat.WRITE);
	}
	
	/**
	 * Call this method to get a reference to a *new* HierarchicalDataFile.
	 * 
	 * Do 
	 * 
	 * @param absolutePath
	 * @return
	 * @throws Exception
	 */
	public static void create(final String absolutePath) throws Exception {
		IHierarchicalDataFile file = HierarchicalDataFile.open(absolutePath, FileFormat.CREATE);
		file.close();
	}


}
