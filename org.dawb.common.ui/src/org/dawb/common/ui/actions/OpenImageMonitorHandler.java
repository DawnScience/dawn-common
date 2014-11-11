/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.ui.actions;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.views.ImageMonitorView;
import org.dawb.common.util.io.FileUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;


public class OpenImageMonitorHandler extends AbstractHandler implements IObjectActionDelegate {

//	private static final Logger logger = LoggerFactory.getLogger(OpenImageMonitorHandler.class);
	
	@Override
	public void run(IAction action) {
		doAction();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		return doAction();
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub
		
	}

	
	private Object doAction() {
		
		final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		final IStructuredSelection sel = (IStructuredSelection)page.getSelection();
		ImageMonitorView view=null;
		try {
			view = (ImageMonitorView)EclipseUtils.getPage().showView(ImageMonitorView.ID);
		} catch (PartInitException e1) {
			return null;
		}
		
		if (sel!=null && view!=null && sel.getFirstElement()!=null) {
			
			final ImageMonitorView finalView = view;
			Runnable job = new Runnable() {
				
				@Override
				public void run() {
					
					final Object object = sel.getFirstElement();
					final String path = FileUtils.getDirectoryAbsolutePath(object);
					if (path!=null) finalView.setDirectoryPath(path);

				}
			};
			PlatformUI.getWorkbench().getDisplay().asyncExec(job);
		}
		
		
		return Boolean.TRUE;
	}


}
