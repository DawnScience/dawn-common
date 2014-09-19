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
import java.util.Iterator;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolylineROI;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PolylineROIBean extends ROIBean{

	public static final String TYPE = "PolylineROI";
	private List<double[]> points;

	public PolylineROIBean(){
		type = TYPE;
	}

	/**
	 * Set the list of points 
	 * @param points
	 */
	public void setPoints(List<double[]> points){
		this.points = points;
	}

	/**
	 * Returns the list of points (x[0] and y[1] coordinates)
	 * @return points
	 */
	public List<double[]> getPoints(){
		return points;
	}

	@Override
	public String toString(){
		return String.format("{\"type\": \"%s\", \"name\": \"%s\", \"startPoint\": \"%s\", \"points\": \"%s\"}", 
				type, name, Arrays.toString(startPoint), Arrays.toString(points.toArray()));
	}

	@Override
	@JsonIgnore
	public IROI getROI() {
		PolylineROI plroi = new PolylineROI(getStartPoint());
		Iterator<double[]> it = getPoints().iterator();
		while (it.hasNext()) {
			double[] point = it.next();
			plroi.insertPoint(point);
		}
		return plroi;
	}
}
