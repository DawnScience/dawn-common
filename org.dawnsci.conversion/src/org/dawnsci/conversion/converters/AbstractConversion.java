/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.conversion.converters;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.conversion.IConversion;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionVisitor;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceViewIterator;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceVisitor;
import org.eclipse.dawnsci.analysis.dataset.slicer.Slicer;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;

import uk.ac.diamond.osgi.services.ServiceProvider;

/**
 * AbstractConversion details converting from hdf/nexus to other
 * things only at the moment.
 * 
 * @author Matthew Gerring
 *
 */
public abstract class AbstractConversion implements IConversion {
	
	protected IConversionContext context;

	AbstractConversion() {
		// OSGi
	}
	
	public AbstractConversion(IConversionContext context) {
		this.context = context;
	}

	@Override
	public void process(IConversionContext context) throws Exception {
		
		// If they directly specify an ILazyDataset, loop it and only
		// it directly. Ignore file paths.
		ILazyDataset lz = context.getLazyDataset();
		final List<String> filePaths = context.getFilePaths();
		if (lz != null) {
			iterate(lz, lz.getName(), context);
		} else if (filePaths != null && !filePaths.isEmpty()) {
			for (String filePathRegEx : filePaths) {
				final List<File> paths = expand(filePathRegEx);
				for (File path : paths) {
					
					context.setSelectedConversionFile(path);
					if (path.isFile()) {
						final List<String> sets  = getDataNames(path);
						final List<String> names = context.getDatasetNames();
						for (String nameRegExp : names) {
							final List<String> data = getData(sets, nameRegExp);
							if (data == null) continue;
							for (String dsPath : data) {
								lz = getLazyDataset(path, dsPath, context);
								if (lz!=null) iterate(lz, dsPath, context);
							}
						}
					} else { 
						lz = getLazyDataset(path, null, context);
						iterate(lz, path.getName(), context);
					}
				}
			}
		} else {
			// If no lazy dataset and no file paths specified then try conversion visitor directly
			// as the existing implementations get their data from plotting systems
			IConversionVisitor v = context.getConversionVisitor();
			if (v != null) {
				v.visit(context, null);
			}
		}
	}
	
	/**
	 * Override this method to provide things which should happen after the processing.
	 * Call super() in the override if the conversion needs to be tracked for analytics purposes.
	 * 
	 * @param context
	 */
	@Override
	public void close(IConversionContext context) throws Exception{
		//do nothing
	}
	
	protected IConversionContext getContext() {
		return context;
	}

	/**
	 * Please implement this method to process a single conversion. The files will have been 
	 * expanded, the datasets expanded, the slice done, you need to implement the writing of the
	 * appropriate file for this slice.
	 * 
	 * @param slice
	 * @param context, used to provide the output location mainly.
	 */
	protected abstract void convert(IDataset slice) throws Exception;
	

	/**
	 * This method can be overridden for returning stacks of images from 
	 * a directory for instance.
	 * 
	 * @param path
	 * @param dsPath
	 * @param sliceDimensions
	 * @param context, might be null for testing
	 * @return
	 * @throws Exception
	 */
	protected ILazyDataset getLazyDataset(final File                 path, 
						                  final String               dsPath,
						                  final IConversionContext   context) throws Exception {
		// if there is a lazydataset, we return it
		ILazyDataset lazy = context.getLazyDataset();
		if (lazy != null)
			return lazy;

		final IDataHolder dh = ServiceProvider.getService(ILoaderService.class).getData(path.getAbsolutePath(), null);
		context.setSelectedH5Path(dsPath);
		if (context.getSliceDimensions()==null) {
			// Because the data might be lazy and unloadable. We want to load all the data now.
			IDataset data = ServiceProvider.getService(ILoaderService.class)
					.getDataset(path.getAbsolutePath(),dsPath,(IMonitor)null);
			data.setName(dsPath);
			convert(data);
			return null;
		}
		if (context.getMonitor()!=null) {
			context.getMonitor().subTask("Process '"+path.getAbsolutePath() +"''"+dsPath+"'");
		}
		return dh.getLazyDataset(dsPath);
	}
		
	protected void iterate(final ILazyDataset         lz, 
			               final String               nameFrag,
		                   final IConversionContext   context) throws Exception {
		
		multiRangeIterate(lz,nameFrag,context);
	}
	
	/**
	 * Method tries to get the input datasets with the regular expressions, if any, expanded.
	 * @return
	 */
	protected List<String> getExpandedDatasets() throws Exception {
		
		final List<String> names = new ArrayList<String>(31);
		
		final List<String> filePaths = context.getFilePaths();
		if (filePaths.isEmpty() || filePaths.get(0).isEmpty()) return null;
		for (String filePathRegEx : filePaths) {
			final List<File> paths = expand(filePathRegEx);
			for (File path : paths) {
				
				context.setSelectedConversionFile(path);
				if (path.isFile()) {
					final List<String> sets   = getDataNames(path);
					final List<String> dNames = context.getDatasetNames();
					for (String nameRegExp : dNames) {
						final List<String> data = getData(sets, nameRegExp);
					    if (data != null) names.addAll(data);
					}
				}
			}
		}
		if (names.isEmpty()) return null;
		return names;
	}

	private void multiRangeIterate(final ILazyDataset         lz, 
			                       final String               nameFrag,
		                           final IConversionContext   context) throws Exception {
		
		final Map<Integer, String> dims = context.getSliceDimensions();
		
		SliceND slice = Slicer.getSliceNDFromSliceDimensions(dims, lz.getShape());
		int[] axes = Slicer.getDataDimensions(lz.getShape(), dims);
		final SliceViewIterator it = new SliceViewIterator(lz, slice, axes);
		
		Slicer.visit(it, new SliceVisitor() {

			@Override
			public void visit(IDataset slice) throws Exception {
				//no longer squeeze in slicer
				slice.squeeze();
				context.setSelectedSlice(slice.getFirstMetadata(SliceFromSeriesMetadata.class).getSliceInOutput());
				context.setSelectedShape(it.getShape());
				convert(slice);
			}

			@Override
			public boolean isCancelled() {
				return context.getMonitor()!=null ? context.getMonitor().isCancelled() : false;
			}
			
		});
	}

	@Override
	public List<String> getData(File path, String datasetName) throws Exception {
        return getData(getDataNames(path), datasetName);
	}
	
	private List<String> getData(List<String> sets, String datasetName) {

		final List<String> ds = new ArrayList<String>(7);
		
		if (sets.contains(datasetName)) {
			ds.add(datasetName);
		} else {
			for (String hdfPath : sets) {
				if (hdfPath.matches(datasetName)) {
					ds.add(hdfPath);
				}
			}
		}
		
		return ds.isEmpty() ? null : ds;
	}
	

	/**
	 * Can be used to get a list of Dataset which should be converted. Processes the
	 * regexp for the dataset path and returns the Dataset which can be sliced to get
	 * the array of numbers for the export.
	 * 
	 * @param ioFile
	 * @param context
	 * @return null if none match, the datasets otherwise
	 * @throws Exception
	 */
	@Override
	public List<String> getDataNames(File ioFile) throws Exception {

		if (ioFile.isDirectory()) return Collections.emptyList();
		final IDataHolder dh = ServiceProvider.getService(ILoaderService.class).getData(ioFile.getAbsolutePath(),null);
		
		if (dh == null || dh.getNames() == null) return Collections.emptyList();
		return Arrays.asList(dh.getNames());
	}

	/**
	 * expand the regex according to the javadoc for getFilePath().
	 * @param context
	 * @return
	 */
	@Override
	public List<File> expand(String path) {
		
		if (path.isEmpty()) return null;
		
		final List<File> files = new ArrayList<File>(7);
		path = path.replace('\\', '/');
		final String dir    = path.substring(0, path.lastIndexOf("/"));
		final String regexp = path.substring(path.lastIndexOf("/")+1);
		
		final File[] fa = new File(dir).listFiles();
		for (File file : fa) {
			if (regexp==null || "".equals(regexp)) {
				files.add(file);
				continue;
			}
			if (file.getName().matches(regexp) || file.getName().equals(regexp)) {
				files.add(file);
			}
		}
		
		return files.isEmpty() ? null : files;
	}
	
	
	protected String getFileNameNoExtension(File file) {
		final String fileName = file.getName();
		int posExt = fileName.lastIndexOf(".");
		// No File Extension
		return posExt == -1 ? fileName : fileName.substring(0, posExt);
	}

}
