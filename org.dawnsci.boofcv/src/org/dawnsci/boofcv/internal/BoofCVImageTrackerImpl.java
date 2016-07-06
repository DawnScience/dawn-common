/*
 * Copyright (c) 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.boofcv.internal;

import org.dawnsci.boofcv.converter.ConvertIDataset;
import org.eclipse.dawnsci.analysis.api.image.IImageTracker;
import org.eclipse.january.dataset.IDataset;

import boofcv.abst.tracker.TrackerObjectQuad;
import boofcv.factory.tracker.FactoryTrackerObjectQuad;
import boofcv.struct.image.ImageFloat32;
import georegression.struct.shapes.Quadrilateral_F64;

public class BoofCVImageTrackerImpl implements IImageTracker {

	private TrackerObjectQuad<ImageFloat32> tracker;
	private Quadrilateral_F64 locationTracker;

	@Override
	public void initialize(IDataset input, double[] location, TrackerType type) throws Exception {
		ImageFloat32 image = ConvertIDataset.convertFrom(input, ImageFloat32.class, 1);
		switch (type) {
		case TLD:
			tracker = FactoryTrackerObjectQuad.tld(null, ImageFloat32.class);
			break;
		case CIRCULANT:
			tracker = FactoryTrackerObjectQuad.circulant(null, ImageFloat32.class);
			break;
		case SPARSEFLOW:
			throw new Exception("Not yet implemented");
		case MEANSHIFTCOMANICIU2003:
			throw new Exception("Not yet implemented");
		default:
			break;
		}
		// specify the target's initial location and initialize with the
		// first frame
		locationTracker = new Quadrilateral_F64(location[0], location[1], location[2], location[3], location[4],
				location[5], location[6], location[7]);

		tracker.initialize(image, locationTracker);
	}

	@Override
	public double[] track(IDataset data) throws Exception {
		ImageFloat32 image = ConvertIDataset.convertFrom(data, ImageFloat32.class, 1);
		boolean visible = tracker.process(image, locationTracker);
		if (!visible)
			return null;
		double[] resultLocation = new double[8];
		resultLocation[0] = locationTracker.getA().x;
		resultLocation[1] = locationTracker.getA().y;
		resultLocation[2] = locationTracker.getB().x;
		resultLocation[3] = locationTracker.getB().y;
		resultLocation[4] = locationTracker.getC().x;
		resultLocation[5] = locationTracker.getC().y;
		resultLocation[6] = locationTracker.getD().x;
		resultLocation[7] = locationTracker.getD().y;
		return resultLocation;
	}

}
