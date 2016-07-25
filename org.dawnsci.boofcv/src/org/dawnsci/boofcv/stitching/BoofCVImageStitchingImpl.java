/*-
 * Copyright (c) 2011-2016 Diamond Light Source Ltd.
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
import org.eclipse.dawnsci.analysis.api.image.IImageStitchingProcess;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
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
	public IDataset stitch(List<IDataset> input, IMonitor monitor) throws Exception {
		return stitch(input, 1, 6, 49, monitor);
	}

	@Override
	public IDataset stitch(ILazyDataset input, IMonitor monitor) throws Exception {
		return stitch(input, 1, 6, 49, monitor);
	}

	@Override
	public IDataset stitch(List<IDataset> input, int rows, int columns, IMonitor monitor) throws Exception {
		List<double[]> translations = new ArrayList<double[]>();
		translations.add(new double[] {25, 25});
		return stitch(input, rows, columns, 50, translations, false, monitor);
	}

	@Override
	public IDataset stitch(ILazyDataset input, int rows, int columns, IMonitor monitor) throws Exception {
		double[][][] translations = makeTranslationArray(rows, columns, 25);
		return stitch(input, rows, columns, 50, translations, true, input.getShape(), monitor);
	}

	@Override
	public IDataset stitch(List<IDataset> input, int rows, int columns, double fieldOfView, IMonitor monitor) throws Exception {
		List<double[]> translations = new ArrayList<double[]>();
		translations.add(new double[] {25, 25});
		return stitch(input, rows, columns, fieldOfView, translations, true, monitor);
	}

	@Override
	public IDataset stitch(List<IDataset> input, int rows, int columns, double fieldOfView, List<double[]> translations, boolean hasFeatureAssociation, IMonitor monitor) throws Exception {
		int[] shape = input.get(0).getShape();
		return stitch(input, rows, columns, fieldOfView, translations, hasFeatureAssociation, shape, monitor);
	}

	@Override
	public IDataset stitch(ILazyDataset input, int rows, int columns, double fieldOfView, IMonitor monitor)
			throws Exception {
		double[][][] translations = makeTranslationArray(rows, columns, 25);
		return stitch(input, rows, columns, 50, translations, true, input.getShape(), monitor);
	}

	@Override
	public IDataset stitch(List<IDataset> input, int rows, int columns,
			double fieldOfView, List<double[]> translations,
			boolean hasFeatureAssociation, int[] originalShape, IMonitor monitor) throws Exception {
		throw new Exception("Use the stitch method with the array parameter instead");
	}

	@Override
	public IDataset stitch(ILazyDataset input, int rows, int columns,
			double fieldOfView, List<double[]> translations,
			boolean hasFeatureAssociation, int[] originalShape, IMonitor monitor) throws Exception {
		throw new Exception("Use the stitch method with the array parameter instead");
	}

	@Override
	public IDataset stitch(ILazyDataset input, int rows, int columns,
			double fieldOfView, double[][][] translations,
			boolean hasFeatureAssociation, int[] originalShape, IMonitor monitor) throws Exception {
		int[] shape = input.getShape();
		if (originalShape ==null)
			originalShape = input.getShape();
		if (shape.length != 3)
			throw new Exception("This stitching routine works only with 3D dataset");

		Class<ImageFloat32> imageType = ImageFloat32.class;
		DetectDescribePoint<?, ?> detDesc = FactoryDetectDescribe.surfStable(
				new ConfigFastHessian(1, 2, 200, 1, 9, 4, 4), null,null, imageType);
		ScoreAssociation<?> scorer = FactoryAssociation.defaultScore(detDesc.getDescriptionType());
		AssociateDescription<?> associate = FactoryAssociation.greedy(scorer, Double.MAX_VALUE, true);

		FullStitchingObject<?, ?> stitchObj = new FullStitchingObject(detDesc, associate, imageType);
		stitchObj.setConversion(originalShape, fieldOfView);
		if (hasFeatureAssociation) {
			stitchObj.translationArray(input, translations, rows, columns, monitor);
		} else {
			stitchObj.theoreticalTranslation(rows, columns, translations, monitor);
		}
		ImageSingleBand<?> result = stitchObj.stitch(input, rows, columns, monitor);
		return ConvertIDataset.convertTo(result, true);
	}

	@Override
	public IDataset stitch(IDataset imageA, IDataset imageB, double[] translation) throws Exception {
		int[] shapeA = imageA.getShape();
		int[] shapeB = imageB.getShape();
		if (shapeA.length != 2 || shapeB.length != 2)
			throw new Exception("This stitching routine works only with 2D datasets");

		ImageSingleBand<?> image1 = ConvertIDataset.convertFrom(imageA, ImageFloat32.class, 1);
		ImageSingleBand<?> image2 = ConvertIDataset.convertFrom(imageB, ImageFloat32.class, 1);

		StitchingObject<?> stitcher = new StitchingObject<>(translation);
		ImageSingleBand<?> result = stitcher.stitch(image1, image2, new double[] {0, 0});
		return ConvertIDataset.convertTo(result, true);
	}

	private double[][][] makeTranslationArray(int rows, int columns, double value) {
		double[][][] array = new double[columns][rows][2];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				array[j][i][0] = value;
				array[j][i][1] = value;
			}
		}
		return array;
	}
}
