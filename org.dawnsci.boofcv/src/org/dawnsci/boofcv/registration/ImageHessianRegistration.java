/*-
 * Copyright (c) 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.boofcv.registration;

import java.util.ArrayList;
import java.util.List;

import org.ddogleg.fitting.modelset.ModelManager;
import org.ddogleg.fitting.modelset.ModelMatcher;
import org.ddogleg.fitting.modelset.lmeds.LeastMedianOfSquares;
import org.ddogleg.fitting.modelset.ransac.Ransac;
import org.ddogleg.struct.FastQueue;
import org.eclipse.dawnsci.analysis.api.image.HessianRegParameters;
import org.eclipse.dawnsci.analysis.api.image.HessianRegParameters.TransformationType;
import org.eclipse.dawnsci.analysis.api.image.DetectionAlgoParameters;
import org.eclipse.dawnsci.analysis.api.image.DetectionAlgoParameters.DetectionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import boofcv.abst.feature.associate.AssociateDescription;
import boofcv.abst.feature.associate.ScoreAssociation;
import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.abst.feature.detect.interest.ConfigFastHessian;
import boofcv.alg.distort.ImageDistort;
import boofcv.alg.distort.PixelTransformAffine_F32;
import boofcv.alg.distort.PixelTransformHomography_F32;
import boofcv.alg.distort.impl.DistortSupport;
import boofcv.alg.distort.impl.ImplImageDistort_F32;
import boofcv.alg.feature.UtilFeature;
import boofcv.alg.interpolate.impl.ImplBilinearPixel_F32;
import boofcv.alg.sfm.robust.DistanceAffine2D;
import boofcv.alg.sfm.robust.DistanceHomographySq;
import boofcv.alg.sfm.robust.GenerateAffine2D;
import boofcv.alg.sfm.robust.GenerateHomographyLinear;
import boofcv.factory.feature.associate.FactoryAssociation;
import boofcv.factory.feature.detdesc.FactoryDetectDescribe;
import boofcv.struct.feature.AssociatedIndex;
import boofcv.struct.feature.SurfFeature;
import boofcv.struct.feature.TupleDesc;
import boofcv.struct.geo.AssociatedPair;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.image.MultiSpectral;
import georegression.fitting.affine.ModelManagerAffine2D_F64;
import georegression.fitting.homography.ModelManagerHomography2D_F64;
import georegression.struct.affine.Affine2D_F64;
import georegression.struct.homography.Homography2D_F64;
import georegression.struct.point.Point2D_F64;

/**
 * This code is heavily based on the example code given as part of the BoofCV
 * documentation. Example Image Stitching:
 * http://boofcv.org/index.php?title=Example_Image_Stitching
 * 
 * @author wqk87977
 * 
 */
public class ImageHessianRegistration {
	private static final Logger logger = LoggerFactory.getLogger(ImageHessianRegistration.class);

	private static <T extends ImageSingleBand<?>, FD extends TupleDesc<?>> Affine2D_F64 computeTransform(
			T imageA, T imageB, DetectDescribePoint<T, FD> detDesc,
			AssociateDescription<FD> associate,
			ModelMatcher<Affine2D_F64, AssociatedPair> modelMatcher) {
		// get the length of the description
		List<Point2D_F64> pointsA = new ArrayList<Point2D_F64>();
		FastQueue<FD> descA = UtilFeature.createQueue(detDesc, 100);
		List<Point2D_F64> pointsB = new ArrayList<Point2D_F64>();
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
			logger.info("Model matcher failed: images cannot be registered");
			return null;
		}
		// return the found image transform
		return modelMatcher.getModelParameters().copy();
	}

	private static <T extends ImageSingleBand<?>, FD extends TupleDesc<?>> Homography2D_F64 computeTransform2(
			T imageA, T imageB, DetectDescribePoint<T, FD> detDesc,
			AssociateDescription<FD> associate,
			ModelMatcher<Homography2D_F64, AssociatedPair> modelMatcher) {
		// get the length of the description
		List<Point2D_F64> pointsA = new ArrayList<Point2D_F64>();
		FastQueue<FD> descA = UtilFeature.createQueue(detDesc, 100);
		List<Point2D_F64> pointsB = new ArrayList<Point2D_F64>();
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
			logger.info("Model matcher failed: images cannot be registered");
			return null;
		}
		// return the found image transform
		return modelMatcher.getModelParameters().copy();
	}

	/**
	 * Detects features inside the two images and computes descriptions at those
	 * points.
	 */
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
	 * Registers imageB to imageA
	 * 
	 * @param imageA
	 *            The reference image
	 * @param imageB
	 *            The image to be registered
	 * @return The rendered, registered image
	 */
	public static ImageSingleBand<?> registerHessian(ImageSingleBand<?> imageA,
			ImageSingleBand<?> imageB) {
		DetectionAlgoParameters detectionParams = new DetectionAlgoParameters(10, 100000, 1, 0, DetectionType.RANSAC);
		HessianRegParameters hessianParams = new HessianRegParameters(1, 2, 200, 1, TransformationType.AFFINE);
		return registerHessian(imageA, imageB, detectionParams, hessianParams);
	}

	/**
	 * Registers imageB to imageA
	 * 
	 * @param imageA
	 *             The reference image
	 * @param imageB
	 *             The image to be registered
	 * @param detectionParams
	 * @param hessianParams
	 * @return The rendered, registered image
	 */
	public static ImageSingleBand<?> registerHessian(ImageSingleBand<?> imageA, ImageSingleBand<?> imageB, DetectionAlgoParameters detectionParams, HessianRegParameters hessianParams) {
		if (detectionParams == null) {
			detectionParams = new DetectionAlgoParameters(10, 100000, 1, 0, DetectionType.RANSAC);
		}
		if (hessianParams == null) {
			hessianParams = new HessianRegParameters(1, 2, 200, 1, TransformationType.AFFINE);
		}
		DetectionType algoType = detectionParams.getType();
		long randSeed = detectionParams.getRandSeed();
		int maxIterations = detectionParams.getMaxIterations();
		double thresholdFit = detectionParams.getThresholdFit();
		int inlierFraction = detectionParams.getInlierFraction();
		float detectThreshold = hessianParams.getDetectThreshold();
		int extractRadius = hessianParams.getExtractRadius();
		int maxFeaturesPerScale = hessianParams.getMaxFeaturesPerScale();
		int initialSampleSize = hessianParams.getInitialSampleSize();
		int initialSize = 9;
		int numberScalesPerOctave = 4;
		int numberOfOctaves = 4;
		TransformationType type = hessianParams.getType();
		ConfigFastHessian config = new ConfigFastHessian(detectThreshold, extractRadius, maxFeaturesPerScale,
				initialSampleSize, initialSize, numberScalesPerOctave, numberOfOctaves);
		// Detect using the standard SURF feature descriptor and describer
		DetectDescribePoint detDesc = FactoryDetectDescribe.surfStable(config,
				null, null, ImageFloat32.class);
		ScoreAssociation<SurfFeature> scorer = FactoryAssociation
				.scoreEuclidean(SurfFeature.class, true);
		AssociateDescription<SurfFeature> associate = FactoryAssociation
				.greedy(scorer, 2, true);
		// fit the images using a homography. This works well for rotations and
		// distant objects.
		if (type == TransformationType.AFFINE) {
			ModelManager<Affine2D_F64> manager = new ModelManagerAffine2D_F64();
			
			GenerateAffine2D modelFitter = new GenerateAffine2D();
			
			DistanceAffine2D distance = new DistanceAffine2D();
			// manage Ransac and Lmeds algo 
			ModelMatcher<Affine2D_F64, AssociatedPair> modelMatcher;
			if (algoType == DetectionType.LMEDS) {
				modelMatcher = new Ransac<Affine2D_F64, AssociatedPair>(
						randSeed, manager, modelFitter, distance, maxIterations, thresholdFit);
			} else {
				modelMatcher = new LeastMedianOfSquares<Affine2D_F64, AssociatedPair>(randSeed, maxIterations, Double.MAX_VALUE, inlierFraction, manager, modelFitter, distance);
			}
				
			Affine2D_F64 H = computeTransform(imageA, imageB, detDesc, associate, modelMatcher);
			
			if (H == null) {
				return (ImageFloat32) imageB;
			}
			ImageSingleBand<?> output = renderHessianRegistration(imageA, (ImageFloat32) imageB, H);

			return output;
		} else if (type == TransformationType.HOMOGRAPHY) {
			ModelManager<Homography2D_F64> manager = new ModelManagerHomography2D_F64();
			
			GenerateHomographyLinear modelFitter = new GenerateHomographyLinear(true);
			
			DistanceHomographySq distance = new DistanceHomographySq();

			// manage Ransac and Lmeds algo 
			ModelMatcher<Homography2D_F64, AssociatedPair> modelMatcher;
			if (algoType == DetectionType.LMEDS) {
				modelMatcher = new Ransac<Homography2D_F64, AssociatedPair>(randSeed, manager, modelFitter, distance,
						maxIterations, thresholdFit);
			} else {
				modelMatcher = new LeastMedianOfSquares<Homography2D_F64, AssociatedPair>(randSeed, maxIterations,
						Double.MAX_VALUE, inlierFraction, manager, modelFitter, distance);
			}
			
			Homography2D_F64 H = computeTransform2(imageA, imageB, detDesc, associate, modelMatcher);
			
			if (H == null) {
				return (ImageFloat32) imageB;
			}
			ImageSingleBand<?> output = renderHessianRegistration(imageA, (ImageFloat32) imageB, H);

			return output;
		}
		return null;
	}

	/**
	 * Renders and displays the stitched together images
	 */
	private static ImageSingleBand<?> renderHessianRegistration(
			ImageSingleBand<?> imageA, ImageSingleBand<?> colorB,
			Affine2D_F64 fromAtoB) {
		// specify size of output image
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
						new ImplBilinearPixel_F32((ImageFloat32) imageA), true, null); //ImplBilinearPixel_F32(),
		
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

	private static ImageSingleBand<?> renderHessianRegistration(
			ImageSingleBand<?> imageA, ImageSingleBand<?> colorB,
			Homography2D_F64 fromAtoB) {
		// specify size of output image
		int outputWidth = imageA.getWidth();
		int outputHeight = imageA.getHeight();
		// Where the output images are rendered into
		ImageFloat32 work = new ImageFloat32(outputWidth, outputHeight);
		Homography2D_F64 fromAToWork = new Homography2D_F64();
		Homography2D_F64 fromWorkToA = fromAToWork.invert(null);

		// Used to render the results onto an image
		PixelTransformHomography_F32 model = new PixelTransformHomography_F32();
		ImplImageDistort_F32<ImageFloat32> distort = new ImplImageDistort_F32<ImageFloat32>(
				new ImplBilinearPixel_F32(), null);
		distort.setModel(model);

		// Apply transformation to register the image
		Homography2D_F64 fromWorkToB = fromWorkToA.concat(fromAtoB, null);

		model.set(fromWorkToB);
		
		MultiSpectral<ImageFloat32> colorrgb = new MultiSpectral<>(
				ImageFloat32.class, outputWidth, outputHeight, 1);
		colorrgb.bands[0] = (ImageFloat32)colorB;
		MultiSpectral<ImageFloat32> workrgb = new MultiSpectral<>(
				ImageFloat32.class, outputWidth, outputHeight, 1);
		workrgb.bands[0] = (ImageFloat32)work;
		
//		distort.apply(colorrgb, workrgb);
		distort.apply((ImageFloat32)colorrgb.bands[0], (ImageFloat32) workrgb.bands[0], 0, 0, outputWidth, outputHeight);
		
		// create rgb version of work - to convert to BufferedImage
		return workrgb.bands[0];
	}
}