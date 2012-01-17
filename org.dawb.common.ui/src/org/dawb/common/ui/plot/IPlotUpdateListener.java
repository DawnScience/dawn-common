/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.ui.plot;

import java.util.EventListener;

public interface IPlotUpdateListener extends EventListener{

	/**
	 * Notifies the listener that the plot technology
	 * requires the data to be resent back to the plot.
	 * 
	 * For instance the user has reconfigured the plot and
	 * the data should be sent again.
	 * 
	 * It is done this way to avoid caches of the plot data,
	 * which may be large, being made.
	 * 
	 * @param evt
	 */
	public void plotRequested(final PlotUpdateEvent evt);
}
