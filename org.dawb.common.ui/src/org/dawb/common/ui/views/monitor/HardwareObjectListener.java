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

/**
 *
 */
public interface HardwareObjectListener {
	/**
	 * You must implement this method in a thread safe (SWT) manner.
	 * Many notifications can come through on this method from different
	 * threads. A queue should be used to deal with this.
	 * 
	 * @param evt
	 */
	void hardwareObjectChangePerformed(HardwareObjectEvent evt);
}
