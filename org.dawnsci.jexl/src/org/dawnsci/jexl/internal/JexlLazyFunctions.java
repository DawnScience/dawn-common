package org.dawnsci.jexl.internal;

import java.util.Arrays;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;

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
		
		final int        axisSize = data.getShape()[axis];
		return rsum(data, axis).idivide(axisSize);
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
		
		final int[] shape  = data.getShape();
		final int[] rshape = new int[shape.length-1];
		
		int idim = 0;
		for (int i = 0; i < shape.length; i++) {
			if (i==axis) continue;
			rshape[idim] = shape[i];
			idim++;
		}
		
		final AbstractDataset sum = AbstractDataset.zeros(rshape, AbstractDataset.FLOAT64);
		final int        axisSize = shape[axis];
		
		final int[] start = new int[shape.length]; // zeros
		final int[] stop  = Arrays.copyOf(shape, shape.length);
		final int[] step  = new int[shape.length];
		for (int i = 0; i < step.length; i++) step[i] = 1;

		for (int i = 0; i < axisSize; i++) {
			start[axis] = i;
			stop[axis]  = i+1;
			final IDataset slice = data.getSlice(start, stop, step);
			
			sum.iadd(slice.squeeze());
		}

		return sum;
	}

}
