/*-
 * Copyright (c) 2014 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.persistence.json.roi;

import java.util.Iterator;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularFitROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolylineROI;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CircularFitROIBean extends ROIBean {

	public static final String TYPE = "CircularFitROI";
	private List<double[]> points;
	private double radius;

	public CircularFitROIBean() {
		this.type = TYPE;
	}

	public CircularFitROIBean(String name) {
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

	/**
	 * Set points which are then used to fit circle
	 * @param points
	 */
	public void setPoints(List<double[]> points) {
		this.points = points;
	}

	/**
	 * Set points which are then used to fit circle
	 * @return points
	 */
	public List<double[]> getPoints() {
		return points;
	}

	@Override
	@JsonIgnore
	public IROI getROI() {
		Iterator<double[]> it = this.getPoints().iterator();
		PolylineROI poly = new PolylineROI();
		while (it.hasNext()) {
			double[] point = it.next();
			poly.insertPoint(point);
		}
		CircularFitROI croi = new CircularFitROI(poly);
		croi.setName(getName());
		return croi;
	}

}
