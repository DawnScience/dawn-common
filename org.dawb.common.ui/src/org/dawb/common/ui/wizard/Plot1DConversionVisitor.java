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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dawb.common.util.io.FileUtils;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.PlotExportConstants;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.metadata.IMetadata;

import uk.ac.diamond.scisoft.analysis.io.ColumnTextSaver;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;

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

		Collection<ILineTrace> traces = system.getTracesByClass(ILineTrace.class);
		ILineTrace[] traceArray = traces.toArray(new ILineTrace[traces.size()]);
		if (asDat) {
			if (asSingle || system.getTraces(ILineTrace.class).size() == 1) {
				saveLineTraces(true, context.getOutputPath(), traceArray);
			} else {
				saveLineTracesAsMultipleDat(context.getOutputPath(), traceArray);
			}
		} else {
			saveLineTraces(false, context.getOutputPath(), traceArray);
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
			saveLineTraces(true, String.format(format, i), traces[i]);
		}
	}

	@Override
	public String getExtension() {
		return asDat ? EXTENSION_DAT : EXTENSION_CSV;
	}

	private void saveLineTraces(boolean asDat, String filename, ILineTrace... traces) throws Exception {
		List<String> preHeader = new ArrayList<>();
		List<String> headings = new ArrayList<>();

		int i = 0;
		int imax = traces.length;

		Dataset firstX = DatasetUtils.convertToDataset(traces[0].getXData());
		Dataset firstY = DatasetUtils.convertToDataset(traces[0].getYData());
		int length = firstY.getSize();
		addGoodXName(headings, firstX, imax == 1 ? null : 0);
		addGoodYName(headings, preHeader, firstY, imax == 1 ? null : 0);

		int size = firstY.getSize();
		if (asDat) {
			for (i = 1; i < imax; i++) {
				ILineTrace trace = traces[i];
				IDataset current = trace.getYData();
				
				int cSize = current.getSize();
				if (cSize > length) {
					length = Math.max(length, cSize);
				}
			}
			if (asSingleX) {
				if (firstX.getSize() < length) {
					throw new IllegalArgumentException("First x dataset must be long as longest y dataset");
				}
				firstX = firstX.getSliceView(new Slice(length));
			} else {
				if (firstX.getSize() > size) {
					firstX = firstX.getSliceView(new Slice(size));
				}
			}
		}

		int j = 0;
		DataHolder dh = new DataHolder();
		dh.addDataset(headings.get(j++), firstX);
		dh.addDataset(headings.get(j++), firstY);

		for (i = 1; i < imax; i++) {
			ILineTrace trace = traces[i];
			Dataset x = asSingleX ? null : DatasetUtils.convertToDataset(trace.getXData());
			Dataset y = DatasetUtils.convertToDataset(trace.getYData());

			if (x != null) {
				addGoodXName(headings, x, i);
			}
			addGoodYName(headings, preHeader, y, i);

			size = y.getSize();
			if (x != null) {
				if (asDat) {
					if (x.getSize() > size) {
						x = x.getSliceView(new Slice(size));
					}
				}
				dh.addDataset(headings.get(j++), x);
			}
			dh.addDataset(headings.get(j++), y);
		}

		ColumnTextSaver saver = new ColumnTextSaver(filename);
		saver.setHeadings(headings);
		if (asDat) {
			saver.setCellFormat("%.16g");
		} else {
			saver.setDelimiter(ColumnTextSaver.COMMA);
		}
		saver.setHeaders(saver.createRow(preHeader.toArray(new String[preHeader.size()])));
		saver.saveFile(dh);
	}

	private String addGoodXName(List<String> headings, IDataset d, Integer i) {
		String n = d.getName();
		if (n.isEmpty()) {
			n = "x";
			if (i != null) {
				n += i;
			}
		}
		int j = 1;
		String t = n;
		while (headings.contains(t)) {
			t = String.format("%s_%d", n, j++);
		}
		headings.add(t);
		return t;
	}

	private String addGoodYName(List<String> headings, List<String> preHeader, IDataset d, Integer i) {
		String n = null;
		IMetadata md = d.getFirstMetadata(IMetadata.class);
		if (md != null) {
			try {
				Serializable s = md.getMetaValue(PlotExportConstants.LABEL_NAME);
				if (preHeader.isEmpty()) {
					if (s != null) {
						preHeader.add(0, s.toString());
					} else {
						preHeader.add(0, "scan");
					}
				}
				s = md.getMetaValue(PlotExportConstants.LABEL_VALUE);
				if (s != null) {
					preHeader.add(i + 1, s.toString());
				} else {
					s = md.getMetaValue(PlotExportConstants.SCAN);
					if (s != null) {
						preHeader.add(i + 1, s.toString());
					}
				}

				s = md.getMetaValue(PlotExportConstants.PLOT_NAME);
				if (s != null) {
					n = s.toString();
				}
			} catch (Exception e) {
			}
		}

		if (n == null) {
			n = "y";
			if (i != null) {
				n += i;
			}
		}
		int j = 1;
		String t = n;
		while (headings.contains(t)) {
			t = String.format("%s_%d", n, j++);
		}
		headings.add(t);
		return t;
	}
}
