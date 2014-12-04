/*-
 * Copyright (c) 2014 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.boofcv.stitching;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.boofcv.converter.ConvertIDataset;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.image.IImageStitchingProcess;
import org.eclipse.dawnsci.analysis.api.metadata.PeemMetadata;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.metadata.PeemMetadataImpl;
import org.eclipse.dawnsci.analysis.dataset.roi.EllipticalROI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import boofcv.abst.feature.associate.AssociateDescription;
import boofcv.abst.feature.associate.ScoreAssociation;
import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.abst.feature.detect.interest.ConfigFastHessian;
import boofcv.alg.distort.DistortImageOps;
import boofcv.alg.interpolate.TypeInterpolate;
import boofcv.factory.feature.associate.FactoryAssociation;
import boofcv.factory.feature.detdesc.FactoryDetectDescribe;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSingleBand;

/**
 * Implementation of IImageStitchingProcess<br>
 * 
 * This class is internal and not supposed to be used out of this bundle.
 * 
 * @authors Alex Andrassy, Baha El-Kassaby
 * 
 */
public class BoofCVImageStitchingImpl implements IImageStitchingProcess {

	static {
		System.out.println("Starting BoofCV image Stitching service.");
	}

	/**
	 * Region of Interest used for cropping
	 */
	private IROI roi;
	private static final Logger logger = LoggerFactory.getLogger(BoofCVImageStitchingImpl.class);

	public BoofCVImageStitchingImpl() {
		// Important do nothing here, OSGI may start the service more than once.
	}

	@Override
	public IDataset stitch(List<IDataset> input) {
		return stitch(input, 1, 6, 49);
	}

	@Override
	public IDataset stitch(List<IDataset> input, int rows, int columns, double angle) {
		List<double[]> translations = new ArrayList<double[]>(1);
		translations.add(new double[] {25, 25});
		return stitch(input, rows, columns, angle, 50, translations, false, true);
	}

	@Override
	public IDataset stitch(List<IDataset> input, int rows, int columns, double angle, double fieldOfView, IROI roi) {
		this.roi = roi;
		List<double[]> translations = new ArrayList<double[]>(1);
		translations.add(new double[] {25, 25});
		return stitch(input, rows, columns, angle, fieldOfView, translations, true, true);
	}

	@Override
	public IDataset stitch(List<IDataset> input, int rows, int columns, double angle, double fieldOfView, List<double[]> translations, IROI roi, boolean hasFeatureAssociation) {
		this.roi = roi;
		return stitch(input, rows, columns, angle, fieldOfView, translations, true, hasFeatureAssociation);
	}

	public IDataset stitch(List<IDataset> input, int rows, int columns, double angle, double fieldOfView, List<double[]> translations, boolean hasCropping, boolean hasFeatureAssociation) {

		IDataset[][] images = ImagePreprocessing.listToArray(input, rows, columns);
		double[][][] trans = ImagePreprocessing.transToArraysInMicrons(translations, rows, columns);
		List<List<ImageAndMetadata>> inputImages = new ArrayList<List<ImageAndMetadata>>();

		for (int i = 0; i < images.length; i++) {
			inputImages.add(new ArrayList<ImageAndMetadata>());
			for (int j = 0; j < images[0].length; j++) {
				ImageFloat32 image = ConvertIDataset.convertFrom(images[i][j], ImageFloat32.class, 1);
				int width = 0, height = 0;
				if (hasCropping) {
					width = image.width;
					height = image.height;
				} else {
					// calculate resulting bounding box
					width = (int) (image.width
							* Math.cos(Math.toRadians(angle)) + image.height
							* Math.sin(Math.toRadians(angle)));
					height = (int) (image.height
							* Math.cos(Math.toRadians(angle)) + image.width
							* Math.sin(Math.toRadians(angle)));
				}
				ImageFloat32 rotated = new ImageFloat32(height, width);

				DistortImageOps.rotate(image, rotated, TypeInterpolate.BILINEAR, (float)Math.toRadians(angle));
				// set metadata
				PeemMetadata md = null;
				try {
					md = (PeemMetadata)images[i][j].getMetadata(PeemMetadata.class);
				} catch (Exception e) {
					logger.error("Error getting metadata:" + e.getMessage());
					e.printStackTrace();
				}
				// set default values if no metadata (scaling = width/fieldofview? 512/50)
				if (md == null) {
					md = new PeemMetadataImpl(trans[i][j], image.width / fieldOfView, fieldOfView, angle);
				}
				ImageAndMetadata imageAndMd = null;
				if (hasCropping && roi instanceof EllipticalROI) {
					ImageFloat32 cropped = ImagePreprocessing.maxRectangleFromEllipticalImage(rotated, (EllipticalROI)roi);
					imageAndMd = new ImageAndMetadata(cropped, md);
				} else {
					imageAndMd = new ImageAndMetadata(rotated, md);
				}
				inputImages.get(i).add(imageAndMd);
			}
		}
		Class<ImageFloat32> imageType = ImageFloat32.class;
		DetectDescribePoint detDesc = FactoryDetectDescribe.surfStable(
				new ConfigFastHessian(1, 2, 200, 1, 9, 4, 4), null,null, imageType);
		ScoreAssociation scorer = FactoryAssociation.defaultScore(detDesc.getDescriptionType());
		AssociateDescription associate = FactoryAssociation.greedy(scorer, Double.MAX_VALUE, true);

		FullStitchingObject stitchObj = new FullStitchingObject(detDesc, associate, imageType);
		stitchObj.setConversion(inputImages.get(0).get(0).getImage(), fieldOfView);
		if (hasFeatureAssociation) {
			stitchObj.translationArray(inputImages);
		} else {
			stitchObj.theoreticalTranslation(inputImages);
		}
		ImageSingleBand<?> result = stitchObj.stitch(inputImages);
		return ConvertIDataset.convertTo(result, true);
	}
}
