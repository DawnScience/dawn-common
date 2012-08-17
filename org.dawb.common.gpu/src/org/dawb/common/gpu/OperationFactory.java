package org.dawb.common.gpu;

/**
 * Factory for making operations. May extend if more
 * values of Operator become possible.
 * 
 * @author fcp94556
 *
 */
public class OperationFactory {

	/**
	 * Probably not worth it for O(N) maths.
	 * @return
	 */
	public static IOperation getBasicGpuOperation() {
		return new BasicGPUOperation();
	}
	
	/**
	 * Probably better than getBasicGpuOperation() for O(N)
	 * @return
	 */
	public static IOperation getBasicCpuOperation() {
		return new BasicCPUOperation();
	}

}
