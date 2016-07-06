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

import org.dawnsci.boofcv.converter.ConvertIDataset;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.DatasetException;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Slice;

import boofcv.abst.feature.associate.AssociateDescription;
import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.struct.feature.TupleDesc;
import boofcv.struct.image.ImageFloat32;
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
	private TranslationObject<T, TD> translate;

	// factor to convert between microns and pixels
//	private double micronsToPixels;

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
		this.translate = new TranslationObject<T, TD>(detDesc, associate, imageType);
	}

	/**
	 * Sets the factor to convert between microns and pixels
	 * 
	 * @param shape
	 *            shape of image (width) in pixels
	 * @param fieldOfView
	 *            The field of view of the uncropped image, from which we get
	 *            the corresponding number of microns
	 * @throws Exception 
	 */
	public void setConversion(int[] shape, double fieldOfView) throws Exception {
		// calculates the number of pixels per micron
		translate.setConversion(shape, fieldOfView);
//		micronsToPixels = image.getWidth()
//				/ (fieldOfView * Math.cos(Math.PI / 4));
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
	 * @param monitor
	 *            To monitor progress
	 */
	public void translationArray(List<List<T>> images, List<double[]> motorTranslations, IMonitor monitor) {

		// stores the translations
		translations = new double[images.size()][images.get(0).size()][2];
		int idx = 0;
		for (int x = 0; x < translations.length; x++) {
			for (int y = 0; y < translations[0].length; y++) {
				double xtrans = motorTranslations.get(idx)[0];
				double ytrans = motorTranslations.get(idx)[1];

				if (y == 0) {
					// translation of first image is 0
					if (x == 0) {
					}
					// translation of all images in the first column is
					// calculated using the above image
					else {
						translate.associate(images.get(x - 1).get(y),
								images.get(x).get(y), 0, ytrans);
						translations[x][y] = translate.translation();
						// translation of previous image is added to give the
						// translation relative to the first image
						translations[x][y][0] += translations[x - 1][y][0];
						translations[x][y][1] += translations[x - 1][y][1];
					}
				}
				// translation of all images other images is calculated using the image to the left
				else {
					translate.associate(images.get(x).get(y - 1), images
							.get(x).get(y), xtrans, 0);
					translations[x][y] = translate.translation();
					// translation of previous image is added to give the translation relative to the first image
					translations[x][y][0] += translations[x][y-1][0];
					translations[x][y][1] += translations[x][y-1][1];
				}
				if (monitor != null) {
					if (monitor.isCancelled())
						return;
					monitor.worked(1);
				}
			}
		}
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
	 *            The lazy set of images to be stitched
	 * @param xtrans
	 *            The expected translation in microns in the x direction
	 *            relating successive images in a row
	 * @param ytrans
	 *            The expected translation in microns in the y direction
	 *            relating successive images in a column
	 * @param rows
	 * @param columns
	 * @param monitor
	 *            To monitor progress
	 * @throws DatasetException 
	 */
	public void translationArray(ILazyDataset images, double[][][] motorTranslations, int rows, int columns, IMonitor monitor) throws DatasetException {

		// stores the translations
		translations = new double[columns][rows][2];

		int index = 0;
		for (int x = 0; x < rows; x++) {
			for (int y = 0; y < columns; y++) {
				double xtrans = motorTranslations[y][x][0];
				double ytrans = motorTranslations[y][x][1];

				if (y == 0) {
					// translation of first image is 0
					if (x == 0) {
					}
					// translation of all images in the first column is
					// calculated using the above image
					else {
						IDataset image1 = images.getSlice(new Slice((index-1), images.getShape()[0], images.getShape()[1])).squeeze();
						ImageSingleBand<?> aimage = ConvertIDataset.convertFrom(image1, ImageFloat32.class, 1);
						IDataset image2 = images.getSlice(new Slice(index, images.getShape()[0], images.getShape()[1])).squeeze();
						ImageSingleBand<?> bimage = ConvertIDataset.convertFrom(image2, ImageFloat32.class, 1);
						translate.associate((T)aimage, (T)bimage, 0, ytrans);
						translations[y][x] = translate.translation();
						// translation of previous image is added to give the
						// translation relative to the first image
						translations[y][x][0] += translations[y][x - 1][0];
						translations[y][x][1] += translations[y][x - 1][1];
					}
				}
				// translation of all images other images is calculated using the image to the left
				else {
					IDataset image1 = images.getSlice(new Slice((index-1), images.getShape()[0], images.getShape()[1])).squeeze();
					ImageSingleBand<?> aimage = ConvertIDataset.convertFrom(image1, ImageFloat32.class, 1);
					IDataset image2 = images.getSlice(new Slice(index, images.getShape()[0], images.getShape()[1])).squeeze();
					ImageSingleBand<?> bimage = ConvertIDataset.convertFrom(image2, ImageFloat32.class, 1);
					translate.associate((T)aimage, (T)bimage, xtrans, 0);
					translations[y][x] = translate.translation();
					// translation of previous image is added to give the translation relative to the first image
					translations[y][x][0] += translations[y - 1][x][0];
					translations[y][x][1] += translations[y - 1][x][1];
				}
				index++;
				if (monitor != null) {
					if (monitor.isCancelled())
						return;
					monitor.worked(1);
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
	 * @param monitor
	 *            To monitor progress
	 */
	public void translationArrayHomography(List<List<T>> images, List<double[]> motorTranslations, IMonitor monitor) {
		// temporarily stores the transforms
		Homography2D_F64 transform;

		// stores the translations
		translations = new double[images.size()][images.get(0).size()][2];

		int idx = 0;
		for (int x = 0; x < translations.length; x++) {
			for (int y = 0; y < translations[0].length; y++) {
				double xtrans = motorTranslations.get(idx)[0];
				double ytrans = motorTranslations.get(idx)[1];

				if (y == 0) {
					// translation of first image is 0
					if (x == 0) {
					}
					// translation of all images in the first column is calculated using the above image
					else {
						translate.associate(images.get(x - 1).get(y),
								images.get(x).get(y), 0, ytrans);
						transform = translate.homographyTransform();
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
					translate.associate(images.get(x).get(y - 1), images
							.get(x).get(y), xtrans, 0);
					transform = translate.homographyTransform();
					// only the translation of the transform is used
					translations[x][y][0] = transform.a13;
					translations[x][y][1] = transform.a23;
					// translation of previous image is added to give the
					// translation relative to the first image
					translations[x][y][0] += translations[x][y - 1][0];
					translations[x][y][1] += translations[x][y-1][1];
				}
				idx++;
				if (monitor != null) {
					if (monitor.isCancelled())
						return;
					monitor.worked(1);
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
	 * @param monitor
	 *            To monitor progress
	 */
	public void translationArraySpecial(List<List<T>> images, List<double[]> motorTranslations, IMonitor monitor) {
		// temporarily stores the transforms
		Se2_F64 transform;

		// stores the translations
		translations = new double[images.size()][images.get(0).size()][2];

		int idx = 0;
		for (int x = 0; x < translations.length; x++) {
			for (int y = 0; y < translations[0].length; y++) {
				double xtrans = motorTranslations.get(idx)[0];
				double ytrans = motorTranslations.get(idx)[1];

				if (y == 0) {
					// translation of first image is 0
					if (x == 0) {
					}
					// translation of all images in the first column is calculated using the above image
					else {
						translate.associate(images.get(x - 1).get(y),
								images.get(x).get(y), 0, ytrans);
						transform = translate.specialTransform();
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
					translate.associate(images.get(x).get(y - 1), images
							.get(x).get(y), xtrans, 0);
					transform = translate.specialTransform();
					// only the translation of the transform is used
					translations[x][y][0] = transform.getX();
					translations[x][y][1] = transform.getY();
					// translation of previous image is added to give the translation relative to the first image
					translations[x][y][0] += translations[x][y-1][0];
					translations[x][y][1] += translations[x][y-1][1];
				}
				idx++;
				if (monitor != null) {
					if (monitor.isCancelled())
						return;
					monitor.worked(1);
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
	 * @param monitor
	 *            To monitor progress
	 */
	public void theoreticalTranslation(int rows, int columns, double[][][] motorTranslations, IMonitor monitor) {
		// stores the translations
		translations = new double[columns][rows][2];
		// calculates the translations of each image relative to the first image
		for (int x = 0; x < rows; x++) {
			for (int y = 0; y < columns; y++) {
				double xtrans = motorTranslations[y][x][0];
				double ytrans = motorTranslations[y][x][1];

				// convert the translations from microns into pixels
				double micronsToPixels = translate.getConversion();
				xtrans = micronsToPixels * xtrans;
				ytrans = micronsToPixels * ytrans;

				translations[y][x][0] = -xtrans*y;
				translations[y][x][1] = -ytrans*x;
				if (monitor != null) {
					if (monitor.isCancelled())
						return;
					monitor.worked(1);
				}
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
					StitchingObject stitch = new StitchingObject<>(translations[i][j]);
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
	 * @param monitor
	 *            monitor progress
	 * @return The new image with all the images stitched to it
	 */
	public ImageSingleBand<?> stitch(List<List<ImageSingleBand<?>>> images, IMonitor monitor) {
		// define an origin to be the top-corner of the first image such that
		// all the translations can be given relative to this image
		double[] origin = new double[2];
		origin[0] = 0;
		origin[1] = 0;
		ImageSingleBand<?> result = images.get(0).get(0);
		// stitch each image together with another, in turn, onto a new image
		for (int i = 0; i < images.size(); i++) {
			for (int j = 0; j < images.get(0).size(); j++) {
				if (i != 0 || j != 0) {
					StitchingObject<?> stitcher = new StitchingObject<>(translations[i][j]);
					result = stitcher.stitch(result, images.get(i).get(j), origin);
				}
				if (monitor != null) {
					if (monitor.isCancelled())
						return result;
					monitor.worked(1);
				}
			}
		}
		return result;
	}

	/**
	 * Stitches a list of images together onto a new image.
	 * 
	 * @param images
	 *            The lazy list of images to be stitched
	 * @param monitor
	 *            monitor progress
	 * @return The new image with all the images stitched to it
	 * @throws DatasetException 
	 */
	public ImageSingleBand<?> stitch(ILazyDataset images, int rows, int columns, IMonitor monitor) throws DatasetException {
		// define an origin to be the top-corner of the first image such that
		// all the translations can be given relative to this image
		double[] origin = new double[2];
		origin[0] = 0;
		origin[1] = 0;
		IDataset image = images.getSlice(new Slice(0, images.getShape()[0], images.getShape()[1])).squeeze();
		ImageSingleBand<?> result = ConvertIDataset.convertFrom(image, ImageFloat32.class, 1);
		// stitch each image together with another, in turn, onto a new image
		int idx = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				if (i != 0 || j != 0) {
					StitchingObject<?> stitcher = new StitchingObject<>(translations[j][i]);
					IDataset slice = images.getSlice(new Slice(idx, images.getShape()[0], images.getShape()[1])).squeeze();
					ImageSingleBand<?> im = ConvertIDataset.convertFrom(slice, ImageFloat32.class, 1);
					result = stitcher.stitch(result, im, origin);
				}
				idx++;
				if (monitor != null) {
					if (monitor.isCancelled())
						return result;
					monitor.worked(1);
				}
			}
		}
		return result;
	}
}
