package org.dawnsci.jexl.internal;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.LazyMaths;

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
	public static AbstractDataset rsum(final ILazyDataset data, final int axis) {
		return LazyMaths.sum(data, axis);
	}

}
