/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawb.common.ui.views.monitor.actions;


import org.dawb.common.ui.views.monitor.MonitorView;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;


/**
 *
 */
public class MoveDownHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get current dashboard view.
		MonitorView dashboard = (MonitorView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
		if (dashboard==null) return Boolean.FALSE;
		
		dashboard.move(1);
		
		return Boolean.TRUE;
	}

}
