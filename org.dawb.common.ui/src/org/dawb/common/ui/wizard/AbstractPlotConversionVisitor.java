/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.common.ui.wizard;

import java.util.List;

import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionVisitor;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.swt.widgets.Composite;

public abstract class AbstractPlotConversionVisitor implements IConversionVisitor {

	protected IPlottingSystem<?> system;
	
	public AbstractPlotConversionVisitor(IPlottingSystem<?> system) {
		this.system = system;
	}

	@Override
	public String getConversionSchemeName() {
		return "Conversion of " + system.getPlotName();
	}

	@Override
	public void init(IConversionContext context) throws Exception {
		//Do nothing
	}
	
	@Override
	public abstract void visit(IConversionContext context, IDataset slice)
			throws Exception ;

	@Override
	public void close(IConversionContext context) throws Exception {
		//Do nothing
	}

	@Override
	public boolean isRankSupported(int length) {
		return true;
	}
	
	public abstract String getExtension();

	@Override
	public void setExpandedDatasets(List<String> expandedDatasets) {
		// We don't care about them
	}

}
