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

import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.wizard.AbstractSliceConversionPage;
import org.dawnsci.conversion.ui.api.IConversionWizardPage;
import org.dawnsci.conversion.ui.api.IConversionWizardPageService;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionScheme;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionService;
import org.eclipse.dawnsci.slicing.api.data.ITransferableDataObject;
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

	private IConversionService service;
	private IConversionWizardPageService pageService;
	private ConversionChoicePage setupPage;

	private List<String> overridePaths;
	private List<String> overrideDatasets;

	public ConvertWizard() {
		setNeedsProgressMonitor(true);
	}

	public void addPages() {
		service = ConversionServiceLoader.getService();
		if (service == null) {
			logger.error("Cannot get conversion service through OSGI injection");
			return;
		}
		
		pageService = ServiceHolder.getConversionWizardPageService();
		if (pageService == null) {
			logger.error("Cannot get conversion wizardpage service through OSGI injection");
			return;
		}
		
		// Add choice of file(s) and conversion type page.
		this.setupPage = new ConversionChoicePage("Conversion Type", service);
		setupPage.setSelectedFiles(overridePaths);
		addPage(setupPage);
		
		IConversionWizardPage[] pages = pageService.getPages(true);
		
		for (IConversionWizardPage page : pages) {
			if (overrideDatasets!=null && overrideDatasets.size()>0 && page instanceof AbstractSliceConversionPage) {
				((AbstractSliceConversionPage) page).setDatasetName(overrideDatasets.get(0));
			}
			addPage(page);
		}
		this.selectedConversionPage = pages[0];
		
		setWindowTitle("Convert Data Wizard");

	}
	

	/**
	 * Set an alternative file list than the current workbench selection by calling this method.
	 * @param selections
	 */
	public void setSelectionOverride(List<ITransferableDataObject> selections) {
		final List<String> paths = new ArrayList<String>(7);
		final List<String> sets  = new ArrayList<String>(7);
		if (!selections.isEmpty()) {
			if (selections.get(0) instanceof ITransferableDataObject) {
				for (ITransferableDataObject ob : selections) {
					if (!paths.contains(ob.getFilePath())) {
						paths.add(ob.getFilePath());
						sets.add(ob.getPath());
					}
				}
			} else if (selections.get(0) instanceof File) {
				for (int i = 0; i < selections.size(); i ++ ) {
					File ob = (File) selections.get(i);
					if (!paths.contains(ob.getAbsolutePath())) {
						paths.add(ob.getAbsolutePath());
						sets.add(ob.getPath());
					}
				}
			}
		}
		this.overridePaths    = paths;
		this.overrideDatasets = sets;
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
													new Status(IStatus.WARNING, "org.edna.workbench.actions", buf.toString(), e));
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
