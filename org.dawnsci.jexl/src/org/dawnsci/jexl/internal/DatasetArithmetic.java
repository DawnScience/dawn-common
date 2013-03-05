package org.dawnsci.jexl.internal;

import org.apache.commons.jexl2.JexlArithmetic;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;

public class DatasetArithmetic extends JexlArithmetic {

	public DatasetArithmetic(boolean lenient) {
		super(lenient);
	}
	
	@Override
	public Object add(Object lhs, Object rhs) {
		
		if (rhs instanceof AbstractDataset &&
			lhs instanceof AbstractDataset) {
			
			return Maths.add((AbstractDataset)lhs, (AbstractDataset)rhs);
		}
		
		if (lhs instanceof AbstractDataset &&
				!(rhs instanceof AbstractDataset)) {
			
			return Maths.add((AbstractDataset)lhs, rhs);
		}
		
		if (!(lhs instanceof AbstractDataset) &&
				rhs instanceof AbstractDataset) {
			
			return Maths.add((AbstractDataset)rhs, lhs);
		}
		return super.add(lhs, rhs);
	}
	
	@Override
	public Object subtract(Object lhs, Object rhs) {
		
		if (rhs instanceof AbstractDataset ||
				lhs instanceof AbstractDataset) {
				
				return Maths.subtract(lhs, rhs);
			}
		
		return super.subtract(lhs, rhs);
	}
	
	@Override
	public Object multiply(Object lhs, Object rhs) {

		if (rhs instanceof AbstractDataset &&
				lhs instanceof AbstractDataset) {

			return Maths.multiply((AbstractDataset)lhs, (AbstractDataset)rhs);
		}

		if (lhs instanceof AbstractDataset &&
				!(rhs instanceof AbstractDataset)) {

			return Maths.multiply((AbstractDataset)lhs, rhs);
		}

		if (!(lhs instanceof AbstractDataset) &&
				rhs instanceof AbstractDataset) {

			return Maths.multiply((AbstractDataset)rhs, lhs);
		}
		return super.multiply(lhs, rhs);
	}
	
	@Override
	public Object divide(Object lhs, Object rhs) {
		
		if (rhs instanceof AbstractDataset ||
				lhs instanceof AbstractDataset) {
				
				return Maths.divide(lhs, rhs);
			}
		
		return super.divide(lhs, rhs);
	}
	
	@Override
	public Object negate(Object ob) {

		if (ob instanceof AbstractDataset) {

			return Maths.multiply((AbstractDataset)ob,-1);
		}

		return super.negate(ob);
	}

}
