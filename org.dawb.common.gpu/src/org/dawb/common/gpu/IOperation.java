/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.common.gpu;

import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;

public interface IOperation {

	/**
	 * Processes the operator on the GPU making a Kernel if required and
	 * storing it as local data. Call deactivate on this IOperation to 
	 * clean up memory being used by the kernel
	 * 
	 * @param a
	 * @param b
	 * @param operation
	 * @return
	 */
	public Dataset process(Dataset a, double b, Operator operation);

	/**
	 * Processes the operator on the GPU making a Kernel if required and
	 * storing it as local data. Call deactivate on this IOperation to 
	 * clean up memory being used by the kernel
	 * 
	 * @param a
	 * @param b
	 * @param operation
	 * @return
	 */
	public Dataset process(Dataset a, Dataset b, Operator operation);
	
	/**
	 * Disposes any GPU Kernels which the operation may have.
	 */
	public void deactivate();
}
