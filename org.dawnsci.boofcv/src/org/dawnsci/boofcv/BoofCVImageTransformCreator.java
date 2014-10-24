/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.boofcv;

import org.dawnsci.boofcv.internal.BoofCVImageTransformImpl;
import org.eclipse.dawnsci.analysis.api.image.IImageTransform;

/**
 * Class used to test the BoofCVImageTransformImpl
 * @author wqk87977
 *
 * @internal only use in unit tests.
 */
public class BoofCVImageTransformCreator {

	public BoofCVImageTransformCreator(){
		
	}

	/**
	 * Used only for testing - DO NOT USE Externally. Instead get the Service by class.
	 * @return
	 */
	public static IImageTransform createTransformService(){
		return new BoofCVImageTransformImpl();
	}
}