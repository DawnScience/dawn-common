/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.persistence.json.roi;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PointROIBean extends ROIBean {

	public static final String TYPE = "PointROI";

	public PointROIBean () {
		type = TYPE;
	}

	@JsonIgnore
	@Override
	public IROI getROI() {
		PointROI proi = new PointROI();
		proi.setName(getName());
		proi.setPoint(getStartPoint());
		return proi;
	}
}
