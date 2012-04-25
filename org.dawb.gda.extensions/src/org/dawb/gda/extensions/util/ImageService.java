/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.gda.extensions.util;

import org.dawb.common.services.IImageService;
import org.dawb.common.services.ImageServiceBean;
import org.dawb.common.services.ImageServiceBean.HistoType;
import org.dawb.common.services.ImageServiceBean.ImageOrigin;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Stats;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.utils.SWTImageUtils;

public class ImageService extends AbstractServiceFactory implements IImageService {
	
	
	static {
		// We just use file extensions
		LoaderFactory.setLoaderSearching(false); 
		// This now applies for the whole workbench
	}

	public ImageService() {
		
	}
	
	/**
	 * 
	 */
	public Image getImage(ImageServiceBean bean) {
		final ImageData data = getImageData(bean);
		return new Image(Display.getCurrent(), data);
	}
	
	/**
	 * getImageData(...) provides an image in a given palette data and origin.
	 * Faster than getting a resolved image
	 */
	public ImageData getImageData(ImageServiceBean bean) {
        
		final AbstractDataset image    = bean.getImage();
		final ImageOrigin     origin   = bean.getOrigin();
		PaletteData     palette  = bean.getPalette();
		
		int depth = bean.getDepth();
		final int size  = (int)Math.round(Math.pow(2, depth));
		createMaxMin(bean);
		final float max = bean.getMax().floatValue();
		final float min = bean.getMin().floatValue();
		
		if (bean.getFunctionObject()!=null && bean.getFunctionObject() instanceof FunctionContainer) {
			final FunctionContainer fc = (FunctionContainer)bean.getFunctionObject();
			return SWTImageUtils.createImageData(image, min, max, fc.getRedFunc(), 
					                                              fc.getGreenFunc(), 
					                                              fc.getBlueFunc(), 
					                                              fc.isInverseRed(), 
					                                              fc.isInverseGreen(), 
					                                              fc.isInverseBlue());
		}
		
		if (depth>8) { // We use the 24-bit processing of SWTImageUtils
			// Normally it will not do this as depth>8 will use SWTImageUtils
			if (depth == 16) palette = new PaletteData(0x7C00, 0x3E0, 0x1F);
			if (depth == 24) palette = new PaletteData(0xFF, 0xFF00, 0xFF0000);
			if (depth == 32) palette = new PaletteData(0xFF00, 0xFF0000, 0xFF000000);
		}
		
		final int[]   shape = image.getShape();
		if (bean.isCancelled()) return null;
				
				
		int len = image.getSize();
		if (len == 0) return null;

		float scale;
		float maxPixel;
		if (max > min) {
			scale = Float.valueOf(size-1) / (max - min);
			maxPixel = max - min;
		} else {
			scale = 1f;
			maxPixel = 0xFF;
		}
		if (bean.isCancelled()) return null;
		
 		ImageData imageData = null;
		if (origin==ImageOrigin.TOP_LEFT) { 
			
			imageData = new ImageData(shape[1], shape[0], depth, palette);
			// This loop is usually the same as the image is read in but not always depending on loader.
			for (int i = 0; i<shape[0]; ++i) {
				for (int j = 0; j<shape[1]; ++j) {
					
					if (bean.isCancelled()) return null;
					final float val = image.getFloat(i, j);
					imageData.setPixel(j, i, getPixelValue(val, min, max, scale, maxPixel));
				}
			}
	
		} else if (origin==ImageOrigin.BOTTOM_LEFT) {

			imageData = new ImageData(shape[0], shape[1], depth, palette);
			// This loop is slower than looping over all data and using image.getElementDoubleAbs(...)
			// However it reorders data for the axes
			for (int i = 0; i<shape[1]; ++i) {
				for (int j = 0; j<shape[0]; ++j) {
					
					if (bean.isCancelled()) return null;
					final float val = image.getFloat(j, i);
					imageData.setPixel(j, shape[1]-i-1, getPixelValue(val, min, max, scale, maxPixel));
				}
			}
			
		} else if (origin==ImageOrigin.BOTTOM_RIGHT) {

			imageData = new ImageData(shape[1], shape[0], depth, palette);
			// This loop is slower than looping over all data and using image.getElementDoubleAbs(...)
			// However it reorders data for the axes
			for (int i = 0; i<shape[0]; ++i) {
				for (int j = 0; j<shape[1]; ++j) {
				
					if (bean.isCancelled()) return null;
					final float val = image.getFloat(i, j);
					imageData.setPixel(shape[1]-j-1, shape[0]-i-1, getPixelValue(val, min, max, scale, maxPixel));
				}
			}
			
		} else if (origin==ImageOrigin.TOP_RIGHT) {

			imageData = new ImageData(shape[0], shape[1], depth, palette);
			// This loop is slower than looping over all data and using image.getElementDoubleAbs(...)
			// However it reorders data for the axes
			for (int i = 0; i<shape[1]; ++i) {
				for (int j = 0; j<shape[0]; ++j) {
					
					if (bean.isCancelled()) return null;
					final float val = image.getFloat(j, i);
					imageData.setPixel(shape[0]-j-1, i, getPixelValue(val, min, max, scale, maxPixel));
				}
			}
		}
		
		if (bean.isCancelled()) return null;
		return imageData;

	}

	private void createMaxMin(ImageServiceBean bean) {
		
		float[] stats  = null;
		if (bean.getMin()==null) {
			if (stats==null) stats = getFastStatistics(bean); // do not get unless have to
			bean.setMin(stats[0]);
		}
		
		if (bean.getMax()==null) {
			if (stats==null) stats = getFastStatistics(bean); // do not get unless have to
		    bean.setMax(stats[1]);
		}		
	}

	/**
	 * private finals inline well by the compiler.
	 * @param val
	 * @param min
	 * @param max
	 * @param scale
	 * @param maxPixel
	 * @param scaledImageAsByte
	 */
	private final int getPixelValue(final float  val, 
									final float  min, 
									final float  max, 
									final float  scale, 
									final float  maxPixel) {
		
		float scaled_pixel;
		if (val < min) {
			scaled_pixel = 0;
		} else if (val >= max) {
			scaled_pixel = maxPixel;
		} else {
			scaled_pixel = val - min;
		}
		scaled_pixel = scaled_pixel * scale;
		
		return (int)scaled_pixel;		
		
	}
	
	/**
	 * Fast statistcs as a rough guide - this is faster than AbstractDataset.getMin()
	 * and getMax() which may cache but slows the opening of images too much.
	 * 
	 * @param bean
	 * @return [0] = min [1] = max
	 */
	public float[] getFastStatistics(ImageServiceBean bean) {
		
		final AbstractDataset image    = bean.getImage();
		float min = Float.MAX_VALUE;
		float max = -Float.MAX_VALUE;
		float sum = 0.0f;
		final int size = image.getSize();
		for (int index = 0; index<size; ++index) {
			
			final double dv = image.getElementDoubleAbs(index);
			if (Double.isNaN(dv))      continue;
			if (Double.isInfinite(dv)) continue;
			
			if (bean.getMaximumCutBound()!=null) {
				if (dv>=bean.getMaximumCutBound().getBound().doubleValue()) continue;
			}
			if (bean.getMinimumCutBound()!=null) {
				if (dv<=bean.getMinimumCutBound().getBound().doubleValue()) continue;
			}
			
			final float val = (float)dv;
			sum += val;
			if (val < min) min = val;
			if (val > max) max = val;
			
		}
		
		float retMin = min;
		float retMax = Float.NaN;
		
		if (bean.getHistogramType()==HistoType.MEAN) {
			float mean = sum / size;
			retMax = ((float)Math.E)*mean; // Not statistical, E seems to be better than 3...
			
		} else if (bean.getHistogramType()==HistoType.MEDIAN) { 
			
			float median = Float.NaN;
			try {
				median = ((Number)Stats.median(image)).floatValue(); // SLOW
			} catch (Exception ne) {
				median = ((Number)Stats.median(image.cast(AbstractDataset.INT16))).floatValue();// SLOWER
			}
			retMax = 2f*median;
		}
		
		if (retMax > max)	retMax = max;
		
		return new float[]{retMin, retMax};

	}

	@Override
	public Object create(Class serviceInterface, IServiceLocator parentLocator, IServiceLocator locator) {
		
		if (serviceInterface==IImageService.class) {
			return new ImageService();
		} 
		return null;
	}
	
	public static final class SDAFunctionBean {
		
	}

}
