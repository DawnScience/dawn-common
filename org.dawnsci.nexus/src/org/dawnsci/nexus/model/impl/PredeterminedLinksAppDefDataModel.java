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

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.hdf5.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXsubentry;
import org.eclipse.dawnsci.nexus.model.api.NexusApplicationDefinitionModel;
import org.eclipse.dawnsci.nexus.model.api.NexusDataModel;
import org.eclipse.dawnsci.nexus.model.api.NexusObjectProvider;

/**
 * A data model, wrapping an {@link NXdata} base class instance, within an application definition where
 * the application definition has predetermined links for the fields within the NxData base class instance
 * to locations within the main {@link NXsubentry} base class instance.
 * <p>
 * This class should only be used where by a class that implements {@link NexusApplicationDefinitionModel}
 * for an application definition where the NeXus application definition specifies links for the
 * locations. NXtomo is an example of this.
 * when its {@link NexusApplicationDefinitionModel#newData()} method is invoked. It should then add
 * the appropriate links using the {@link #addLink(String, String)} method of this class.
 */
public class PredeterminedLinksAppDefDataModel implements NexusDataModel {

	private final NXdata nxData;

	public PredeterminedLinksAppDefDataModel(NXdata nxData) {
		this.nxData = nxData;
	}

	protected void addLink(final String name, final DataNode dataNode) throws NexusException {
		nxData.addDataNode(name, dataNode);
	}

	@Override
	public NXdata getNexusData() {
		return nxData;
	}

	@Override
	public void setDataDevice(NexusObjectProvider<? extends NXobject> nexusDeviceAdapter) {
		// this data model already has all the information it needs to be fully populated
		throw new UnsupportedOperationException("No additional objects are required for this data model");
	}

	@Override
	public void addAxisDevice(int dimensionIndex, NexusObjectProvider<? extends NXobject> nexusDeviceAdapter, boolean makeDefault) {
		throw new UnsupportedOperationException("No additional objects are required for this data model");
	}

	@Override
	public void addAxisDevice(int dimensionIndex,
			NexusObjectProvider<? extends NXobject> nexusDeviceAdapter,
			boolean makeDefault, String dataNodeName) throws NexusException {
		throw new UnsupportedOperationException("No additional objects are required for this data model");
	}

}
