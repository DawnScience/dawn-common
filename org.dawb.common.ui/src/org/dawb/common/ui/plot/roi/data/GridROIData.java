/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawb.common.ui.plot.roi.data;

import org.eclipse.dawnsci.analysis.dataset.roi.GridROI;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.swt.graphics.RGB;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;

/**
 * Class to aggregate information associated with a ROI
 * A GridROI is the same as a RectangularROI, but with grid information
 */
public class GridROIData extends RectangularROIData {

	/**
	 * @param roi
	 * @param data
	 */
	public GridROIData(GridROI roi, Dataset data) {
		super(roi, data);
		plotColourRGB = new RGB(0,0,0);
	}

	/**
	 * @param roi
	 * @param profileData
	 * @param axes
	 * @param profileSum
	 */
	public GridROIData(GridROI roi, Dataset[] profileData, AxisValues[] axes, double profileSum) {
		super(roi, profileData, axes, profileSum);
		plotColourRGB = new RGB(0,0,0);
	}
}
