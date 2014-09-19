/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.common.ui.views;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;

/**
 * Information provided to GalleryDelegate when 
 * implementing a custom gallery.
 * 
 * @author Matthew Gerring
 *
 */
public interface GalleryDelegateInfo {

	/**
	 * Size of gallery
	 * @return
	 */
	int getSize();

	/**
	 * Name of gallery
	 * @return
	 */
	String getName();

	/**
	 * Provides the data of any item in the gallery. For which
	 * a thumbnail image is constructed.
	 * @param ii
	 * @return
	 */
	IDataset getData(boolean fullData, ImageItem ii) throws Exception;

	/**
	 * Optionally implemented to return the path for the item.
	 * @param itemCount
	 * @return
	 */
	String getPath(int index);

	/**
	 * Maybe implemented to define the path as already being a thumbnail.
	 * In which case the image will be loaded directly rather than rescaled.
	 * 
	 * NOTE getData(...) is called before getDirectThumbnailPath()
	 * @return
	 */
	public String getDirectThumbnailPath();

	/**
	 * The name of the image as it is read from disk.
	 * @param index
	 * @return
	 */
	String getItemName(int index, boolean attemptToShorten);
}
