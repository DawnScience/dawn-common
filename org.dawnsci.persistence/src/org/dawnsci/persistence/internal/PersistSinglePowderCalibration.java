/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.persistence.internal;

import javax.vecmath.Vector3d;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.diffraction.DetectorProperties;
import org.eclipse.dawnsci.analysis.api.diffraction.DiffractionCrystalEnvironment;
import org.eclipse.dawnsci.analysis.api.diffraction.IPowderCalibrationInfo;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.hdf.object.HierarchicalDataFileUtils;
import org.eclipse.dawnsci.hdf.object.IHierarchicalDataFile;
import org.eclipse.dawnsci.hdf.object.Nexus;
import org.eclipse.dawnsci.hdf.object.nexus.NexusUtils;

class PersistSinglePowderCalibration {

	public static final String DAWNCALIBRATIONID = "DAWNScience";
	public static final String DEFAULTDETECTORNAME = "detector";
	public static final String DEFAULTINSTRUMENTNAME = "/entry/instrument";
	public static final String DEFAULTDATANAME = "/entry/calibration_data";

	public static final String DATANAME = "data";
	
	public static void writeCalibrationToFile(IHierarchicalDataFile file, IDataset calibrationImage,
			IDiffractionMetadata metadata, IPowderCalibrationInfo info) throws Exception {
		
		String parent = HierarchicalDataFileUtils.createParentEntry(file, DEFAULTINSTRUMENTNAME,Nexus.INST);
		
		parent = file.group("detector", parent);
		file.setNexusAttribute(parent, Nexus.DETECT);
		
		//write in data
		final String dataset = file.createDataset(DATANAME,  calibrationImage, parent);
		file.setNexusAttribute(dataset, Nexus.SDS);
		
		//make NXData in root as link to this data, make sure data has a link attribute back to the detector
		String group = file.group(DEFAULTDATANAME);
		file.setNexusAttribute(group, Nexus.DATA);
		file.createLink(group, dataset.substring(dataset.lastIndexOf('/')+1), dataset); // TODO FIXME Is this right?
		
		
		
		//pixel+beam centre information
		DetectorProperties detprop = metadata.getDetector2DProperties();
		
		PersistDiffractionMetadataUtils.writeDetectorProperties(file, parent, detprop);
		
		createCalibrationMethod(file,info,parent);
		DiffractionCrystalEnvironment de = metadata.getDiffractionCrystalEnvironment();
		PersistDiffractionMetadataUtils.writeWavelengthSample(file, info, de.getWavelength());
		
	}
	
	private static void createDependsOnTransformations(IHierarchicalDataFile file, DetectorProperties det, String parent) throws Exception {
		String transform = file.group("transformations",parent);
		file.setNexusAttribute(transform, "NXtransformations");
		
		DoubleDataset offset = DatasetFactory.zeros(DoubleDataset.class, 3);
		offset.setName("offset");
		
		Vector3d trans = (Vector3d) det.getOrigin().clone();
		double length = trans.length();
		trans.normalize();
		
		DoubleDataset vector = DatasetFactory.zeros(DoubleDataset.class, 3);
		trans.get(vector.getData());
		vector.setName("vector");
		//as passive, so -length
		String ds = createNXtransformation("translation", "translation",vector,offset,"mm",".",length,file,transform);
		
		double[] norm = det.getNormalAnglesInDegrees();
		
		
		vector = DatasetFactory.zeros(DoubleDataset.class, 3);
		vector.set(1, 2);
		vector.setName("vector");
		
		String roll = createNXtransformation("roll", "rotation",vector,offset,"degrees",ds,norm[2],file,transform);
		
		vector = DatasetFactory.zeros(DoubleDataset.class, 3);
		vector.set(1, 0);
		vector.setName("vector");
		
		String pitch = createNXtransformation("pitch", "rotation",vector,offset,"degrees",roll,-norm[1],file,transform);
		
		vector = DatasetFactory.zeros(DoubleDataset.class, 3);
		vector.set(1, 1);
		vector.setName("vector");
		
		String yaw = createNXtransformation("yaw", "rotation",vector,offset,"degrees",pitch,norm[0],file,transform);
	
		file.createStringDataset("depends_on", yaw, parent);
		
	}
	
	private static void createCalibrationMethod(IHierarchicalDataFile file, IPowderCalibrationInfo info, String parent) throws Exception {
		String note = file.group("calibration_method",parent);
		file.setNexusAttribute(note, "NXnote");
		file.createStringDataset("author", DAWNCALIBRATIONID, note);
		file.createStringDataset("description", info.getMethodDescription(), note);
		
		IDataset data = info.getUsedDSpaceIndexValues();
		final String datasetd = file.createDataset("d_space_index",  data, note);
		file.setNexusAttribute(datasetd, Nexus.SDS);
		
		createDoubleDataset("residual", info.getResidual(), file, note);
		
		if (info.getCitationInformation() == null || info.getCitationInformation().length == 0) return;
		
		String cite = file.group("reference", note);
		file.setNexusAttribute(cite, "NXcite");
		file.createStringDataset("description", info.getCitationInformation()[0], cite);
		file.createStringDataset("doi", info.getCitationInformation()[1], cite);
		file.createStringDataset("endnote", info.getCitationInformation()[2], cite);
		file.createStringDataset("bibtex", info.getCitationInformation()[3], cite);
		
	}
	
	private static String createNXtransformation(String name, String type, Dataset vector, Dataset offset, String units, String depends_on, double value, IHierarchicalDataFile file, String group) throws Exception {
		
		String ds = createDoubleDataset(name, value,file, group);
		NexusUtils.setDatasetAttribute(vector, ds, file);
		NexusUtils.setDatasetAttribute(offset, ds, file);
		file.setAttribute(ds, "units", units);
		file.setAttribute(ds, "transformation_type", type);
		file.setAttribute(ds, "depends_on", depends_on);
		return ds;
	}
		
	private static String createDoubleDataset(String name, double val, IHierarchicalDataFile file, String group) throws Exception {
		
		return file.createDataset(name, Dataset.INT64, new long[]{1}, new double[]{val}, group);
		
	}
	
}
