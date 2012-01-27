/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.ui.views.monitor;

import java.util.EventObject;


/**
 *
 */
public class HardwareObjectEvent extends EventObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8767864000873167034L;
	
	/**
	 * @param source
	 */
	public HardwareObjectEvent(Object source) {
		super(source);
	}
}
