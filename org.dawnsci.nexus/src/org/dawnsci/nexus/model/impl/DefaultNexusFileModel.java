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

package org.dawnsci.nexus.model.impl;

import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.hdf5.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.impl.NXentryImpl;
import org.eclipse.dawnsci.nexus.impl.NXrootImpl;
import org.eclipse.dawnsci.nexus.impl.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.model.api.NexusEntryModel;
import org.eclipse.dawnsci.nexus.model.api.NexusFileModel;

public class DefaultNexusFileModel implements NexusFileModel {

	private final NexusNodeFactory nexusNodeFactory = new NexusNodeFactory();

	private int nextEntryNum = 1;

	private final TreeFile treeFile;

	private final NXrootImpl root;

	public DefaultNexusFileModel(final String filePath) {
		treeFile = nexusNodeFactory.createTreeFile(filePath);
		root = nexusNodeFactory.createNXroot();
		// TODO: do we need to set any attributes on root?
		treeFile.setGroupNode(root);
	}

	@Override
	public void saveFile() throws NexusException {
		NexusUtils.saveNexusFile(treeFile);
	}

	@Override
	public TreeFile getNexusTree() {
		return treeFile;
	}

	@Override
	public NXroot getNxRoot() {
		return root;
	}

	@Override
	public NexusEntryModel newEntry() {
		final String entryName = "entry" + nextEntryNum++;
		return newEntry(entryName);
	}

	@Override
	public NexusEntryModel newEntry(String entryName) {
		final NXentryImpl entry = nexusNodeFactory.createNXentry();
		root.setEntry(entryName, entry);

		return new DefaultNexusEntryModel(nexusNodeFactory, entry);
	}

}
