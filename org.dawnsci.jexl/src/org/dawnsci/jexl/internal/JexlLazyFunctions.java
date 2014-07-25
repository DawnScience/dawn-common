package org.dawnsci.jexl.internal;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Dataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.LazyMaths;
import uk.ac.diamond.scisoft.analysis.dataset.Slice;

/**
 * Functions able to process without loading all the data at once.
 * 
 * @author fcp94556
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
	 */
	public static AbstractDataset rmean(final ILazyDataset data, final int axis) {
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
	 */
	public static IDataset rsum(final ILazyDataset data, final int axis) {
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
	 */
	public static IDataset slice(final ILazyDataset data, final int axis) {
		return LazyMaths.sum(data, axis);
	}

	public static IDataset slice(final ILazyDataset data,final int[] start,
								final int[] stop,
								final int[] step) {
		
		return ((Dataset)data.getSlice(start, stop, step)).squeeze();
	}

	public static Dataset slice(ILazyDataset data, String sliceString) {
		
		Slice[] slices = Slice.convertFromString(sliceString);

		if (slices.length != data.getRank()) throw new IllegalArgumentException("Invalid string");

		return ((Dataset)data.getSlice(slices)).squeeze();
	}

}
