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
import java.util.HashMap;
import java.util.Map;

import org.dawb.common.services.ServiceManager;
import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.services.conversion.IConversionContext.ConversionScheme;
import org.dawb.common.services.conversion.IConversionService;
import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
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
	
	private IConversionWizardPage selectedConversionPage;
	private Map<ConversionScheme, IConversionWizardPage> conversionPages;

	private IConversionService service;
	private ConversionChoicePage setupPage;

	public ConvertWizard() {
		
		// It's an OSGI service, not required to use ServiceManager
		try {
			this.service = (IConversionService)ServiceManager.getService(IConversionService.class);
		} catch (Exception e) {
			logger.error("Cannot get conversion service!", e);
			return;
		}
		
		// Add choice of file(s) and conversion type page.
		this.setupPage = new ConversionChoicePage("Conversion Type", service);
		addPage(setupPage);
		
		// Create map of possible pages, only one of which will be selected at one time.
		this.conversionPages = new HashMap<IConversionContext.ConversionScheme, IConversionWizardPage>(7);
		final IConfigurationElement[] ce = Platform.getExtensionRegistry().getConfigurationElementsFor("org.dawnsci.conversion.ui.conversionPage");
		if (ce!=null) for (IConfigurationElement e : ce) {
			
			final String schemeName  = e.getAttribute("conversion_scheme");
			final ConversionScheme s = Enum.valueOf(ConversionScheme.class, schemeName);
			if (s.isUserVisible()) {
				try {
					final IConversionWizardPage p = (IConversionWizardPage)e.createExecutableExtension("conversion_page");
					conversionPages.put(s, p);
					addPage(p);
				} catch (CoreException e1) {
					logger.error("Cannot get page "+e.getAttribute("conversion_page"), e1);
				}
			}
		}
		this.selectedConversionPage = conversionPages.get(ConversionScheme.values()[0]);
		
		setWindowTitle("Convert Data Wizard");
	}
	
    public boolean canFinish() {
    	
   		IConversionContext context = setupPage.getContext();
   		
     	// We select only the preferred page.
    	if (setupPage.isPageComplete() && context!=null) {
    		final ConversionScheme scheme = context.getConversionScheme();
    		selectedConversionPage = conversionPages.get(scheme);
    		for (ConversionScheme s : conversionPages.keySet()) {
    			if (conversionPages.get(s)!=null) {
    				conversionPages.get(s).setVisible(s==scheme);
    			}
			}
    	}
    	if (setupPage.isPageComplete() && context!=null && selectedConversionPage!=null && !selectedConversionPage.isPageComplete()) {
    		selectedConversionPage.setContext(context);
    		return false;
    	}
    	return setupPage.isPageComplete() && (selectedConversionPage==null || selectedConversionPage.isPageComplete());
    }


	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		      
 	}


	@Override
	public boolean performFinish() {
		
		final IConversionContext context = selectedConversionPage!=null
				                         ? selectedConversionPage.getContext()
				                         : setupPage.getContext();
		try {
			// Use the progressible task in the wizard
			getContainer().run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						context.setMonitor(new ProgressMonitorWrapper(monitor));
						monitor.setTaskName("Convert ");
						monitor.beginTask("Convert "+context.getFilePath(), 100);
						monitor.worked(1);
						service.process(context);
						
						IResource res = null;
						try { // Try to refresh parent incase it is in the worspace.
							final String workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
							final File   output    = new File(context.getOutputPath());
							final String fullPath  = output.isDirectory() ? output.getAbsolutePath() : output.getParent();
							final String frag      = fullPath.substring(workspace.length());
							res    = ResourcesPlugin.getWorkspace().getRoot().findMember(frag);
							res.refreshLocal(IResource.DEPTH_ONE, monitor);
							
						} catch (Throwable ne) {
							// it's ok
						}
						
						final IResource finalRes = res; 
						if (selectedConversionPage.isOpen() &&  finalRes!=null && finalRes instanceof IFile) {
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									try {
										EclipseUtils.openEditor((IFile)finalRes);
									} catch (PartInitException e) {
										logger.error("Cannot open "+context.getOutputPath(), e);
									}
								}
							});
						}
						
					} catch (Exception e) {
						throw new InterruptedException(e.getMessage());
					} finally {
						monitor.done();
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
