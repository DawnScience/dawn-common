/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.conversion.converters;

import java.io.File;

import org.dawb.common.services.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.JavaImageSaver;

/**
 * Class to save tiff slices out from an hdf file.
 * 
 * @author Matthew Gerring
 *
 * Mark I suggest that you extend this class either with a subclass or a mode
 * of this class to process the other parts required.
 * 
 * Please add a Test to TiffConvertTest or similar.
 */
public class ImageConverter extends AbstractImageConversion {

	public ImageConverter(IConversionContext context) {
		super(context);
		
		final File dir = new File(context.getOutputPath());
		dir.mkdirs();
	}

	@Override
	protected void convert(IDataset slice) throws Exception {
		
		if (context.getMonitor()!=null && context.getMonitor().isCancelled()) {
			throw new Exception(getClass().getSimpleName()+" is cancelled");
		}
		slice = getDownsampled(slice);

		final File sliceFile = new File(getFilePath(slice));
		if (!sliceFile.getParentFile().exists()) sliceFile.getParentFile().mkdirs();
		
		// JavaImageSaver likes 33 but users don't 
		int bits = getBits();
		if (bits==32 && getExtension().toLowerCase().startsWith("tif")) bits = 33;
		
		final JavaImageSaver saver = new JavaImageSaver(sliceFile.getAbsolutePath(), getExtension(), bits, true);
		final DataHolder     dh    = new DataHolder();
		dh.addDataset(slice.getName(), slice);
		if (context.getSelectedConversionFile()!=null) {
			dh.setFilePath(context.getSelectedConversionFile().getAbsolutePath());
		}
		saver.saveFile(dh);
        if (context.getMonitor()!=null) context.getMonitor().worked(1);
	}
	@Override
	public void close(IConversionContext context) {
        
	}

	protected String getExtension() {
		if (context.getUserObject()==null) return "tif";
		return ((ConversionInfoBean)context.getUserObject()).getExtension();
	}

	private int getBits() {
		if (context.getUserObject()==null) return 33;
		return ((ConversionInfoBean)context.getUserObject()).getBits();
	}
	
	

}
