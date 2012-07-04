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
import java.util.Collection;

import org.dawb.common.services.ILoaderService;
import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawb.common.util.eclipse.BundleUtils;
//import org.dawb.fabio.FabioFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.FloatDataset;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.io.MetaDataAdapter;

/**
 * Provides a class which will use any loaders available to load a particular file
 * 
 * 
 * @author gerring
 *
 */
public class LoaderService extends AbstractServiceFactory implements ILoaderService {

	public AbstractDataset getDataset(String filePath) throws Throwable {
		
        return getDataset(filePath, new NullProgressMonitor());
	}
	
	public AbstractDataset getDataset(String filePath, final IProgressMonitor monitor) throws Throwable {
	    
		return getDataset(new File(filePath), new NullProgressMonitor());
	}

	public AbstractDataset getDataset(File f) throws Throwable {
		return getDataset(f, new NullProgressMonitor());
	}
	
	public IMetaData getMetaData(final String filePath, final IProgressMonitor monitor) throws Exception {
		
		boolean isFabioOnly = LoaderService.getUsingFabio((new File(filePath)).getName());

		IMetaData meta = null;
		
		
		if (!isFabioOnly) try {
			
			meta = LoaderFactory.getMetaData(filePath, new ProgressMonitorWrapper(monitor));
			if (meta == null) throw new Exception("No meta data found!");
			
		} catch (Throwable ne) {
			
			isFabioOnly = true;
			
		}
		if (isFabioOnly && isFabioAvailable()) {
			
			throw new Exception("Fabio is currently not supported in Dawn!");
//			final FabioFile file = new FabioFile(filePath);
//			meta = new MetaDataAdapter() {
//				@Override
//				public String getMetaValue(String key) throws Exception {
//					return file.getValue(key);
//				}
//
//				@Override
//				public Collection<String> getMetaNames() throws Exception {
//					try {
//						return file.getKeysAsListedInHeader();
//					} catch (Exception ne) {
//						throw ne;
//					} catch (Throwable t) {
//						throw new Exception(t);
//					}
//				}
//			};
		}
		
		return meta;
	}
	
	/**
	 * We test if there is any fabio installed in the product
	 * @return
	 */
	private boolean isFabioAvailable() {
		
        final String eclipseDir = BundleUtils.getEclipseHome();
		return (new File(eclipseDir+"/fabio")).exists();
	}

	private AbstractDataset getDataset(final File f, final IProgressMonitor monitor) throws Throwable {
		
		AbstractDataset set = null;
		
		boolean isFabioOnly = LoaderService.getUsingFabio(f.getName());
		
		if (!isFabioOnly) try {
			
			// Load from GDA - error this loads all the data sometimes.
			final DataHolder      dh  = LoaderFactory.getData(f.getAbsolutePath(), new ProgressMonitorWrapper(monitor));
			set = dh.getDataset(0);
			set.setName(f.getName());
			return set;
			
		} catch (Throwable ne) {
			
			isFabioOnly = true;
			
		}
		
		if (isFabioOnly && isFabioAvailable() ) {
			try {
				throw new Exception("Fabio is currently not supported in Dawn!");

//				FabioFile     file = new FabioFile(f.getAbsolutePath());
//				final float[] fa   = file.getImageAsFloat();
//				set = new FloatDataset(fa, new int[]{file.getHeight(), file.getWidth()});
//				set.setName(f.getName());
				
			} finally {
				// FIXME TODO MASSIVEBODGE!! 
				// FableJep memory leaks but if you close it for a given
				// thread, it stops working.
				//FableJep.closeFableJep();
			}
		}
		
		return set;
	}

	private static boolean getUsingFabio(String name) {
		
		// Eclipse prefs to switch on fabio for certain file types
		final ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawb.fable.framework.navigator");
		final boolean      isFabio= store.getBoolean("fable.framework.navigator.preferences.useFabio");
		if (isFabio) {
			final String endings = store.getString("pref_sampleNavigator_type");
			String[] split = endings.split("\\|");

            for (int i = 0; i < split.length; i++) {
				if (name.endsWith("."+split[i])) return true;
			}
		}
		
		// There is a system property to switch on fabio always
		if (System.getProperty("org.dawb.fabio.always.use")!=null) return true;
		
		return false;
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
}
