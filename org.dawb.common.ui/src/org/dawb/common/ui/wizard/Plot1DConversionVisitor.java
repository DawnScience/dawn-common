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

import org.dawb.common.util.io.FileUtils;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.january.dataset.DTypeUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;

import uk.ac.diamond.scisoft.analysis.io.ASCIIDataHolderSaver;
import uk.ac.diamond.scisoft.analysis.io.ASCIIDataWithHeadingSaver;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.utils.VersionSort;

/**
 * TODO FIXME This is not a UI class. Suggest move to data/algorithm plugin like org.dawnsci.persistence perhaps.
 *
 */
public class Plot1DConversionVisitor extends AbstractPlotConversionVisitor {
	
	public static final String EXTENSION_DAT = "dat";
	public static final String EXTENSION_CSV = "csv";

	private boolean asDat = true;
	private boolean asSingle = true;
	private boolean asSingleX = true;

	public Plot1DConversionVisitor(IPlottingSystem<?> system) {
		super(system);
		if (system.getPlotType() != PlotType.XY) {
			throw new IllegalArgumentException("Not a 1D plotting system");
		}
	}

	public void setAsDat(boolean asDat) {
		this.asDat = asDat;
	}

	public void setAsSingle(boolean single) {
		this.asSingle = single;
	}

	public void setAsSingleX(boolean singleX) {
		this.asSingleX  = singleX;
	}

	@Override
	public void visit(IConversionContext context, IDataset slice)
			throws Exception {
		
		if (context.getMonitor()!=null && context.getMonitor().isCancelled()) {
			throw new Exception(getClass().getSimpleName()+" is cancelled");
		}
		
		final File outFile = new File(context.getOutputPath());
		if (!outFile.getParentFile().exists()) {
			outFile.getParentFile().mkdirs();
		}

		if (asDat) {
			List<ILineTrace> traceList = new ArrayList<>(system.getTracesByClass(ILineTrace.class));
			Collections.sort(traceList, ILineTraceComparator);
			ILineTrace[] traces = traceList.toArray(new ILineTrace[traceList.size()]);
			if (asSingle || system.getTraces(ILineTrace.class).size() == 1) {
				saveLineTracesAsDat(context.getOutputPath(), traces);
			} else {
				saveLineTracesAsMultipleDat(context.getOutputPath(), traces);
			}
		} else {
			saveLineTracesAsCSV(context.getOutputPath());
		}
	}

	private void saveLineTracesAsMultipleDat(String outputPath, ILineTrace[] traces) throws Exception {
		File f = new File(outputPath);
		String n = f.getName();
		String ext = FileUtils.getFileExtension(n);
		if (ext.isEmpty()) {
			ext = EXTENSION_DAT;
		} else {
			n = n.substring(0, n.lastIndexOf(ext) - 1); // omit "."
		}

		f = f.getParentFile();
		int imax = traces.length;
		int digits = Math.max(2, (int) Math.ceil(Math.log10(imax)));
		String format = String.format("%s-%%0%dd.%s", new File(f, n).getAbsolutePath(), digits, ext);
		for (int i = 0; i < imax; i++) {
			saveLineTracesAsDat(String.format(format, i), traces[i]);
		}
	}

	@Override
	public String getExtension() {
		return asDat ? EXTENSION_DAT : EXTENSION_CSV;
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
	
	private void saveLineTracesAsDat(String filename, ILineTrace... traces) throws Exception {
		List<String> headings = new ArrayList<>();

		int i = 0;
		int imax = traces.length;

		Dataset firstX = DatasetUtils.convertToDataset(traces[0].getXData());
		Dataset firstY = DatasetUtils.convertToDataset(traces[0].getYData());
		int length = firstY.getSize();
		int xType = DTypeUtils.getDType(firstX);
		int yType = DTypeUtils.getDType(firstY);
		if (firstX.getName().isEmpty()) {
			headings.add(imax == 1 ? "x" : "x0");
		} else {
			headings.add(firstX.getName().replace(' ', '_'));
		}
		if (firstY.getName().isEmpty()) {
			headings.add(imax == 1 ? "y" : "y0");
		} else {
			headings.add(firstY.getName().replace(' ', '_'));
		}

		boolean ragged = false;
		for (i = 1; i < imax; i++) {
			ILineTrace trace = traces[i];
			IDataset current = trace.getYData();
			
			int size = current.getSize();
			if (size > length) {
				length = Math.max(length, size);
				ragged = true;
			}
			xType = DTypeUtils.getBestDType(DTypeUtils.getDType(trace.getXData()), xType);
			yType = DTypeUtils.getBestDType(DTypeUtils.getDType(current), yType);
		}

		if (ragged) { // use doubles or floats
			xType = DTypeUtils.getBestFloatDType(xType);
			yType = DTypeUtils.getBestFloatDType(yType);
		}

		int columns = asSingleX ? imax + 1 : 2 * imax;
		Dataset allTraces = DatasetFactory.zeros(new int[] { length, columns }, DTypeUtils.getBestDType(xType, yType));
		if (ragged && allTraces.hasFloatingPointElements()) {
			allTraces.fill(Double.NaN);
		}

		SliceND slice = new SliceND(allTraces.getShapeRef());

		int[] shape = new int[] {1, 1};
		int size = firstY.getSize();
		shape[0] = size;
		int j = 0;
		slice.setSlice(1, j, j+1, 1);
		if (asSingleX) {
			slice.setSlice(0, 0, length, 1);
			if (firstX.getSize() < length) {
				throw new IllegalArgumentException("First x dataset must be long as longest y dataset");
			}
			firstX = firstX.getSliceView(new Slice(length));
			allTraces.setSlice(firstX.reshape(length, 1), slice);
			slice.setSlice(0, 0, size, 1);
		} else {
			slice.setSlice(0, 0, size, 1);
			if (firstX.getSize() > size) {
				firstX = firstX.getSliceView(new Slice(size));
			}
			allTraces.setSlice(firstX.reshape(shape), slice);
		}
		j++;
		slice.setSlice(1, j, j+1, 1);
		allTraces.setSlice(firstY.reshape(shape), slice);
		j++;

		for (i = 1; i < imax; i++) {
			ILineTrace trace = traces[i];
			Dataset x = asSingleX ? null : DatasetUtils.convertToDataset(trace.getXData());
			Dataset y = DatasetUtils.convertToDataset(trace.getYData()).getSlice();

			if (x != null) {
				if (x.getName().isEmpty()) {
					headings.add("x" + i);
				} else {
					headings.add(x.getName().replace(' ', '_'));
				}
			}
			if (y.getName().isEmpty()) {
				headings.add("y" + i);
			} else {
				headings.add(y.getName().replace(' ', '_'));
			}

			size = y.getSize();
			shape[0] = size;
			slice.setSlice(0, 0, size, 1);
			if (x != null) {
				slice.setSlice(1, j, j+1, 1);
				if (x.getSize() > size) {
					x = x.getSliceView(new Slice(size));
				}
				allTraces.setSlice(x.reshape(shape), slice);
				j++;
			}
			slice.setSlice(1, j, j+1, 1);
			allTraces.setSlice(y.reshape(shape), slice);
			j++;
		}

		ASCIIDataWithHeadingSaver saver = new ASCIIDataWithHeadingSaver(filename);
		DataHolder dh = new DataHolder();
		dh.addDataset("AllTraces", allTraces);
		saver.setHeader("#Traces extracted from Plot");
		saver.setHeadings(headings);
		saver.setCellFormat("%.16g");
		saver.saveFile(dh);
	}

	private void saveLineTracesAsCSV(String filename) throws Exception {
		Collection<ILineTrace> traces = system.getTracesByClass(ILineTrace.class);
		int i = 0;
		DataHolder dh = new DataHolder();
		for (ILineTrace trace : traces) {
			
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
