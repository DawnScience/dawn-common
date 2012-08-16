package org.dawb.common.gpu;

/**
 * Factory for making operations. May extend if more
 * values of Operator become possible.
 * 
 * @author fcp94556
 *
 */
public class OperationFactory {

	public static IOperation getBasicOperation() {
		return new BasicOperation();
	}
}
