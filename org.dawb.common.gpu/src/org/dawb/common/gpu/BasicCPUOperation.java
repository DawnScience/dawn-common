package org.dawb.common.gpu;


import uk.ac.diamond.scisoft.analysis.dataset.Dataset;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;

/**
 * Operation which supports basic operators.
 * 
 * @author fcp94556
 *
 */
class BasicCPUOperation implements IOperation {


	@Override
	public Dataset process(Dataset a, double b, Operator operation) {

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
	public Dataset process(Dataset a, Dataset b, Operator operation) {

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
