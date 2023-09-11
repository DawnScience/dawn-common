/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawnsci.conversion.ui;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.conversion.ui.api.IConversionWizardPage;
import org.dawnsci.conversion.ui.api.IConversionWizardPageService;
import org.dawnsci.conversion.ui.api.IFileOverrideWizard;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionScheme;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionService;
import org.eclipse.dawnsci.plotting.api.ProgressMonitorWrapper;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.osgi.services.ServiceProvider;

/**
 *   ConvertWizard
 *
 *   @author gerring
 *   @date Aug 31, 2010
 *   @project org.dawb.workbench.actions
 **/
public class ConvertWizard extends Wizard implements IExportWizard, IFileOverrideWizard{

	private static final Logger logger = LoggerFactory.getLogger(ConvertWizard.class);
	
	private IConversionWizardPage selectedConversionPage;

	private IConversionService service;
	private IConversionWizardPageService pageService;
	private ConversionChoicePage setupPage;

	private List<String> overridePaths;

	public ConvertWizard() {
		setNeedsProgressMonitor(true);
	}

	public void addPages() {
		service = ConversionServiceLoader.getService();
		if (service == null) {
			logger.error("Cannot get conversion service through OSGI injection");
			return;
		}
		
		try {
			pageService = ServiceProvider.getService(IConversionWizardPageService.class);
		} catch (IllegalArgumentException e) {
			logger.error("Cannot get conversion wizardpage service through OSGI injection");
			return;
		}
		
		// Add choice of file(s) and conversion type page.
		this.setupPage = new ConversionChoicePage("Conversion Type", service);
		setupPage.setSelectedFiles(overridePaths);
		addPage(setupPage);
		
		IConversionWizardPage[] pages = pageService.getPages(true);
		
		for (IConversionWizardPage page : pages) {
			addPage(page);
		}
		this.selectedConversionPage = pages[0];
		
		setWindowTitle("Convert Data Wizard");

	}
	
	public void setFileSelectionOverride(List<File> files) {
		
		final List<String> paths = new ArrayList<>();
		final List<String> sets  = new ArrayList<>();
		
		if (!files.isEmpty()) {
			for (File ob : files) {
				if (!paths.contains(ob.getAbsolutePath())) {
					paths.add(ob.getAbsolutePath());
					sets.add(ob.getPath());
				}
			}
		}

		this.overridePaths    = paths;
	}
	
	

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		      
 	}
	
    public boolean canFinish() {
       if (setupPage!=null && !setupPage.isPageComplete()) return false;
       if (selectedConversionPage!=null && !selectedConversionPage.isPageComplete()) return false;
       return true;
    }
    
	@Override
    public IWizardPage getNextPage(IWizardPage page) {

    	if (page==setupPage) {
       		IConversionScheme scheme = setupPage.getScheme();
       		selectedConversionPage = pageService.getPage(scheme);
       		if (selectedConversionPage!=null) selectedConversionPage.setContext(setupPage.getContext());
       		return selectedConversionPage;
    	} else if (page instanceof IConversionWizardPage) {
    		return null; // Only 1 allowed.
    	}
    	return null;
    }
	
	@Override
    public IWizardPage getPreviousPage(IWizardPage page) {

    	if (page==setupPage) {
       		return null;
     	} else if (page instanceof IConversionWizardPage) {
    		return setupPage;
    	}
    	return null;
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
						monitor.beginTask("Convert to "+context.getConversionScheme().getUiLabel(), context.getWorkSize());
						monitor.worked(1);
						service.process(context);
						File f = new File(context.getOutputPath());
						if (f.exists() && f.isFile() && !f.getName().toLowerCase().endsWith(".avi") && !f.getName().toLowerCase().endsWith(".mp4")) {
							EclipseUtils.refreshAndOpen(context.getOutputPath(), selectedConversionPage.isOpen(), monitor);
						}
						
					} catch (final Exception e) {
						logger.error("Cannot process", e);
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								final StringBuilder buf = new StringBuilder();
								if (context.getFilePaths()!=null) {
									buf.append( "The file(s) ");
									buf.append(Arrays.toString(context.getFilePaths().toArray()));
									buf.append( " were not converted!\n");
								}
								if (e.getMessage()!=null) buf.append(e.getMessage());
								ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
													"File(s) Not Converted", 
													null,
													new Status(IStatus.WARNING, "org.dawb.workbench.actions", buf.toString(), e));
							}
						});
					return;
					} finally {
						monitor.done();
					}
				}
			});
		} catch (Throwable ne) {
            logger.warn("Conversion interupted!", ne);
		}

		return true;
	}
}
