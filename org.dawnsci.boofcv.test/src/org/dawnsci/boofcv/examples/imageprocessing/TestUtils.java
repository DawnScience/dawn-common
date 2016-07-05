/*-
 * Copyright (c) 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.boofcv.examples.imageprocessing;

import java.io.File;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.hdf5.HDF5FileFactory;
import org.eclipse.dawnsci.hdf5.HDF5Utils;

import uk.ac.diamond.scisoft.analysis.utils.FileUtils;

public class TestUtils {

	/**
	 * Method that appends a dataset to an existing lazy writable dataset
	 * 
	 * @param lazy
	 * @param data
	 * @param idx
	 * @param monitor
	 * @throws Exception
	 */
	public static void appendDataset(ILazyWriteableDataset lazy, IDataset data, int idx, IMonitor monitor) throws Exception {
		SliceND ndSlice = new SliceND(lazy.getShape(), new int[] { idx, 0, 0 },
				new int[] { (idx + 1), data.getShape()[0], data.getShape()[1] }, null);
		lazy.setSlice(monitor, data, ndSlice);
	}

	/**
	 * Method that creates an hdf5 file in the temp directory of the OS
	 * 
	 * @param name
	 * @return lazy writable dataset on disk
	 * @throws ScanFileHolderException 
	 */
	public static synchronized ILazyWriteableDataset createTempLazyFile(int[] newShape, String name) throws ScanFileHolderException {
		String file = FileUtils.getTempFilePath(name);
		try {
			// save on a temp file
			String nodepath = "/entry/data/";
			File tmpFile = new File(file);
			if (tmpFile.exists())
				tmpFile.delete();
			return HDF5Utils.createLazyDataset(file, nodepath, name, newShape, null, newShape, Dataset.FLOAT32,
					null, false);
		} finally {
			HDF5FileFactory.releaseFile(file, true);
		}
	}

}
