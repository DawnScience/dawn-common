/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.ui.project;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

public class FolderImportHandler extends AbstractHandler implements
		IWorkbenchWindowActionDelegate {

	@Override
	public void run(IAction action) {
		openWizard();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		openWizard();
		return null;
	}

	private void openWizard() {
		
		final IImportWizard     wizard    = new FolderLinkWizard();
		final IWorkbench        workbench = PlatformUI.getWorkbench();
		final ISelectionService service   = workbench.getActiveWorkbenchWindow().getSelectionService();
		
		final ISelection     selection = service.getSelection();
		final IStructuredSelection sel = selection instanceof IStructuredSelection ? (IStructuredSelection)selection : null;
		wizard.init(workbench, sel);
		
		WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell() , wizard);
		dialog.setBlockOnOpen(true);
		dialog.open();
		
	}

	@Override
	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub

	}

}
