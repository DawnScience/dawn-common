/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawnsci.io.spec;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.metadata.IMetadata;

import uk.ac.diamond.scisoft.analysis.io.AbstractFileLoader;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.ExtendedMetadataAdapter;

public class SpecLoader extends AbstractFileLoader {
	
//	private static Logger logger = LoggerFactory.getLogger(SpecLoader.class);
	
	private Collection<String> dataNames;
	private Map<String,Integer>dataSizes;
	private Map<String,int[]>  dataShapes;

	public SpecLoader() {
		
	}
	
	public SpecLoader(final String path) {
		this.fileName = path;
	}

	@Override
	protected void clearMetadata() {
		metadata = null;
		dataNames.clear();
		dataSizes.clear();
		dataShapes.clear();
	}

	@Override
	public DataHolder loadFile() throws ScanFileHolderException {
        return loadFile(null);
	}
	
	/**
	 * Loads the complete spec file into a list of scan_name/dataset_name
	 */
	@Override
	public DataHolder loadFile(IMonitor mon) throws ScanFileHolderException {
		
		try {
			final DataHolder holder = new DataHolder();
			final MultiScanDataParser parser = new MultiScanDataParser(new FileInputStream(new File(fileName)));
			final Collection<String> scans = parser.getScanNames();
			for (String scanName : scans) {
				final Collection<Dataset> sets = parser.getSets(scanName);
				for (Dataset dataset : sets) {
					holder.addDataset(scanName+"/"+dataset.getName(), dataset);
				}
			}
			
			if (holder.size() == 0) throw new ScanFileHolderException(fileName + "Contains no data!");
			
			return holder;
			
		} catch (Exception e) {
			throw new ScanFileHolderException("Cannot parse "+fileName, e);
		}
	}

	/**
	 * Just loads the whole data file! Does not keep data in memory through. Therefore
	 * it is slow to run on big files and light on memory.
	 */
	@Override
	public void loadMetadata(IMonitor mon) throws IOException {
		try {
			this.dataNames  = new ArrayList<String>(31);
			this.dataSizes  = new HashMap<String,Integer>(31);
			this.dataShapes = new HashMap<String,int[]>(31);
			
			final MultiScanDataParser parser = new MultiScanDataParser(new FileInputStream(new File(fileName)));
			final Collection<String> scans = parser.getScanNames();
			for (String scanName : scans) {
				final Collection<Dataset> sets = parser.getSets(scanName);
				for (Dataset abstractDataset : sets) {
					final String name = scanName+"/"+abstractDataset.getName();
					dataNames.add(name);
					dataSizes.put(name, abstractDataset.getSize());
					dataShapes.put(name, abstractDataset.getShape());
				}
			}
					
		} catch (Exception e) {
			throw new IOException("Cannot parse "+fileName, e);
		}
	}

	@Override
	public IMetadata getMetadata() {
		return new ExtendedMetadataAdapter(new File(fileName)) {
			private static final long serialVersionUID = IMetadata.serialVersionUID;

			@Override
			public Collection<String> getDataNames() {
				return Collections.unmodifiableCollection(dataNames);
			}
			@Override
			public Map<String, Integer> getDataSizes() {
				return Collections.unmodifiableMap(dataSizes);
			}

			@Override
			public Map<String, int[]> getDataShapes() {
				return Collections.unmodifiableMap(dataShapes);
			}
			
		};
	}
}
