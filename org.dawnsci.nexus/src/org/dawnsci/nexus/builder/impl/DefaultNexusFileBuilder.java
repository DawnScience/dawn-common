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

import java.util.HashMap;
import java.util.Map;

import org.dawnsci.nexus.ServiceHolder;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.builder.NexusEntryBuilder;
import org.eclipse.dawnsci.nexus.builder.NexusFileBuilder;
import org.eclipse.dawnsci.nexus.impl.NXentryImpl;
import org.eclipse.dawnsci.nexus.impl.NXrootImpl;
import org.eclipse.dawnsci.nexus.impl.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.validation.NexusValidationException;

/**
 * Default implementation of {@link NexusFileBuilder}.
 */
public class DefaultNexusFileBuilder implements NexusFileBuilder {

	private final NexusNodeFactory nexusNodeFactory = new NexusNodeFactory();

	private final TreeFile treeFile;

	private final NXrootImpl nxRoot;
	
	private Map<String, NexusEntryBuilder> entries = new HashMap<>();

	/**
	 * Creates a new {@link DefaultNexusFileBuilder}.
	 * @param filePath
	 */
	public DefaultNexusFileBuilder(final String filePath) {
		treeFile = nexusNodeFactory.createTreeFile(filePath);
		nxRoot = nexusNodeFactory.createNXroot();
		
		// TODO: do we need to set any attributes on root?
		nxRoot.setAttributeFile_name(filePath);
		treeFile.setGroupNode(nxRoot);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.NexusFileBuilder#saveFile()
	 */
	@Override
	public void saveFile() throws NexusException {
		final INexusFileFactory nexusFileFactory = ServiceHolder.getNexusFileFactory();
		final String filename = treeFile.getFilename();
		NexusFile nexusFile = nexusFileFactory.newNexusFile(filename);
		nexusFile.createAndOpenToWrite();
		nexusFile.addNode("/", treeFile.getGroupNode());
		nexusFile.flush();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.NexusFileBuilder#getNexusTree()
	 */
	@Override
	public TreeFile getNexusTree() {
		return treeFile;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.NexusFileBuilder#getNodeFactory()
	 */
	@Override
	public NexusNodeFactory getNodeFactory() {
		return nexusNodeFactory;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.NexusFileBuilder#getNxRoot()
	 */
	@Override
	public NXrootImpl getNXroot() {
		return nxRoot;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.NexusFileBuilder#newEntry()
	 */
	@Override
	public NexusEntryBuilder newEntry() throws NexusException {
		return newEntry("entry");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.NexusFileBuilder#newEntry(java.lang.String)
	 */
	@Override
	public NexusEntryBuilder newEntry(String entryName) throws NexusException {
		if (entries.containsKey(entryName)) {
			throw new NexusException("An entry with the name " + entryName + " already exists");
		}
		
		final NXentryImpl entry = nexusNodeFactory.createNXentry();
		nxRoot.setEntry(entryName, entry);

		NexusEntryBuilder entryModel = new DefaultNexusEntryBuilder(nexusNodeFactory, entry);
		entries.put(entryName, entryModel);
		
		return entryModel;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.NexusFileBuilder#validate()
	 */
	@Override
	public void validate() throws NexusValidationException {
		for (NexusEntryBuilder entry : entries.values()) {
			entry.validate();
		}
	}

}
