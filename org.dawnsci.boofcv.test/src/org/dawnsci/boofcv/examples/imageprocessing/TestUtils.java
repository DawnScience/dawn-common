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
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.hdf5.HDF5FileFactory;
import org.eclipse.dawnsci.hdf5.HDF5Utils;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.FloatDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;

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
		int[] shape = data.getShape();
		SliceND ndSlice = new SliceND(lazy.getShape(), new int[] { idx, 0, 0 },
				new int[] { (idx + 1), shape[0], shape[1] }, null);
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
			tmpFile.deleteOnExit();
			return HDF5Utils.createLazyDataset(file, nodepath, name, newShape, null, newShape, FloatDataset.class,
					null, false);
		} finally {
			HDF5FileFactory.releaseFile(file, true);
		}
	}

	/**
	 * Returns a string array of file names in a path directory, with or without
	 * the full path
	 * 
	 * @param path
	 * @param withFullPath
	 * @return string array
	 */
	public static String[] getFileNames(String path, boolean withFullPath) {
		File dir = new File(path);
		String[] children = dir.list();
		if (children == null) {
			return null;
		}
		// We filter any files that start with '.' or directory
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				File f = new File(dir.getAbsolutePath()+"/"+name);
				if (f.isDirectory())
					return false;
				return !name.startsWith(".");
			}
		};
		children = dir.list(filter);
		Arrays.sort(children);
		if (withFullPath) {
			for (int i = 0; i < children.length; i++) {
				children[i] = path + "/" + children[i];
			}
		}
		return children;
	}

	/**
	 * Put a String[] in a List of String
	 * 
	 * @param array
	 * @param list
	 */
	public static void getArrayAsList(String[] array, List<String> list) {
		list.clear();
		for (int i = 0; i < array.length; i++) {
			list.add(array[i]);
		}
	}
}
