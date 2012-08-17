package org.dawb.common.gpu;


import com.amd.aparapi.Kernel;
import com.amd.aparapi.Range;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;

/**
 * Operation which supports basic operators.
 * 
 * @author fcp94556
 *
 */
class BasicCPUOperation implements IOperation {


	@Override
	public AbstractDataset process(AbstractDataset a, double b, Operator operation) {

		switch (operation) {
		case ADD:
			return Maths.add(a,b);
		case SUBTRACT:
			return Maths.subtract(a,b);
		case MULTIPLY:
			return Maths.multiply(a,b);
		case DIVIDE:
			return Maths.divide(a,b);
		}
		return null;
	}

	@Override
	public AbstractDataset process(AbstractDataset a, AbstractDataset b, Operator operation) {

		switch (operation) {
		case ADD:
			return Maths.add(a,b);
		case SUBTRACT:
			return Maths.subtract(a,b);
		case MULTIPLY:
			return Maths.multiply(a,b);
		case DIVIDE:
			return Maths.divide(a,b);
		}
		return null;
	}


	/**
	 * Dispose is not a final state. You can still reuse the IOperation after this.
	 */
	@Override
	public void deactivate() {
		// Nothing to do
	}

}
