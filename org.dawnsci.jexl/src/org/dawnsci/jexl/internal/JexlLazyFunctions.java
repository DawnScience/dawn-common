/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.jexl.internal;

import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.LazyMaths;
import org.eclipse.january.dataset.Slice;

/**
 * Functions able to process without loading all the data at once.
 * 
 * @author Matthew Gerring
 *
 */
public class JexlLazyFunctions {

	/**
	 * Apparently this is not a true rolling mean. However it does the
	 * mean in a lazy way providing the size does not overrun the size
	 * of a double.
	 * 
	 * @param data
	 * @param axis
	 * @return
	 * @throws DatasetException 
	 */
	public static Dataset rmean(final ILazyDataset data, final int axis) throws DatasetException {
		final int length = data.getShape()[axis];
		return LazyMaths.sum(data, axis).idivide(length);
	}

	
	/**
	 * Apparently this is not a true rolling sum. However it does the
	 * job in a lazy way providing the size does not overrun the size
	 * of a double.
	 * 
	 * @param data
	 * @param axis
	 * @return
	 * @throws DatasetException 
	 */
	public static IDataset rsum(final ILazyDataset data, final int axis) throws DatasetException {
		return LazyMaths.sum(data, axis);
	}
	
	/**
	 * Apparently this is not a true rolling sum. However it does the
	 * job in a lazy way providing the size does not overrun the size
	 * of a double.
	 * 
	 * @param data
	 * @param axis
	 * @return
	 * @throws DatasetException 
	 */
	public static IDataset slice(final ILazyDataset data, final int axis) throws DatasetException {
		return LazyMaths.sum(data, axis);
	}

	public static IDataset slice(final ILazyDataset data,final int[] start,
								final int[] stop,
								final int[] step) throws DatasetException {
		
		return data.getSlice(start, stop, step).squeeze();
	}

	public static IDataset slice(ILazyDataset data, String sliceString) throws DatasetException {
		
		Slice[] slices = Slice.convertFromString(sliceString);

		if (slices.length != data.getRank()) throw new IllegalArgumentException("Invalid string");

		return data.getSlice(slices).squeeze();
	}

}
