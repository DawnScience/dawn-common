/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
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
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.hdf5.HierarchicalDataFileUtils;
import org.eclipse.dawnsci.hdf5.IHierarchicalDataFile;
import org.eclipse.dawnsci.hdf5.Nexus;
import org.eclipse.dawnsci.hdf5.nexus.NexusUtils;

class PersistDiffractionMetadataUtils {
	
	public static final String SAMPLEGROUPNAME = "/entry/calibration_sample";

	public static void writeDetectorProperties(IHierarchicalDataFile file, String parent, DetectorProperties detprop) throws Exception {
		int intType    = Dataset.INT32;
		int doubleType = Dataset.FLOAT64;

		final String nXPix = file.replaceDataset("x_pixel_number", intType, new long[] {1}, new int[]{detprop.getPx()}, parent);
		file.setAttribute(nXPix,NexusUtils.UNIT, "pixels");
		final String nYPix = file.replaceDataset("y_pixel_number", intType, new long[] {1}, new int[]{detprop.getPy()}, parent);
		file.setAttribute(nYPix,NexusUtils.UNIT , "pixels");

		final String sXPix = file.replaceDataset("x_pixel_size", doubleType, new long[] {1}, new double[]{detprop.getHPxSize()}, parent);
		file.setAttribute(sXPix, NexusUtils.UNIT, "mm");
		final String sYPix = file.replaceDataset("y_pixel_size", doubleType, new long[] {1}, new double[]{detprop.getVPxSize()}, parent);
		file.setAttribute(sYPix, NexusUtils.UNIT, "mm");

		double dist = detprop.getBeamCentreDistance();
		double[] bc = detprop.getBeamCentreCoords();
		
		final String bcXPix = file.replaceDataset("beam_center_x", doubleType, new long[] {1}, new double[]{bc[0]}, parent);
		file.setAttribute(bcXPix, NexusUtils.UNIT, "pixels");
		final String bcYPix = file.replaceDataset("beam_center_y", doubleType, new long[] {1}, new double[]{bc[1]}, parent);
		file.setAttribute(bcYPix, NexusUtils.UNIT, "pixels");
		
		final String distance = file.replaceDataset("distance", doubleType, new long[] {1}, new double[] {dist}, parent);
		file.setAttribute(distance, NexusUtils.UNIT, "mm");

		Matrix3d or = detprop.getOrientation();
		double[] orientation = new double[] {or.m00 ,or.m01, or.m02,
				or.m10, or.m11, or.m12,
				or.m20, or.m21, or.m22};

		file.replaceDataset("detector_orientation", doubleType, new long[] {9}, orientation, parent);
	}
	
	public static void writeWavelengthMono(IHierarchicalDataFile file, String instrument, double wavelength) throws Exception {
		String group = file.group("monochromator", instrument);
		file.setNexusAttribute(group, Nexus.MONO);
		final String energy = file.replaceDataset("wavelength", Dataset.FLOAT64, new long[] {1}, new double[]{wavelength}, group);
		file.setAttribute(energy, NexusUtils.UNIT, "Angstrom");
	}
	
	public static void writeWavelengthSample(IHierarchicalDataFile file, IPowderCalibrationInfo info, double wavelength) throws Exception {
		
		String sample = HierarchicalDataFileUtils.createParentEntry(file, SAMPLEGROUPNAME,"NXsample");
		file.replaceStringDataset("name", info.getCalibrantName(), sample);
		file.replaceStringDataset("type", "calibration sample", sample);
		//write in data
		final String datasetd = file.replaceDataset("d_space",  info.getCalibrantDSpaceValues(), sample);
		file.setNexusAttribute(datasetd, Nexus.SDS);
		
		String beam = file.group("beam",sample);
		file.setNexusAttribute(beam, "NXbeam");
		
		final String w = file.createDataset("incident_wavelength", Dataset.FLOAT64, new long[] {1}, new double[]{wavelength}, beam);
		file.setAttribute(w, NexusUtils.UNIT, "Angstrom");
	}
}
