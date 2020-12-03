/*
 * Copyright (c) 2012, 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.persistence.internal;

import javax.vecmath.Vector3d;

import org.eclipse.dawnsci.analysis.api.diffraction.DetectorProperties;
import org.eclipse.dawnsci.analysis.api.diffraction.DiffractionCrystalEnvironment;
import org.eclipse.dawnsci.analysis.api.diffraction.IPowderCalibrationInfo;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.LongDataset;

class PersistSinglePowderCalibration {

	public static final String DAWNCALIBRATIONID = "DAWNScience";
	public static final String DEFAULTDETECTORNAME = "detector";
	public static final String DEFAULTINSTRUMENTNAME = "/entry/instrument";
	public static final String DEFAULTDATANAME = "/entry/calibration_data";

	public static final String DATANAME = "data";
	
	public static void writeCalibrationToFile(NexusFile file, IDataset calibrationImage,
			IDiffractionMetadata metadata, IPowderCalibrationInfo info) throws Exception {
		
		GroupNode parentNode = file.getGroup(DEFAULTINSTRUMENTNAME, true);
		parentNode.addAttribute(NexusUtils.createNXclassAttribute(NexusConstants.INSTRUMENT));
//		String parent = HierarchicalDataFileUtils.createParentEntry(file, DEFAULTINSTRUMENTNAME,Nexus.INST);
		
		String parent = DEFAULTINSTRUMENTNAME + Node.SEPARATOR + "detector";
		GroupNode detectorNode = file.getGroup(parentNode, "detector", NexusConstants.DETECTOR, true);
		
		//write in data
		calibrationImage.setName(DATANAME);
		DataNode calibrationDataNode = file.createData(parentNode, calibrationImage);
		
		//make NXData in root as link to this data, make sure data has a link attribute back to the detector
		GroupNode defaultGroupNode = file.getGroup(DEFAULTDATANAME, true);
//		String group = file.group(DEFAULTDATANAME);
		file.addAttribute(defaultGroupNode, NexusUtils.createNXclassAttribute(NexusConstants.DATA));
//		file.setNexusAttribute(group, "NXdata");
//		file.createLink(group, dataset.substring(dataset.lastIndexOf('/')+1), dataset); // TODO FIXME Is this right?
		
		//pixel+beam centre information
		DetectorProperties detprop = metadata.getDetector2DProperties();
		
		PersistDiffractionMetadataUtils.writeDetectorProperties(file, parent, detprop);
		
		createCalibrationMethod(file,info,parent);
		DiffractionCrystalEnvironment de = metadata.getDiffractionCrystalEnvironment();
		PersistDiffractionMetadataUtils.writeWavelengthSample(file, info, de.getWavelength());
		
	}
	
	private static void createDependsOnTransformations(NexusFile file, DetectorProperties det, String parent) throws Exception {
		
		String transformationPath = parent + Node.SEPARATOR + "transformations";
		GroupNode parentGroup = file.getGroup(parent, true);
		
		file.getGroup(parentGroup, "transformations", NexusConstants.TRANSFORMATIONS, true);
		
		DoubleDataset offset = DatasetFactory.zeros(DoubleDataset.class, 3);
		offset.setName("offset");
		
		Vector3d trans = (Vector3d) det.getOrigin().clone();
		double length = trans.length();
		trans.normalize();
		
		DoubleDataset vector = DatasetFactory.zeros(DoubleDataset.class, 3);
		trans.get(vector.getData());
		vector.setName("vector");
		//as passive, so -length
		String ds = createNXtransformation("translation", "translation",vector,offset,"mm",".",length,file,transformationPath);
		
		double[] norm = det.getNormalAnglesInDegrees();
		
		
		vector = DatasetFactory.zeros(DoubleDataset.class, 3);
		vector.set(1, 2);
		vector.setName("vector");
		
		String roll = createNXtransformation("roll", "rotation",vector,offset,"degrees",ds,norm[2],file,transformationPath);
		
		vector = DatasetFactory.zeros(DoubleDataset.class, 3);
		vector.set(1, 0);
		vector.setName("vector");
		
		String pitch = createNXtransformation("pitch", "rotation",vector,offset,"degrees",roll,-norm[1],file,transformationPath);
		
		vector = DatasetFactory.zeros(DoubleDataset.class, 3);
		vector.set(1, 1);
		vector.setName("vector");
		
		String yaw = createNXtransformation("yaw", "rotation",vector,offset,"degrees",pitch,norm[0],file,transformationPath);
	
		Dataset yawData = DatasetFactory.createFromObject(yaw);
		yawData.setName("depends_on");
		file.createData(parent, yawData, true);
		
	}
	
	private static void createCalibrationMethod(NexusFile file, IPowderCalibrationInfo info, String parent) throws Exception {
		String calibrationPath = parent + Node.SEPARATOR + "calibration_method";
		GroupNode parentGroup = file.getGroup(parent, true);
		
		GroupNode noteGroup = file.getGroup(parentGroup, "calibration_method", NexusConstants.NOTE, true);
		Dataset authorData = DatasetFactory.createFromObject(DAWNCALIBRATIONID);
		authorData.setName("author");
		file.createData(noteGroup, authorData);
		Dataset descriptionData = DatasetFactory.createFromObject(info.getMethodDescription());
		descriptionData.setName("description");
		file.createData(noteGroup, descriptionData);
		
		IDataset data = info.getUsedDSpaceIndexValues();
		data.setName("d_space_index");
		file.createData(noteGroup, data);
		
		createDoubleDataset("residual", info.getResidual(), file, calibrationPath);
		
		if (info.getCitationInformation() == null || info.getCitationInformation().length == 0) return;
		
		GroupNode referenceGroup = file.getGroup(noteGroup, "reference", NexusConstants.CITE, true);
		Dataset descrData = DatasetFactory.createFromObject(info.getCitationInformation()[0]);
		descrData.setName("description");
		file.createData(referenceGroup, descrData);
		Dataset doiData = DatasetFactory.createFromObject(info.getCitationInformation()[1]);
		doiData.setName("doi");
		file.createData(referenceGroup, doiData);
		Dataset endNodeData = DatasetFactory.createFromObject(info.getCitationInformation()[2]);
		endNodeData.setName("endnote");
		file.createData(referenceGroup, endNodeData);
		Dataset bibtexData = DatasetFactory.createFromObject(info.getCitationInformation()[3]);
		bibtexData.setName("bibtex");
		file.createData(referenceGroup, bibtexData);
	}
	
	private static String createNXtransformation(String name, String type, Dataset vector, Dataset offset, String units, String depends_on, double value, NexusFile file, String group) throws Exception {
		
		String ds = createDoubleDataset(name, value,file, group);
		file.addAttribute(ds, TreeFactory.createAttribute(ds, vector));
		file.addAttribute(ds, TreeFactory.createAttribute(ds, offset));
		file.addAttribute(ds, TreeFactory.createAttribute("transformation_type", type));
		file.addAttribute(ds, TreeFactory.createAttribute("depends_on", depends_on));
		return ds;
	}
		
	private static String createDoubleDataset(String name, double val, NexusFile file, String group) throws Exception {
		file.createData(group, name, DatasetFactory.createFromObject(LongDataset.class, new double[]{val}, 1), true);
		group = group + Node.SEPARATOR + name;
		return group;
	}
	
}
