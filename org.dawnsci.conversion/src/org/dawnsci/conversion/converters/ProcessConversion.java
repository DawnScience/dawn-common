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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.conversion.IProcessingConversionInfo;
import org.eclipse.dawnsci.analysis.api.conversion.ProcessingOutputType;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.processing.ExecutionType;
import org.eclipse.dawnsci.analysis.api.processing.IExecutionVisitor;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperationContext;
import org.eclipse.dawnsci.analysis.api.processing.IOperationFileMonitor;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.analysis.api.processing.ISavesToFile;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.analysis.dataset.slicer.Slicer;
import org.eclipse.dawnsci.analysis.dataset.slicer.SourceInformation;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.metadata.AxesMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.osgi.services.ServiceProvider;
import uk.ac.diamond.scisoft.analysis.utils.FileUtils;

public class ProcessConversion extends AbstractConversion {
	
	private static final Logger logger = LoggerFactory.getLogger(ProcessConversion.class);

	private static final String PROCESSED = "_processed";
	private static final String EXT = ".nxs";
	
	public ProcessConversion(IConversionContext context) {
		super(context);
		context.setEchoMacro(false); // We do not tell the user about doing this conversion in macros.
	}

	protected void iterate( final ILazyDataset         lz, 
				            final String               nameFrag,
				            final IConversionContext   context) throws Exception {
		
		Object userObject = context.getUserObject();
		
		if (!(userObject instanceof IProcessingConversionInfo)) throw new IllegalArgumentException("User object not valid for conversion");
		
		IProcessingConversionInfo info = (IProcessingConversionInfo) userObject;
		final Map<Integer, String> sliceDimensions = context.getSliceDimensions();
		//take a local view of the lazy dataset, since we are messing with its metadata
		ILazyDataset localLazy = lz.getSliceView();
		
		Map<Integer, String> axesNames = context.getAxesNames();
		AxesMetadata axm = ServiceProvider.getService(ILoaderService.class)
				.getAxesMetadata(localLazy, context.getSelectedConversionFile().getAbsolutePath(), axesNames, false);

		SourceInformation si = new SourceInformation(context.getSelectedConversionFile().getAbsolutePath(), context.getDatasetNames().get(0), localLazy);
		localLazy.setMetadata(new SliceFromSeriesMetadata(si));
		localLazy.setMetadata(axm);
		
		IOperationContext cc = ServiceProvider.getService(IOperationService.class).createContext();
		cc.setData(localLazy);
		cc.setSlicing(Slicer.getSliceNDFromSliceDimensions(context.getSliceDimensions(), localLazy.getShape()));
		cc.setDataDimensions(Slicer.getDataDimensions(localLazy.getShape(), context.getSliceDimensions()));

		IOperation[] operationSeries = info.getOperationSeries();
		String suffix = operationSeries[operationSeries.length - 1].getFilenameSuffix();
		if (suffix == null) {
			suffix = "";
		} else {
			suffix = "_" + suffix;
		}

		String name = getFileNameNoExtension(context.getSelectedConversionFile());
		String outputFolder = context.getOutputPath();
		Date date = new Date() ;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd_HHmmss") ;
		String timeStamp = "_" +dateFormat.format(date);
		String full = outputFolder + File.separator + name + PROCESSED + suffix + timeStamp + EXT;
		File fh = new File(outputFolder);
		fh.mkdir();
		
		//TODO output path
		
		// If we need to keep the original data, sort it out here.
		if (info.getProcessingOutputType() == ProcessingOutputType.ORIGINAL_AND_PROCESSED) {
			File source = new File(context.getSelectedConversionFile().getAbsolutePath());
			File dest = new File(full);
			logger.debug("Copying original data ({}) to output file ({})", source.getAbsolutePath(), dest.getAbsolutePath());
			long start = System.currentTimeMillis();
			FileUtils.copyNio(source, dest);
			logger.debug("Copy ran in: {}s : Thread {}", (System.currentTimeMillis()-start)/1000.,Thread.currentThread().toString());
		}
		
		ExecutionType executionType = info.getExecutionType();
		
		IExecutionVisitor exVisitor = info.getExecutionVisitor(full);
		
		if (info.getProcessingOutputType() == ProcessingOutputType.LINK_ORIGINAL && exVisitor instanceof ISavesToFile) {
			((ISavesToFile)exVisitor).includeLinkTo(context.getSelectedConversionFile().getAbsolutePath());
		}
		
		IMonitor monitor = context.getMonitor();
		
		if (monitor instanceof IOperationFileMonitor opFileMonitor) {
			opFileMonitor.appendFilePath(full);
		}
		
		cc.setMonitor(monitor);
		cc.setVisitor(exVisitor);
		cc.setSeries(info.getOperationSeries());
		cc.setExecutionType(executionType);
		ServiceProvider.getService(IOperationService.class).execute(cc);
	}
	
	protected ILazyDataset getLazyDataset(final File                 path, 
            final String               dsPath,
            final IConversionContext   context) throws Exception {
		ILazyDataset lazyDataset = super.getLazyDataset(path, dsPath, context);
		
		if (lazyDataset != null) return lazyDataset;
		

		final IDataHolder dh = ServiceProvider.getService(ILoaderService.class)
				.getData(path.getAbsolutePath(),null);
		context.setSelectedH5Path(dsPath);
		if (context.getMonitor()!=null) {
			context.getMonitor().subTask("Process '"+path.getAbsolutePath());
		}
		return dh.getLazyDataset(dsPath);
	}
	
	@Override
	protected void convert(IDataset slice) throws Exception {
		// does nothing, conversion is in the iterate method
	}


}
