/*-
 * Copyright (c) 2014 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.persistence.json.roi;

import java.util.Arrays;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.ParabolicROI;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A parabolic region of interest with the start point as the focus. In the rotated frame,
 * it can be represented as x-p = 4 a y^2 where p = 2 a
 */
public class ParabolicROIBean extends ROIBean {
	public static final String TYPE = "ParabolicROI";

	private double focalParameter;
	private double angle;

	public ParabolicROIBean() {
	}

	/**
	 * @return Returns focal parameter
	 */
	public double getFocalParameter() {
		return focalParameter;
	}

	/**
	 * Set focal parameter
	 * @param focal
	 */
	public void setFocalParameter(double focalParameter) {
		this.focalParameter = focalParameter;
	}

	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	@Override
	public String toString() {
		return super.toString() + String.format("point=%s, focal=%g, angle=%g", Arrays.toString(startPoint),
				getFocalParameter(), getAngle());
	}

	@Override
	@JsonIgnore
	public IROI getROI() {
		ParabolicROI proi = new ParabolicROI();
		proi.setPoint(getStartPoint());
		proi.setFocalParameter(getFocalParameter());
		proi.setAngle(getAngle());
		proi.setName(getName());
		return proi;
	}
}
