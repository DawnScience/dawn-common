/*-
 * Copyright (c) 2014 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.boofcv.stitching;

import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.function.MapToRotatedCartesian;

/**
 * 
 * @authors Alex Andrassy, Baha El-Kassaby
 *
 */
public class ImagePreprocessing {

	private static int xdiameter = 478;
	private static int ydiameter = 472;
	private static int  buffer = 10;

	public static void rotateTranslation(double[] translation, double angle) {
		/*
		 * x2 = x1costheta + y1sintheta y2 = -x1sintheta + y1costheta
		 */
		// calculates the horizontal and vertical components of the given translation
		double x1Horizontal = translation[0] * Math.cos(angle);
		double x1Vertical = translation[0] * Math.sin(angle);
		double y1Horizontal = translation[1] * Math.sin(angle);
		double y1Vertical = translation[1] * Math.cos(angle);

		// calculates the total horizontal and vertical translations
		translation[0] = x1Horizontal + y1Horizontal;
		translation[1] = y1Vertical - x1Vertical;
		
	}

	public static IDataset rotateAndCrop(IDataset image, double angle) {
		double angleFromMeta = 0;
		//TODO use metadata
//		try {
//			angleFromMeta = ((IPeemMetadata)image.getMetadata(IPeemMetadata.class)).getRotation();
//		} catch (Exception e) {
//			e.printStackTrace();
//		};
		// set dimensions of returned image
		int width = (int) (xdiameter * Math.cos(Math.PI / 4) - buffer);
		int height = (int) (ydiameter * Math.cos(Math.PI / 4) - buffer);

		// find the top left corner of the largest square within the circle
		int cornerx = (image.getShape()[0] - width) / 2;
		int cornery = ((image.getShape()[1] - height) / 2);
		// find its position relative to the centre
		double[] translation = new double[2];
		translation[0] = image.getShape()[0] / 2 - cornerx;
		translation[1] = image.getShape()[1] / 2 - cornery;

		// find the top left corner of the largest square within the rotated
		// circle
		rotateTranslation(translation, Math.toRadians(angleFromMeta));
		// find its position relative to the top left corner of the image
		int useablex = (int) (image.getShape()[0] / 2 - translation[0]);
		int useabley = (int) (image.getShape()[1] / 2 - translation[1]);

		// rotate image
		MapToRotatedCartesian rotator = new MapToRotatedCartesian(useablex,
				useabley, width, height, angleFromMeta);
		List<Dataset> rotated = rotator.value(image);

		return rotated.get(0);
	}

	/**
	 * 
	 * @param input
	 * @param matrixSize
	 * @return Ordered array of Dataset
	 * @throws Exception
	 */
	public static IDataset[][] ListToArray(List<IDataset> input, int matrixSize) {
		int rows = matrixSize;
		int columns = matrixSize;
		IDataset[][] images = new Dataset[rows][columns];
		for (int i = 0; i < rows; i++) {
			for(int j = 0; j < columns; j++) {
				images[i][j] = input.get((i * columns) + j);
				//	images[i][j].setMetadata(getUniqueMetadata(i+1, j+1));
			}
		}
		return images;
	}
}
