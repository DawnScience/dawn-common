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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.StringDataset;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.hdf5.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.impl.NXdataImpl;
import org.eclipse.dawnsci.nexus.model.api.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.model.api.NexusDataModel;

public class DefaultNexusDataModel implements NexusDataModel {

	private static final String NO_DEFAULT_AXIS_PLACEHOLDER = ".";

	private static final String ATTR_NAME_SIGNAL = "signal";

	private static final String ATTR_NAME_AXES = "axes";

	private static final String INDICES_SUFFIX = "_indices";

	private final NXdata nxData;

	private int rank = -1;

	// TODO use multiset from guava?
	private Map<String, Set<Integer>> axesIndices = null;

	private StringDataset dimensionDefaultAxisNames = null;
	
	private final DefaultNexusEntryModel entryModel;

	public DefaultNexusDataModel(DefaultNexusEntryModel entryModel,
			final NXdataImpl nxData) {
		this.entryModel = entryModel;
		this.nxData = nxData;
	}

	@Override
	public NXdata getNexusData() {
		return nxData;
	}

	@Override
	public void setDataDevice(NexusObjectProvider<? extends NXobject> baseClassProvider) throws NexusException {
		if (dimensionDefaultAxisNames != null) {
			throw new IllegalStateException("Data device already set");
		}

		// data node name is 'data' for NXdetector, 'value' for NXpositioner, etc.
		final DataNode dataNode = getDataNode(baseClassProvider, null);

		// TODO what should data node be called,
		final String deviceName = baseClassProvider.getName();
		nxData.addDataNode(deviceName, dataNode);

		// add 'signal' attribute giving name of default data field
		final Attribute signalAttribute = TreeFactory.createAttribute(ATTR_NAME_SIGNAL, deviceName, false);
		nxData.addAttribute(signalAttribute);

		rank = dataNode.getDataset().getRank();
		dimensionDefaultAxisNames = new StringDataset(rank);
		dimensionDefaultAxisNames.fill(NO_DEFAULT_AXIS_PLACEHOLDER);

		final Attribute axesAttribute = TreeFactory.createAttribute(ATTR_NAME_AXES, dimensionDefaultAxisNames, false);
		nxData.addAttribute(axesAttribute);

		axesIndices = new HashMap<>();
	}

	@Override
	public void addAxisDevice(int dimensionIndex, NexusObjectProvider<? extends NXobject> baseClassProvider,
			boolean makeDefault) throws NexusException {
		addAxisDevice(dimensionIndex, baseClassProvider, makeDefault, null);
	}

	@Override
	public void addAxisDevice(int dimensionIndex,
			NexusObjectProvider<? extends NXobject> nexusDeviceAdapter,
			boolean makeDefault, String dataNodeName) throws NexusException {
		if (dimensionIndex < 0 || dimensionIndex >= rank) {
			throw new IllegalArgumentException(MessageFormat.format("Axis index must be between {0} and {1}, was {2}", 0, rank, dimensionIndex));
		}

		// data node name is 'data' for NXdetector, 'value' for NXpositioner, etc.
		final DataNode dataNode = getDataNode(nexusDeviceAdapter, dataNodeName);
		String name = nexusDeviceAdapter.getName();

		// add new data node with name of axis device
		nxData.addDataNode(name, dataNode);

		if (makeDefault) {
			// update the dataset holding the values for the 'axes' attribute
			dimensionDefaultAxisNames.set(name, dimensionIndex);
		}

		updateAxisIndicesAttribute(name, dimensionIndex);
	}

	private void updateAxisIndicesAttribute(final String axisName, final int dimensionIndex) {
		Set<Integer> indices = axesIndices.get(axisName);
		if (indices == null) {
			indices = new TreeSet<Integer>();
		}
		indices.add(dimensionIndex);

		final String attrName = axisName + INDICES_SUFFIX;

		final IDataset indicesDataset = DatasetFactory.createFromList(new ArrayList<Integer>(indices));
		final Attribute axisIndicesAttribute = TreeFactory.createAttribute(attrName, indicesDataset, false);
		nxData.addAttribute(axisIndicesAttribute);
	}

	private DataNode getDataNode(NexusObjectProvider<? extends NXobject> baseClassProvider,
			String dataNodeName) throws NexusException {
		final NXobject deviceBaseClassInstance = entryModel.getNexusBaseClassInstance(baseClassProvider);
		if (dataNodeName == null) {
			dataNodeName = baseClassProvider.getDefaultDataNodeName();
		}
		final DataNode dataNode = deviceBaseClassInstance.getDataNode(dataNodeName);
		if (dataNode == null) {
			throw new IllegalArgumentException(MessageFormat.format("No such data node for group {0}: {1}",
					deviceBaseClassInstance.getNXclass().getSimpleName(), dataNodeName));
		}

		return dataNode;
	}

}
