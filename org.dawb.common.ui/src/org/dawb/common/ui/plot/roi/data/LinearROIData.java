/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawb.common.ui.plot.roi.data;

import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.january.dataset.CompoundDataset;
import org.eclipse.january.dataset.Dataset;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;

/**
 * Class to aggregate information associated with a ROI
 */
public class LinearROIData extends ROIData {
	/**
	 * Construct new object from given roi and data
	 * @param roi
	 * @param data
	 * @param step
	 */
	public LinearROIData(LinearROI roi, Dataset data, double step) {
		super();

		setROI(roi.copy());
		profileData = ROIProfile.line(data, roi, step);
		if (profileData != null && profileData[0].getShape()[0] > 1) {
			Dataset pdata;
			for (int i = 0; i < 2; i++) {
				pdata = profileData[i];
				if (pdata instanceof CompoundDataset) // use first element
					profileData[i] = ((CompoundDataset) pdata).getElements(0);
			}
			Number sum = (Number) profileData[0].sum();
			profileSum = sum.doubleValue() * step;

			xAxes = new AxisValues[] { null, null };
			xAxes[0] = new AxisValues();
			xAxes[1] = new AxisValues();

			Dataset axis;
			axis = profileData[0].getIndices().squeeze();
			axis.imultiply(step);
			xAxes[0].setValues(axis);

			if (roi.isCrossHair()) {
				axis = profileData[1].getIndices().squeeze();
				axis.imultiply(step);
				xAxes[1].setValues(axis);
			}
		} else {
			roi.setPlot(false);
		}
	}

	/**
	 * Aggregate a copy of ROI data to this object
	 * @param roi
	 * @param profileData
	 * @param axes
	 * @param profileSum
	 */
	public LinearROIData(LinearROI roi, Dataset[] profileData, AxisValues[] axes, double profileSum) {
		super();
		setROI(roi.copy());
		this.profileData = profileData.clone();
		for (int i = 0; i < profileData.length; i++) {
			this.profileData[i] = profileData[i].clone();
		}
		xAxes = axes.clone();
		for (int i = 0; i < axes.length; i++) {
			xAxes[i] = axes[i].clone();
		}
		this.profileSum = profileSum;
		roi.setPlot(false);
	}

	/**
	 * @return linear region of interest
	 */
	@Override
	public LinearROI getROI() {
		return (LinearROI) roi;
	}
}
