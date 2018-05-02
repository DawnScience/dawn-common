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
import org.eclipse.january.dataset.DTypeUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;

import uk.ac.diamond.scisoft.analysis.io.ASCIIDataHolderSaver;
import uk.ac.diamond.scisoft.analysis.io.ASCIIDataWithHeadingSaver;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.utils.VersionSort;

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
		
		Collection<ILineTrace> traces = system.getTracesByClass(ILineTrace.class);
		
		//Determine if x datasets are the same
		IDataset x = null;
		boolean xAxisEquivalent = true;
		
		for (ILineTrace trace : traces) {
			
			IDataset currentx = trace.getXData();
				
			if (x == null) {
				x = currentx;
				continue;
			}
				
			if (!x.equals(currentx)) {
				xAxisEquivalent = false;
				break;
			}
		}
		
		if (xAxisEquivalent) saveLineTracesAsAscii(context.getOutputPath());
		else saveLineTracesXDifferent(context.getOutputPath());

	}

	@Override
	public String getExtension() {

		return "dat";
	}

	private static final Comparator<ILineTrace> ILineTraceComparator = new Comparator<ILineTrace>() {
		@Override
		public int compare(ILineTrace a, ILineTrace b) {
			if (a == null || a.getYData() == null || a.getYData().getName() == null || a.getYData().getName().isEmpty())
				return 1;

			if (b == null || b.getYData() == null || b.getYData().getName() == null || b.getYData().getName().isEmpty())
				return -1;
			
			return VersionSort.versionCompare(
					a.getYData().getName(),
					b.getYData().getName()
				);
		}
	};
	
	private void saveLineTracesAsAscii(String filename) throws Exception {

		List<ILineTrace> tracesList = new ArrayList<>(system.getTracesByClass(ILineTrace.class));

		boolean firstTrace = true;
		List<IDataset> datasets = new ArrayList<>();
		List<String> headings = new ArrayList<>();
		IDataset data;

		int dtype = 0;

		int i = 0;

		Collections.sort(tracesList, ILineTraceComparator);
		
		for (ILineTrace trace : tracesList ) {

			if (firstTrace) {
				int ddtype = DTypeUtils.getDType(trace.getData());
				data = trace.getXData().getSliceView();
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
					headings.add(data.getName().replace(' ', '_'));
				else
					headings.add("x");
				firstTrace = false;
			}

			data = trace.getYData().getSliceView();
			data.squeeze();
			if (dtype != DTypeUtils.getDType(data)) {
				data = DatasetUtils.cast(data, dtype);
			}

			data.setShape(data.getShape()[0],1);
			datasets.add(data);
			if (data.getName() != null && !data.getName().isEmpty())
				headings.add(data.getName().replace(' ', '_'));
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
		Collection<ILineTrace> traces = system.getTracesByClass(ILineTrace.class);

		int i = 0;
		
		DataHolder dh = new DataHolder();
		
		for (ILineTrace trace : traces ) {
			
			IDataset x = trace.getXData();
			IDataset y = trace.getYData();
			
			
			
			dh.addDataset("Tracex_" + i, x);
			dh.addDataset("Tracey_y" + i, y);
			i++;
		}
		
		ASCIIDataHolderSaver saver = new ASCIIDataHolderSaver(filename);

		saver.saveFile(dh);

	}

}
