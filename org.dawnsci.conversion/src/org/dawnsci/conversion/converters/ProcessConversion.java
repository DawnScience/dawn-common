/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.conversion.converters;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.dawb.common.services.ServiceManager;
import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.services.conversion.IProcessingConversionInfo;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.analysis.api.processing.IRichDataset;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.metadata.AxesMetadataImpl;

public class ProcessConversion extends AbstractConversion {

	IOperationService service;
	private final static String PROCESSED = "_processed";
	private final static String EXT= ".nxs";
	
	public ProcessConversion(IConversionContext context) {
		super(context);
		
	}

	protected void iterate(final ILazyDataset         lz, 
            final String               nameFrag,
            final IConversionContext   context) throws Exception {
		
		if (service == null) service = (IOperationService)ServiceManager.getService(IOperationService.class);
		
		Object userObject = context.getUserObject();
		
		if (userObject == null || !(userObject instanceof IProcessingConversionInfo)) throw new IllegalArgumentException("User object not valid for conversion");
		
		IProcessingConversionInfo info = (IProcessingConversionInfo) userObject;
		final Map<Integer, String> sliceDimensions = context.getSliceDimensions();
		
		Map<Integer, String> axesNames = context.getAxesNames();
		
		if (axesNames != null) {
			
			AxesMetadataImpl axMeta = null;
			
			try {
				axMeta = new AxesMetadataImpl(lz.getRank());
				for (Integer key : axesNames.keySet()) {
					String axesName = axesNames.get(key);
					IDataHolder dataHolder = LoaderFactory.getData(context.getSelectedConversionFile().getAbsolutePath());
					ILazyDataset lazyDataset = dataHolder.getLazyDataset(axesName);
					if (lazyDataset != null && lazyDataset.getRank() != lz.getRank()) {
						lazyDataset = lazyDataset.getSlice();
						int[] shape = new int[lz.getRank()];
						Arrays.fill(shape, 1);
						shape[key-1]= lazyDataset.getShape()[0];
						lazyDataset.setShape(shape);
					}
					
					axMeta.setAxis(key-1, new ILazyDataset[] {lazyDataset});
				}
				
				lz.setMetadata(axMeta);
			} catch (Exception e) {
				//no axes metadata
				e.printStackTrace();
			}
		}
		
		IRichDataset rich = new IRichDataset() {
			
			@Override
			public Map<Integer, String> getSlicing() {
				return sliceDimensions;
			}
			
			@Override
			public ILazyDataset getData() {
				return lz;
			}
		};
		
		String name = getFileNameNoExtension(context.getSelectedConversionFile());
		String outputFolder = context.getOutputPath();
		Date date = new Date() ;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd_HHmmss") ;
		String timeStamp = "_" +dateFormat.format(date);
		String full = outputFolder + File.separator + name + PROCESSED+ timeStamp + EXT;
		
		//TODO output path
		service.executeSeries(rich, context.getMonitor(), info.getExecutionVisitor(full), info.getOperationSeries());
	}
	
	protected ILazyDataset getLazyDataset(final File                 path, 
            final String               dsPath,
            final IConversionContext   context) throws Exception {
		ILazyDataset lazyDataset = super.getLazyDataset(path, dsPath, context);
		
		if (lazyDataset != null) return lazyDataset;
		

		final IDataHolder   dh = LoaderFactory.getData(path.getAbsolutePath());
		context.setSelectedH5Path(dsPath);
		if (context.getMonitor()!=null) {
			context.getMonitor().subTask("Process '"+path.getAbsolutePath() +"''"+dsPath+"'");
		}
		return dh.getLazyDataset(dsPath);
	}
	
	@Override
	protected void convert(IDataset slice) throws Exception {
		// does nothing, conversion is in the iterate method
	}


}
