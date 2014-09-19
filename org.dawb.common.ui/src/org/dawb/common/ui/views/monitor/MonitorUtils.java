/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.ui.views.monitor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

public class MonitorUtils {

	/**
	 * Reads extension point to get class implementing ServerObject which
	 * knows how to connect to the hardware.
	 * 
	 * @return
	 * @throws CoreException 
	 */
	public static HardwareObject createHardwareObject() throws CoreException {
		
		return createHardwareObject(null);
	}

	/**
	 * Reads extension point to get class implementing ServerObject which
	 * knows how to connect to the hardware.
	 * 
	 * @param hardwareName, may be null
	 * @return
	 * @throws CoreException 
	 */
	public static HardwareObject createHardwareObject(final String hardwareName) throws CoreException {
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
		                                       "org.dawb.common.ui.hardwareClass");
		
		final HardwareObject newOb = (HardwareObject)config[0].createExecutableExtension("class");
		if (hardwareName!=null) newOb.setHardwareName(hardwareName);
		
		return newOb;

	}

}
