/*-
 * Copyright (c) 2014 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.boofcv.stitching;

import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;

import georegression.struct.homography.Homography2D_F64;
import boofcv.alg.distort.ImageDistort;
import boofcv.alg.distort.PixelTransformHomography_F32;
import boofcv.alg.distort.impl.DistortSupport;
import boofcv.alg.distort.impl.ImplImageDistort_F32;
import boofcv.alg.distort.impl.ImplImageDistort_I16;
import boofcv.alg.interpolate.impl.ImplBilinearPixel_F32;
import boofcv.alg.interpolate.impl.NearestNeighborPixel_S16;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSInt16;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.image.MultiSpectral;

/**
 * StitchingObject is a class for stitching two images together onto a new
 * image, given the translation that relates them. Several image types are
 * supported.
 * 
 * @author Alex Andrassy, Baha El-Kassaby
 * 
 * @param <T>
 *            The type of images being stitched
 */
public class StitchingObject<T extends ImageSingleBand<?>> {

	private Homography2D_F64 fromAtoB;

	/**
	 * Creates a StitchingObject
	 * 
	 * @param translation
	 *            The translation that relates the two images
	 */
	public StitchingObject(double[] translation) {
		
		// creates a Homograpyh2D_F64 which represents the given translation
		fromAtoB = new Homography2D_F64(1, 0, translation[0], 0, 1, translation[1], 0, 0, 1);
		
	}
	
	/**
	 * Creates a StitchingObject
	 * 
	 * @param x
	 *            The x translation that relates the two images
	 * @param y
	 *            The y translation that relates the two images
	 */
	public StitchingObject(double x, double y) {
		// creates a Homography2D_F64 which represents the given translation
		fromAtoB = new Homography2D_F64(1, 0, x, 0, 1, y, 0, 0, 1);
	}

	/**
	 * Creates a StitchingObject
	 * 
	 * @param transform
	 *            The transform that relates the two images
	 */
	public StitchingObject(Homography2D_F64 transform) {
		// creates a Homograpyh2D_F64 which represents the given transform
		fromAtoB = transform;
	}

	/**
	 * 
	 * Creates a new image exactly big enough to fit both images at the given
	 * relative distance. The images are then stitched to this new image
	 * according to the translation between them. Used for MultiSpectral
	 * (coloured) images.
	 * 
	 * @param imageA
	 *            The first image to be stitched
	 * @param imageB
	 *            The second image to be stitched
	 * @param origin
	 *            The pixel coordinates of some origin. If the position of the
	 *            second image is specified relative to the first image, the
	 *            origin should be 0. If the position of the second image is
	 *            specified relative to an origin, the coordinates of the origin
	 *            should be specified here. Note, the first image will be
	 *            stitched at the origin.
	 * @return The new image with both images stitched to it
	 */
	public MultiSpectral<ImageFloat32> stitchMultiBand(
			MultiSpectral<ImageFloat32> imageA,
			MultiSpectral<ImageFloat32> imageB, double[] origin) {

		// update the translation such that it is relative to the given origin
		fromAtoB.set(fromAtoB.a11, fromAtoB.a12, fromAtoB.a13 - origin[0],
				fromAtoB.a21, fromAtoB.a22, fromAtoB.a23 - origin[1],
				fromAtoB.a31, fromAtoB.a32, fromAtoB.a33);

		// specify size of output image
		int outputWidth = imageA.getWidth();
		int outputHeight = imageA.getHeight();

		// if the second image is right of the first, extend the output image to
		// accommodate both
		if (fromAtoB.a13 - imageB.getWidth() < -outputWidth) {
			outputWidth = (int) (-fromAtoB.a13 + imageB.getWidth());
		}

		// if the second image is below the first, extend the output image to
		// accommodate both
		if (fromAtoB.a23 - imageB.getHeight() < -outputHeight) {
			outputHeight = (int) (-fromAtoB.a23 + imageB.getHeight());
		}

		// if the second image is left of the first, extend the output image and
		// shift the images to accommodate and display both
		double xshift = 0;
		if (fromAtoB.a13 > 0) {
			outputWidth = (int) (outputWidth + fromAtoB.a13);
			xshift = fromAtoB.a13;
			origin[0] = origin[0] + xshift;
		}

		// if the second image is above the first, extend the output image and
		// shift the images to accommodate and display both
		double yshift = 0;
		if (fromAtoB.a23 > 0) {
			outputHeight = (int) (outputHeight + fromAtoB.a23);
			yshift = fromAtoB.a23;
			origin[1] = origin[1] + yshift;
		}
		// where the output images are rendered into
		MultiSpectral<ImageFloat32> work = new MultiSpectral<ImageFloat32>(
				ImageFloat32.class, outputWidth, outputHeight, 3);

		// create a transform to stitch an image to the top corner of the new image
		Homography2D_F64 fromAToWork = new Homography2D_F64(1, 0, xshift, 0, 1, yshift, 0, 0, 1);
		Homography2D_F64 fromWorkToA = fromAToWork.invert(null);

		// used to render the results onto an image
		PixelTransformHomography_F32 model = new PixelTransformHomography_F32();
		ImageDistort<MultiSpectral<ImageFloat32>, MultiSpectral<ImageFloat32>> distort =
		DistortSupport.createDistortMS(ImageFloat32.class, model, new ImplBilinearPixel_F32(), true, null);
		// render first image
		model.set(fromWorkToA);
		distort.apply(imageA, work);

		// render second image
		Homography2D_F64 fromWorkToB = fromWorkToA.concat(fromAtoB,null);
		model.set(fromWorkToB);
		distort.apply(imageB,work);
		return work;
	}

	/**
	 * 
	 * Creates a new image exactly big enough to fit both images at the given
	 * relative distance. The images are then stitched to this new image
	 * according to the translation between them.
	 * 
	 * @param imageA
	 *            The first image to be stitched
	 * @param imageB
	 *            The second image to be stitched
	 * @param origin
	 *            The pixel coordinates of some origin. If the position of the
	 *            second image is specified relative to the first image, the
	 *            origin should be 0. If the position of the second image is
	 *            specified relative to an origin, the coordinates of the origin
	 *            should be specified here. Note, the first image will be
	 *            stitched at the origin.
	 * @param monitor
	 *            monitor progress
	 * @return The new image with both images stitched to it
	 */
	public ImageSingleBand<?> stitch(ImageSingleBand<?> imageA, ImageSingleBand<?> imageB, double[] origin, IMonitor monitor) {
		// update the translation such that it is relative to the given origin
		fromAtoB.set(fromAtoB.a11, fromAtoB.a12, fromAtoB.a13 - origin[0],
				fromAtoB.a21, fromAtoB.a22, fromAtoB.a23 - origin[1],
				fromAtoB.a31, fromAtoB.a32, fromAtoB.a33);
		// specify size of output image
		int outputWidth = imageA.getWidth();
		int outputHeight = imageA.getHeight();
		// if the second image is right of the first, extend the output image to
		// accommodate both
		if (fromAtoB.a13 - imageB.getWidth() < -outputWidth) {
			outputWidth = (int) (-fromAtoB.a13 + imageB.getWidth());
		}
		// if the second image is below the first, extend the output image to
		// accommodate both
		if (fromAtoB.a23 - imageB.getHeight() < -outputHeight) {
			outputHeight = (int) (-fromAtoB.a23 + imageB.getHeight());
		}
		// if the second image is left of the first, extend the output image and
		// shift the images to accommodate and display both
		double xshift = 0;
		if (fromAtoB.a13 > 0) {
			outputWidth = (int) (outputWidth + fromAtoB.a13);
			xshift = fromAtoB.a13;
			origin[0] = origin[0] + xshift;
		}
		// if the second image is above the first, extend the output image and
		// shift the images to accommodate and display both
		double yshift = 0;
		if (fromAtoB.a23 > 0) {
			outputHeight = (int) (outputHeight + fromAtoB.a23);
			yshift = fromAtoB.a23;
			origin[1] = origin[1] + yshift;
		}
		// create a transform to stitch an image to the top corner of the new
		// image
		Homography2D_F64 fromAToWork = new Homography2D_F64(1, 0, xshift, 0, 1, yshift, 0, 0, 1);
		Homography2D_F64 fromWorkToA = fromAToWork.invert(null);
		ImageSingleBand<?> work = null;
		if (imageA instanceof ImageSInt16 && imageB instanceof ImageSInt16) {
			// where the output images are rendered into
			work = new ImageSInt16(outputWidth, outputHeight);

			// used to render the results onto an image
			PixelTransformHomography_F32 model = new PixelTransformHomography_F32();
			ImplImageDistort_I16<ImageSInt16, ImageSInt16> distort = new ImplImageDistort_I16<ImageSInt16, ImageSInt16>(
					new NearestNeighborPixel_S16(), null);
			distort.setModel(model);

			// render first image
			model.set(fromWorkToA);
			distort.apply((ImageSInt16)imageA, (ImageSInt16) work, 0, 0, outputWidth, outputHeight);

			// render second image
			Homography2D_F64 fromWorkToB = fromWorkToA.concat(fromAtoB, null);
			model.set(fromWorkToB);
			distort.apply((ImageSInt16)imageB, (ImageSInt16) work, 0, 0, outputWidth, outputHeight);

		} else if (imageA instanceof ImageFloat32 && imageB instanceof ImageFloat32) {
			// where the output images are rendered into
			work = new ImageFloat32(outputWidth, outputHeight);
			// used to render the results onto an image
			PixelTransformHomography_F32 model = new PixelTransformHomography_F32();
			ImplImageDistort_F32 distort = new ImplImageDistort_F32(
					new ImplBilinearPixel_F32(), null);
			distort.setModel(model);
			// render first image
			model.set(fromWorkToA);
			distort.apply((ImageFloat32)imageA, (ImageFloat32) work, 0, 0, outputWidth, outputHeight);

			// render second image
			Homography2D_F64 fromWorkToB = fromWorkToA.concat(fromAtoB, null);
			model.set(fromWorkToB);
			distort.apply((ImageFloat32)imageB, (ImageFloat32) work, 0, 0, outputWidth, outputHeight);
		}
		if (monitor.isCancelled())
			return work;
		monitor.worked(1);
		return work;
	}
}
