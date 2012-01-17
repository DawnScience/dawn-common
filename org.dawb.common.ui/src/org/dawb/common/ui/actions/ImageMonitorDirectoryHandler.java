/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawb.common.ui.actions;

import org.dawb.common.ui.views.ImageMonitorView;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ImageMonitorDirectoryHandler extends AbstractHandler {

    private static final Logger logger = LoggerFactory.getLogger(ImageMonitorDirectoryHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		ImageMonitorView view = (ImageMonitorView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(ImageMonitorView.ID);
		if (view != null) {
			DirectoryDialog dirDialog = new DirectoryDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), SWT.OPEN);
			dirDialog.setFilterPath(view.getDirectoryPath());
			final String filepath = dirDialog.open();
			if (filepath != null) {
				view.setDirectoryPath(filepath);
			}		
		} else {
			logger.info("Couldn't find view to load for");
		}

		return Boolean.FALSE;
	}

	
}
