package org.dawnsci.jexl.internal;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Slice;
import uk.ac.diamond.scisoft.analysis.dataset.Stats;

/**
 * Class to wrap the methods on the AbstractDataset object (and a few select others)
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
	public static AbstractDataset transpose(final IDataset a, int... axes) {
		return DatasetUtils.transpose(a, axes);
	}
    /**
     * Makes a tile of the passed in data with the passed in repetition shape.
     * @param copy
     * @return
     */
	public static AbstractDataset tile(final AbstractDataset copy, int... reps) {
		return DatasetUtils.tile(copy, reps);
	}
    /**
     * Makes an arange(...) using the size of the passed in data.
     * @param copy
     * @return
     */
	public static AbstractDataset arange(final AbstractDataset copy) {
		return AbstractDataset.arange(copy.getSize(), AbstractDataset.INT32);
	}

	public static AbstractDataset mean(final AbstractDataset data,final int axis) {
		return data.mean(axis);
	}
	
	public static AbstractDataset sum(final AbstractDataset data,final int axis) {
		return data.sum(axis);
	}
	
	public static AbstractDataset slice(final AbstractDataset data,final int[] start,
																   final int[] stop,
																   final int[] step) {
		return data.getSlice(start, stop, step);
	}
	
	public static AbstractDataset stdDev(final AbstractDataset data, final int axis) {
		return data.stdDeviation(axis);
	}
	
	public static AbstractDataset max (final AbstractDataset data, final int axis) {
		return data.max(axis);
	}
	
	public static AbstractDataset min (final AbstractDataset data, final int axis) {
		return data.min(axis);
	}
	
	public static AbstractDataset peakToPeak(final AbstractDataset data, final int axis) {
		return data.peakToPeak(axis);
	}
	
	public static AbstractDataset product(final AbstractDataset data, final int axis) {
		return data.product(axis);
	}
	
	public static AbstractDataset rootMeanSquare(AbstractDataset data, int axis) {
		return data.rootMeanSquare(axis);
	}
	
	public static AbstractDataset median(AbstractDataset data, int axis) {
		return Stats.median(data,axis);
	}
	
	public static AbstractDataset slice(AbstractDataset data, String sliceString) {
		Slice[] slices = Slice.convertFromString(sliceString);

		if (slices.length != data.getRank()) throw new IllegalArgumentException("Invalid string");

		return data.getSlice(slices).squeeze();
	}
}
