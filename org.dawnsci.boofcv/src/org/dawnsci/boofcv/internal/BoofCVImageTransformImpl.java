/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.boofcv.internal;

import org.dawnsci.boofcv.converter.ConvertIDataset;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.image.IImageTransform;

import boofcv.alg.distort.DistortImageOps;
import boofcv.alg.interpolate.TypeInterpolate;
import boofcv.struct.image.ImageFloat32;

/**
 * Implementation of IImageTransform<br>
 * 
 * This class is internal and not supposed to be used out of this bundle.
 * 
 * @author wqk87977
 *
 */
public class BoofCVImageTransformImpl implements IImageTransform {

	static {
		System.out.println("Starting BoofCV image transform service.");
	}

	public BoofCVImageTransformImpl() {
		// Important do nothing here, OSGI may start the service more than once.
	}

	@Override
	public IDataset rotate(IDataset data, double angle) throws Exception {
		return rotate(data, angle, true);
	}

	@Override
	public IDataset rotate(IDataset data, double angle, boolean keepShape) throws Exception {
		if (data.getShape().length != 2)
			throw new Exception("Data shape is not 2D");
		ImageFloat32 image = ConvertIDataset.convertFrom(data, ImageFloat32.class, 1);
		int width = 0, height = 0;
		if (keepShape) {
			width = image.width;
			height = image.height;
		} else {
			// calculate resulting bounding box
			width = (int) (image.width * Math.cos(Math.toRadians(angle)) + image.height
					* Math.sin(Math.toRadians(angle)));
			height = (int) (image.height * Math.cos(Math.toRadians(angle)) + image.width
					* Math.sin(Math.toRadians(angle)));
		}
		ImageFloat32 rotated = new ImageFloat32(height, width);

		DistortImageOps.rotate(image, rotated, TypeInterpolate.BILINEAR, (float)Math.toRadians(angle));
		return ConvertIDataset.convertTo(rotated, true);
	}

}
