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
import static org.dawnsci.nexus.NexusTestUtils.assertNexusTreesEqual;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.dawnsci.nexus.NexusTestUtils;
import org.dawnsci.nexus.builder.impl.DefaultNexusFileBuilder;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.hdf5.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.builder.NexusEntryBuilder;
import org.eclipse.dawnsci.nexus.builder.NexusFileBuilder;
import org.eclipse.dawnsci.nexus.builder.NexusEntryModification;
import org.eclipse.dawnsci.nexus.impl.NXobjectImpl;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.TestUtils;

public abstract class AbstractNexusFileModelTestBase {
	
	public static final String TEST_FILE_FOLDER = "testfiles/dawnsci/data/nexus/";

	private static String testScratchDirectoryName;
	
	private String filePath;
	
	private String comparisonFilePath;

	@Before
	public void setUp() throws Exception {
		testScratchDirectoryName = TestUtils.generateDirectorynameFromClassname(getClass().getCanonicalName());
		TestUtils.makeScratchDirectory(testScratchDirectoryName);
		filePath = testScratchDirectoryName + getFilename();
		comparisonFilePath = TEST_FILE_FOLDER + getFilename();
	}
	
	protected abstract String getFilename();
	
	@Test
	public void testBuildNexusFile() throws Exception {
		final NexusFileBuilder fileModel = new DefaultNexusFileBuilder(filePath);
		final NexusEntryBuilder entryModel = fileModel.newEntry();
		entryModel.addDefaultGroups();
		List<NexusEntryModification> treeModifications = getNexusTreeModifications();
		entryModel.modifyEntry(treeModifications);
		configureEntryModel(entryModel);
		
		addDataModel(entryModel);
		addApplicationDefinitions(entryModel);
		
		// save the nexus file
		fileModel.saveFile();
		
		// compare with file in repository
		final TreeFile nexusTree = fileModel.getNexusTree();
		TreeFile comparisonNexusTree = NexusUtils.loadNexusFile(comparisonFilePath, true);
		assertNexusTreesEqual(nexusTree, comparisonNexusTree);
	}
	
	protected void configureEntryModel(NexusEntryBuilder nexusEntryModel) throws NexusException {
		// do nothing, subclasses may override
	}
	
	protected void addApplicationDefinitions(NexusEntryBuilder nexusEntryModel) throws NexusException {
		// do nothing, subclasses may override
	}
	
	protected abstract List<NexusEntryModification> getNexusTreeModifications();
	
	protected abstract void addDataModel(NexusEntryBuilder entryModel) throws NexusException;
	
	protected void assertNumChildNodes(NXobject parentNode, int numGroupNodes, int numDataNodes) {
		assertEquals(numGroupNodes, parentNode.getNumberOfGroupNodes());
		assertEquals(numDataNodes, parentNode.getNumberOfDataNodes());
	}

	protected ILazyDataset getDataset(NXobjectImpl group, String name,
			boolean loadedFromDisk) {
		if (loadedFromDisk) {
			return group.getDataset(name);
		}
		
		return group.getLazyWritableDataset(name);
	}
	
	protected void checkWriteableDataset(NXobjectImpl group, String name,
			int expectedRank, Class<?> expectedElementClass, boolean loadedFromDisk) {
		ILazyDataset dataset = getDataset(group, name, loadedFromDisk);
		assertNotNull(dataset);
		assertEquals(expectedRank, dataset.getRank());
		assertEquals(expectedElementClass, dataset.elementClass());
	}

}
