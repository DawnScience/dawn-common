/*-
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.persistence.json.roi;

import java.util.Arrays;

import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.RingROI;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class RingROIBean extends ROIBean{

	public static final String TYPE = "RingROI";

	private double[] angles;   // angles in radians

	private int symmetry; // symmetry

	private double[] radii; // radii

	private double dpp; // Sampling rate used for profile calculations in dots per pixel

	public RingROIBean(){
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
	 * Set the radii
	 * @param radii
	 */
	public void setRadii(double[] radii){
		this.radii = radii;
	}

	/**
	 * Set sampling rate used in profile calculations  
	 * 
	 * @param dpp
	 *			sampling rate in dots per pixel; 
	 */
	public void setDpp(double dpp) {
		this.dpp = dpp;
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

	/**
	 * Returns the radii
	 * @return radii
	 */
	public double[] getRadii(){
		return radii;
	}

	/**
	 * Return sampling rate used in profile calculations
	 * 
	 * @return
	 * 			sampling rate in dots per pixel
	 */
	public double getDpp() {
		return dpp;
	}

	@Override
	public String toString(){
		return String.format("{\"type\": \"%s\", \"name\": \"%s\", \"startPoint\": \"%s\", \"angles\": \"%s\", \"symmetry\": \"%s\", \"radii\": \"%s\", \"dpp\": \"%s\"}", 
				type, name, Arrays.toString(startPoint), Arrays.toString(angles), symmetry, Arrays.toString(radii), dpp);
	}

	@Override
	@JsonIgnore
	public IROI getROI() {
		RingROI sroi = new RingROI();
		sroi.setName(getName());
		sroi.setPoint(getStartPoint());
		sroi.setRadii(getRadii());
		sroi.setAngles(getAngles());
		sroi.setDpp(getDpp());
		sroi.setSymmetry(getSymmetry());
		return sroi;
	}
}

