/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawb.common.ui.plot.roi.data;

import org.eclipse.dawnsci.analysis.dataset.impl.CompoundDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;

/**
 * Class to aggregate information associated with a ROI
 */
public class SectorROIData extends ROIData {
	/**
	 * Construct new object from given roi and data
	 * @param sroi
	 * @param data 
	 */
	public SectorROIData(SectorROI sroi, Dataset data) {
		this(sroi, data, null, 1.);
	}

	public SectorROIData(SectorROI sroi, Dataset data, Dataset mask) {
		this(sroi, data, mask, 1.);
	}

	/**
	 * Construct new object from given roi and data
	 * @param sroi
	 * @param data
	 * @param subFactor
	 */
	public SectorROIData(SectorROI sroi, Dataset data, double subFactor) {
		this(sroi, data, null, subFactor);
	}

	public SectorROIData(SectorROI sroi, Dataset data, Dataset mask, double subFactor) {
		super();
		setROI(sroi.copy());
		roi.downsample(subFactor);
		profileData = ROIProfile.sector(data, mask, (SectorROI) roi);
		if (profileData != null && profileData[0].getShape()[0] > 1 && profileData[1].getShape()[0] > 1) {
			Dataset pdata;
			for (int i = 0; i < 4; i++) {
				pdata = profileData[i];
				if (pdata instanceof CompoundDataset) // use first element
					profileData[i] = ((CompoundDataset) pdata).getElements(0);
			}
			Number sum = (Number) profileData[0].sum();
			profileSum = sum.doubleValue();

			xAxes = new AxisValues[] { null, null, null, null };
			xAxes[0] = new AxisValues();
			xAxes[1] = new AxisValues();
			xAxes[2] = new AxisValues();
			xAxes[3] = new AxisValues();

			Dataset axis;
			axis = DatasetFactory.createLinearSpace(sroi.getRadius(0), sroi.getRadius(1), profileData[0].getSize(), Dataset.FLOAT64);//profileData[0].getIndices().squeeze();
			xAxes[0].setValues(axis);

			if (sroi.getSymmetry() != SectorROI.FULL)
				axis = DatasetFactory.createLinearSpace(sroi.getAngleDegrees(0), sroi.getAngleDegrees(1), profileData[1].getSize(), Dataset.FLOAT64);
			else
				axis = DatasetFactory.createLinearSpace(sroi.getAngleDegrees(0), sroi.getAngleDegrees(0) + 360., profileData[1].getSize(), Dataset.FLOAT64);
			xAxes[1].setValues(axis);

			if (sroi.hasSeparateRegions()) {
				axis = DatasetFactory.createLinearSpace(sroi.getRadius(0), sroi.getRadius(1), profileData[2].getSize(), Dataset.FLOAT64);//profileData[0].getIndices().squeeze();
				xAxes[2].setValues(axis);
				axis = DatasetFactory.createLinearSpace(sroi.getAngleDegrees(0), sroi.getAngleDegrees(1), profileData[3].getSize(), Dataset.FLOAT64);
				xAxes[3].setValues(axis);
			}
		} else {
			setPlot(false);
		}
	}

	/**
	 * Aggregate a copy of ROI data to this object
	 * @param sroi
	 * @param profileData
	 * @param axes
	 * @param profileSum
	 */
	public SectorROIData(SectorROI sroi, Dataset[] profileData, AxisValues[] axes, double profileSum) {
		super();
		setROI(sroi.copy());
		this.profileData = profileData.clone();
		for (int i = 0; i < profileData.length; i++) {
			this.profileData[i] = profileData[i].clone();
		}
		xAxes = axes.clone();
		for (int i = 0; i < axes.length; i++) {
			xAxes[i] = axes[i].clone();
		}
		this.profileSum = profileSum;
		setPlot(false);
	}

	/**
	 * @return linear region of interest
	 */
	@Override
	public SectorROI getROI() {
		return (SectorROI) roi;
	}
}
