/*-
 * Copyright (c) 2011, 2015 Diamond Light Source Ltd.
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
import org.eclipse.dawnsci.analysis.dataset.metadata.PeemMetadataImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import boofcv.abst.feature.associate.AssociateDescription;
import boofcv.abst.feature.associate.ScoreAssociation;
import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.abst.feature.detect.interest.ConfigFastHessian;
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

	private static final Logger logger = LoggerFactory.getLogger(BoofCVImageStitchingImpl.class);

	public BoofCVImageStitchingImpl() {
		// Important do nothing here, OSGI may start the service more than once.
	}

	@Override
	public IDataset stitch(List<IDataset> input) {
		return stitch(input, 1, 6, 49);
	}

	@Override
	public IDataset stitch(List<IDataset> input, int rows, int columns) {
		List<double[]> translations = new ArrayList<double[]>();
		translations.add(new double[] {25, 25});
		return stitch(input, rows, columns, 50, translations, false);
	}

	@Override
	public IDataset stitch(List<IDataset> input, int rows, int columns, double fieldOfView) {
		List<double[]> translations = new ArrayList<double[]>();
		translations.add(new double[] {25, 25});
		return stitch(input, rows, columns, fieldOfView, translations, true);
	}

	@Override
	public IDataset stitch(List<IDataset> input, int rows, int columns, double fieldOfView, List<double[]> translations, boolean hasFeatureAssociation) {
		IDataset[][] images = ImagePreprocessing.listToArray(input, rows, columns);
		List<List<ImageSingleBand<?>>> inputImages = new ArrayList<List<ImageSingleBand<?>>>();
		for (int i = 0; i < images.length; i++) {
			inputImages.add(new ArrayList<ImageSingleBand<?>>());
			for (int j = 0; j < images[0].length; j++) {
				ImageFloat32 image = ConvertIDataset.convertFrom(images[i][j], ImageFloat32.class, 1);
				// set metadata
				PeemMetadata md = null;
				try {
					md = (PeemMetadata)images[i][j].getMetadata(PeemMetadata.class);
				} catch (Exception e) {
					logger.error("Error getting metadata:" + e.getMessage());
					e.printStackTrace();
				}
				// set default values if no metadata (scaling = width/fieldofview? 512/50)
//				if (md == null) {
//					md = new PeemMetadataImpl(translations.get(j), image.width / fieldOfView, fieldOfView);
//				}
				inputImages.get(i).add(image);
			}
		}
		Class<ImageFloat32> imageType = ImageFloat32.class;
		DetectDescribePoint<?, ?> detDesc = FactoryDetectDescribe.surfStable(
				new ConfigFastHessian(1, 2, 200, 1, 9, 4, 4), null,null, imageType);
		ScoreAssociation<?> scorer = FactoryAssociation.defaultScore(detDesc.getDescriptionType());
		AssociateDescription<?> associate = FactoryAssociation.greedy(scorer, Double.MAX_VALUE, true);

		FullStitchingObject stitchObj = new FullStitchingObject(detDesc, associate, imageType);
		stitchObj.setConversion(inputImages.get(0).get(0), fieldOfView);
		if (hasFeatureAssociation) {
			stitchObj.translationArray(inputImages, translations);
		} else {
			stitchObj.theoreticalTranslation(columns, rows, translations);
		}
		ImageSingleBand<?> result = stitchObj.stitch(inputImages);
		return ConvertIDataset.convertTo(result, true);
	}
}
