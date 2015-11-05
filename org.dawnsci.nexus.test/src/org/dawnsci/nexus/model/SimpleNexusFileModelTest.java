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

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.nexus.model.impl.AbstractNexusBaseClassProvider;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.StringDataset;
import org.eclipse.dawnsci.hdf5.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NXbeam;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.impl.NXbeamImpl;
import org.eclipse.dawnsci.nexus.impl.NXdataImpl;
import org.eclipse.dawnsci.nexus.impl.NXdetectorImpl;
import org.eclipse.dawnsci.nexus.impl.NXinstrumentImpl;
import org.eclipse.dawnsci.nexus.impl.NXsampleImpl;
import org.eclipse.dawnsci.nexus.impl.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.model.api.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.model.api.NexusDataModel;
import org.eclipse.dawnsci.nexus.model.api.NexusTreeModification;
import org.junit.Assert;

public class SimpleNexusFileModelTest extends AbstractNexusFileModelTestBase {

	private static class TestDetector extends AbstractNexusBaseClassProvider<NXdetector> {

		public TestDetector() {
			super("analyser", NexusBaseClass.NX_DETECTOR,
					NXdetectorImpl.NX_DATA);
		}
		
		@Override
		protected NXdetector doCreateNexusBaseClassInstance(NexusNodeFactory nodeFactory) {
			final NXdetectorImpl nxDetector = nodeFactory.createNXdetector();

			nxDetector.setDescription(StringDataset.createFromObject("Test Detector"));
			nxDetector.initializeLazyDataset(NXdetectorImpl.NX_DATA, 2, Dataset.FLOAT64);
			// could add more fields

			return nxDetector;
		}

	}

	private static class TestBeam extends AbstractNexusBaseClassProvider<NXbeam> {

		public TestBeam() {
			super("beam", NexusBaseClass.NX_BEAM, null, NexusBaseClass.NX_SAMPLE);
		}
		
		@Override
		protected NXbeam doCreateNexusBaseClassInstance(NexusNodeFactory nodeFactory) {
			final NXbeamImpl beam = nodeFactory.createNXbeam();
			beam.setIncident_wavelength(DatasetFactory.createFromObject(123.456));
			beam.setFlux(DatasetFactory.createFromObject(12.34f));

			return beam;
		}

	}

	private static final String FILE_NAME = "simpleTestFile.nx5";

	private TestDetector detector;
	
	private TestBeam beam;

	protected String getFilename() {
		return FILE_NAME;
	}
	
	protected void configureDataModel(NexusDataModel dataModel) throws NexusException {
		dataModel.setDataDevice(detector);
	}
	
	protected List<NexusTreeModification> getNexusTreeModifications() {
		detector = new TestDetector();
		beam = new TestBeam();

		final List<NexusTreeModification> modifications = new ArrayList<>();
		modifications.add(detector);
		modifications.add(beam);
		
		return modifications;
	}
	
	protected void validateNexusTree(TreeFile nexusTree, boolean loadedFromDisk) {
		// TODO: validate tree by loading previously written file from disk?

		final NXroot rootNode = (NXroot) nexusTree.getGroupNode();
		assertNotNull(rootNode);
		assertNumChildNodes(rootNode, 1, 0);

		NXentry entry = rootNode.getEntry("entry1");
		assertNotNull(entry);
		assertNumChildNodes(entry, 3, 0);

		NXinstrumentImpl instrument = (NXinstrumentImpl) entry.getInstrument();
		checkInstrument(instrument, loadedFromDisk);

		NXsampleImpl sample = (NXsampleImpl) entry.getSample();
		checkSample(sample, loadedFromDisk);

		NXdataImpl data = (NXdataImpl) entry.getData();
		checkData(data, loadedFromDisk);
		assertSame(data.getDataNode("analyser"), instrument.getDetector("analyser").getDataNode("data"));
	}

	private void checkInstrument(NXinstrumentImpl instrument, boolean beforeSave) {
		assertNotNull(instrument);
		assertNumChildNodes(instrument, 1, 0);

		NXdetectorImpl detector = (NXdetectorImpl) instrument.getDetector("analyser");
		assertNotNull(detector);
		assertNumChildNodes(detector, 0, 2);
		
		assertEquals("Test Detector", detector.getDescription().getString());
		checkWriteableDataset(detector, NXdetectorImpl.NX_DATA, 2, Double.class, beforeSave);
	}

	private void checkSample(NXsampleImpl sample, boolean beforeSave) {
		assertNotNull(sample);
		assertNumChildNodes(sample, 1, 0);

		NXbeam beam = sample.getBeam();
		assertNotNull(beam);
		assertEquals(123.456, beam.getIncident_wavelength().getDouble(), 1e-15);
		assertEquals(12.34f, beam.getFlux().getFloat(), 1e-7);
	}

	private void checkData(NXdataImpl data, boolean beforeSave) {
		assertNotNull(data);
		assertNumChildNodes(data, 0, 1);
		// data node has the name of the device that provided it
		checkWriteableDataset(data, "analyser", 2, Double.class, beforeSave);
	}

}
