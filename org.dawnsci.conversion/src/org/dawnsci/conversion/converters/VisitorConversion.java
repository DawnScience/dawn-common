/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.conversion.converters;

import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.january.dataset.IDataset;

public class VisitorConversion extends AbstractConversion {

	public VisitorConversion(IConversionContext context) throws Exception {
		super(context);
		if (context.getConversionVisitor()==null) throw new RuntimeException("You must set a conversion visitor to use "+getClass().getName());
	    context.getConversionVisitor().init(context);
	}

	@Override
	protected void convert(IDataset slice) throws Exception{
		context.getConversionVisitor().setExpandedDatasets(getExpandedDatasets());
		context.getConversionVisitor().visit(context, slice);
	}

	public void close(IConversionContext context) throws Exception{
		context.getConversionVisitor().close(context);
	}

}
