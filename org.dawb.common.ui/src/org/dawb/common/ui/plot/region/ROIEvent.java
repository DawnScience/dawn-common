package org.dawb.common.ui.plot.region;

import java.util.EventObject;

import uk.ac.diamond.scisoft.analysis.roi.ROIBase;

public class ROIEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5892437380421200585L;
	private ROIBase roi;

	public ROIEvent(Object source, ROIBase region) {
		super(source);
		this.roi = region;
	}

	public ROIBase getROI() {
		return roi;
	}

}
