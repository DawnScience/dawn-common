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

import boofcv.abst.feature.associate.AssociateDescription;
import boofcv.abst.feature.associate.ScoreAssociation;
import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.abst.feature.detect.interest.ConfigFastHessian;
import boofcv.factory.feature.associate.FactoryAssociation;
import boofcv.factory.feature.detdesc.FactoryDetectDescribe;
import boofcv.struct.image.ImageFloat32;

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

	public BoofCVImageStitchingImpl() {
		// Important do nothing here, OSGI may start the service more than once.
	}

	@Override
	public IDataset stitch(List<IDataset> input) {
		return stitch(input, 6);
	}

	@Override
	public IDataset stitch(List<IDataset> input, int matrixSize) {

		IDataset[][] images = ImagePreprocessing.ListToArray(input, matrixSize);
		List<List<ImageFloat32>> inputImages = new ArrayList<List<ImageFloat32>>();

		for (int i = 0; i < images.length; i++) {
			inputImages.add(new ArrayList<ImageFloat32>());
			for (int j = 0; j < images[0].length; j++) {
				images[i][j] = ImagePreprocessing.rotateAndCrop(images[i][j], -49);
				ImageFloat32 image = ConvertIDataset.convertFrom(images[i][j], ImageFloat32.class, 1);
//				IPeemMetadata md = (IPeemMetadata)images[i][j].getMetadata(IPeemMetadata.class);
//				ImageAndMetadata imageAndMd = new ImageAndMetadata(image, md);
				inputImages.get(i).add(image);
			}
		}
		Class<ImageFloat32> imageType = ImageFloat32.class;
		DetectDescribePoint detDesc = FactoryDetectDescribe.surfStable(
				new ConfigFastHessian(1, 2, 200, 1, 9, 4, 4), null,null, imageType);
		ScoreAssociation scorer = FactoryAssociation.defaultScore(detDesc.getDescriptionType());
		AssociateDescription associate = FactoryAssociation.greedy(scorer, Double.MAX_VALUE, true);

		//TODO to be retrieved from metadata
		double xtrans = 25;
		double ytrans = 25;

		FullStitchingObject stitchObj = new FullStitchingObject(detDesc, associate, imageType);

		stitchObj.translationArray(inputImages, xtrans, ytrans);
		ImageFloat32 result = stitchObj.stitchFloat32(inputImages);
		return ConvertIDataset.convertTo(result, true);
	}
}
