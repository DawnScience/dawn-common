/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.ui.plot.trace;

import java.util.EventListener;


public interface ITraceListener extends EventListener{

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
	 *  Source of event is IPlottingSystem
	 * 
	 * @param evt
	 */
	public void tracesAltered(final TraceEvent evt);
	
	/**
	 * Called when all traces are cleared. Source of event is IPlottingSystem
	 * @param evet
	 */
	public void tracesCleared(TraceEvent evet);
	
	/**
	 * Fired when a new trace is plotted. Source of event is ITrace
	 * @param evt
	 */
	public void tracePlotted(TraceEvent evt);
	
	/**
	 * Convenience class for creating listeners
	 * @author fcp94556
	 *
	 */
	public class Stub implements ITraceListener {

		@Override
		public void tracesAltered(TraceEvent evt) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void tracesCleared(TraceEvent evet) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void tracePlotted(TraceEvent evt) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
