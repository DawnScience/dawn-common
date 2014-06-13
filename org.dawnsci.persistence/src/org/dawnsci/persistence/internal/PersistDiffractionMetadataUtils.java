package org.dawnsci.persistence.internal;

import javax.vecmath.Matrix3d;

import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.h5.H5Datatype;

import org.dawb.hdf5.HierarchicalDataFileUtils;
import org.dawb.hdf5.IHierarchicalDataFile;
import org.dawb.hdf5.Nexus;
import org.dawb.hdf5.nexus.NexusUtils;
import org.dawnsci.io.h5.H5Utils;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
import uk.ac.diamond.scisoft.analysis.diffraction.IPowderCalibrationInfo;

public class PersistDiffractionMetadataUtils {
	
	public static final String SAMPLEGROUPNAME = "/entry/calibration_sample";

	public static void writeDetectorProperties(IHierarchicalDataFile file, Group parent, DetectorProperties detprop) throws Exception {
		H5Datatype intType = new H5Datatype(Datatype.CLASS_INTEGER, 32/8, Datatype.NATIVE, Datatype.NATIVE);
		H5Datatype doubleType = new H5Datatype(Datatype.CLASS_FLOAT, 64/8, Datatype.NATIVE, Datatype.NATIVE);

		final Dataset nXPix = file.replaceDataset("x_pixel_number", intType, new long[] {1}, new int[]{detprop.getPx()}, parent);
		file.setAttribute(nXPix,NexusUtils.UNIT, "pixels");
		final Dataset nYPix = file.replaceDataset("y_pixel_number", intType, new long[] {1}, new int[]{detprop.getPy()}, parent);
		file.setAttribute(nYPix,NexusUtils.UNIT , "pixels");

		final Dataset sXPix = file.replaceDataset("x_pixel_size", doubleType, new long[] {1}, new double[]{detprop.getHPxSize()}, parent);
		file.setAttribute(sXPix, NexusUtils.UNIT, "mm");
		final Dataset sYPix = file.replaceDataset("y_pixel_size", doubleType, new long[] {1}, new double[]{detprop.getVPxSize()}, parent);
		file.setAttribute(sYPix, NexusUtils.UNIT, "mm");

		double dist = detprop.getBeamCentreDistance();
		double[] bc = detprop.getBeamCentreCoords();
		
		final Dataset bcXPix = file.replaceDataset("beam_center_x", doubleType, new long[] {1}, new double[]{bc[0]}, parent);
		file.setAttribute(bcXPix, NexusUtils.UNIT, "pixels");
		final Dataset bcYPix = file.replaceDataset("beam_center_y", doubleType, new long[] {1}, new double[]{bc[1]}, parent);
		file.setAttribute(bcYPix, NexusUtils.UNIT, "pixels");
		
		final Dataset distance = file.replaceDataset("distance", doubleType, new long[] {1}, new double[] {dist}, parent);
		file.setAttribute(distance, NexusUtils.UNIT, "mm");

		Matrix3d or = detprop.getOrientation();
		double[] orientation = new double[] {or.m00 ,or.m01, or.m02,
				or.m10, or.m11, or.m12,
				or.m20, or.m21, or.m22};

		file.replaceDataset("detector_orientation", doubleType, new long[] {9}, orientation, parent);
	}
	
	public static void writeWavelengthMono(IHierarchicalDataFile file, Group instrument, double wavelength) throws Exception {
		Group group = file.group("monochromator", instrument);
		H5Datatype doubleType = new H5Datatype(Datatype.CLASS_FLOAT, 64/8, Datatype.NATIVE, Datatype.NATIVE);
		file.setNexusAttribute(group, Nexus.MONO);
		final Dataset energy = file.replaceDataset("wavelength", doubleType, new long[] {1}, new double[]{wavelength}, group);
		file.setAttribute(energy, NexusUtils.UNIT, "Angstrom");
	}
	
	public static void writeWavelengthSample(IHierarchicalDataFile file, IPowderCalibrationInfo info, double wavelength) throws Exception {
		Group sample = HierarchicalDataFileUtils.createParentEntry(file, SAMPLEGROUPNAME,"NXsample");
		file.replaceDataset("name", info.getCalibrantName(), sample);
		file.replaceDataset("type", "calibration sample", sample);
		//write in data
		final Datatype      datatyped = H5Utils.getDatatype(info.getCalibrantDSpaceValues());
		long[] shaped = H5Utils.getLong(info.getCalibrantDSpaceValues().getShape());
		final Dataset datasetd = file.replaceDataset("d_space",  datatyped, shaped, ((AbstractDataset)info.getCalibrantDSpaceValues()).getBuffer(), sample);
		file.setNexusAttribute(datasetd, Nexus.SDS);
		
		Group beam = file.group("beam",sample);
		file.setNexusAttribute(beam, "NXbeam");
		
		H5Datatype doubleType = new H5Datatype(Datatype.CLASS_FLOAT, 64/8, Datatype.NATIVE, Datatype.NATIVE);
		final Dataset w = file.createDataset("incident_wavelength", doubleType, new long[] {1}, new double[]{wavelength}, beam);
		file.setAttribute(w, NexusUtils.UNIT, "Angstrom");
	}
}
