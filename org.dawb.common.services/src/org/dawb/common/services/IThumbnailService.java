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

import org.eclipse.swt.graphics.Image;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

/**
 * A service for getting thumnails from datasets as images and for
 * getting system file icons for File objects.
 * 
 * The implementor or this service contributes using an eclipse extension
 * point and then later any plugin may ask for an implementation of the service.
 * 
 * @author fcp94556
 */
public interface IThumbnailService extends IFileIconService{

	/**
	 * Create a sqaure image from a specified file, f of given side size, size in pixels.
	 * @param f
	 * @param size
	 * @return
	 */
	public Image createImage(final File f, final int width, int height);

	/**
	 * Get a thumbnail AbstractDataset of square shape.
	 * @param set - must be 2D set
	 * @param size
	 * @return
	 */
	public IDataset getThumbnail(IDataset set, final int width, int height);

	/**
	 * Create an image from an AbstractDataset
	 * @param thumb - must be 2D set
	 * @return
	 */
	public Image createImage(IDataset thumb) throws Exception;
	
	/**
	 * Main method for thumbnails, deals with 1D and 2D set thumbnails.
	 * @param set
	 * @param size
	 * @return
	 */
	public Image getThumbnailImage(IDataset set, final int width, int height) throws Exception;
	
}
