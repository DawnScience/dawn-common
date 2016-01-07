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

package org.dawnsci.nexus.builder.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.StringDataset;
import org.eclipse.dawnsci.nexus.NXbeam;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.builder.AbstractNexusProvider;
import org.eclipse.dawnsci.nexus.builder.NexusDataBuilder;
import org.eclipse.dawnsci.nexus.builder.NexusEntryBuilder;
import org.eclipse.dawnsci.nexus.builder.NexusEntryModification;
import org.eclipse.dawnsci.nexus.impl.NXbeamImpl;
import org.eclipse.dawnsci.nexus.impl.NXdetectorImpl;
import org.eclipse.dawnsci.nexus.impl.NexusNodeFactory;

public class SimpleNexusFileBuilderTest extends AbstractNexusFileBuilderTestBase {
	
	private static class TestDetector extends AbstractNexusProvider<NXdetector> {

		public TestDetector() {
			super("analyser", NexusBaseClass.NX_DETECTOR,
					NXdetectorImpl.NX_DATA);
		}
		
		@Override
		protected NXdetector doCreateNexusObject(NexusNodeFactory nodeFactory) {
			final NXdetectorImpl nxDetector = nodeFactory.createNXdetector();

			nxDetector.setDescription(StringDataset.createFromObject("Test Detector"));
			nxDetector.initializeLazyDataset(NXdetectorImpl.NX_DATA, 2, Dataset.FLOAT64);
			// could add more fields

			return nxDetector;
		}

	}

	private static class TestBeam extends AbstractNexusProvider<NXbeam> {

		public TestBeam() {
			super("beam", NexusBaseClass.NX_BEAM, null, NexusBaseClass.NX_SAMPLE);
		}
		
		@Override
		protected NXbeam doCreateNexusObject(NexusNodeFactory nodeFactory) {
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
	
	protected void addDataBuilder(NexusEntryBuilder entryModel) throws NexusException {
		NexusDataBuilder dataModel = entryModel.createDefaultData();
		dataModel.setDataDevice(detector);
	}
	
	protected List<NexusEntryModification> getNexusTreeModifications() {
		detector = new TestDetector();
		beam = new TestBeam();

		final List<NexusEntryModification> modifications = new ArrayList<>();
		modifications.add(detector);
		modifications.add(beam);
		
		return modifications;
	}

	@Override
	protected String getTestClassName() {
		return SimpleNexusFileBuilderTest.class.getCanonicalName();
	}
	
}
