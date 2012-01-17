/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.services;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;

/**
 * A service with loads data using any loader it can, depending on the 
 * service implementation and returns it as an AbstractDataset
 * 
 * The implementor or this service contributes using an eclipse extension
 * point and then later any plugin may ask for an implementation of the service.
 * 
 * @author fcp94556
 *
 */
public interface ILoaderService {

	/**
	 * Reads a dataset and returns it as an AbstractDataset
	 * @param filePath
	 * @return
	 * @throws Throwable
	 */
    public AbstractDataset getDataset(String filePath) throws Throwable;
	
	/**
	 * Reads a dataset and returns it as an AbstractDataset, with progress
	 * @param filePath
	 * @return
	 * @throws Throwable
	 */
    public AbstractDataset getDataset(String filePath, final IProgressMonitor monitor) throws Throwable;

	 /**
	  * Reads a file and returns it as an AbstractDataset, used for image files.
	  * @param filePath
	  * @return
	  * @throws Throwable
	  */
	 public AbstractDataset getDataset(File f) throws Throwable;
	 
	 /**
	  * This method can be used to load meta data. It will use Fabio if
	  * LoaderFactory does not work.
	  */
	 public IMetaData getMetaData(final String filePath, final IProgressMonitor monitor) throws Exception;
}
