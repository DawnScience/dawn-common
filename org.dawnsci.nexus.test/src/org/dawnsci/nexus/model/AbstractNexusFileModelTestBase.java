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

import java.util.List;

import org.dawnsci.nexus.model.impl.DefaultNexusFileModel;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.hdf5.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.impl.NXobjectImpl;
import org.eclipse.dawnsci.nexus.model.api.NexusDataModel;
import org.eclipse.dawnsci.nexus.model.api.NexusEntryModel;
import org.eclipse.dawnsci.nexus.model.api.NexusFileModel;
import org.eclipse.dawnsci.nexus.model.api.NexusTreeModification;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.TestUtils;

public abstract class AbstractNexusFileModelTestBase {

	private static String testScratchDirectoryName;
	
	private String filePath;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testScratchDirectoryName = TestUtils.generateDirectorynameFromClassname(AbstractNexusFileModelTestBase.class.getCanonicalName());
		TestUtils.makeScratchDirectory(testScratchDirectoryName);
	}
	
	@Before
	public void setUp() {
		filePath = testScratchDirectoryName + getFilename();
	}
	
	protected abstract String getFilename();

	@Test
	public void testBuildNexusFile() throws Exception {
		final NexusFileModel nexusFileModel = new DefaultNexusFileModel(filePath);
		final NexusEntryModel nexusEntryModel = nexusFileModel.newEntry();
		nexusEntryModel.addDefaultGroups();
		List<NexusTreeModification> treeModifications = getNexusTreeModifications();
		nexusEntryModel.addNexusTreeModifications(treeModifications);
		configureEntryModel(nexusEntryModel);
		
		NexusDataModel dataModel = nexusEntryModel.createDefaultData();
		configureDataModel(dataModel);
		
		addApplicationDefinitions(nexusEntryModel);
		
		final TreeFile nexusTree = nexusFileModel.getNexusTree();
		validateNexusTree(nexusTree, false);
		
		nexusFileModel.saveFile();
		
		TreeFile reloadedNexusTree = NexusUtils.loadNexusFile(filePath, true);
		validateNexusTree(reloadedNexusTree, true);
	}
	
	protected void configureEntryModel(NexusEntryModel nexusEntryModel) throws NexusException {
		// do nothing, subclasses may override
	}
	
	protected void addApplicationDefinitions(NexusEntryModel nexusEntryModel) throws NexusException {
		// do nothing, subclasses may override
	}
	
	protected abstract List<NexusTreeModification> getNexusTreeModifications();
	
	protected abstract void configureDataModel(NexusDataModel dataModel) throws NexusException;
	
	protected abstract void validateNexusTree(final TreeFile nexusTree, boolean loadedFromDisk);
	
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
