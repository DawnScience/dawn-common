/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dawb.common.ui.plot.roi.data;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractCompoundDataset;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
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
	public LinearROIData(LinearROI roi, AbstractDataset data, double step) {
		super();

		setROI(roi.copy());
		profileData = ROIProfile.line(data, roi, step);
		if (profileData != null && profileData[0].getShape()[0] > 1) {
			AbstractDataset pdata;
			for (int i = 0; i < 2; i++) {
				pdata = profileData[i];
				if (pdata instanceof AbstractCompoundDataset) // use first element
					profileData[i] = ((AbstractCompoundDataset) pdata).getElements(0);
			}
			Number sum = (Number) profileData[0].sum();
			profileSum = sum.doubleValue() * step;

			xAxes = new AxisValues[] { null, null };
			xAxes[0] = new AxisValues();
			xAxes[1] = new AxisValues();

			AbstractDataset axis;
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
	public LinearROIData(LinearROI roi, AbstractDataset[] profileData, AxisValues[] axes, double profileSum) {
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
