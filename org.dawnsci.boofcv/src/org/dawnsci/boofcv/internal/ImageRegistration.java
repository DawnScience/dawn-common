package org.dawnsci.boofcv.internal;

/* Copyright (C) 2013, 2014 Zachary Scott <zscott.dev@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
import georegression.fitting.affine.ModelManagerAffine2D_F64;
import georegression.struct.affine.Affine2D_F64;
import georegression.struct.point.Point2D_F64;

import java.util.ArrayList;
import java.util.List;

import org.ddogleg.fitting.modelset.ModelManager;
import org.ddogleg.fitting.modelset.ModelMatcher;
import org.ddogleg.fitting.modelset.ransac.Ransac;
import org.ddogleg.struct.FastQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import boofcv.abst.feature.associate.AssociateDescription;
import boofcv.abst.feature.associate.ScoreAssociation;
import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.abst.feature.detect.interest.ConfigFastHessian;
import boofcv.alg.distort.ImageDistort;
import boofcv.alg.distort.PixelTransformAffine_F32;
import boofcv.alg.distort.impl.DistortSupport;
import boofcv.alg.feature.UtilFeature;
import boofcv.alg.interpolate.impl.ImplBilinearPixel_F32;
import boofcv.alg.sfm.robust.DistanceAffine2D;
import boofcv.alg.sfm.robust.GenerateAffine2D;
import boofcv.factory.feature.associate.FactoryAssociation;
import boofcv.factory.feature.detdesc.FactoryDetectDescribe;
import boofcv.struct.feature.AssociatedIndex;
import boofcv.struct.feature.SurfFeature;
import boofcv.struct.feature.TupleDesc;
import boofcv.struct.geo.AssociatedPair;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.image.MultiSpectral;

/**
 * Utility for registering/stabilising images. Does so using BoofCV to first
 * find pints of interest then transform.
 * 
 * This code is heavily based on the example code given as part of the BoofCV
 * documentation. Example Image Stitching:
 * http://boofcv.org/index.php?title=Example_Image_Stitching
 * 
 * @author Zachary Scott <zscott.dev@gmail.com>, Peter Abeles
 */
public class ImageRegistration {
	private static final Logger logger = LoggerFactory.getLogger(ImageRegistration.class);
	/** Hessian feature detector configuration. */
	public static final float detectThreshold = 1;
	public static final int extractRadius = 2;
	public static final int maxFeaturesPerScale = 200;
	public static final int sampleSize = 1;
	public static final int initialSize = 9;
	public static final int numberOfOctaves = 4;
	public static final int numberScalesPerOctave = 4;

	private static <T extends ImageSingleBand<?>, FD extends TupleDesc<?>> Affine2D_F64 computeTransform(
			T imageA, T imageB, DetectDescribePoint<T, FD> detDesc,
			AssociateDescription<FD> associate,
			ModelMatcher<Affine2D_F64, AssociatedPair> modelMatcher) {
		// get the length of the description
		List<Point2D_F64> pointsA = new ArrayList<>();
		FastQueue<FD> descA = UtilFeature.createQueue(detDesc, 100);
		List<Point2D_F64> pointsB = new ArrayList<>();
		FastQueue<FD> descB = UtilFeature.createQueue(detDesc, 100);
		// extract feature locations and descriptions from each image
		describeImage(imageA, detDesc, pointsA, descA);
		describeImage(imageB, detDesc, pointsB, descB);
		// Associate features between the two images
		associate.setSource(descA);
		associate.setDestination(descB);
		associate.associate();
		// create a list of AssociatedPairs that tell the model matcher how a
		// feature moved
		FastQueue<AssociatedIndex> matches = associate.getMatches();
		List<AssociatedPair> pairs = new ArrayList<>();
		for (int i = 0; i < matches.size(); i++) {
			AssociatedIndex match = matches.get(i);
			Point2D_F64 a = pointsA.get(match.src);
			Point2D_F64 b = pointsB.get(match.dst);
			pairs.add(new AssociatedPair(a, b, false));
		}
		// find the best fit model to describe the change between these images
		if (!modelMatcher.process(pairs)) {
			logger.info("Model matcher failed (i.e. images cannot be registered)");
			return null;
		}
		// return the found image transform
		return modelMatcher.getModelParameters().copy();
	}

	// Detects features inside the two images and computes descriptions at those
	// points.
	private static <T extends ImageSingleBand<?>, FD extends TupleDesc> void describeImage(
			T image, DetectDescribePoint<T, FD> detDesc,
			List<Point2D_F64> points, FastQueue<FD> listDescs) {
		detDesc.detect(image);
		listDescs.reset();
		for (int i = 0; i < detDesc.getNumberOfFeatures(); i++) {
			points.add(detDesc.getLocation(i).copy());
			listDescs.grow().setTo(detDesc.getDescription(i));
		}
	}

	/**
	 * Registers {@code imageB} to the source image {@code imageA}.
	 * 
	 * @param imageA
	 *            The reference image. Must not be {@code null}.
	 * @param imageB
	 *            The image to be registered. Must not be {@code null}.
	 * @return The rendered, registered image.
	 */
	public static ImageSingleBand<?> register(ImageSingleBand<?> imageA,
			ImageSingleBand<?> imageB) {
		ConfigFastHessian config = new ConfigFastHessian(detectThreshold,
				extractRadius, maxFeaturesPerScale, sampleSize, initialSize,
				numberOfOctaves, numberScalesPerOctave);
		// Detect using the standard SURF feature descriptor and describer
		DetectDescribePoint detDesc = FactoryDetectDescribe.surfStable(config,
				null, null, ImageFloat32.class);
		ScoreAssociation<SurfFeature> scorer = FactoryAssociation
				.scoreEuclidean(SurfFeature.class, true);
		AssociateDescription<SurfFeature> associate = FactoryAssociation
				.greedy(scorer, 2, true);
		// fit the images using a homography. This works well for rotations and
		// distant objects.
		ModelManager<Affine2D_F64> manager = new ModelManagerAffine2D_F64();

		GenerateAffine2D modelFitter = new GenerateAffine2D();

		DistanceAffine2D distance = new DistanceAffine2D();

		ModelMatcher<Affine2D_F64, AssociatedPair> modelMatcher = new Ransac<>(
				123, manager, modelFitter, distance, 60, 9);
		Affine2D_F64 H = computeTransform(imageA, imageB, detDesc,
				associate, modelMatcher);
		if (H == null) {
			return (ImageFloat32) imageB;
		}
		// render the
		ImageSingleBand<?> output = render(imageA, (ImageFloat32) imageB, H);
		return output;
	}

	// render the registration transformation
	private static ImageSingleBand<?> render(
			ImageSingleBand<?> imageA, ImageSingleBand<?> colorB,
			Affine2D_F64 fromAtoB) {
		// specify size of output image
		double scale = 1;
		int outputWidth = imageA.getWidth();
		int outputHeight = imageA.getHeight();
		// Where the output images are rendered into
		ImageFloat32 work = new ImageFloat32(outputWidth, outputHeight);
		Affine2D_F64 fromAToWork = new Affine2D_F64();
		Affine2D_F64 fromWorkToA = fromAToWork.invert(null);

		// Used to render the results onto an image
		PixelTransformAffine_F32 model = new PixelTransformAffine_F32();
		ImageDistort<MultiSpectral<ImageFloat32>, MultiSpectral<ImageFloat32>> distort = DistortSupport
				.createDistortMS(ImageFloat32.class, model,
						new ImplBilinearPixel_F32(), true, null);
		
		// Apply transformation to register the image
		Affine2D_F64 fromWorkToB = fromWorkToA.concat(fromAtoB, null);

		model.set(fromWorkToB);
		
		MultiSpectral<ImageFloat32> colorrgb = new MultiSpectral<>(
				ImageFloat32.class, outputWidth, outputHeight, 1);
		colorrgb.bands[0] = (ImageFloat32)colorB;
		MultiSpectral<ImageFloat32> workrgb = new MultiSpectral<>(
				ImageFloat32.class, outputWidth, outputHeight, 1);
		workrgb.bands[0] = (ImageFloat32)work;
		
		distort.apply(colorrgb, workrgb);
		// create rgb version of work - to convert to BufferedImage
		return workrgb.bands[0];
	}
}