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

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.dawb.common.ui.project.DawbResourcePage.RESOURCE_CHOICE;
import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FolderLinkWizard extends Wizard implements IImportWizard {

	private static Logger logger = LoggerFactory.getLogger(FolderLinkWizard.class);
	
	private DawbResourcePage page;
	private IStructuredSelection selection;

	public FolderLinkWizard() {
		super();
		setNeedsProgressMonitor(true);
	}
	
	/**
	 * Adding the page to the wizard.
	 */
	public void addPages() {
		page = new DawbResourcePage("Folder Import",selection, RESOURCE_CHOICE.PROJECT_AND_EXTERNAL_FOLDER, "Sequence.xml");
		page.setWizard(this);
		addPage(page);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

	@Override
	public boolean performFinish() {

		try {
			final IContainer container = page.getProjectContainer();
			final File       external  = page.getExternalFolder();
			
			final IFolder folder = container.getFolder(new Path(external.getName()));
			getContainer().run(true, true, new IRunnableWithProgress() {
				
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						monitor.beginTask("Creating link to "+folder.getName(), 10);
						folder.createLink(external.toURI(), IResource.DEPTH_ONE, monitor);
						container.refreshLocal(IResource.DEPTH_ONE, monitor);
					} catch (ResourceException e) {
						MessageDialog.openError(getShell(), "Cannot Import Folder", e.getMessage());
						logger.error("Cannot create link folder "+folder.getName(), e);
						
					} catch (CoreException e) {
						logger.error("Cannot create link folder "+folder.getName(), e);
					} finally {
						monitor.done();
					}
				}
			});

		} catch (Exception ne) {
			logger.error("Cannot create link", ne);
			return false;
		}
		return true;
	}

}
