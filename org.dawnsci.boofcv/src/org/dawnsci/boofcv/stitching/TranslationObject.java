/*-
 * Copyright (c) 2014 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.boofcv.stitching;

import georegression.fitting.MotionTransformPoint;
import georegression.fitting.homography.ModelManagerHomography2D_F64;
import georegression.fitting.se.ModelManagerSe2_F64;
import georegression.fitting.se.MotionSe2PointSVD_F64;
import georegression.struct.homography.Homography2D_F64;
import georegression.struct.point.Point2D_F64;
import georegression.struct.se.Se2_F64;

import java.util.ArrayList;
import java.util.List;

import org.ddogleg.fitting.modelset.ModelManager;
import org.ddogleg.fitting.modelset.ModelMatcher;
import org.ddogleg.fitting.modelset.ransac.Ransac;
import org.ddogleg.struct.FastQueue;

import boofcv.abst.feature.associate.AssociateDescription;
import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.alg.feature.UtilFeature;
import boofcv.alg.sfm.robust.DistanceHomographySq;
import boofcv.alg.sfm.robust.DistanceSe2Sq;
import boofcv.alg.sfm.robust.GenerateHomographyLinear;
import boofcv.alg.sfm.robust.GenerateSe2_AssociatedPair;
import boofcv.struct.feature.AssociatedIndex;
import boofcv.struct.feature.TupleDesc;
import boofcv.struct.geo.AssociatedPair;
import boofcv.struct.image.ImageSingleBand;

/**
 * TranslationObject is a class for calculating the translation between two
 * overlapping images. It is for images related by a simple transformation i.e
 * just x and y translation. It works by detecting features in each image,
 * identifying corresponding features and calculating the average distance
 * between two corresponding features.
 * 
 * @author Alex Andrassy, Baha El-Kassaby
 * 
 * @param <T>
 *            The type of images being compared
 * @param <TD>
 *            The type of feature descriptor that is used
 */
public class TranslationObject<T extends ImageSingleBand<?>, TD extends TupleDesc> {
	// algorithm used to detect and describe interest points
	private DetectDescribePoint<T, TD> detDesc;
	// associates descriptions together by minimizing an error metric
	private AssociateDescription<TD> associate;

	private Class<T> imageType;

	// locations of detected features
	private List<Point2D_F64> pointsA;
	private List<Point2D_F64> pointsB;

	// corresponding features between the two images
	private FastQueue<AssociatedIndex> matches;

	// locations of corresponding features (associated pairs)
	private List<AssociatedPair> pairs;

	// factor to convert between microns and pixels
	private double micronsToPixels;

	/**
	 * Creates the TranslationObject
	 * 
	 * @param detDesc
	 *            algorithm used to detect and describe interest points
	 * @param associate
	 *            associates descriptions together by minimizing an error metric
	 * @param imageType
	 *            The type of images being compared
	 */
	public TranslationObject(DetectDescribePoint<T, TD> detDesc,
			AssociateDescription<TD> associate, Class<T> imageType) {
		this.detDesc = detDesc;
		this.associate = associate;
		this.setImageType(imageType);
	}
	
	/**
	 * Sets the factor to convert between microns and pixels
	 * 
	 * @param shape
	 *            shape of image (width)
	 * @param fieldOfView
	 *            The field of view of the uncropped image, from which we get
	 *            the corresponding number of microns
	 * @throws Exception 
	 */
	public void setConversion(int[] shape, double fieldOfView) throws Exception {
		// calculates the number of pixels per micron
		int length = 0;
		if (shape.length == 2)
			length = shape[0];
		else if (shape.length == 3)
			length = shape[1];
		else
			throw new Exception(
					"Error in setting micron/pixel conversion. Shape is not supported: allowed shape are 2 and 3, given was "
							+ shape.length);
		micronsToPixels = length / (fieldOfView * Math.cos(Math.PI / 4));
	}

	public double getConversion() {
		return micronsToPixels;
	}

	/**
	 * Detects features in an image and computes descriptions for each point.
	 * 
	 * @param image
	 *            To have features detected
	 * @param points
	 *            'Empty' list to be 'filled' with points corresponding to
	 *            features
	 * @param listDescs
	 *            'Empty list to be 'filled' with descriptions of features
	 */
	public void describeImage(T image, List<Point2D_F64> points,
			FastQueue<TD> listDescs) {
		// obtain points and descriptions of features
		detDesc.detect(image);

		// fills a list with points and a list with descriptors
		for (int i = 0; i < detDesc.getNumberOfFeatures(); i++) {

			points.add(detDesc.getLocation(i).copy());
			listDescs.grow().setTo(detDesc.getDescription(i));
		}
	}

	/**
	 * Detects features in only a specified region of an image and computes
	 * descriptions for each point. The region is a rectangle, defined by the
	 * top-left and bottom-right corners, it can be specified to be only the
	 * overlapping region of the image.
	 * 
	 * @param image
	 *            To have features detected
	 * @param points
	 *            'Empty' list to be 'filled' with points corresponding to
	 *            features
	 * @param listDescs
	 *            'Empty list to be 'filled' with descriptions of features
	 * @param x1
	 *            The x coordinate in pixels of the top-left corner of the
	 *            region
	 * @param y1
	 *            The y coordinate in pixels of the top-left corner of the
	 *            region
	 * @param x2
	 *            The x coordinate in pixels of the bottom-right corner of the
	 *            region
	 * @param y2
	 *            The y coordinate in pixels of the bottom-right corner of the
	 *            region
	 */
	public void describeImage(T image, List<Point2D_F64> points, FastQueue<TD> listDescs, double x1, double y1, double x2, double y2) {
		// obtain points and descriptions of features
		detDesc.detect(image);

		// fills a list with points and a list with descriptors
		for (int i = 0; i < detDesc.getNumberOfFeatures(); i++ ) {
			// features are only added to the lists if they lie within the specified region
			if (detDesc.getLocation(i).x > x1 && detDesc.getLocation(i).x < x2 &&
					detDesc.getLocation(i).y > y1 && detDesc.getLocation(i).y < y2) {
				points.add( detDesc.getLocation(i).copy() );
				listDescs.grow().setTo(detDesc.getDescription(i));
			}
		}
	}

	/**
	 * Identifies corresponding features between two images. Features are looked
	 * for across all of both images.
	 * 
	 * @param imageA
	 *            The first image to be compared
	 * @param imageB
	 *            The second image to be compared
	 */
	public void findMatches(T imageA, T imageB) {

		// stores the location of detected features
		pointsA = new ArrayList<Point2D_F64>();
		pointsB = new ArrayList<Point2D_F64>();

		// stores the description of detected features
		FastQueue<TD> descA = UtilFeature.createQueue(detDesc, 100);
		FastQueue<TD> descB = UtilFeature.createQueue(detDesc, 100);

		// describe each image using features
		describeImage(imageA, pointsA, descA);
		describeImage(imageB, pointsB, descB);

		// associate features between the two images
		associate.setSource(descA);
		associate.setDestination(descB);
		associate.associate();

		// store all corresponding features
		matches = associate.getMatches();

		// stores locations of selected corresponding features
		pairs = new ArrayList<AssociatedPair>();
	}

	/**
	 * Identifies corresponding features between two images. Features are only
	 * looked for within the overlapping region of the two images. The
	 * overlapping region is found using the amount by which the second image is
	 * expected to be translated relative to the first.
	 * 
	 * @param imageA
	 *            The first image to be compared
	 * @param imageB
	 *            The second image to be compared
	 * @param xtrans
	 *            The expected translation in microns in the x direction
	 *            relating the two images
	 * @param ytrans
	 *            The expected translation in microns in the y direction
	 *            relating the two images
	 */
	public void findMatches(T imageA, T imageB, double xtrans, double ytrans) {

		// convert the translations from microns into pixels
		xtrans = xtrans * micronsToPixels;
		ytrans = ytrans * micronsToPixels;

		// the coordinates of the top-left and bottom-right corners of the
		// overlapping region for both images
		double ax1, ay1, ax2, ay2, bx1, by1, bx2, by2;

		// sets the x coordinates if the second image is to the left of the
		// first image
		if (xtrans < 0) {
			ax1 = 0;
			ax2 = imageA.getWidth() + xtrans;
			bx1 = -xtrans;
			bx2 = imageB.getWidth();
		}

		// sets the x coordinates if the second image is to the right of the
		// first image
		else {
			ax1 = xtrans;
			ax2 = imageA.getWidth();
			bx1 = 0;
			bx2 = imageB.getWidth() - xtrans;
		}

		// sets the y coordinates if the second image is above the first image
		if (ytrans < 0) {
			ay1 = 0;
			ay2 = imageA.getHeight() + ytrans;
			by1 = -ytrans;
			by2 = imageB.getHeight();
		}

		// sets the y coordinates if the second image is below the first image
		else {
			ay1 = ytrans;	
			ay2 = imageA.getHeight();
			by1 = 0;
			by2 = imageB.getHeight() - ytrans;
		}

		// stores the location of detected features
		pointsA = new ArrayList<Point2D_F64>();
		pointsB = new ArrayList<Point2D_F64>();

		// stores the description of detected features
		FastQueue<TD> descA = UtilFeature.createQueue(detDesc,100);
		FastQueue<TD> descB = UtilFeature.createQueue(detDesc,100);

		// describe the overlapping region of each image using features
		describeImage(imageA,pointsA,descA, ax1, ay1, ax2, ay2);
		describeImage(imageB,pointsB,descB, bx1, by1, bx2, by2);

		// associate features between the two images
		associate.setSource(descA);
		associate.setDestination(descB);
		associate.associate();

		// store all corresponding features
		matches = associate.getMatches();
		
		// stores locations of selected corresponding features
		pairs = new ArrayList<AssociatedPair>();
	}

	/**
	 * Stores the locations of all corresponding features between two images.
	 * 
	 * @param imageA
	 *            The first image to be compared
	 * @param imageB
	 *            The second image to be compared
	 */
	public void associate(T imageA, T imageB) {
		// find corresponding features
		findMatches(imageA, imageB);
		// store the location of all corresponding features
		for( int i = 0; i < matches.size(); i++ ) {
			AssociatedIndex match = matches.get(i);
			Point2D_F64 a = pointsA.get(match.src);
			Point2D_F64 b = pointsB.get(match.dst);
			pairs.add( new AssociatedPair(a,b,false));
		}
	}

	/**
	 * Stores the locations of corresponding features within the the overlapping
	 * region between two images. If the translation relating corresponding
	 * features is not within an expected range, the features have been found to
	 * correspond incorrectly and are rejected.
	 * 
	 * @param imageA
	 *            The first image to be compared
	 * @param imageB
	 *            The second image to be compared
	 * @param xtrans
	 *            The expected translation in microns in the x direction
	 *            relating the two images
	 * @param ytrans
	 *            The expected translation in microns in the y direction
	 *            relating the two images
	 */
	public void associate(T imageA, T imageB, double xtrans, double ytrans) {
		// find corresponding features within the overlapping region
		findMatches(imageA, imageB, xtrans, ytrans);

		// convert the translations from microns into pixels
		xtrans = xtrans * micronsToPixels;
		ytrans = ytrans*micronsToPixels;

		// error in expected translations in pixels
		double error = 45;

		// store the location of corresponding features within the overlapping region
		for( int i = 0; i < matches.size(); i++ ) {
			AssociatedIndex match = matches.get(i);
			Point2D_F64 a = pointsA.get(match.src);
			Point2D_F64 b = pointsB.get(match.dst);

			// only corresponding features that give a translation within the
			// expected range are stored
			if (b.x - a.x > -xtrans - error && b.x - a.x < -xtrans + error
					&& b.y - a.y > -ytrans - error
					&& b.y - a.y < -ytrans + error) {
				pairs.add(new AssociatedPair(a, b, false));
			}
		}
	}

	/**
	 * Calculates the average translation between corresponding features, thus
	 * giving the translation between two images.
	 * 
	 * @return The x and y translation between two images
	 */
	public double[] translation() {

		double x1, y1, x2, y2, xtrans, ytrans;
		Point2D_F64 p1, p2;

		// used to calculate the mean
		double totalX = 0;
		double totalY = 0;
		int numberAssociations = pairs.size();
		double[] mean = new double[2];

		for (int i = 0; i < pairs.size(); i++) {

			// get the position of a feature in imageA
			p1 = pairs.get(i).getP1();
			x1 = p1.getX();
			y1 = p1.getY();

			// get the position of the corresponding feature in imageB
			p2 = pairs.get(i).getP2();
			x2 = p2.getX();
			y2 = p2.getY();

			// calculate the x and y translation
			xtrans = x2 - x1;
			ytrans = y2 - y1;

			// add translation to the total
			totalX += xtrans;
			totalY += ytrans;
		}

		// calculate the mean
		mean[0] = totalX / numberAssociations;
		mean[1] = totalY / numberAssociations;

		return mean;
	}

	/**
	 * Finds the homography that best models the transform between selected
	 * corresponding features, thus giving the transform between two images. The
	 * homography may describe any transformation, elements a13 and a23 describe
	 * the x and y translations respectively.
	 * 
	 * @return The homography that transforms between two images
	 */
	public Homography2D_F64 homographyTransform() {
		
		// generates an object to find a homography
		ModelManager<Homography2D_F64> manager = new ModelManagerHomography2D_F64();
		GenerateHomographyLinear modelFitter = new GenerateHomographyLinear(true);
		DistanceHomographySq distance = new DistanceHomographySq();

		// generates an object to find the best fit homography 
		ModelMatcher<Homography2D_F64,AssociatedPair> modelMatcher =
				new Ransac<Homography2D_F64,AssociatedPair>(123,manager,modelFitter,distance,60,9);

		// finds the best fit homography to describe the transform between corresponding points and hence images
		modelMatcher.process(pairs);
		
		// return the homography representing the transform between the images
		return modelMatcher.getModelParameters();
		
	}

	/**
	 * Finds the specialEuclidian that best models the transform between
	 * selected corresponding features, thus giving the transform between two
	 * images. The specialEuclidian describes translations and rotations, but
	 * the x and y translations can be obtained from the specialEuclidean.
	 * 
	 * @return The specialEuclidian that transforms between the two images
	 */
	public Se2_F64 specialTransform() {
		// generates an object to find a specialEuclidian
		ModelManager<Se2_F64> manager = new ModelManagerSe2_F64();
		MotionTransformPoint<Se2_F64, Point2D_F64> alg = new MotionSe2PointSVD_F64();
		GenerateSe2_AssociatedPair modelFitter = new GenerateSe2_AssociatedPair(alg);
		DistanceSe2Sq distance = new DistanceSe2Sq();

		// generates an object to find the best fit specialEuclidean 
		ModelMatcher<Se2_F64,AssociatedPair> modelMatcher =
				new Ransac<Se2_F64,AssociatedPair>(123,manager,modelFitter,distance, 60, 9);

		// finds the best fit specialEuclidean to describe the transform between
		// corresponding points and hence images
		modelMatcher.process(pairs);

		// return the specialEuclidian representing the transform between the
		// images
		return modelMatcher.getModelParameters();
	}

	public Class<T> getImageType() {
		return imageType;
	}

	public void setImageType(Class<T> imageType) {
		this.imageType = imageType;
	}
}
