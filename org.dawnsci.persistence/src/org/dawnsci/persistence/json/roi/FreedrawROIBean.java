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
import org.eclipse.dawnsci.analysis.dataset.roi.FreeDrawROI;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class FreedrawROIBean extends ROIBean{

	public static final String TYPE = "FreedrawROI";
	private List<double[]> points;

	public FreedrawROIBean(){
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
		FreeDrawROI fdroi = new FreeDrawROI(getStartPoint());
		Iterator<double[]> it = getPoints().iterator();
		while (it.hasNext()){
			double[] point = it.next();
			fdroi.insertPoint(point);
		}
		fdroi.setName(getName());
		return fdroi;
	}
}
