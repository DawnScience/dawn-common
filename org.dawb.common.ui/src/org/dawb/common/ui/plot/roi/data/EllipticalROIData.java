/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawb.common.ui.plot.roi.data;

import org.eclipse.dawnsci.analysis.dataset.roi.EllipticalFitROI;
import org.eclipse.dawnsci.analysis.dataset.roi.EllipticalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolylineROI;

public class EllipticalROIData extends ROIData {
	public EllipticalROIData(EllipticalROI eroi) {
		roi = eroi;
	}

	public EllipticalROIData(PolylineROI proi) {
		roi = new EllipticalFitROI(proi);
	}
}
