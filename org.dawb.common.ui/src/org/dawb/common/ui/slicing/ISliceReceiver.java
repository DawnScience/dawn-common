/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.ui.slicing;

import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.io.SliceObject;

public interface ISliceReceiver {

	/**
	 * For instance when a slice is asked to be opened in a gallery view,
	 * this method will be called to send the slice to the gallery.
	 * 
	 * @param shape
	 * @param slice
	 */
	public void updateSlice(final ILazyDataset lazySet, final SliceObject slice);
}
