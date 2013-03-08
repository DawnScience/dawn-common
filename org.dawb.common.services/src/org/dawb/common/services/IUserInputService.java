/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.services;


public interface IUserInputService {

	/**
	 * Opens and returns a part which implements RemoveWorkbenchPart. The part is used to 
	 * edit user input.
	 * 
	 * @param partName
	 * @param isDialog
	 * @return a IRemoteWorkbenchPart
	 */
	public Object openUserInputPart(final String partName, final boolean isDialog)  throws Exception;

	/**
	 * Call to get a RemoteWorkbenchPart for editing the plot. The RemoteWorkbenchPart must have
	 * setUserInput(UserInputBean bean) called on it or it will not configure propertly.
	 * 
	 * 
	 * @param partName
	 * @param dialog
	 * @return IRemoteWorkbenchPart
	 */
	public Object openUserPlotPart(String partName, boolean dialog) throws Exception;

	/**
	 * Sends debug information to the Value view or a similar part for inspecting data.
	 * 
	 * This part in this context also provides next and play / stop buttons
	 * 
	 * @param partName
	 * @param dialog - should be false currently, debugging is done in the Value view.
	 * @return IRemoteWorkbenchPart
	 */
	public Object openDebugPart(String partName, boolean dialog) throws Exception;
}
