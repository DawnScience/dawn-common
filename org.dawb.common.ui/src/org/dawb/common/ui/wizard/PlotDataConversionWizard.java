/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.common.ui.wizard;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dawb.common.ui.Activator;
import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawb.common.ui.plot.PlottingSystemUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlotDataConversionWizard extends Wizard implements IExportWizard {
	
	public static final String ID = "org.dawb.common.ui.wizard.plotdataconversion";
	
	private IConversionService            service;
	private IConversionContext            context;
	private AbstractPlotConversionVisitor visitor;
	private PlotDataConversionPage        conversionPage;
	private IPlottingSystem<?>            system;
	private String                        filePath;

	private boolean hasBrowseToExternalOnly;
	
	private static final Logger logger = LoggerFactory.getLogger(PlotDataConversionWizard.class);
	
	public PlotDataConversionWizard() {
		super();
		setWindowTitle("Export Data");
		setNeedsProgressMonitor(true);
		
		// It's an OSGI service, not required to use ServiceManager
		try {
			this.service = Activator.getService(IConversionService.class);
		} catch (Exception e) {
			logger.error("Cannot get conversion service!", e);
			return;
		}
	}
	
	public void addPages() {
		
		if (system == null) system = PlottingSystemUtils.getPlottingSystem();
		
		if (system == null) {
			logger.error("Could not find plotting system to export data from");
			return;
		}
		
		if (system.is2D()) {
			visitor = new Plot2DConversionVisitor(system);
		} else {
			visitor = new Plot1DConversionVisitor(system);
		}
		
		context = service.open();
		context.setConversionVisitor(visitor);

		conversionPage = new PlotDataConversionPage();
		if (filePath!=null) {
			conversionPage.setPath(filePath);
		} else {
			conversionPage.setPath(System.getProperty("user.home") +File.separator+ "plotdata."+ visitor.getExtension());
		}
		conversionPage.setBrowseToExternalOnly(hasBrowseToExternalOnly);

		boolean sameX = areXAxesAllEqual();
		conversionPage.setAllXEqual(sameX);
		addPage(conversionPage);
	}

	private boolean areXAxesAllEqual() {
		Collection<ILineTrace> traces = system.getTracesByClass(ILineTrace.class);
		// Determine if x datasets are the same
		IDataset x = null;
		for (ILineTrace trace : traces) {
			
			IDataset currentx = trace.getXData();
				
			if (x == null) {
				x = currentx;
				continue;
			}
				
			if (!x.equals(currentx)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean performFinish() {
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {				 

					try {
						filePath = conversionPage.getAbsoluteFilePath();
						
						final File ioFile = new File(filePath);
						
						final List<Boolean> ok = new ArrayList<Boolean>(1);
						ok.add(Boolean.TRUE);
						if (ioFile.exists() && !conversionPage.isOverwrite()) {
							
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									boolean fine = MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Do you want to overwrite?", "The file '"+ioFile.getName()+"' exists.\n\nWould like to overwrite it anyway?");
								    if (!fine) {
								    	ok.clear();
								    	ok.add(Boolean.FALSE);
								    }
								}
							});
						}
						if (!ok.get(0)) return;
						
						context.setOutputPath(filePath);
						context.setMonitor(new ProgressMonitorWrapper(monitor));
						context.setConversionVisitor(visitor);
						if (visitor instanceof Plot1DConversionVisitor) {
							Plot1DConversionVisitor v1d = (Plot1DConversionVisitor) visitor;
							
							v1d.setAsDat(conversionPage.isDat());
							v1d.setAsSingle(conversionPage.isSingle());
							v1d.setAsSingleX(conversionPage.isAsSingleX());
						}

						// Bit with the juice
						monitor.beginTask(visitor.getConversionSchemeName(), context.getWorkSize());
						monitor.worked(1);
						service.process(context);

					} catch (final Exception ne) {

						logger.error("Cannot run export process for "+visitor.getConversionSchemeName()+"'", ne);

					} 

				}
			});
		} catch (Exception ne) {
			logger.error("Cannot run export process  "+visitor.getConversionSchemeName(), ne);
		}
		return true;
	}
	
	public void setPlottingSystem(IPlottingSystem<?> system) {
		this.system = system;
	}
	
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
		if (conversionPage!=null && filePath!=null) conversionPage.setPath(filePath);
	}

	/**
	 * Set after conversion page has been created
	 * If set to true, the conversion page will only have an external file dialog. False by default
	 * with the conversion to a project location and an external location.
	 * 
	 * @param hasBrowseToExternalOnly
	 */
	public void setBrowseToExternalOnly(boolean hasBrowseToExternalOnly) {
		this.hasBrowseToExternalOnly = hasBrowseToExternalOnly;
		if (conversionPage != null) conversionPage.setBrowseToExternalOnly(hasBrowseToExternalOnly);
	}
}
