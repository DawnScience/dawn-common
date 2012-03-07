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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.FloatDataset;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class ImageService extends AbstractServiceFactory implements IImageService {
	
	static {
		// We just use file extensions
		LoaderFactory.setLoaderSearching(false); 
		// This now applies for the whole workbench
	}

	public ImageService() {
		
	}
	/**
	 * Modified from fable
	 * @param thumbnail
	 * @return
	 */
	public Image getImage(AbstractDataset image, PaletteData palette) {
        
		final int[]   size = image.getShape();
		FloatDataset  set  = (FloatDataset)DatasetUtils.cast(image, AbstractDataset.FLOAT32);
		final float[] data = set.getData();
		
		final float[] stats  = getStatistics(size, data);
		
		float min = stats[0];
		float max = 3*stats[2];
		if (max > stats[1])	max = stats[1];
				
		
		int len = data.length;
		if (len == 0) return null;

		// Loop over pixels
		float scale_8bit;
		float maxPixel;
		if (max > min) {
			scale_8bit = 255f / (max - min);
			maxPixel = max - min;
		} else {
			scale_8bit = 1f;
			maxPixel = 0xFF;
		}
		
		byte[] scaledImageAsByte = new byte[len];
		float scaled_pixel;

		int byteIndex = 0;
		for (int col = 0; col<image.getShape()[1]; ++col) {
			for (int row = 0; row<image.getShape()[0]; ++row) {
				
				final float val = ((Number)image.getObject(row, col)).floatValue();
				if (val < min) {
					scaled_pixel = 0;
				} else if (val >= max) {
					scaled_pixel = maxPixel;
				} else {
					scaled_pixel = val - min;
				}
				scaled_pixel = scaled_pixel * scale_8bit;
				// Keep it in bounds
				final byte pixel = (byte) (0x000000FF & ((int) scaled_pixel));

				scaledImageAsByte[byteIndex] = pixel;
				++byteIndex;
			}
		}
		
		ImageData imageData = new ImageData(size[0], size[1], 8, palette, 1, scaledImageAsByte);
		
		return new Image(Display.getCurrent(), imageData);

	}

	private static float[] getStatistics(final int[] size, final float[] data) {
		
		float min = Float.MAX_VALUE;
		float max = -Float.MAX_VALUE;
		float sum = 0.0f;
		float val;
		for (int j = 0; j < size[1]; j++) {
			for (int i = 0; i < size[0]; i++) {
				val = data[i + j*size[0]];
				sum += val;
				if (val < min) min = val;
				if (val > max) max = val;
			}
		}
		float mean = sum / (size[0] * size[1]);
		return new float[] { min, max, mean };
	}
	

	@Override
	public Object create(Class serviceInterface, IServiceLocator parentLocator,
			IServiceLocator locator) {
		
		if (serviceInterface==IImageService.class) {
			return new ImageService();
		} 
		return null;
	}

}
