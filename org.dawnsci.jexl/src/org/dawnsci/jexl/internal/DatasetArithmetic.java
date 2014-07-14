package org.dawnsci.jexl.internal;

import org.apache.commons.jexl2.JexlArithmetic;

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
		return Maths.unwrap(Maths.power(lhs, rhs), lhs, rhs);
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
		return Maths.unwrap(Maths.add(lhs, rhs), lhs, rhs);
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
		return Maths.unwrap(Maths.subtract(lhs, rhs), lhs, rhs);
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
		return Maths.unwrap(Maths.multiply(lhs, rhs), lhs, rhs);
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
		return Maths.unwrap(Maths.divide(lhs, rhs), lhs, rhs);
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
		return Maths.unwrap(Maths.negative(ob), ob);
	}
}
