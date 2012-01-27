/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.ui.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

/**
 *   CSVUtils
 *
 *   @author gerring
 *   @date Aug 31, 2010
 *   @project org.edna.workbench.actions
 **/
public class CSVUtils {

	private static final Logger logger = LoggerFactory.getLogger(CSVUtils.class);

	
	/**
	 * Constructs a csv file with a similar name in the same project.
	 * 
	 * @param dataFile
	 * @param data
	 */
	public static void createCSV(final IFile dataFile, final Map<String, ? extends IDataset> data, final String conjunctive)  {

		
		final IFile csv  = EclipseUtils.getUniqueFile(dataFile, conjunctive, "csv");
		try {
			
			final StringBuilder contents = new StringBuilder();
			final boolean is2D = data.size()==1 && data.values().iterator().next().getShape().length==2;
			if (is2D) {
				get2DDataSetCVS(contents, data.values().iterator().next());
			} else {
				int maxSize = Integer.MIN_VALUE;
				for (String name : data.keySet()) {
					final IDataset set = data.get(name);
					if (set.getShape()==null)     continue;
					if (set.getShape().length!=1) continue;
					maxSize = Math.max(maxSize, set.getSize());
				}
				get1DDataSetCVS(contents, data, maxSize);
			}
			
			InputStream stream = new ByteArrayInputStream(contents.toString().getBytes());
			csv.create(stream, true, new NullProgressMonitor());
			csv.getParent().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			
			final String message = "The file '"+dataFile.getName()+"' was converted to '"+csv.getName()+"' and placed in the same folder and project.";
			MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "CVS Exported", message);
			
		} catch (Exception ne) {
			final String message = "The file '"+dataFile.getName()+"' was not converted to '"+csv.getName()+"'";
			ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					              "File Not Converted", 
					              ne.getMessage(),
					              new Status(IStatus.WARNING, "org.edna.workbench.actions", message, ne));

		}
		
	}
	
	/**
	 * Constructs a csv file with the same name in the same project.
	 * 
	 * @param dataFile
	 * @param dataSetNames
	 */
	public static void createCSV(final IFile dataFile, final Object[] dataSetNames) {

		final IFile csv  = EclipseUtils.getUniqueFile(dataFile, "csv");
		
		final IProgressService service = PlatformUI.getWorkbench().getProgressService();
		try {
			service.run(true, true, new IRunnableWithProgress() {
				
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException,
						                                         InterruptedException {
			        try {
						final DataHolder dh = LoaderFactory.getData(dataFile.getLocation().toOSString(), new ProgressMonitorWrapper(monitor));								
						csv.create(getCVSStream(dh, dataSetNames), true, monitor);
						csv.getParent().refreshLocal(IResource.DEPTH_INFINITE, monitor);
						
						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								try {
									EclipseUtils.openEditor(csv);
								} catch (PartInitException e) {
									logger.error("Cannot open editor for "+csv, e);
								}
							}
						});
						
			        } catch (Exception e) {
						throw new InvocationTargetException(e, e.getMessage());
					}
					
				}
			});
		} catch (Exception ne) {
			final String message = "The file '"+dataFile.getName()+"' was not converted to '"+csv.getName()+"'";
			ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					              "File Not Converted", 
					              ne.getMessage(),
					              new Status(IStatus.WARNING, "org.edna.workbench.actions", message, ne));
		}
		
	}

	
	protected static InputStream getCVSStream(final DataHolder dh, final Object[] dataSetNames) throws Exception {
		
		final Collection<?> requiredNames = dataSetNames!=null
		                                  ? Arrays.asList(dataSetNames)
		                                  : null;
		                                  
		final Map<String, ILazyDataset> sortedData = new TreeMap<String, ILazyDataset>();
		sortedData.putAll(dh.getMap());
		if (requiredNames!=null) sortedData.keySet().retainAll(requiredNames);
		
		boolean is1DExport = false;
	    int maxSize = Integer.MIN_VALUE;
		for (String name : sortedData.keySet()) {
			final ILazyDataset set = sortedData.get(name);
			if (set.getShape()==null)     continue;
			if (set.getShape().length!=1) continue;
			maxSize = Math.max(maxSize, set.getSize());
			is1DExport = true;
		}

		final StringBuilder contents = new StringBuilder();
		if (is1DExport) {
            get1DDataSetCVS(contents, sortedData, maxSize);
		} else if (sortedData.size()==1){
			final ILazyDataset dataset2d = sortedData.values().iterator().next();
			if (dataset2d.getShape()[0]*dataset2d.getShape()[1]>64000) {
			    throw new Exception("The data contains an image "+dataset2d.getShape()[0]+"x"+dataset2d.getShape()[1]+" which cannot be converted to csv.");
			}
			get2DDataSetCVS(contents,dataset2d);
		} else {
			throw new Exception("The data cannot be parsed to csv.");
		}
		
		return new ByteArrayInputStream(contents.toString().getBytes());

	}


	private static void get2DDataSetCVS(StringBuilder contents, ILazyDataset dataset2d) {

		final int xSize = dataset2d.getShape()[0];
		final int ySize = dataset2d.getShape()[1];
		for (int y = 0; y < ySize; y++) {
			for (int x = 0; x < xSize; x++) {
				contents.append(((IDataset)dataset2d).getDouble(x,y));
				if (x<xSize-1)contents.append(",");
			}
			contents.append("\r\n"); // Intentionally windows.
		}
		
	}


	private static void get1DDataSetCVS(final StringBuilder        contents,
			                            final Map<String, ? extends ILazyDataset> sortedData,
			                            final int                  maxSize) {
		
		for (Iterator<String> it = sortedData.keySet().iterator(); it.hasNext(); ) {
			
			final String name = it.next();
			contents.append("\"");
			contents.append(name);
			contents.append("\"");
			if (it.hasNext()) contents.append(",");
		}
		contents.append("\r\n"); // Intentionally windows.
		
		for (int i = 0; i < maxSize; i++) {
			for (Iterator<String> it = sortedData.keySet().iterator(); it.hasNext(); ) {
				
				final String name = it.next();

				final ILazyDataset set = sortedData.get(name);
				final String     value = (i<set.getSize()) ? String.valueOf(((IDataset)set).getDouble(i)) : " ";
				contents.append(value);
				if (it.hasNext()) contents.append(",");
			}
			contents.append("\r\n"); // Intentionally windows.
		}
	}


}
