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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dawb.common.util.list.SortNatural;
import org.dawnsci.conversion.converters.util.LocalServiceManager;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.downsample.DownsampleMode;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.dataset.impl.LazyDataset;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean;

import uk.ac.diamond.scisoft.analysis.dataset.function.Downsample;
import uk.ac.diamond.scisoft.analysis.io.ImageStackLoader;

public abstract class AbstractImageConversion extends AbstractConversion {

	int imageCounter = 0;
	
	AbstractImageConversion() {
		// OSGi
	}
	
	public AbstractImageConversion(IConversionContext context) {
		super(context);
	}
	
	public void process(IConversionContext context) throws Exception {
		
		if (context.getLazyDataset()==null) { // We might be able to make one from images
			try {
				// Detect if the input is a directory of images and
				// then set the lazy dataset to be the loader factory getImageStack(...)
				List<String> images = null;
				
				final List<String> filePaths = context.getFilePaths();
				String firstPath = filePaths.get(0);
				File first = expand(firstPath).get(0);
				if (first.isFile()) {
					final List<String> sets  = getDataNames(first);
					final List<String> names = context.getDatasetNames();
	
					// If they chose 'Image Stack' only, it might be a stack of images 
					// In this case we get the one image we are on for this file and
					// this is the data top convert.
					// This fixes being able to convert a directory of images to tiffs.
					if (sets!=null && sets.size()==1 && names!=null && names.size()==1) {
						// In this case sets contains something like 'EDF' and names contains Image Stack.
						if (names.get(0).equals("Image Stack") && !sets.containsAll(names)) {
							images = new ArrayList<String>(89);
						}
					}
				}
				
				// Directory parse wanted if not, null.
				if (images!=null) {
					for (String filePathRegEx : filePaths) {
						final List<File> paths = expand(filePathRegEx);
						for (File file : paths) images.add(file.getAbsolutePath());
					}
					
					final IDataHolder holder = LocalServiceManager.getLoaderService().getData(images.get(0), context.getMonitor());
		 		    Collections.sort(images, new SortNatural<String>(true));
					ImageStackLoader loader = new ImageStackLoader(images, holder, context.getMonitor());
					LazyDataset lazyDataset = new LazyDataset("Image Stack", loader.getDtype(), loader.getShape(), loader);
				    context.setLazyDataset(lazyDataset);
				}
				
			} catch (Exception ne) {
				// We have not managed to assign the image directory and carry on...
			}
		}

		
		super.process(context);
	}
	
	protected void iterate(final ILazyDataset         lz, 
		                   final String               nameFrag,
		                   final IConversionContext   context) throws Exception {
		
		imageCounter = 0;
		
		super.iterate(lz, nameFrag, context);
	}

	/**
	 * Please override getExtension() if using getFileName(...)
	 * @return
	 */
	protected final String getFilePath(IDataset slice) {
		final String sliceFileName = getFileName(slice);
		
		final File selectedFile = context.getSelectedConversionFile();
		final String fileNameFrag = selectedFile!=null ? getFileNameNoExtension(selectedFile)+"/" : "";
		
		return context.getOutputPath()+"/"+fileNameFrag+sliceFileName;
	}

	//private Pattern INDEX_PATTERN = Pattern.compile("(.+index=)(\\d+)\\)");
	
	private String getFileName(IDataset slice) {
		
		String fileName = slice.getName();
		if (context.getUserObject()!=null) {
			ConversionInfoBean bean = (ConversionInfoBean)context.getUserObject();
			
			String namePrefix = null;
			if (bean.getAlternativeNamePrefix()!=null && !"".equals(bean.getAlternativeNamePrefix())) {
				namePrefix = bean.getAlternativeNamePrefix();
			}
			
			// Used to be: fileName example " data(Dim 0; index=0) "
			// now contains full slice information ie data (0,:2048,:2048)
			// so the old method of parsing the data name no longer works
			if (bean.getSliceIndexFormat()!=null) {
//				final Matcher matcher = INDEX_PATTERN.matcher(fileName);
//				if (matcher.matches()) {
//					final NumberFormat format = new DecimalFormat(bean.getSliceIndexFormat());
//					namePrefix = namePrefix!=null ? namePrefix :  matcher.group(1);
//					fileName   = namePrefix+format.format(Integer.parseInt(matcher.group(2)))+")";
//				}
				final NumberFormat format = new DecimalFormat(bean.getSliceIndexFormat());
				fileName   = namePrefix+format.format(imageCounter++);
				
			}
		}
		fileName = fileName.replace('\\', '_');
		fileName = fileName.replace('/', '_');
		fileName = fileName.replace(' ', '_');
		fileName = fileName.replace('(', '_');
		fileName = fileName.replaceAll("[^a-zA-Z0-9_]", "");
		
		if (context.getFilePaths().size()>1 && context.getSelectedConversionFile()!=null) {
			final String originalName = getFileNameNoExtension(context.getSelectedConversionFile());
			fileName  = originalName+"_"+fileName;
		}
		
		return fileName+"."+getExtension();
	}

	/**
	 * Please override if using getFileName
	 * @return
	 */
	protected String getExtension() {
		return "tiff";
	}

	protected int getDownsampleBin() {
		if (context.getUserObject()==null) return 1;
        return ((ConversionInfoBean)context.getUserObject()).getDownsampleBin();
	}
	protected DownsampleMode getDownsampleMode() {
		if (context.getUserObject()==null) return DownsampleMode.MAXIMUM;
        return ((ConversionInfoBean)context.getUserObject()).getDownsampleMode();
	}
	
	protected IDataset getDownsampled(IDataset slice) {
		final String name = slice.getName();
		if (getDownsampleBin()>1) {
			final Downsample down = new Downsample(getDownsampleMode(), getDownsampleBin(), getDownsampleBin());
			slice = down.value(slice).get(0);
			slice.setName(name);
		}
		return slice;
	}
	
	protected Enum<?> getSliceType() {
		if (context.getUserObject()==null) return null;
        return ((ConversionInfoBean)context.getUserObject()).getSliceType();
	}
	protected ImageServiceBean getImageServiceBean() {
		if (context.getUserObject()==null) return null;
        return ((ConversionInfoBean)context.getUserObject()).getImageServiceBean();
	}
	protected boolean isAlwaysShowTitle() {
		if (context.getUserObject()==null) return false;
        return ((ConversionInfoBean)context.getUserObject()).isAlwaysShowTitle();
	}

}
