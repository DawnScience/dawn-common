/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawb.common.ui.plot.roi.data;

import java.awt.Color;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.swt.graphics.RGB;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;

/**
 * Base class to contain bare essentials of region of interest data
 */
public class ROIData implements IRowData {
	protected IROI roi;
	protected Dataset[] profileData;
	protected double profileSum;
	protected RGB plotColourRGB;
	protected AxisValues[] xAxes;

	/**
	 * @param index
	 * @param xAxis The xAxis to set.
	 */
	public void setXAxis(int index, AxisValues xAxis) {
		this.xAxes[index] = xAxis;
	}

	/**
	 * @return Returns the xAxes.
	 */
	public AxisValues[] getXAxes() {
		return xAxes;
	}

	/**
	 * @param index
	 * @return Returns the xAxis.
	 */
	public AxisValues getXAxis(int index) {
		return xAxes[index];
	}

	/**
	 * @return plot colour
	 */
	@Override
	public RGB getPlotColourRGB() {
		return plotColourRGB;
	}

	/**
	 * @return plot colour
	 */
	public Color getPlotColour() {
		return new Color(plotColourRGB.red, plotColourRGB.green, plotColourRGB.blue);
	}

	/**
	 * @param rgb
	 */
	public void setPlotColourRGB(RGB rgb) {
		plotColourRGB = rgb;
	}

	/**
	 * @param index
	 * @param profileData The profileData to set.
	 */
	public void setProfileData(int index, Dataset profileData) {
		this.profileData[index] = profileData;
	}

	/**
	 * @param index
	 * @return Returns the profileData.
	 */
	public Dataset getProfileData(int index) {
		return profileData[index];
	}

	/**
	 * @return Returns the profileData.
	 */
	public Dataset[] getProfileData() {
		return profileData;
	}

	/**
	 * @param profileSum The profileSum to set.
	 */
	public void setProfileSum(double profileSum) {
		this.profileSum = profileSum;
	}

	/**
	 * @return Returns the profileSum.
	 */
	public double getProfileSum() {
		return profileSum;
	}

	/**
	 * @param require set true if plot required 
	 */
	@Override
	public void setPlot(boolean require) {
		roi.setPlot(require);
	}

	/**
	 * @return true if plot is enabled
	 */
	@Override
	public boolean isPlot() {
		return roi.isPlot();
	}

	/**
	 * @param roi
	 */
	public void setROI(IROI roi) {
		this.roi = roi;
	}

	/**
	 * @return region of interest
	 */
	public IROI getROI() {
		return roi;
	}
}
