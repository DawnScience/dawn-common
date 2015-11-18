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
import java.util.Collection;

import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.AbstractDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.JavaImageSaver;

/**
 * TODO FIXME This is not a UI class. Suggest move to data/algorithm plugin like org.dawnsci.persistence perhaps.
 *
 */
public class Plot2DConversionVisitor extends AbstractPlotConversionVisitor {

	public Plot2DConversionVisitor(IPlottingSystem<?> system) {
		super(system);
		if (system.getPlotType() != PlotType.IMAGE) {
			throw new IllegalArgumentException("Not a 2D plotting system");
		}
	}

	@Override
	public void visit(IConversionContext context, IDataset slice)
			throws Exception {
		
		if (context.getMonitor()!=null && context.getMonitor().isCancelled()) {
			throw new Exception(getClass().getSimpleName()+" is cancelled");
		}
		
		Collection<ITrace> traces = system.getTraces(IImageTrace.class);
		
		if (traces.size() != 1) throw new IllegalArgumentException("Only expect one image in a 2D plot");
		
		for (ITrace trace : traces) {
			if (trace instanceof IImageTrace) {
				
				IDataset data = trace.getData();
				
				int bits = 33;
				int dbits = AbstractDataset.getDType(data);
				
				switch (dbits) {
				case Dataset.INT8:
					bits = 8;
					break;
				case Dataset.INT16:
					bits = 16;
					break;
				case Dataset.INT32:
					bits = 32;
					break;
				}
				
				final File outFile = new File(context.getOutputPath());
				if (!outFile.getParentFile().exists()) outFile.getParentFile().mkdirs();
				final JavaImageSaver saver = new JavaImageSaver(outFile.getAbsolutePath(), getExtension(),bits, true);
				final DataHolder     dh    = new DataHolder();
				dh.addDataset(data.getName(), data);
				saver.saveFile(dh);
				if (context.getMonitor()!=null) context.getMonitor().worked(1);
				
			} else {
				throw new IllegalArgumentException("Non-image trace in a 2D plot");
			}
		}
	}


	@Override
	public String getExtension() {
		return "tif";
	}
}
