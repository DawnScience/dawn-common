/*-
 * Copyright (c) 2014 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.boofcv.stitching;

import georegression.struct.homography.Homography2D_F64;
import georegression.struct.se.Se2_F64;

import java.util.List;

import boofcv.abst.feature.associate.AssociateDescription;
import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.struct.feature.TupleDesc;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.image.MultiSpectral;

/**
 * FullStitchingObject is a class for finding the translation between the first
 * image out of a set, and all other images, and stitching the set together onto
 * a new image. Several image types are supported. 
 * TODO refactor code replication
 * 
 * @author Alex Andrassy, Baha El-Kassaby
 * 
 * @param <T>
 *            The type of images being stitched
 * @param <TD>
 *            The type of feature descriptor that is used
 */
public class FullStitchingObject<T extends ImageSingleBand<?>, TD extends TupleDesc<?>> {

	// stores translations
	private double[][][] translations;

	// used to calculate translation between images
	private TranslationObject test;

	// factor to convert between microns and pixels
	private double micronsToPixels = 512/50;

	/**
	 * Creates a FullStitchingObject
	 * 
	 * @param detDesc
	 *            algorithm used to detect and describe interest points, used to
	 *            create TranslationObject
	 * @param associate
	 *            associates descriptions together by minimizing an error
	 *            metric, used to create TranslationObject
	 * @param imageType
	 *            The type of images being stitched, used to create
	 *            TranslationObject
	 */
	public FullStitchingObject(DetectDescribePoint<T, TD> detDesc,
			AssociateDescription<TD> associate, Class<T> imageType) {
		this.test = new TranslationObject<T, TD>(detDesc, associate, imageType);
	}

	/**
	 * Sets the factor to convert between microns and pixels
	 * 
	 * @param image
	 *            A cropped image, from which we get a number of pixels
	 * @param fieldOfView
	 *            The field of view of the uncropped image, from which we get
	 *            the corresponding number of microns
	 */
	public void setConversion(T image, double fieldOfView) {
		// calculates the number of pixels per micron
		micronsToPixels = image.getWidth()
				/ (fieldOfView * Math.cos(Math.PI / 4));
	}

	/**
	 * Calculates the translation that relates each image to the first image.
	 * Relative translations are found using feature recognition within the
	 * overlapping region, identifying corresponding features and calculating
	 * the average translation relation corresponding features. The translation
	 * is found between successive images and added to the translation found for
	 * the previous image, thus giving all translations relative to the first
	 * image.
	 * 
	 * @param images
	 *            The set of images to be stitched
	 * @param xtrans
	 *            The expected translation in microns in the x direction
	 *            relating successive images in a row
	 * @param ytrans
	 *            The expected translation in microns in the y direction
	 *            relating successive images in a column
	 */
	public void translationArray(List<List<ImageAndMetadata>> images) {

		// stores the translations
		translations = new double[images.size()][images.get(0).size()][2];

		for (int x = 0; x < translations.length; x++) {
			for (int y = 0; y < translations[0].length; y++) {
				double xtrans = images.get(x).get(y).getMetadata().getXYMotorPosition()[0];
				double ytrans = images.get(x).get(y).getMetadata().getXYMotorPosition()[1];
				if (y == 0) {
					// translation of first image is 0
					if (x == 0) {
					}
					// translation of all images in the first column is
					// calculated using the above image
					else {
						test.associate(images.get(x - 1).get(y).getImage(),
								images.get(x).get(y).getImage(), 0, ytrans);
						translations[x][y] = test.translation();
						// translation of previous image is added to give the
						// translation relative to the first image
						translations[x][y][0] += translations[x - 1][y][0];
						translations[x][y][1] += translations[x - 1][y][1];
					}
				}
				// translation of all images other images is calculated using the image to the left
				else {
					test.associate(images.get(x).get(y - 1).getImage(), images
							.get(x).get(y).getImage(), xtrans, 0);
					translations[x][y] = test.translation();
					// translation of previous image is added to give the translation relative to the first image
					translations[x][y][0] += translations[x][y-1][0];
					translations[x][y][1] += translations[x][y-1][1];
				}
			}
		}
	}

	/**
	 * Calculates the translation that relates each image to the first image.
	 * Relative translations are found using feature recognition within the
	 * overlapping region, identifying corresponding features and a best fit
	 * homography is found to describe the transform. Only the x and y
	 * translations are extracted from this. The translation is found between
	 * successive images and added to the translation found for the previous
	 * image, thus giving all translations relative to the first image.
	 * 
	 * @param images
	 *            The set of images to be stitched
	 * @param xtrans
	 *            The expected translation in microns in the x direction
	 *            relating successive images in a row
	 * @param ytrans
	 *            The expected translation in microns in the y direction
	 *            relating successive images in a column
	 */
	public void translationArrayHomography(List<List<ImageAndMetadata>> images) {
		// temporarily stores the transforms
		Homography2D_F64 transform;

		// stores the translations
		translations = new double[images.size()][images.get(0).size()][2];

		for (int x = 0; x < translations.length; x++) {
			for (int y = 0; y < translations[0].length; y++) {
				double xtrans = images.get(x).get(y).getMetadata().getXYMotorPosition()[0];
				double ytrans = images.get(x).get(y).getMetadata().getXYMotorPosition()[1];
				if (y == 0) {
					// translation of first image is 0
					if (x == 0) {
					}
					// translation of all images in the first column is calculated using the above image
					else {
						test.associate(images.get(x - 1).get(y).getImage(),
								images.get(x).get(y).getImage(), 0, ytrans);
						transform = test.homographyTransform();
						// only the translation of the transform is used
						translations[x][y][0] = transform.a13;
						translations[x][y][1] = transform.a23;
						// translation of previous image is added to give the
						// translation relative to the first image
						translations[x][y][0] += translations[x - 1][y][0];
						translations[x][y][1] += translations[x - 1][y][1];
					}
				}
				// translation of all images other images is calculated using the image to the left
				else {
					test.associate(images.get(x).get(y - 1).getImage(), images
							.get(x).get(y).getImage(), xtrans, 0);
					transform = test.homographyTransform();
					// only the translation of the transform is used
					translations[x][y][0] = transform.a13;
					translations[x][y][1] = transform.a23;
					// translation of previous image is added to give the
					// translation relative to the first image
					translations[x][y][0] += translations[x][y - 1][0];
					translations[x][y][1] += translations[x][y-1][1];
				}
			}
		}
	}

	/**
	 * Calculates the translation that relates each image to the first image.
	 * Relative translations are found using feature recognition within the
	 * overlapping region, identifying corresponding features and a best fit
	 * specialEuclidean is found to describe the transform. Only the x and y
	 * translations are extracted from this. The translation is found between
	 * successive images and added to the translation found for the previous
	 * image, thus giving all translations relative to the first image.
	 * 
	 * @param images
	 *            The set of images to be stitched
	 * @param xtrans
	 *            The expected translation in microns in the x direction
	 *            relating successive images in a row
	 * @param ytrans
	 *            The expected translation in microns in the y direction
	 *            relating successive images in a column
	 */
	public void translationArraySpecial(List<List<ImageAndMetadata>> images) {
		// temporarily stores the transforms
		Se2_F64 transform;

		// stores the translations
		translations = new double[images.size()][images.get(0).size()][2];

		for (int x = 0; x < translations.length; x++) {
			for (int y = 0; y < translations[0].length; y++) {
				double xtrans = images.get(x).get(y).getMetadata().getXYMotorPosition()[0];
				double ytrans = images.get(x).get(y).getMetadata().getXYMotorPosition()[1];
				if (y == 0) {
					// translation of first image is 0
					if (x == 0) {
					}
					// translation of all images in the first column is calculated using the above image
					else {
						test.associate(images.get(x - 1).get(y).getImage(),
								images.get(x).get(y).getImage(), 0, ytrans);
						transform = test.specialTransform();
						// only the translation of the transform is used
						translations[x][y][0] = transform.getX();
						translations[x][y][1] = transform.getY();
						// translation of previous image is added to give the
						// translation relative to the first image
						translations[x][y][0] += translations[x - 1][y][0];
						translations[x][y][1] += translations[x - 1][y][1];
					}
				}
				// translation of all images other images is calculated using the image to the left
				else {
					test.associate(images.get(x).get(y - 1).getImage(), images
							.get(x).get(y).getImage(), xtrans, 0);
					transform = test.specialTransform();
					// only the translation of the transform is used
					translations[x][y][0] = transform.getX();
					translations[x][y][1] = transform.getY();
					// translation of previous image is added to give the translation relative to the first image
					translations[x][y][0] += translations[x][y-1][0];
					translations[x][y][1] += translations[x][y-1][1];
				}
			}
		}
	}

	/**
	 * Calculates the translation that relates each image to the first image
	 * using the expected translations only.
	 * 
	 * @param images
	 *            The set of images to be stitched
	 * @param xtrans
	 *            The expected translation in microns in the x direction
	 *            relating successive images in a row
	 * @param ytrans
	 *            The expected translation in microns in the y direction
	 *            relating successive images in a column
	 */
	public void theoreticalTranslation(List<List<ImageAndMetadata>> images) {
		// stores the translations
		translations = new double[images.size()][images.get(0).size()][2];
		// calculates the translations of each image relative to the first image
		for (int x = 0; x < translations.length; x++) {
			for (int y = 0; y < translations[0].length; y++) {
				double xtrans = images.get(x).get(y).getMetadata().getXYMotorPosition()[0];
				double ytrans = images.get(x).get(y).getMetadata().getXYMotorPosition()[1];
				// convert the translations from microns into pixels
				xtrans = micronsToPixels * xtrans;
				ytrans = micronsToPixels * ytrans;

				translations[x][y][0] = -xtrans*y;
				translations[x][y][1] = -ytrans*x;
			}
		}
	}

	/**
	 * Stitches a set of images together onto a new image. The image type is
	 * MultiSpectral (coloured)
	 * 
	 * @param images
	 *            The set of images to be stitched
	 * @return The new image with all the images stitched to it
	 */
	public MultiSpectral<T> stitchMultiSpectral(
			List<List<MultiSpectral<T>>> images) {
		// define an origin to be the top-corner of the first image such that
		// all the translations can be given relative to this image
		double[] origin = new double[2];
		origin[0] = 0;
		origin[1] = 0;
		MultiSpectral<T> result = images.get(0).get(0);
		// stitch each image together with another, in turn, onto a new image
		for (int i = 0; i < images.size(); i++) {
			for (int j = 0; j < images.get(0).size(); j++) {

				if (i != 0 || j != 0) {
					StitchingObject stitch = new StitchingObject(translations[i][j]);
					result = stitch.stitchMultiBand(result, images.get(i).get(j), origin);
				}
			}
		}
		return result;
	}

	/**
	 * Stitches a list of images together onto a new image.
	 * 
	 * @param images
	 *            The list of images to be stitched
	 * @return The new image with all the images stitched to it
	 */
	public ImageSingleBand<?> stitch(List<List<ImageAndMetadata>> images) {
		// define an origin to be the top-corner of the first image such that
		// all the translations can be given relative to this image
		double[] origin = new double[2];
		origin[0] = 0;
		origin[1] = 0;
		ImageSingleBand<?> result = images.get(0).get(0).getImage();
		// stitch each image together with another, in turn, onto a new image
		for (int i = 0; i < images.size(); i++) {
			for (int j = 0; j < images.get(0).size(); j++) {
				if (i != 0 || j != 0) {
					StitchingObject<?> stitcher = new StitchingObject(translations[i][j]);
					result = stitcher.stitch(result, images.get(i).get(j).getImage(), origin);
				}
			}
		}
		return result;
	}
}
