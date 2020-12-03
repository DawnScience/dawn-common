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
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;

class PersistDiffractionMetadataUtils {
	
	public static final String SAMPLEGROUPNAME = "/entry/calibration_sample";

	public static void writeDetectorProperties(NexusFile file, String parent, DetectorProperties detprop) throws Exception {
		GroupNode parentGroup = file.getGroup(parent, true);
		Dataset xpixelNumber = DatasetFactory.createFromObject(new int[]{detprop.getPx()});
		xpixelNumber.setName("x_pixel_number");
		DataNode xPixelNumberNode = file.createData(parentGroup, xpixelNumber);
		file.addAttribute(xPixelNumberNode, TreeFactory.createAttribute(NexusConstants.UNITS, "pixels"));

		Dataset ypixelNumber = DatasetFactory.createFromObject(new int[]{detprop.getPy()});
		ypixelNumber.setName("y_pixel_number");
		DataNode yPixelNumberNode = file.createData(parentGroup, ypixelNumber);
		file.addAttribute(yPixelNumberNode, TreeFactory.createAttribute(NexusConstants.UNITS, "pixels"));

		Dataset xpixelSize = DatasetFactory.createFromObject(new double[]{detprop.getHPxSize()});
		xpixelSize.setName("x_pixel_size");
		DataNode xPixelSizeNode = file.createData(parentGroup, xpixelSize);
		file.addAttribute(xPixelSizeNode, TreeFactory.createAttribute(NexusConstants.UNITS, "mm"));

		Dataset ypixelSize = DatasetFactory.createFromObject(new double[]{detprop.getVPxSize()});
		ypixelSize.setName("y_pixel_size");
		DataNode yPixelSizeNode = file.createData(parentGroup, ypixelSize);
		file.addAttribute(yPixelSizeNode, TreeFactory.createAttribute(NexusConstants.UNITS, "mm"));

		double dist = detprop.getBeamCentreDistance();
		double[] bc = detprop.getBeamCentreCoords();

		Dataset bcXPix = DatasetFactory.createFromObject(new double[]{bc[0]});
		bcXPix.setName("beam_center_x");
		DataNode bcXPixNode = file.createData(parentGroup, bcXPix);
		file.addAttribute(bcXPixNode, TreeFactory.createAttribute(NexusConstants.UNITS, "pixels"));

		Dataset bcYPix = DatasetFactory.createFromObject(new double[]{bc[1]});
		bcYPix.setName("beam_center_y");
		DataNode bcYPixNode = file.createData(parentGroup, bcYPix);
		file.addAttribute(bcYPixNode, TreeFactory.createAttribute(NexusConstants.UNITS, "pixels"));
		
		Dataset distance = DatasetFactory.createFromObject(new double[]{dist});
		distance.setName("distance");
		DataNode distanceNode = file.createData(parentGroup, distance);
		file.addAttribute(distanceNode, TreeFactory.createAttribute(NexusConstants.UNITS, "mm"));

		Matrix3d or = detprop.getOrientation();
		double[] orientation = new double[] {or.m00 ,or.m01, or.m02,
				or.m10, or.m11, or.m12,
				or.m20, or.m21, or.m22};

		Dataset orientationData = DatasetFactory.createFromObject(orientation);
		orientationData.setName("detector_orientation");
		file.createData(parentGroup, orientationData);
	}
	
	public static void writeWavelengthMono(NexusFile file, String instrument, double wavelength) throws Exception {
		GroupNode instrumentGroup = file.getGroup(instrument, true);
		GroupNode monochromatorGroup = file.getGroup(instrumentGroup, "monochromator", NexusConstants.MONOCHROMATOR, true);
		
		Dataset energy = DatasetFactory.createFromObject(new double[]{wavelength});
		energy.setName("wavelength");
		DataNode energyNode = file.createData(monochromatorGroup, energy);
		file.addAttribute(energyNode, TreeFactory.createAttribute(NexusConstants.UNITS, "Angstrom"));
	}
	
	public static void writeWavelengthSample(NexusFile file, IPowderCalibrationInfo info, double wavelength) throws Exception {
		
		GroupNode parentGroup = file.getGroup("/entry", true);
		GroupNode calibrationSampleGroup = file.getGroup(parentGroup, "calibration_sample", NexusConstants.SAMPLE, true);
		
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
		
		GroupNode beamNode = file.getGroup(calibrationSampleGroup, "beam", NexusConstants.BEAM, true);
		
		Dataset w = DatasetFactory.createFromObject(new double[]{wavelength});
		w.setName("incident_wavelength");
		DataNode wNode = file.createData(beamNode, w);
		file.addAttribute(wNode, TreeFactory.createAttribute(NexusConstants.UNITS, "Angstrom"));
	}

}
