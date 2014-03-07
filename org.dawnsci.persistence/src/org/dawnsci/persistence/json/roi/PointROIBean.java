/*-
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.persistence.json.roi;

import com.fasterxml.jackson.annotation.JsonIgnore;

import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.PointROI;

public class PointROIBean extends ROIBean {

	public static final String TYPE = "PointROI";

	public PointROIBean () {
		type = TYPE;
	}

	@JsonIgnore
	@Override
	public IROI getROI() {
		PointROI proi = new PointROI();
		proi.setName(this.getName());
		proi.setPoint(this.getStartPoint());
		return proi;
	}
}
