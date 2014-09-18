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

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CircularROIBean extends ROIBean {

	public static final String TYPE = "CircularROI";
	private double radius;

	public CircularROIBean() {
		this.type = TYPE;
	}

	public CircularROIBean(String name) {
		this.type = name;
	}

	/**
	 * Returns the radius
	 * @return radius
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * Set radius
	 * @param radius
	 */
	public void setRadius(double radius) {
		this.radius = radius;
	}

	@Override
	public String toString(){
		return String.format("{\"type\": \"%s\", \"name\": \"%s\", \"startPoint\": \"%s\", \"radius\": \"%s\"}", 
				type, name, Arrays.toString(startPoint), radius);
	}

	@Override
	@JsonIgnore
	public IROI getROI() {
		CircularROI croi = new CircularROI(getRadius(), 
				getStartPoint()[0], getStartPoint()[1]);
		croi.setName(getName());
		return croi;
	}
}
