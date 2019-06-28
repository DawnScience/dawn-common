/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.jexl.internal;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.Stats;

/**
 * Class to wrap the methods on the Dataset object (and a few select others)
 * allowing them to be called in a manner consistent with the other Jexl functions
 * i.e. namespace:methodName()
 * <p>
 * Methods only for use in the DawnJexlEngine which can be obtained from JexlUtils
 */
public class JexlGeneralFunctions {
	
	/**
	 * @see DatasetUtils.transpose(IDataset a, int... axes)
	 * @param a
	 * @param axes
	 * @return
	 */
	public static Dataset transpose(final IDataset a, int... axes) {
		return DatasetUtils.transpose(a, axes);
	}

	/**
     * Makes a tile of the passed in data with the passed in repetition shape.
     * @param copy
     * @return
     */
	public static Dataset tile(final IDataset copy, int... reps) {
		return DatasetUtils.tile(copy, reps);
	}

	/**
     * Makes an arange(...) using the size of the passed in data.
     * @param copy
     * @return
     */
	public static Dataset arange(final IDataset copy) {
		return DatasetFactory.createRange(IntegerDataset.class, copy.getSize());
	}

	public static Dataset mean(final Dataset data,final int axis) {
		data.squeeze();
		return data.mean(axis);
	}
	
	public static Dataset sum(final Dataset data,final int axis) {
		data.squeeze();
		return data.sum(axis);
	}
	
	public static IDataset slice(final IDataset data,final int[] start,
																   final int[] stop,
																   final int[] step) {
		return data.getSlice(start, stop, step);
	}
	
	public static Dataset stdDev(final Dataset data, final int axis) {
		data.squeeze();
		return data.stdDeviation(axis);
	}
	
	public static Dataset max (final Dataset data, final int axis) {
		data.squeeze();
		return data.max(axis);
	}
	
	public static Dataset min(final Dataset data, final int axis) {
		data.squeeze();
		return data.min(axis);
	}
	
	public static Dataset peakToPeak(final Dataset data, final int axis) {
		data.squeeze();
		return data.peakToPeak(axis);
	}
	
	public static Dataset product(final Dataset data, final int axis) {
		data.squeeze();
		return data.product(axis);
	}
	
	public static Dataset rootMeanSquare(Dataset data, int axis) {
		data.squeeze();
		return data.rootMeanSquare(axis);
	}
	
	public static Dataset median(Dataset data, int axis) {
		data.squeeze();
		return Stats.median(data,axis);
	}
	
	public static Dataset slice(Dataset data, String sliceString) {
		Slice[] slices = Slice.convertFromString(sliceString);

		if (slices.length != data.getRank()) throw new IllegalArgumentException("Invalid string");

		return data.getSlice(slices).squeeze();
	}
	
	public static IDataset squeeze(IDataset data) {
		return data.squeeze();
	}

	public static Dataset reshape(Dataset data, int[] shape) {
		Dataset out = data.clone();
		return out.reshape(shape);
	}
}
