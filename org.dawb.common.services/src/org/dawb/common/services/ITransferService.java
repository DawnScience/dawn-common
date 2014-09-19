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
 * This class contains different methods for creating transferrable
 * objects. For instance for creating an Object which can be used as 
 * a transferrable by the plotting for a ROISource in the workflows.
 * 
 * This allows plotting and workflows not to be connected.
 */
public interface ITransferService {

	
	/**
	 * Creates a ROISource for a transferrable without giving a hard dependency
	 * on ROISource.
	 * 
	 * @param name
	 * @param roi
	 * @return
	 */
	public Object createROISource(final String name, final Object roi) throws Exception;
}
