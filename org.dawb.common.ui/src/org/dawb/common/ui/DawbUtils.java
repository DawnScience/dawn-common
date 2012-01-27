/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.ui;

import org.eclipse.core.resources.ResourcesPlugin;

public class DawbUtils {

	public static String getDawbHome() {
		return ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString()+"/.metadata/.dawb/";
	}

}
