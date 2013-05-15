/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.gda.extensions.loaders;

import java.io.File;
import java.net.URL;

import org.dawb.common.services.ILoaderService;
import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawb.common.util.eclipse.BundleUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
//import org.dawb.fabio.FabioFile;

/**
 * Provides a class which will use any loaders available to load a particular file
 * 
 * TODO FIXME This class should be moved to a proper OSGI service.
 * 
 * @author gerring
 *
 */
public class LoaderService extends AbstractServiceFactory implements ILoaderService {

	public IDataset getDataset(String filePath) throws Throwable {
		
        return getDataset(filePath, new NullProgressMonitor());
	}
	
	public IDataset getDataset(String filePath, final IProgressMonitor monitor) throws Throwable {
	    try {
		    final URL uri = new URL(filePath);
		    filePath = uri.getPath();
		} catch (Throwable ignored) {
		    // We try the file path anyway
		}
		return getDataset(new File(filePath), new NullProgressMonitor());
	}

	public IDataset getDataset(File f) throws Throwable {
		return getDataset(f, new NullProgressMonitor());
	}
	
	public IMetaData getMetaData(final String filePath, final IProgressMonitor monitor) throws Exception {
				
		return LoaderFactory.getMetaData(filePath, new ProgressMonitorWrapper(monitor));
	}

	private IDataset getDataset(final File f, final IProgressMonitor monitor) throws Throwable {
		
		AbstractDataset set = null;
		
		final DataHolder      dh  = LoaderFactory.getData(f.getAbsolutePath(), new ProgressMonitorWrapper(monitor));
		set = dh.getDataset(0);
		set.setName(f.getName());
		return set;
	}
	
	public IDataset getDataset(final String path, final String datasetName, final IProgressMonitor monitor) throws Throwable {
		return LoaderFactory.getDataSet(path, datasetName, new ProgressMonitorWrapper(monitor));
	}


	@Override
	public Object create(@SuppressWarnings("rawtypes") Class serviceInterface, 
			             IServiceLocator parentLocator,
			             IServiceLocator locator) {
		
        if (serviceInterface==ILoaderService.class) {
        	return new LoaderService();
        }
		return null;
	}
	
	private IDiffractionMetadata lockedDiffractionMetaData;

	@Override
	public IDiffractionMetadata getLockedDiffractionMetaData() {
		return lockedDiffractionMetaData;
	}

	@Override
	public IDiffractionMetadata setLockedDiffractionMetaData(IDiffractionMetadata diffMetaData) {
		IDiffractionMetadata old = lockedDiffractionMetaData;
		lockedDiffractionMetaData= diffMetaData;
		return old;
	}
}
