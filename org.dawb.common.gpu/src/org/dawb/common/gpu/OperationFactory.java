/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
