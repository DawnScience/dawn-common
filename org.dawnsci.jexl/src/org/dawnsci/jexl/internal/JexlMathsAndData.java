/*-
 *******************************************************************************
 * Copyright (c) 2017 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Timothy Spain - data-ish functions from JexlGeneralFunctions
 *******************************************************************************/


package org.dawnsci.jexl.internal;

import org.eclipse.january.dataset.Dataset;

public class JexlMathsAndData extends JexlMaths {

	public static Dataset mean(final Dataset data, final int axis) {
		return JexlGeneralFunctions.mean(data, axis);
	}
	
	public static Dataset sum(final Dataset data,final int axis) {
		return JexlGeneralFunctions.sum(data, axis);
	}
	
	public static Dataset stdDev(final Dataset data, final int axis) {
		return JexlGeneralFunctions.stdDev(data, axis);
	}
	
	public static Dataset max(final Dataset data, final int axis) {
		return JexlGeneralFunctions.max(data, axis);
	}
	
	public static Dataset min(final Dataset data, final int axis) {
		return JexlGeneralFunctions.min(data, axis);
	}
	
	public static Dataset peakToPeak(final Dataset data, final int axis) {
		return JexlGeneralFunctions.peakToPeak(data, axis);
	}
	
	public static Dataset product(final Dataset data, final int axis) {
		return JexlGeneralFunctions.product(data, axis);
	}
	
	public static Dataset rootMeanSquare(Dataset data, int axis) {
		return JexlGeneralFunctions.rootMeanSquare(data, axis);
	}
	
	public static Dataset median(Dataset data, int axis) {
		return JexlGeneralFunctions.median(data, axis);
	}

}
