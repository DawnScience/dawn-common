/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.gda.extensions.util;

import org.dawb.common.util.io.IPathConstructor;

/**
 *   PathIO
 *
 *   @author gerring
 *   @date Jul 26, 2010
 *   @project org.edna.workbench.application
 **/
public class PathIO implements IPathConstructor {

	/**
	 * Gets the default path for the workspace results to be imported
	 */
	@Override
	public String getDefaultDataDir() {
		
        return null;
	}

	@Override
	public String getFromTemplate(String pattern) {
		return null;
	}

}
