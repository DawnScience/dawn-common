package org.dawnsci.jexl.internal;

import org.apache.commons.jexl2.JexlArithmetic;
import org.eclipse.dawnsci.analysis.dataset.impl.Maths;

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
	 * @param lhs
	 *            Left hand operand
	 * @param rhs
	 *            Right hand operand
	 * @return result
	 */
	@Override
	public Object bitwiseXor(Object lhs, Object rhs) {
		return Maths.unwrap(Maths.power(lhs, rhs), lhs, rhs);
	}

	/**
	 * This function adds two objects, including datasets
	 * 
	 * @param lhs
	 *            Left hand operand
	 * @param rhs
	 *            Right hand operand
	 * @return result
	 */
	@Override
	public Object add(Object lhs, Object rhs) {
		return Maths.unwrap(Maths.add(lhs, rhs), lhs, rhs);
	}

	/**
	 * This function subtracts two objects, including datasets
	 * 
	 * @param lhs
	 *            Left hand operand
	 * @param rhs
	 *            Right hand operand
	 * @return result
	 */
	@Override
	public Object subtract(Object lhs, Object rhs) {
		return Maths.unwrap(Maths.subtract(lhs, rhs), lhs, rhs);
	}

	/**
	 * This function multiplies two objects, including datasets
	 * 
	 * @param lhs
	 *            Left hand operand
	 * @param rhs
	 *            Right hand operand
	 * @return result
	 */
	@Override
	public Object multiply(Object lhs, Object rhs) {
		return Maths.unwrap(Maths.multiply(lhs, rhs), lhs, rhs);
	}

	/**
	 * This function divides two objects, including datasets
	 * 
	 * @param lhs
	 *            Left hand operand
	 * @param rhs
	 *            Right hand operand
	 * @return result
	 */
	@Override
	public Object divide(Object lhs, Object rhs) {
		return Maths.unwrap(Maths.divide(lhs, rhs), lhs, rhs);
	}

	/**
	 * This function returns the negative of the supplied object, including
	 * datasets
	 * 
	 * @param ob
	 *            Object to the right of negative sign
	 * @return result
	 */
	@Override
	public Object negate(Object ob) {
		return Maths.unwrap(Maths.negative(ob), ob);
	}

	/**
	 * This function calculates the modulus
	 * @param lhs
	 *            Left hand operand
	 * @param rhs
	 *            Right hand operand
	 * @return result
	 */
	@Override
	public Object mod(Object lhs, Object rhs) {
		return Maths.unwrap(Maths.remainder(lhs, rhs), lhs, rhs);
	}
}
