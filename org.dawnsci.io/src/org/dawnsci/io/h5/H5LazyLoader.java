/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.io.h5;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.io.ILazyLoader;
import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.analysis.api.io.SliceObject;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.hdf5.H5Utils;
import org.eclipse.dawnsci.hdf5.HierarchicalDataFactory;
import org.eclipse.dawnsci.hdf5.IHierarchicalDataFile;

public class H5LazyLoader implements ILazyLoader {

	private Map<SliceObject,Reference<Dataset>> cache;
	/**
	 * 
	 */
	private static final long serialVersionUID = 8487401618753205118L;
	
	private String   path; // file path
	private String   fullPath; // dataset path within file
	private H5Loader loader;

	/**
	 * @param path file path
	 * @param fullPath dataset path within file
	 */
	public H5LazyLoader(final String   path, 
			            final String   fullPath) {
		this.loader   = new H5Loader();
		this.path     = path;
		this.fullPath = fullPath;
		this.cache    = new HashMap<SliceObject, Reference<Dataset>>(31);
	}

	@Override
	public boolean isFileReadable() {
		return (new File(path)).canRead();
	}

	@Override
	public Dataset getDataset(IMonitor mon, 
			                          int[] shape, int[] start,
			                          int[] stop,  int[] step) throws ScanFileHolderException {
		
		
		if (shape==null||start==null||stop==null||step==null) {
			try {
				return getCompleteData(mon);
			} catch (Exception e) {
				throw new ScanFileHolderException("Cannot read "+path+", "+fullPath, e);
			}
		}
		
		final SliceObject slice = new SliceObject();
		slice.setPath(path);
		slice.setName(fullPath);
		slice.setSlicedShape(shape);
		slice.setSliceStart(start);
		slice.setSliceStop(stop);
		slice.setSliceStep(step);
		
		if (cache.containsKey(slice)) {
			final Reference<Dataset> sr = cache.get(slice);
			if (sr.get()!=null) {
				return sr.get();
			}
		}
		try {
			Dataset set = loader.slice(slice, mon);
			cache.put(slice, new SoftReference<Dataset>(set));
			return set;
		} catch (Exception e) {
			throw new ScanFileHolderException("Cannot slice "+path+", "+fullPath, e);
		}	
	}

	protected Dataset getCompleteData(IMonitor mon) throws Exception {
		
		IHierarchicalDataFile file = null;
		try {
			if (mon!=null) mon.worked(1);
			file = HierarchicalDataFactory.getReader(path);
			
			final ncsa.hdf.object.Dataset set = (ncsa.hdf.object.Dataset)file.getData(fullPath);
			if (set.getStartDims()==null) set.getMetadata();
			
			/**
			 * The diamond slicing can leave the dataset in memory, and the selection.
			 * Therefore if they were slicing in the DExplore view before going here,
			 * the full selection is broken, there is a sub slice selected.
			 * 
			 * TODO Get Peter to fix this some time.
			 * 
			 */
			loader.resetDims(set);
			
			final Object  val = set.read();
			return H5Utils.getSet(val,set);
						
		} finally {
			if (file!=null) file.close();
		}
	}

}
