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
	 * Fired whenever a new trace is created, the source of TraceEvent
	 * is the ITrace created.
	 * 
	 * @param evt
	 */
	public void traceCreated(final TraceEvent evt);
	
	/**
	 * Fired whenever an existing trace is updated, the source of TraceEvent
	 * is the ITrace created.
	 * 
	 * @param evt
	 */
	public void traceUpdated(final TraceEvent evt);

	/**
	 * Fired whenever a new trace is added, the source of TraceEvent
	 * is the ITrace added.
	 * 
	 * @param evt
	 */
	public void traceAdded(final TraceEvent evt);

	/**
	 * Fired whenever a  trace is deleted, the source of TraceEvent
	 * is the ITrace.
	 * 
	 * @param evt
	 */
	public void traceRemoved(final TraceEvent evt);

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
	public void tracesUpdated(final TraceEvent evt);
	
	/**
	 * Called when all traces are cleared. Source of event is IPlottingSystem
	 * @param evet
	 */
	public void tracesRemoved(TraceEvent evet);
	
	/**
	 * Fired when new traces are plotted. Source of event is ITrace or List<ITrace>
	 * @param evt
	 */
	public void tracesAdded(TraceEvent evt);
	
	/**
	 * Convenience class for creating listeners
	 * @author fcp94556
	 *
	 */
	public class Stub implements ITraceListener {

		@Override
		public void tracesUpdated(TraceEvent evt) {
			update(evt);
		}

		@Override
		public void tracesRemoved(TraceEvent evt) {
			update(evt);
		}

		@Override
		public void tracesAdded(TraceEvent evt) {
			update(evt);
		}

		@Override
		public void traceCreated(TraceEvent evt) {
			update(evt);
		}

		@Override
		public void traceUpdated(TraceEvent evt) {
			update(evt);
		}

		@Override
		public void traceAdded(TraceEvent evt) {
			update(evt);
		}

		@Override
		public void traceRemoved(TraceEvent evt) {
			update(evt);
		}
		
		protected void update(TraceEvent evt) {
			// Nothing
		}
		
	}
}
