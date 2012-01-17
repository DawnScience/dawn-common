/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.ui.project;

import java.lang.reflect.InvocationTargetException;

import org.dawb.common.ui.project.DawbResourcePage.RESOURCE_CHOICE;
import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DADWizard extends Wizard implements INewWizard {

	private static Logger logger = LoggerFactory.getLogger(DADWizard.class);
	
	private DawbResourcePage     page;
	private IStructuredSelection selection;

	public DADWizard() {
		super();
		setNeedsProgressMonitor(true);
	}
	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page = new DawbResourcePage("Folder for Sequence File",selection, RESOURCE_CHOICE.PROJECT_AND_NAME, "DADSequence.xml");
		addPage(page);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

	

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	public boolean performFinish() {
		
		final IContainer container = page.getProjectContainer();
		final String     seqName   = page.getSequenceName();
		
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(container,  seqName, monitor);
				} catch (Exception e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}

		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}
	
	private void doFinish(IContainer container, String seqName, IProgressMonitor monitor) {
		try {
			
			monitor.beginTask("Create "+seqName, 5);
            final IFile      file      = container instanceof IFolder
                                       ? ((IFolder)container).getFile(seqName)
                                       : ((IProject)container).getFile(seqName);
            
            file.create(DADWizard.class.getResourceAsStream("sequence.xml"), true, monitor);
            monitor.worked(1);
            container.refreshLocal(IResource.DEPTH_ONE, monitor);
            
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
		            try {
						EclipseUtils.openEditor(file);
					} catch (PartInitException e) {
						logger.error("Cannot open editor", e);
					}
				}
            });
            
		} catch (Exception ne) {
			logger.error("Cannot create sequence", ne);
		}		
	}


}
