package org.dawb.common.ui.plot.axis;

import java.util.EventListener;


public interface ICoordinateSystemListener extends EventListener {
	public void coordinatesChanged(CoordinateSystemEvent evt);
}
