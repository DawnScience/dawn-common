/*-
 * Copyright (c) 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.boofcv.util;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;

public class Utils {

	/**
	 * 
	 * @param input
	 * @return 2 or throws an IllegalArgumentException
	 */
	public static int[] getShape(IDataset input) {
		int[] shape = input instanceof Dataset ? ((Dataset) input).getShapeRef() : input.getShape();
		if (shape.length != 2)
			throw new IllegalArgumentException("The input data must be of dimension 2");
		return shape;
	}
}
