/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.conversion;

import java.util.List;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.services.conversion.IConversionService;
import org.dawnsci.conversion.converters.AVIImageConverter;
import org.dawnsci.conversion.converters.AbstractConversion;
import org.dawnsci.conversion.converters.AsciiConvert1D;
import org.dawnsci.conversion.converters.AsciiConvert2D;
import org.dawnsci.conversion.converters.CompareConverter;
import org.dawnsci.conversion.converters.Convert1DtoND;
import org.dawnsci.conversion.converters.CustomNCDConverter;
import org.dawnsci.conversion.converters.CustomTomoConverter;
import org.dawnsci.conversion.converters.ImageConverter;
import org.dawnsci.conversion.converters.ImagesToHDFConverter;
import org.dawnsci.conversion.converters.ImagesToStitchedConverter;
import org.dawnsci.conversion.converters.ProcessConversion;
import org.dawnsci.conversion.converters.VisitorConversion;
import org.eclipse.dawnsci.macro.api.IMacroService;
import org.eclipse.dawnsci.macro.api.MacroEventObject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

public class ConversionServiceImpl implements IConversionService {
	
	private static IMacroService mservice;
	
	public static void setMacroService(IMacroService s) {
		mservice = s;
	}
	
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
				switch(context.getConversionScheme()) {
				case ASCII_FROM_2D:
					delegate = new AsciiConvert2D(context);
					break;
				case ASCII_FROM_1D:
					delegate = new AsciiConvert1D(context);
					break;
				case CUSTOM_NCD:
					delegate = new CustomNCDConverter(context);
					break;
				case TIFF_FROM_3D:
					delegate = new ImageConverter(context);
					break;
				case STITCHED_FROM_IMAGEDIR:
					delegate = new ImagesToStitchedConverter(context);
					break;
				case H5_FROM_IMAGEDIR:
					delegate = new ImagesToHDFConverter(context);
					break;
				case AVI_FROM_3D:
					delegate = new AVIImageConverter(context);
					break;
				case CUSTOM_TOMO:
					delegate = new CustomTomoConverter(context);
					break;
				case H5_FROM_1D:
					delegate = new Convert1DtoND(context);
					break;
				case COMPARE:
					delegate = new CompareConverter(context);
					break;
				case PROCESS:
					delegate = new ProcessConversion(context);
					break;
				default:
					throw new Exception("No conversion for "+context.getConversionScheme());
				}
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
		
		if (mservice==null) return;
		
		MacroEventObject evt = new MacroEventObject(this);
		evt.setPythonCommand("cservice = dnp.plot.getService('"+IConversionService.class.getName()+"')\n");
		evt.append("context = cservice.open("+evt.getStringArguments(context.getFilePaths())+")");
		evt.append("context.setConversionScheme('"+context.getConversionScheme().name()+"')");
		evt.append("context.setDatasetNames("+evt.getStringArguments(context.getDatasetNames())+")");
		mservice.publish(evt);
	}

}
