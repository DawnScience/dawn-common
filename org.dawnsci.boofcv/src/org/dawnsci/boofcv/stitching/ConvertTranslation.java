/*-
 * Copyright (c) 2014 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.boofcv.stitching;

import boofcv.struct.image.ImageSingleBand;

/**
 * Class to convert translations, into pixels and into rotated coordinate
 * systems
 * 
 * @author Alex Andrassy
 * 
 * @param <T>
 */
public class ConvertTranslation<T extends ImageSingleBand<?>> {

	// stores the translation
	private double[] translation;

	public ConvertTranslation(double[] translation) {
		this.translation = translation;
	}

	public ConvertTranslation(double x, double y) {

		translation = new double[2];
		translation[0] = x;
		translation[1] = y;
	}

	/**
	 * @return the translation
	 */
	public double[] getTranslation() {
		return translation;
	}

	/**
	 * Converts the translation into pixels.
	 * 
	 * @param image
	 *            To get size in pixels
	 * @param fieldOfViewWidth
	 *            To get size in microns (or other relevant unit)
	 */
	public void micronsToPixels(T image, double fieldOfViewWidth) {
		// calculates number of pixels per micron
		double factor = image.width / fieldOfViewWidth;
		// converts the translation from microns into pixels
		translation[0] = translation[0] * factor;
		translation[1] = translation[1] * factor;

	}

	/**
	 * Converts the translation into a rotated coordinate system, in particular
	 * to find the horizontal and vertical translation.
	 * 
	 * @param angle
	 *            By which the coordinates are rotated
	 */
	public void rotateTranslation(double angle) {
		/*
		 * x2 = x1costheta + y1sintheta y2 = -x1sintheta + y1costheta
		 */

		// calculates the horizontal and vertical components of the given
		// translation
		double x1Horizontal = translation[0] * Math.cos(angle);
		double x1Vertical = translation[0] * Math.sin(angle);
		double y1Horizontal = translation[1] * Math.sin(angle);
		double y1Vertical = translation[1] * Math.cos(angle);

		// calculates the total horizontal and vertical translations
		translation[0] = x1Horizontal + y1Horizontal;
		translation[1] = y1Vertical - x1Vertical;
	}
}
