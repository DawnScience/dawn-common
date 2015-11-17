/*-
 * Copyright 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.nexus;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.StringDataset;
import org.eclipse.dawnsci.analysis.tree.impl.DataNodeImpl;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.impl.NXdataImpl;
import org.eclipse.dawnsci.nexus.impl.NXdetectorImpl;
import org.eclipse.dawnsci.nexus.impl.NXentryImpl;
import org.eclipse.dawnsci.nexus.impl.NXmonitorImpl;
import org.eclipse.dawnsci.nexus.impl.NXrootImpl;
import org.eclipse.dawnsci.nexus.impl.NXsampleImpl;
import org.eclipse.dawnsci.nexus.impl.NXtransformationsImpl;
import org.eclipse.dawnsci.nexus.impl.NXuserImpl;
import org.junit.After;

public class NexusMappingFileTest extends AbstractNexusFileTestBase {

	private static final String FILE_NAME = "mapping.nxs";

	private NXrootImpl root;

	private GroupNode userCache = null;

	private DataNodeImpl experimentIDCache = null;

	private GroupNode microDataCache = null;

	private GroupNode monitorArmTransformCache = null;

	private GroupNode detectorArmTransformCache = null;

	@Override
	@After
	public void tearDown() {
		nexusNodeFactory = null;
	}

	private DataNode getDataNode(final String path) {
		return (DataNode) root.findNodeLink(path).getDestination();
	}

	@Override
	protected NXroot createNXroot() {
		root = nexusNodeFactory.createNXroot();

		addEntry1(root);
		addEntryMicro(root);



		return root;
	}


	private void addEntry1(NXrootImpl root) {
		NXentryImpl entry = nexusNodeFactory.createNXentry();

		entry.setProgram_name(StringDataset.createFromObject("GDA 9.0.0"));
		entry.setField("scan_command", "Mapping Scan");
		entry.setField("scan_identifier", "aa-bb-cc-dd-ee");
		entry.setField("title", "Example Mapping Scan");
		entry.setEntry_identifier(StringDataset.createFromObject("24737"));

		addExperimentID(entry);
		addUser(entry);

		addInstrument(entry);

		addSample(entry);


		root.setEntry("entry1", entry);
		addDataBlock(entry);
	}

	private void addDataBlock(NXentryImpl entry) {

		NXdataImpl data = nexusNodeFactory.createNXdata();
		data.setAttribute(null, "signal", "data");
		data.setAttribute(null, "axes", DatasetFactory.createFromObject(new String[] {"x_stage_set", "y_stage_set", "t_stage_set", "energy"}));
		data.setAttribute(null, "t_stage_indices", DatasetFactory.createFromObject(new int[] {0, 1, 2}));
		data.setAttribute(null, "x_stage_indices", DatasetFactory.createFromObject(new int[] {0, 1, 2}));
		data.setAttribute(null, "y_stage_indices", DatasetFactory.createFromObject(new int[] {0, 1, 2}));
		data.setAttribute(null, "energy_indices", DatasetFactory.createFromObject(new int[] {3}));
		data.setAttribute(null, "t_stage_set_indices", DatasetFactory.createFromObject(new int[] {2}));
		data.setAttribute(null, "x_stage_set_indices", DatasetFactory.createFromObject(new int[] {1}));
		data.setAttribute(null, "y_stage_set_indices", DatasetFactory.createFromObject(new int[] {0}));

		data.addDataNode("data", getDataNode("/entry1/instrument/fluo/data"));
		data.addDataNode("energy", getDataNode("/entry1/instrument/fluo/energy"));
		data.addDataNode("t_stage", getDataNode("/entry1/sample/transformations/t_stage"));
		data.addDataNode("x_stage", getDataNode("/entry1/sample/transformations/x_stage"));
		data.addDataNode("y_stage", getDataNode("/entry1/sample/transformations/y_stage"));
		data.addDataNode("t_stage_set", getDataNode("/entry1/sample/t_stage_set"));
		data.addDataNode("x_stage_set", getDataNode("/entry1/sample/x_stage_set"));
		data.addDataNode("y_stage_set", getDataNode("/entry1/sample/y_stage_set"));

		entry.addGroupNode("data", data);
	}

	private void addSample(NXentryImpl entry) {
		NXsampleImpl sample = nexusNodeFactory.createNXsample();
		sample.setAttribute(null, "depends_on", "t_stage");

		NXtransformationsImpl transforms = nexusNodeFactory.createNXtransformations();

		DataNode t_stage = nexusNodeFactory.createDataNode();
		t_stage.setDataset(DatasetFactory.ones(new int[] {10, 20, 180}, Dataset.INT16));
		transforms.addDataNode("t_stage", t_stage);
		transforms.setAttribute("t_stage", "depends_on", "x_stage");
		transforms.setAttribute("t_stage", "transformation_type", "rotation");

		DataNode x_stage = nexusNodeFactory.createDataNode();
		x_stage.setDataset(DatasetFactory.ones(new int[] {10, 20, 180}, Dataset.INT16));
		transforms.addDataNode("x_stage", x_stage);
		transforms.setAttribute("x_stage", "depends_on", "y_stage");
		transforms.setAttribute("x_stage", "transformation_type", "translation");

		DataNode y_stage = nexusNodeFactory.createDataNode();
		y_stage.setDataset(DatasetFactory.ones(new int[] {10, 20, 180}, Dataset.INT16));
		transforms.addDataNode("y_stage", y_stage);
		transforms.setAttribute("y_stage", "depends_on", ".");
		transforms.setAttribute("y_stage", "transformation_type", "translation");

		sample.addNode("transformations", transforms);

		sample.setDataset("t_stage_set", DatasetFactory.createRange(180, Dataset.INT16));
		sample.setDataset("x_stage_set", DatasetFactory.createRange(10, Dataset.INT16));
		sample.setDataset("y_stage_set", DatasetFactory.createRange(20, Dataset.INT16));

		entry.setSample(sample);
	}

	private void addInstrument(NXentryImpl entry) {
		NXinstrument instrument = nexusNodeFactory.createNXinstrument();

		addFluorecenceDetector(instrument);
		addDiffractionDetector(instrument);
		addTransmissionMonitor(instrument);
		addI0Monitor(instrument);
		addItMonitor(instrument);

		entry.setInstrument(instrument);
	}

	private void addItMonitor(NXinstrument instrument) {
		NXmonitorImpl monitor = nexusNodeFactory.createNXmonitor();
		monitor.setDataset("data", DatasetFactory.ones(new int[] {10, 20, 180}, Dataset.INT16));
		addMonitorArmTransform(monitor);
		instrument.addGroupNode("It", monitor);
	}

	private void addMonitorArmTransform(NXmonitorImpl monitor) {
		if (monitorArmTransformCache == null) {
			NXtransformationsImpl transforms = nexusNodeFactory.createNXtransformations();
			DataNode monitor_arm = nexusNodeFactory.createDataNode();
			monitor_arm.setDataset(DatasetFactory.ones(new int[] {10, 20, 180}, Dataset.INT16));
			transforms.addDataNode("monitor_arm", monitor_arm);
			transforms.setAttribute("monitor_arm", "depends_on", ".");
			transforms.setAttribute("monitor_arm", "transformation_type", "translation");
			monitor.addNode("transformations", transforms);
			monitorArmTransformCache  = monitor.getGroupNode("transformations");
		} else {
			monitor.addNode("transformations", monitorArmTransformCache);
		}	
	}

	private void addI0Monitor(NXinstrument instrument) {
		NXmonitorImpl monitor = nexusNodeFactory.createNXmonitor();
		monitor.setDataset("data", DatasetFactory.ones(new int[] {10, 20, 180}, Dataset.INT16));
		addMonitorArmTransform(monitor);
		instrument.addGroupNode("I0", monitor);
	}

	private void addTransmissionMonitor(NXinstrument instrument) {
		NXmonitorImpl monitor = nexusNodeFactory.createNXmonitor();
		monitor.setDataset("data", DatasetFactory.ones(new int[] {10, 20, 180}, Dataset.INT16));
		addMonitorArmTransform(monitor);
		instrument.addGroupNode("trans", monitor);
	}

	private void addDiffractionDetector(NXinstrument instrument) {
		NXdetectorImpl detector = nexusNodeFactory.createNXdetector();
		detector.setAttribute(null, "signal", "data");
		detector.setX_pixel_sizeScalar(1);
		detector.setAttribute(NXdetectorImpl.NX_X_PIXEL_SIZE, "units", "um");
		detector.setY_pixel_sizeScalar(1);
		detector.setAttribute(NXdetectorImpl.NX_Y_PIXEL_SIZE, "units", "um");

		detector.setDataset("data", DatasetFactory.ones(new int[] {10, 20, 180, 32, 32}, Dataset.INT16));
		detector.setAttribute("data", "interpretation", "image");

		addDetectorArmTransform(detector);

		instrument.addGroupNode("diff", detector);
	}

	private void addFluorecenceDetector(NXinstrument instrument) {
		NXdetectorImpl detector = nexusNodeFactory.createNXdetector();
		detector.setAttribute(null, "signal", "data");
		detector.setAttribute(null, "axes", DatasetFactory.createFromObject(new String[] {".", ".", ".", "energy"}));
		detector.setAttribute(null, "energy_indices", "3");

		detector.setDataset("data", DatasetFactory.ones(new int[] {10, 20, 180, 1024}, Dataset.INT16));
		detector.setAttribute("data", "interpretation", "spectrum");

		detector.setDataset("energy", DatasetFactory.createRange(1024, Dataset.INT16));
		detector.setAttribute("energy", "units", "keV");

		addDetectorArmTransform(detector);

		instrument.addGroupNode("fluo", detector);
	}

	private void addDetectorArmTransform(NXdetectorImpl detector) {
		if (detectorArmTransformCache == null) {
			NXtransformationsImpl transforms = nexusNodeFactory.createNXtransformations();
			DataNode detector_arm = nexusNodeFactory.createDataNode();
			detector_arm.setDataset(DatasetFactory.ones(new int[] {10, 20, 180}, Dataset.INT16));
			transforms.addDataNode("detector_arm", detector_arm);
			transforms.setAttribute("detector_arm", "depends_on", ".");
			transforms.setAttribute("detector_arm", "transformation_type", "translation");
			detector.addNode("transformations", transforms);
			detectorArmTransformCache   = detector.getGroupNode("transformations");
		} else {
			detector.addNode("transformations", detectorArmTransformCache);
		}	
	}

	private void addEntryMicro(NXrootImpl root) {
		NXentryImpl entry = nexusNodeFactory.createNXentry();

		entry.setProgram_name(StringDataset.createFromObject("Microscope Software 1.0.0"));
		entry.setField("title", "White image of scan");

		addExperimentID(entry);
		addUser(entry);

		addMicroscopeData(entry);

		root.setEntry("entry_micro", entry);
	}

	private void addUser(NXentryImpl entry) {
		if (userCache == null) {
			NXuserImpl user = nexusNodeFactory.createNXuser();
			user.setField("username", "ssg37927");
			entry.setUser(user);
			userCache = entry.getGroupNode("user");
		} else {
			entry.addGroupNode("user", userCache);
		}	
	}

	private void addExperimentID(NXentryImpl entry) {
		if (experimentIDCache == null) {
			entry.setExperiment_identifier(StringDataset.createFromObject("mt9396-1"));
			experimentIDCache = entry.getDataNode(NXentryImpl.NX_EXPERIMENT_IDENTIFIER);
		} else {
			entry.addDataNode(NXentryImpl.NX_EXPERIMENT_IDENTIFIER, experimentIDCache);
		}
	}

	private void addMicroscopeData(NXentryImpl entry) {
		if (microDataCache == null) {
			NXdataImpl data = nexusNodeFactory.createNXdata();

			data.setDataset("data", DatasetFactory.ones(new int[] {4, 1024, 2048}, Dataset.INT16));
			data.setAttribute(null, "signal", "data");	
			data.setAttribute(null, "axes", DatasetFactory.createFromObject(new String[] {"depth", "image_x", "image_y"}));
			data.setAttribute(null, "image_x_indices", "1");
			data.setAttribute(null, "image_y_indices", "2");

			data.setDataset("image_x", DatasetFactory.createRange(1024, Dataset.INT16));
			data.setAttribute("image_x", "units", "mm");

			data.setDataset("image_y", DatasetFactory.createRange(2048, Dataset.INT16));
			data.setAttribute("image_y", "units", "mm");

			entry.setData(data);
			microDataCache = entry.getGroupNode("data");
		} else {
			entry.addGroupNode("data", microDataCache);
		}
	}

	@Override
	protected String getFilename() {
		return FILE_NAME;
	}

}