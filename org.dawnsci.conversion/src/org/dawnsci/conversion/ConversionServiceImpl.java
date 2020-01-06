/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.conversion;

import java.lang.reflect.Constructor;

import org.dawnsci.conversion.converters.AbstractConversion;
import org.dawnsci.conversion.converters.VisitorConversion;
import org.eclipse.dawnsci.analysis.api.conversion.IConversion;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionScheme;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConversionServiceImpl implements IConversionService {
	
	private static final Logger logger = LoggerFactory.getLogger(ConversionServiceImpl.class);
	
	static {
		System.out.println("Starting conversion service.");
	
	}
	public ConversionServiceImpl() {
		// Important do nothing here, OSGI may start the service more than once.
	}
	
	@Override
	public IConversionContext open(String... paths) {
		ConversionContext context = new ConversionContext();
		try {
			context.setFilePaths(paths);
		} catch (Exception e) {
			//TODO open should throw an exception!
			logger.error("Could not set file paths!");
		}
		return context;
	}

	@Override
	public void process(IConversionContext context) throws Exception {
		AbstractConversion delegate=null;
		try {
			if (context.getConversionVisitor()!=null) {
				delegate = new VisitorConversion(context);
			}
			if (delegate==null) {
				IConversionScheme scheme = context.getConversionScheme();
				Constructor<? extends IConversion> constructor = scheme.getConversion().getConstructor(IConversionContext.class);
				delegate = (AbstractConversion) constructor.newInstance(context);
			}
			
			delegate.process(context);
		} finally {
			if (delegate!=null) delegate.close(context);
		}
	}
}
