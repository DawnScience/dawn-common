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
import org.eclipse.dawnsci.macro.api.IMacroService;
import org.eclipse.dawnsci.macro.api.MacroEventObject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ConversionServiceImpl implements IConversionService {
	
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
			MessageDialog.openWarning(Display.getDefault().getActiveShell(), "Error opening conversion wizard", e.getMessage());
			e.printStackTrace();
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
			
			// We send some macro commands, to tell people how to drive the service with
			// macros.
			sendMacroCommands(context);
			
			delegate.process(context);
		} finally {
			if (delegate!=null) delegate.close(context);
		}
	}

	/**
	 * Constructs a macro by mirroring the context into the python layer. 
	 */
	private void sendMacroCommands(IConversionContext context) {
		
		if (!context.isEchoMacro()) return;
		
		IMacroService mservice = (IMacroService)Activator.getService(IMacroService.class);
		if (mservice==null) return;
		if (context.getConversionScheme() == null) {
			return;
		}

		try {
			MacroEventObject evt = new MacroEventObject(this);
			evt.setPythonCommand("cservice = dnp.plot.getService('"+IConversionService.class.getName()+"')\n");
			evt.append("context = cservice.open("+evt.getStringArguments(context.getFilePaths())+")");
			evt.append("context.setConversionScheme('"+context.getConversionScheme().getClass().getName()+"')");
			evt.append("context.setDatasetNames("+evt.getStringArguments(context.getDatasetNames())+")");
			evt.append("context.setOutputPath('"+context.getOutputPath().replace('\\', '/')+"')");
			evt.append("context.setSliceDimensions("+evt.getMap(context.getSliceDimensions())+")");
			evt.append("context.setAxesNames("+evt.getMap(context.getAxesNames())+")");
			
			if (context.getUserObject()!=null) { // We make a JSON one to provide that
				ObjectMapper mapper = new ObjectMapper();
				final Object uOb    = context.getUserObject();
                String       bean   = mapper.writeValueAsString(uOb);
                evt.append("context.createUserObject('"+uOb.getClass().getName()+"', '"+bean+"')");
			}
			
			evt.append("# To execute the conversion (commented out for now)");
			evt.append("# cservice.process(context)");

			mservice.publish(evt);
			
		} catch (Exception ne) {
			ne.printStackTrace();
		}
	}

}
