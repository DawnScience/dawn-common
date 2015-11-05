/*-
 *******************************************************************************
 * Copyright (c) 2015 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Dickie - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.dawnsci.nexus.model;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.nexus.model.impl.AbstractNexusBaseClassProvider;
import org.dawnsci.nexus.model.impl.MapBasedMetadataProvider;
import org.dawnsci.nexus.model.impl.NexusUser;
import org.dawnsci.nexus.model.impl.TomoApplicationDefinitionModel;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.IntegerDataset;
import org.eclipse.dawnsci.hdf5.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NXsource;
import org.eclipse.dawnsci.nexus.NexusApplicationDefinition;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.impl.NXcollectionImpl;
import org.eclipse.dawnsci.nexus.impl.NXdetectorImpl;
import org.eclipse.dawnsci.nexus.impl.NXsourceImpl;
import org.eclipse.dawnsci.nexus.impl.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.model.api.NexusDataModel;
import org.eclipse.dawnsci.nexus.model.api.NexusEntryModel;
import org.eclipse.dawnsci.nexus.model.api.NexusTreeModification;


public class ComplexNexusFileModelTest extends AbstractNexusFileModelTestBase {
	
	private static class SimplePositioner extends AbstractNexusBaseClassProvider<NXpositioner> {

		public SimplePositioner(final String name) {
			super(name, NexusBaseClass.NX_POSITIONER, name, NexusBaseClass.NX_INSTRUMENT);
		}
		
		@Override
		public NXpositioner doCreateNexusBaseClassInstance(
				NexusNodeFactory nodeFactory) {
			NXpositioner positioner = nodeFactory.createNXpositioner();
			positioner.initializeLazyDataset(name, 1, Dataset.FLOAT64);
			
			return positioner;
		}

	}
	
	private static final class TomoScanDevicePositioner extends AbstractNexusBaseClassProvider<NXpositioner> {

		public TomoScanDevicePositioner() {
			super("tomoScanDevice", NexusBaseClass.NX_POSITIONER, "ss1_rot");
		}
		@Override
		public NXpositioner doCreateNexusBaseClassInstance(
				NexusNodeFactory nodeFactory) {
			NXpositioner positioner = nodeFactory.createNXpositioner();
			positioner.initializeLazyDataset("imageNumber", 1, Dataset.FLOAT64);
			positioner.initializeLazyDataset("image_key", 1, Dataset.FLOAT64);
			positioner.initializeLazyDataset("ss1_X", 1, Dataset.FLOAT64);
			positioner.initializeLazyDataset("ss1_rot", 1, Dataset.FLOAT64);
			positioner.initializeLazyDataset("tomography_shutter", 1, Dataset.FLOAT64);
			
			return positioner;
		}

	}
	
	private static class TestDetector extends AbstractNexusBaseClassProvider<NXdetector> {
		
		public TestDetector() {
			super("pc01_hw_hdf", NexusBaseClass.NX_DETECTOR);
		}
		
		@Override
		public NXdetector doCreateNexusBaseClassInstance(NexusNodeFactory nodeFactory) {
			final NXdetectorImpl detector = nodeFactory.createNXdetector();
			
			detector.initializeLazyDataset(NXdetectorImpl.NX_DATA, 3, Dataset.INT16);
			detector.initializeLazyDataset(NXdetectorImpl.NX_COUNT_TIME, 1, Dataset.FLOAT64);
			IDataset regionOrigin = new IntegerDataset(new int[] { 0, 0 }, 1, 2);
			detector.setField("region_origin", regionOrigin);
			IDataset regionSize = new IntegerDataset(new int[] { 2560, 2160 }, 1, 2);
			detector.setField("region_size", regionSize);
			detector.initializeLazyDataset("start_time", 1, Dataset.FLOAT64);
			detector.initializeLazyDataset("time_ms", 1, Dataset.INT64); // actually unsigned int 32 in original nexus file
			
			return detector;
		}

	}
	
	private static class TestSource extends AbstractNexusBaseClassProvider<NXsource> {

		public TestSource() {
			super("source", NexusBaseClass.NX_SOURCE);
		}

		@Override
		protected NXsource doCreateNexusBaseClassInstance(NexusNodeFactory nodeFactory) {
			final NXsourceImpl source = nodeFactory.createNXsource();
			source.setCurrentScalar(-1.0);
			source.setEnergyScalar(-1.0);
			source.setNameScalar("DLS");
			source.setProbeScalar("X-ray");
			source.setTypeScalar("type");
			
			return source;
		}
		
	}
	
	/**
	 * In the real world the before_scan collection is used by GDA to store additional data it needs.
	 * In a real world system this provider could fetch the details from the scan.
	 */
	private static class BeforeScan extends AbstractNexusBaseClassProvider<NXcollection> {

		public BeforeScan() {
			super("before_scan", NexusBaseClass.NX_COLLECTION);
		}

		@Override
		protected NXcollection doCreateNexusBaseClassInstance(
				NexusNodeFactory nodeFactory) {
			NXcollectionImpl beforeScan = nodeFactory.createNXcollection();
			
			// Create cs1 collection: 
			NXcollectionImpl cs1 = nodeFactory.createNXcollection();
			beforeScan.addGroupNode("cs1", cs1);
			cs1.setAttribute(null, "metadata_type", "scannable");	
			
			String[] cs1FieldNames = new String[] { "cs1_x", "cs1_y", "cs1_z" };
			double[] cs1FieldValues = new double[] { 202.2512, 0.15, -976.472 };
			for (int i = 0; i < cs1FieldNames.length; i++) {
				cs1.setField(cs1FieldNames[i], cs1FieldValues[i]);
				cs1.setAttribute(cs1FieldNames[i], "field_type", "input");
				cs1.setAttribute(cs1FieldNames[i], "format", "%5.5g");
				cs1.setAttribute(cs1FieldNames[i], "units", "mm");
			}
			
			// Create sample stage collection
			NXcollectionImpl sampleStage = nodeFactory.createNXcollection();
			beforeScan.addGroupNode("sample_stage", sampleStage);
			String[] ss1FieldNames = new String[] { "ss1_X", "ss1_Y", "ss1_Z", "ss1_rot",
					"ss1_samplex", "ss1_sampley", "ss1_samplez" };
			double[] ss1FieldValues = new double[] { -976.472, 9.9764, 25.1812, -88.2, 1365.111, -3900.095, 75.966 };
			String[] units = new String[] { "mm", "mm", "mm", "deg", "um", "um", "um" };
			for (int i = 0; i < ss1FieldNames.length; i++) {
				sampleStage.setField(ss1FieldNames[i], ss1FieldValues[i]);
				sampleStage.setAttribute(ss1FieldNames[i], "field_type", "input");
				sampleStage.setAttribute(ss1FieldNames[i], "units", units[i]);
				sampleStage.setAttribute(ss1FieldNames[i], "format", "%5.5g");
			}
			
			return beforeScan;
		}
		
	}
	
	private static final String FILE_NAME = "complexTestFile.nx5";
	
	@Override
	protected String getFilename() {
		return FILE_NAME;
	}
	
	private SimplePositioner actualTimePositioner;
	
	private SimplePositioner beamOkPositioner;
	
	private SimplePositioner ioncIPositioner;
	
	private TestDetector testDetector;
	
	private TestSource testSource;
	
	private BeforeScan beforeScan;
	
	private TomoScanDevicePositioner tomoScanDevicePositioner;
	
	private NexusUser user;

	private MapBasedMetadataProvider scanData;

	@Override
	protected List<NexusTreeModification> getNexusTreeModifications() {
		beforeScan = new BeforeScan();
		actualTimePositioner = new SimplePositioner("actualTime");
		beamOkPositioner = new SimplePositioner("beakok");
		ioncIPositioner = new SimplePositioner("ionc_i");
		tomoScanDevicePositioner = new TomoScanDevicePositioner();
		testDetector = new TestDetector();
		testSource = new TestSource();
		user = new NexusUser("user01");
		user.setUsername("myusername");
		
		scanData = new MapBasedMetadataProvider();
		scanData.addMetadata("entry_identifier", 24737);
		scanData.addMetadata("experiment_identifier", "nt9396-1");
		scanData.addMetadata("program_name", "GDA 8.36.0");
		scanData.addMetadata("scan_command", "scan tomoScanDevice Start: -88.200000 Stop: 91.800000 Step: 2.000000 Darks every:0 imagesPerDark:5 Flats every:0 imagesPerFlat:5 InBeamPosition:11.150000 OutOfBeamPosition:5.000000 numImages 111  actualTime ionc_i pco1_hw_hdf 0.1 beamok");
		scanData.addMetadata("scan_dimensions", 111);
		scanData.addMetadata("scan_identifier", "a3d668c0-e3c4-4ed9-b127-4a202b2b6bac");
		scanData.addMetadata("title", "AKingUVA_7050wSSwire_InSitu_95RH_2MMgCl2_p4ul_p4h");
		
		List<NexusTreeModification> nexusObjects = new ArrayList<>();
		nexusObjects.add(beforeScan);
		nexusObjects.add(scanData);
		nexusObjects.add(actualTimePositioner);
		nexusObjects.add(beamOkPositioner);
		nexusObjects.add(ioncIPositioner);
		nexusObjects.add(testDetector);
		nexusObjects.add(testSource);
		nexusObjects.add(tomoScanDevicePositioner);
		nexusObjects.add(user);
		
		return nexusObjects;
	}
	
	@Override
	protected void configureEntryModel(NexusEntryModel nexusEntryModel) {
		nexusEntryModel.setInstrumentName("i13");
	}

	@Override
	protected void configureDataModel(NexusDataModel dataModel) throws NexusException {
		dataModel.setDataDevice(testDetector);
		dataModel.addAxisDevice(0, tomoScanDevicePositioner, true);
		dataModel.addAxisDevice(0, actualTimePositioner, false);
		dataModel.addAxisDevice(0, beamOkPositioner, false);
		dataModel.addAxisDevice(0, testDetector, false, "count_time");
		
		dataModel.addAxisDevice(0, tomoScanDevicePositioner, false, "imageNumber");
		dataModel.addAxisDevice(0, tomoScanDevicePositioner, false, "image_key");
		dataModel.addAxisDevice(0, ioncIPositioner, false);
		
		dataModel.addAxisDevice(0, tomoScanDevicePositioner, false, "ss1_X");
		dataModel.addAxisDevice(0, tomoScanDevicePositioner, false, "tomography_shutter");

		dataModel.addAxisDevice(0, testDetector, false, "start_time");
		dataModel.addAxisDevice(0, testDetector, false, "time_ms");
		dataModel.addAxisDevice(0, tomoScanDevicePositioner, false);
	}
	
	protected void addApplicationDefinitions(NexusEntryModel nexusEntryModel) throws NexusException {
		TomoApplicationDefinitionModel appDefModel =
				(TomoApplicationDefinitionModel) nexusEntryModel.newApplicationDefinition(NexusApplicationDefinition.NX_TOMO);
		appDefModel.addDefaultGroups();
		appDefModel.setSource(testSource);
		appDefModel.setDetector(testDetector);
		appDefModel.setSampleName("test sample");
		appDefModel.setRotationAnglePositioner(tomoScanDevicePositioner);
		appDefModel.setXTranslation(nexusEntryModel.getDataNode("before_scan/sample_stage/ss1_samplex"));
		appDefModel.setYTranslation(nexusEntryModel.getDataNode("before_scan/sample_stage/ss1_sampley"));
		appDefModel.setZTranslation(nexusEntryModel.getDataNode("before_scan/sample_stage/ss1_samplez"));
	}

	@Override
	protected void validateNexusTree(TreeFile nexusTree, boolean loadedFromDisk) {
		final NXroot rootNode = (NXroot) nexusTree.getGroupNode();
		assertNotNull(rootNode);
		assertNumChildNodes(rootNode, 1, 0);
		
		NXentry entry = rootNode.getEntry("entry1");
		assertNotNull(entry);
		assertNumChildNodes(entry, 6, 7);
		
		NXcollection beforeScan = entry.getChild("before_scan", NXcollection.class);
//		validateBeforeScan(beforeScan);
		
		
		// TODO: validate tree by loading previously written file from disk?
		
	}
	

}
