/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawb.common.util.io;


/**
 * Used to expose PathConstructor to projects not explicitly importing core.
 */
public interface IPathConstructor {

	String getDefaultDataDir();

	String getFromTemplate(String template);

}
