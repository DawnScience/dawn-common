/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.persistence.json.roi;

import java.util.Arrays;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;

import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * Sector ROI bean
 * @author wqk87977
 *
 */
public class SectorROIBean extends RingROIBean {

	public static final String TYPE = "SectorROI";

	private double[] angles;   // angles in radians

	private int symmetry; // symmetry

	private boolean combineSymmetry; // combine symmetry option for profile (where appropriate)

	public SectorROIBean(){
		type = TYPE;
	}

	/**
	 * Set the angles
	 * @param angles
	 */
	public void setAngles(double[] angles){
		this.angles = angles;
	}

	/**
	 * Set the symmetry
	 * @param symmetry
	 */
	public void setSymmetry(int symmetry){
		this.symmetry = symmetry;
	}

	/**
	 * Returns the angle
	 * @return angles
	 */
	public double[] getAngles(){
		return angles;
	}

	/**
	 * Returns the symmetry
	 * @return symmetry
	 */
	public int getSymmetry(){
		return symmetry;
	}

	public boolean isCombineSymmetry() {
		return combineSymmetry;
	}

	public void setCombineSymmetry(boolean combineSymmetry) {
		this.combineSymmetry = combineSymmetry;
	}

	@Override
	public String toString() {
		return String.format("{\"type\": \"%s\", \"name\": \"%s\", \"startPoint\": \"%s\", \"angles\": \"%s\", \"symmetry\": \"%s\", \"radii\": \"%s\", \"dpp\": \"%s\"}", 
				type, name, Arrays.toString(startPoint), Arrays.toString(angles), symmetry, Arrays.toString(radii), dpp);
	}

	@Override
	@JsonIgnore
	public IROI getROI() {
		SectorROI sroi = new SectorROI();
		sroi.setName(getName());
		sroi.setPoint(getStartPoint());
		sroi.setRadii(getRadii());
		sroi.setAngles(getAngles());
		sroi.setDpp(getDpp());
		sroi.setSymmetry(getSymmetry());
		sroi.setClippingCompensation(isClippingCompensation());
		sroi.setCombineSymmetry(isCombineSymmetry());
		sroi.setAverageArea(isAverageArea());
		return sroi;
	}
}
