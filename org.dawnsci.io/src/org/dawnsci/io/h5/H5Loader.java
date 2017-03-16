/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawnsci.io.h5;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawb.common.util.io.FileUtils;
import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.analysis.api.io.SliceObject;
import org.eclipse.dawnsci.hdf.object.H5Utils;
import org.eclipse.dawnsci.hdf.object.HierarchicalDataFactory;
import org.eclipse.dawnsci.hdf.object.HierarchicalDataUtils;
import org.eclipse.dawnsci.hdf.object.HierarchicalInfo;
import org.eclipse.dawnsci.hdf.object.IHierarchicalDataFile;
import org.eclipse.january.IMonitor;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.LazyDataset;
import org.eclipse.january.metadata.IMetadata;

import uk.ac.diamond.scisoft.analysis.io.AbstractFileLoader;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.MetaDataAdapter;

public class H5Loader extends AbstractFileLoader {

	public final static List<String> EXT;
	static {
		List<String> tmp = new ArrayList<String>(7);
		tmp.add("h5");
		tmp.add("nxs");
		tmp.add("hd5");
		tmp.add("hdf5");
		tmp.add("hdf");
		tmp.add("nexus");
		EXT = Collections.unmodifiableList(tmp);
	}	

	public static boolean isH5(final String filePath) {
		if (filePath == null) { return false; }
		final String ext = FileUtils.getFileExtension(filePath);
		if (ext == null) { return false; }
		return EXT.contains(ext.toLowerCase());
	}

	
	public H5Loader() {
		
	}
	
	public H5Loader(final String path) {
		this.fileName = path;
	}

	@Override
	protected void clearMetadata() {
		if (metaInfo != null)
			metaInfo.clear();
	}

	@Override
	public DataHolder loadFile() throws ScanFileHolderException {
        return this.loadFile(null);
	}
	
	@Override
	public DataHolder loadFile(IMonitor mon) throws ScanFileHolderException {
		
		final DataHolder holder = new DataHolder();
		IHierarchicalDataFile file = null;
		try {
			file = HierarchicalDataFactory.getReader(fileName);
			if (mon!=null) mon.worked(1);
			final List<String>     fullPaths = file.getDatasetNames(IHierarchicalDataFile.NUMBER_ARRAY);
			if (mon!=null) mon.worked(1);
			final Map<String, ILazyDataset> sets = getSets(file, fullPaths, mon);
			for (String fullPath : fullPaths) {
				holder.addDataset(fullPath, sets.get(fullPath));
				if (mon!=null) mon.worked(1);
			}

			if (loadMetadata) {
				metaInfo = file.getDatasetInformation(IHierarchicalDataFile.NUMBER_ARRAY);
				holder.setMetadata(getMetadata());
			}
			return holder;
			
		} catch (Exception ne) {
			throw new ScanFileHolderException(ne.getMessage());
		} finally {
			try {
			    if (file!=null) file.close();
			} catch (Exception ne) {
				throw new ScanFileHolderException(ne.getMessage());
			}
		}
	}

	protected synchronized Dataset slice(SliceObject bean, IMonitor mon) throws Exception {
		IHierarchicalDataFile file = null;
		try {
			file = HierarchicalDataFactory.getReader(bean.getPath());
			final hdf.object.Dataset dataset = (hdf.object.Dataset)file.getData(bean.getName());
			
			if (dataset.getStartDims()==null) dataset.getMetadata();
  		    long[] start    = dataset.getStartDims(); // the off set of the selection
			long[] stride   = dataset.getStride(); // the stride of the dataset
			long[] selected = dataset.getSelectedDims(); // the selected size of the dataet
			
			if (mon!=null) mon.worked(1);
			for (int i = 0; i < selected.length; i++) {
				start[i] = bean.getSliceStart()[i];
			}
			for (int i = 0; i < stride.length; i++) {
				stride[i] = bean.getSliceStep()[i];
			}
			for (int i = 0; i < selected.length; i++) {
				selected[i] = bean.getSliceStop()[i]-bean.getSliceStart()[i];
			}

			if (mon!=null) mon.worked(1);
			final Object    val  = dataset.read(); // Appears in stack traces of VM exists
			if (mon!=null) mon.worked(1);
			Dataset aset = H5Utils.getSet(val,selected,dataset);
			
			// Reset dims
			resetDims(dataset);
			if (mon!=null) mon.worked(1);
			return aset;
           
		} finally {
			if (file!=null) file.close();
		}
	}

	protected void resetDims(hdf.object.Dataset dataset) {
		long[] selected = dataset.getSelectedDims(); // the selected size of the dataet
		long[] dims     = dataset.getDims();
		if (dims==null||selected==null) return;
		for (int i = 0; i < selected.length; i++) {
			selected[i] = dims[i];
		}
	}


	public Dataset loadSet(String path, String fullPath, IMonitor mon) throws Exception {
		IHierarchicalDataFile file = null;
		try {
			file = HierarchicalDataFactory.getReader(path);
			if (mon!=null) mon.worked(1);
			final hdf.object.Dataset set = (hdf.object.Dataset)file.getData(fullPath);
			if (mon!=null) mon.worked(1);
			resetDims(set);
			final Object  val = set.read(); // Dangerous if data large!
			if (mon!=null) mon.worked(1);
			

			Dataset ret =  H5Utils.getSet(val,set);
			
			final String errorPath = getErrorPath(fullPath);
			if (errorPath!=null) {
				final hdf.object.Dataset errSet = (hdf.object.Dataset)file.getData(errorPath);
				if (errSet!=null) {
					resetDims(errSet);
					final Object  errVal = errSet.read(); // Dangerous if data large!
					ret.setErrors(H5Utils.getSet(errVal,errSet));
				}
			}
			
			return ret;
		} finally {
			if (file!=null) file.close();
		}
	}

	public static Map<String, ILazyDataset> loadSets(String path, List<String> fullPaths, IMonitor mon) throws Exception {
		IHierarchicalDataFile file = null;
		try {
			if (mon!=null) mon.worked(1);
			file = HierarchicalDataFactory.getReader(path);
			return getSets(file, fullPaths, mon);
		} finally {
			if (file!=null) file.close();
		}
	}

	private static Map<String, ILazyDataset> getSets(final IHierarchicalDataFile file, List<String> fullPaths, IMonitor mon) throws Exception {
		
		final Map<String, ILazyDataset> ret = new HashMap<String,ILazyDataset>(fullPaths.size());
		for (String fullPath : fullPaths) {
			if (mon!=null) mon.worked(1);
			
			if (ret.containsKey(fullPath)) continue;
			
			final hdf.object.Dataset      set = (hdf.object.Dataset)file.getData(fullPath);
			set.getMetadata();
			final LazyDataset  lazy   = new H5LazyDataset(set);
			ret.put(fullPath, lazy);
			
			final String errorPath = getErrorPath(fullPath);
			if (fullPaths.contains(errorPath)) {
				final hdf.object.Dataset error = (hdf.object.Dataset)file.getData(errorPath);
				if (error!=null) {
					error.getMetadata();
					final LazyDataset errLazy = new H5LazyDataset(error);
					lazy.setErrors(errLazy);
					ret.put(errorPath, errLazy);
				}
			}
		}
		return ret;
	}

	private static String getErrorPath(String fullPath) {
		if (fullPath==null) return  null;
		if (fullPath.endsWith("/data")) {
			return fullPath.substring(0, fullPath.lastIndexOf('/'))+"/errors";
		}
		return fullPath+"_errors";
	}


	private HierarchicalInfo metaInfo;

	@Override
	public void loadMetadata(IMonitor mon) throws IOException {
		
		IHierarchicalDataFile file = null;
		try {
			file = HierarchicalDataFactory.getReader(fileName);
			if (mon!=null) mon.worked(1);
			
			metaInfo = file.getDatasetInformation(IHierarchicalDataFile.NUMBER_ARRAY);
		} catch (Exception e) {
			throw new IOException(e);
			
		} finally {
			if (file!=null)
				try {
					file.close();
				} catch (Exception e) {
					throw new IOException("Could not close file", e);
				}
		}
		
	}

	@Override
	public IMetadata getMetadata() {
		
 		return new MetaDataAdapter() {
			private static final long serialVersionUID = IMetadata.serialVersionUID;
			private Map<String, Object> attributeValues;
			
			@Override
			public Collection<String> getMetaNames() throws MetadataException {
				/**
				 * We lazy load the meta data attributes as it's not always needed.
				 */
				if (attributeValues==null) readAttributes();
				return Collections.unmodifiableCollection(attributeValues.keySet());
			}

			@Override
			public String getMetaValue(String fullAttributeKey) throws MetadataException {
				/**
				 * We lazy load the meta data attributes as it's not always needed.
				 */
				if (attributeValues==null) readAttributes();
				return HierarchicalDataUtils.extractValue(attributeValues.get(fullAttributeKey));
			}
			
			@Override
			public Collection<String> getDataNames() {
				return Collections.unmodifiableCollection(metaInfo.getDataSetNames());
			}

			@Override
			public Map<String, Integer> getDataSizes() {
				return Collections.unmodifiableMap(metaInfo.getDataSetSizes());
			}

			@Override
			public Map<String, int[]> getDataShapes() {
				return Collections.unmodifiableMap(metaInfo.getDataSetShapes());
			}
			
			private void readAttributes() throws MetadataException {
				if (attributeValues==null) {
					IHierarchicalDataFile file = null;
					try {
						file = HierarchicalDataFactory.getReader(fileName);
						
						attributeValues = file.getAttributeValues();
					} catch (Exception e) {
						throw new MetadataException(e);
					} finally {
						if (file!=null)
							try {
								file.close();
							} catch (Exception e) {
								throw new MetadataException("Could not close file", e);
							}
					}
				
				}
			}
		};
	}

}
