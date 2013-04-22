/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawnsci.conversion.ui;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.dawb.common.services.ServiceManager;
import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.services.conversion.IConversionService;
import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.conversion.ui.pages.AsciiConvertPage;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 *   ConvertWizard
 *
 *   @author gerring
 *   @date Aug 31, 2010
 *   @project org.edna.workbench.actions
 **/
public class ConvertWizard extends Wizard implements IExportWizard{

	private static final Logger logger = LoggerFactory.getLogger(ConvertWizard.class);
	
	private AbstractConversionPage customConversionPage;

	private IConversionService service;
	private ConversionChoicePage setupPage;

	public ConvertWizard() {
		
		// It's an OSGI service, not required to use ServiceManager
		try {
			this.service = (IConversionService)ServiceManager.getService(IConversionService.class);
		} catch (Exception e) {
			logger.error("Cannot get cnversion service!", e);
		}
		
		// Add choice of file(s) and conversion type page.
		this.setupPage = new ConversionChoicePage("Conversion Type", service);
		addPage(setupPage);
		
		// TODO Hard coded to ascii! Need extension point
		this.customConversionPage = new AsciiConvertPage();
		addPage(customConversionPage);
		setWindowTitle("Convert Data Wizard");
	}
	
    public boolean canFinish() {
    	if (setupPage.isPageComplete() && !customConversionPage.isPageComplete()) {
    		IConversionContext context = setupPage.getContext();
    		customConversionPage.setContext(context);
    		return false;
    	}
    	return setupPage.isPageComplete() && customConversionPage.isPageComplete();
    }


	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		      
 	}


	@Override
	public boolean performFinish() {
		
		final IConversionContext context = customConversionPage.getContext();
		try {
			// Use the progressible task in the wizard
			getContainer().run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						monitor.beginTask("Convert "+context.getFilePath(), 100);
						context.setMonitor(new ProgressMonitorWrapper(monitor));
						service.process(context);
						
						try { // Try to refresh parent incase it is in the worspace.
							final String workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
							final File   output    = new File(context.getOutputPath());
							final String fullPath  = output.isDirectory() ? output.getAbsolutePath() : output.getParent();
							final String frag      = fullPath.substring(workspace.length());
							final IResource res    = ResourcesPlugin.getWorkspace().getRoot().findMember(frag);
							res.refreshLocal(IResource.DEPTH_ONE, monitor);
							
						} catch (Throwable ne) {
							// it's ok
						}
						
						if (customConversionPage.isOpen()) {
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									try {
										final String workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
										final String frag      = context.getOutputPath().substring(workspace.length());
										final IResource res    = ResourcesPlugin.getWorkspace().getRoot().findMember(frag);
										EclipseUtils.openEditor((IFile)res);
									} catch (PartInitException e) {
										logger.error("Cannot open "+context.getOutputPath(), e);
									}
								}
							});
						}
						
						monitor.done();
					} catch (Exception e) {
						throw new InterruptedException(e.getMessage());
					}
				}
			});
		} catch (Throwable ne) {
			final String message = "The file '"+context.getFilePath()+"' was not converted!";
			ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					"File Not Converted", 
					ne.getMessage(),
					new Status(IStatus.WARNING, "org.edna.workbench.actions", message, ne));

		}

		return true;
	}

}
