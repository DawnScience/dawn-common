/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.common.services;


/**
 * A service for registering and retrieving plotting systems by name.
 * 
 * NOTE A programmer can decide not to register a plotting system by providing no (null)
 * part to the createPlotPart(...) method @see IPlottingSystem.
 * 
 * The parameterised class which this service refers to is usually IPlottingSystem<Composite>,
 * however this interface cannot see that interface because we are in the services
 * plugin which must have no dependencies.
 * 
 * The principle use of this service is to link plotting with name so that the python
 * interface to the plotting works. However you could use it whenever you have a part
 * which you think has a plotting system on it. AbstractPlottingSystem does the registration.
 * 
 * To get the service do:
 * 
 * <code>
 * 		final ISystemService<IPlottingSystem<Composite>> service = (ISystemService<IPlottingSystem<Composite>>)PlatformUI.getWorkbench().getService(ISystemService.class);
 *      final IPlottingSystem<Composite> system = service.getPlottingSystem("Plot 1");
 *      // We now have the plotter for plot 1 and can do things, for instance from SDAPlotter...
 *      // For this to work 'Plot 1' in this case would also need to be a view using PlottingFactory to 
 *      // provide its plotting.
 *<code>
 *
 * @author Matthew Gerring
 *
 */
public interface ISystemService<T> {

	/**
	 * Get an IPlottingSystem<Composite> for a given part name.
	 * @param name
	 * @return
	 */
	public T getSystem(final String partName);
	
	/**
	 * Used to record plotting systems in the service.
	 * @param partName
	 * @param plottingSystem
	 * @return the previous plotting system, if any.
	 */
	public T putSystem(final String partName, final T plottingSystem);
	
	/**
	 * Removes and returns a plotting system. Generally used on the dispose() of 
	 * AbstractPlottingSystem to avoid memory leaks.
	 * @param partName
	 * @return
	 */
	public T removeSystem(final String partName);
	
	/**
	 * Clears everything registered. Avoids memory leaks but rather drastic.
	 */
	public void clear();
}
