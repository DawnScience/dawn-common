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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.DTypeUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;

import uk.ac.diamond.scisoft.analysis.io.ASCIIDataHolderSaver;
import uk.ac.diamond.scisoft.analysis.io.ASCIIDataWithHeadingSaver;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;

/**
 * TODO FIXME This is not a UI class. Suggest move to data/algorithm plugin like org.dawnsci.persistence perhaps.
 *
 */
public class Plot1DConversionVisitor extends AbstractPlotConversionVisitor {
	
	public Plot1DConversionVisitor(IPlottingSystem<?> system) {
		super(system);
		if (system.getPlotType() != PlotType.XY) {
			throw new IllegalArgumentException("Not a 1D plotting system");
		}
	}

	@Override
	public void visit(IConversionContext context, IDataset slice)
			throws Exception {
		
		if (context.getMonitor()!=null && context.getMonitor().isCancelled()) {
			throw new Exception(getClass().getSimpleName()+" is cancelled");
		}
		
		final File outFile = new File(context.getOutputPath());
		if (!outFile.getParentFile().exists()) outFile.getParentFile().mkdirs();
		
		Collection<ITrace> traces = system.getTraces(ILineTrace.class);
		
		//Determine if x datasets are the same
		IDataset x = null;
		boolean xAxisEquivalent = true;
		
		for (ITrace trace : traces) {
			
			if (trace instanceof ILineTrace) {
				IDataset currentx = ((ILineTrace)trace).getXData();
				
				if (x == null) {
					x = currentx;
					continue;
				}
				
				if (!x.equals(currentx)) {
					xAxisEquivalent = false;
					break;
				}
			}
		}
		
		if (xAxisEquivalent) saveLineTracesAsAscii(context.getOutputPath());
		else saveLineTracesXDifferent(context.getOutputPath());

	}

	@Override
	public String getExtension() {

		return "dat";
	}

	private static final Comparator<ITrace> ITraceComparator = new Comparator<ITrace>() {
		@Override
		public int compare(ITrace a, ITrace b) {
			if (a == null || a.getData() == null || a.getData().getName() == null || a.getData().getName().isEmpty())
				return 1;

			if (b == null || b.getData() == null || b.getData().getName() == null || b.getData().getName().isEmpty())
				return -1;
			
			return a.getData().getName().compareTo(b.getData().getName());
		}
	};
	
	private void saveLineTracesAsAscii(String filename) throws Exception {

		Collection<ITrace> traces = system.getTraces(ILineTrace.class);
		List<ITrace> tracesList = new ArrayList<>(traces);

		boolean firstTrace = true;
		List<IDataset> datasets = new ArrayList<IDataset>();
		List<String> headings = new ArrayList<String>();
		IDataset data;

		int dtype = 0;

		int i = 0;

		Collections.sort(tracesList, ITraceComparator);
		
		for (ITrace trace : tracesList ) {

			if (firstTrace) {
				int ddtype = DTypeUtils.getDType(((ILineTrace)trace).getData());
				data = ((ILineTrace)trace).getXData().getSliceView();
				data.squeeze();
				int axdtype = DTypeUtils.getDType(data);

				if (ddtype == axdtype) {
					dtype = ddtype;
				} else if (ddtype > axdtype) {
					data = DatasetUtils.cast(data, ddtype);
					dtype = ddtype;
				} else {
					dtype = axdtype;
				}

				data.setShape(data.getShape()[0],1);
				datasets.add(data);
				if (data.getName() != null && !data.getName().isEmpty())
					headings.add(data.getName());
				else
					headings.add("x");
				firstTrace = false;
			}

			data = ((ILineTrace)trace).getData().getSliceView();
			data.squeeze();
			if (dtype != DTypeUtils.getDType(data)) {
				data = DatasetUtils.cast(data, dtype);
			}

			data.setShape(data.getShape()[0],1);
			datasets.add(data);
			if (data.getName() != null && !data.getName().isEmpty())
				headings.add(data.getName());
			else
				headings.add("dataset_" + i++);
		}

		Dataset allTraces = DatasetUtils.concatenate(datasets.toArray(new IDataset[datasets.size()]), 1);

		ASCIIDataWithHeadingSaver saver = new ASCIIDataWithHeadingSaver(filename);
		DataHolder dh = new DataHolder();
		dh.addDataset("AllTraces", allTraces);
		saver.setHeader("#Traces extracted from Plot");
		saver.setHeadings(headings);

		saver.saveFile(dh);
	}
	
	private void saveLineTracesXDifferent(String filename) throws Exception {
		Collection<ITrace> traces = system.getTraces(ILineTrace.class);

		int i = 0;
		
		DataHolder dh = new DataHolder();
		
		for (ITrace trace : traces ) {
			
			IDataset x = ((ILineTrace)trace).getXData();
			IDataset y = trace.getData();
			
			
			
			dh.addDataset("Tracex_" + i, x);
			dh.addDataset("Tracey_y" + i, y);
			i++;
		}
		
		ASCIIDataHolderSaver saver = new ASCIIDataHolderSaver(filename);

		saver.saveFile(dh);

	}

}
