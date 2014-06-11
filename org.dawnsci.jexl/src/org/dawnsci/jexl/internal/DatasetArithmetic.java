package org.dawnsci.jexl.internal;

import org.apache.commons.jexl2.JexlArithmetic;

import uk.ac.diamond.scisoft.analysis.dataset.Dataset;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;

/**
 * DatasetArithmetic class overriding the plus, minus, multiplication and
 * division operators of the JexlArithmetic object to work with datasets.
 */
public class DatasetArithmetic extends JexlArithmetic {

	public DatasetArithmetic(boolean lenient) {
		super(lenient);
	}

	/**
	 * This replaces the exclusive or with an exponentiation operation
	 */
	@Override
	public Object bitwiseXor(Object lhs, Object rhs) {
		if (lhs instanceof Dataset || rhs instanceof Dataset) {
			return Maths.power(lhs, rhs);
		}

		double l = toDouble(lhs);
		double r = toDouble(rhs);
		return Math.pow(l, r);
	}

	/**
	 * This function adds two objects, including datasets
	 * 
	 * @param lhs
	 *            Object on the left hand side of the equation
	 * @param rhs
	 *            Object on the right hand side of the equation
	 * @return Object containing result (either dataset or whatever super.add
	 *         returns)
	 */
	@Override
	public Object add(Object lhs, Object rhs) {
		if (lhs instanceof Dataset) {
			return Maths.add((Dataset) lhs, rhs);
		}

		if (rhs instanceof Dataset) {
			return Maths.add((Dataset) rhs, lhs);
		}

		return super.add(lhs, rhs);
	}

	/**
	 * This function subtracts two objects, including datasets
	 * 
	 * @param lhs
	 *            Object on the left hand side of the equation
	 * @param rhs
	 *            Object on the right hand side of the equation
	 * @return Object containing result (either dataset or whatever
	 *         super.subtract returns)
	 */
	@Override
	public Object subtract(Object lhs, Object rhs) {
		if (lhs instanceof Dataset || rhs instanceof Dataset) {
			return Maths.subtract(lhs, rhs);
		}

		return super.subtract(lhs, rhs);
	}

	/**
	 * This function multiplies two objects, including datasets
	 * 
	 * @param lhs
	 *            Object on the left hand side of the equation
	 * @param rhs
	 *            Object on the right hand side of the equation
	 * @return Object containing result (either dataset or whatever
	 *         super.multipy returns
	 */
	@Override
	public Object multiply(Object lhs, Object rhs) {
		if (lhs instanceof Dataset) {
			return Maths.multiply((Dataset) lhs, rhs);
		}

		if (rhs instanceof Dataset) {
			return Maths.multiply((Dataset) rhs, lhs);
		}

		return super.multiply(lhs, rhs);
	}

	/**
	 * This function divides two objects, including datasets
	 * 
	 * @param lhs
	 *            Object on the left hand side of the equation
	 * @param rhs
	 *            Object on the right hand side of the equation
	 * @return Object containing result (either dataset or whatever super.divide
	 *         returns)
	 */
	@Override
	public Object divide(Object lhs, Object rhs) {
		if (lhs instanceof Dataset || rhs instanceof Dataset) {
			return Maths.divide(lhs, rhs);
		}

		return super.divide(lhs, rhs);
	}

	/**
	 * This function returns the negative of the supplied Object, including
	 * datasets
	 * 
	 * @param ob
	 *            Object to the right of negative sign
	 * @return Object containing result (either dataset or whatever super.negate
	 *         returns)
	 */
	@Override
	public Object negate(Object ob) {
		if (ob instanceof Dataset) {
			return Maths.negative((Dataset) ob);
		}

		return super.negate(ob);
	}
}
