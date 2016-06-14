/*
 * Copyright (c) 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.boofcv;

import org.dawnsci.boofcv.internal.BoofCVImageTrackerImpl;
import org.eclipse.dawnsci.analysis.api.image.IImageTracker;

/**
 * Class used to test the BoofCVImageTrackerImpl
 * 
 * @author Baha El Kassaby
 *
 * @internal only use in unit tests.
 */
public class BoofCVImageTrackerServiceCreator {

	public BoofCVImageTrackerServiceCreator() {

	}

	/**
	 * Used only for testing - DO NOT USE Externally. Instead get the Service by
	 * class.
	 * 
	 * @return
	 */
	public static IImageTracker createImageTrackerService() {
		return new BoofCVImageTrackerImpl();
	}
}
