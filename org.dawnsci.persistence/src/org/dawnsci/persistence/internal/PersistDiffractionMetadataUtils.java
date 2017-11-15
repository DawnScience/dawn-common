/*
 * Copyright (c) 2012, 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.persistence.internal;

import javax.vecmath.Matrix3d;

import org.eclipse.dawnsci.analysis.api.diffraction.DetectorProperties;
import org.eclipse.dawnsci.analysis.api.diffraction.IPowderCalibrationInfo;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.tree.impl.AttributeImpl;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;

class PersistDiffractionMetadataUtils {
	
	public static final String SAMPLEGROUPNAME = "/entry/calibration_sample";

	public static void writeDetectorProperties(NexusFile file, String parent, DetectorProperties detprop) throws Exception {
		int intType    = Dataset.INT32;
		int doubleType = Dataset.FLOAT64;

		GroupNode parentGroup = file.getGroup(parent, true);
		Dataset xpixelNumber = DatasetFactory.createFromObject(intType, new int[]{detprop.getPx()}, new int[] {1});
		xpixelNumber.setName("x_pixel_number");
		DataNode xPixelNumberNode = file.createData(parentGroup, xpixelNumber);
		file.addAttribute(xPixelNumberNode, new AttributeImpl("units", "pixels"));
		
		Dataset ypixelNumber = DatasetFactory.createFromObject(intType, new int[]{detprop.getPy()}, new int[] {1});
		ypixelNumber.setName("y_pixel_number");
		DataNode yPixelNumberNode = file.createData(parentGroup, ypixelNumber);
		file.addAttribute(xPixelNumberNode, new AttributeImpl("units", "pixels"));

		Dataset xpixelSize = DatasetFactory.createFromObject(doubleType, new double[]{detprop.getHPxSize()}, new int[] {1});
		xpixelSize.setName("x_pixel_size");
		DataNode xPixelSizeNode = file.createData(parentGroup, xpixelSize);
		file.addAttribute(xPixelSizeNode, new AttributeImpl("units", "mm"));

		Dataset ypixelSize = DatasetFactory.createFromObject(doubleType, new double[]{detprop.getVPxSize()}, new int[] {1});
		ypixelSize.setName("y_pixel_size");
		DataNode yPixelSizeNode = file.createData(parentGroup, ypixelSize);
		file.addAttribute(yPixelSizeNode, new AttributeImpl("units", "mm"));

		double dist = detprop.getBeamCentreDistance();
		double[] bc = detprop.getBeamCentreCoords();
		
		Dataset bcXPix = DatasetFactory.createFromObject(doubleType, new double[]{bc[0]}, new int[] {1});
		bcXPix.setName("beam_center_x");
		DataNode bcXPixNode = file.createData(parentGroup, bcXPix);
		file.addAttribute(bcXPixNode, new AttributeImpl("units", "pixels"));

		Dataset bcYPix = DatasetFactory.createFromObject(doubleType, new double[]{bc[1]}, new int[] {1});
		bcYPix.setName("beam_center_y");
		DataNode bcYPixNode = file.createData(parentGroup, bcYPix);
		file.addAttribute(bcYPixNode, new AttributeImpl("units", "pixels"));
		
		Dataset distance = DatasetFactory.createFromObject(doubleType, new double[]{dist}, new int[] {1});
		distance.setName("distance");
		DataNode distanceNode = file.createData(parentGroup, distance);
		file.addAttribute(distanceNode, new AttributeImpl("units", "mm"));

		Matrix3d or = detprop.getOrientation();
		double[] orientation = new double[] {or.m00 ,or.m01, or.m02,
				or.m10, or.m11, or.m12,
				or.m20, or.m21, or.m22};

		Dataset orientationData = DatasetFactory.createFromObject(doubleType, orientation, new int[] {9});
		orientationData.setName("detector_orientation");
		file.createData(parentGroup, orientationData);
	}
	
	public static void writeWavelengthMono(NexusFile file, String instrument, double wavelength) throws Exception {
		GroupNode instrumentGroup = file.getGroup(instrument, true);
		GroupNode monochromatorGroup = file.getGroup(instrumentGroup, "monochromator", "NXmonochromator", true);
		
		Dataset energy = DatasetFactory.createFromObject(Dataset.FLOAT64, new double[]{wavelength}, new int[] {1});
		energy.setName("wavelength");
		DataNode energyNode = file.createData(monochromatorGroup, energy);
		file.addAttribute(energyNode, new AttributeImpl("units", "Angstrom"));
	}
	
	public static void writeWavelengthSample(NexusFile file, IPowderCalibrationInfo info, double wavelength) throws Exception {
		
		GroupNode parentGroup = file.getGroup("/entry", true);
		GroupNode calibrationSampleGroup = file.getGroup(parentGroup, "calibration_sample", "NXsample", true);
		
		Dataset nameData = DatasetFactory.createFromObject(info.getCalibrantName());
		nameData.setName("name");
		file.createData(calibrationSampleGroup, nameData);
		Dataset typeData = DatasetFactory.createFromObject("calibration sample");
		typeData.setName("type");
		file.createData(calibrationSampleGroup, typeData);
		//write in data
		Dataset datasetd = DatasetFactory.createFromObject(info.getCalibrantDSpaceValues());
		datasetd.setName("d_space");
		file.createData(calibrationSampleGroup, datasetd);
		
		GroupNode beamNode = file.getGroup(calibrationSampleGroup, "beam", "NXbeam", true);
		
		Dataset w = DatasetFactory.createFromObject(Dataset.FLOAT64, new double[]{wavelength}, new int[] {1});
		w.setName("incident_wavelength");
		DataNode wNode = file.createData(beamNode, w);
		file.addAttribute(wNode, new AttributeImpl("units", "Angstrom"));
	}

}
