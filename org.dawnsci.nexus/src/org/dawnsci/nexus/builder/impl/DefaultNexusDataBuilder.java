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

import java.text.MessageFormat;

import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.dataset.impl.IntegerDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.StringDataset;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.hdf5.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.builder.NexusDataBuilder;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.impl.NXdataImpl;

/**
 * Default implementation of {@link NexusDataBuilder}.
 *
 */
public class DefaultNexusDataBuilder extends AbstractNexusDataBuilder implements NexusDataBuilder {

	private DataNode defaultDataNode;

	private StringDataset dimensionDefaultAxisNames = null;

	/**
	 * Create a new {@link DefaultNexusDataBuilder}. This constructor should only be
	 * called by {@link DefaultNexusEntryBuilder}.
	 * @param entryModel parent entry model
	 * @param nxData {@link NXdata} object to wrap
	 */
	protected DefaultNexusDataBuilder(DefaultNexusEntryBuilder entryModel,
			final NXdataImpl nxData) {
		super(entryModel, nxData);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.NexusDataBuilder#getNexusData()
	 */
	@Override
	public NXdataImpl getNxData() {
		return nxData;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.model.api.NexusDataModel#setDataDevice(org.eclipse.dawnsci.nexus.model.api.NexusObjectProvider, java.lang.String)
	 */
	@Override
	public void setDataDevice(
			NexusObjectProvider<? extends NXobject> nexusObjectProvider,
			String dataFieldName) throws NexusException {
		if (dimensionDefaultAxisNames != null) {
			throw new IllegalStateException("Data device already set");
		}

		// get the data field from the nexus object add add it to the NXdata group
		// data node name is 'data' for NXdetector, 'value' for NXpositioner, etc.
		defaultDataNode = getDataNode(nexusObjectProvider, null);

		if (dataFieldName == null) {
			dataFieldName = nexusObjectProvider.getName();
		}
		nxData.addDataNode(dataFieldName, defaultDataNode);

		// add 'signal' attribute giving name of default data field
		final Attribute signalAttribute = TreeFactory.createAttribute(ATTR_NAME_SIGNAL, dataFieldName, false);
		nxData.addAttribute(signalAttribute);

		// create the 'axes' attribute of the NXgroup and set each axis name to "."
		dimensionDefaultAxisNames = new StringDataset(defaultDataNode.getDataset().getRank());
		dimensionDefaultAxisNames.fill(NO_DEFAULT_AXIS_PLACEHOLDER);
		final Attribute axesAttribute = TreeFactory.createAttribute(ATTR_NAME_AXES, dimensionDefaultAxisNames, false);
		nxData.addAttribute(axesAttribute);
	}

	protected void addAxisDevice(NexusObjectProvider<? extends NXobject> nexusObjectProvider,
			String sourceFieldName, String destinationFieldName, int[] dimensionMappings, Integer primaryAxisForDimensionIndex)
			throws NexusException {
		if (defaultDataNode == null) {
			throw new IllegalStateException("The default dataset must exist before an axis can be added.");
		}
		
		// check that the dimension index is one of the dimensions of the default dataset
		final int rank = defaultDataNode.getDataset().getRank();
		final Integer defaultDataAxisIndex = primaryAxisForDimensionIndex;
		if (defaultDataAxisIndex != null &&
				(defaultDataAxisIndex < 0 || defaultDataAxisIndex >= rank)) {
			throw new IllegalArgumentException(MessageFormat.format("Axis index must be between {0} and {1}, was {2}.", 0, rank, defaultDataAxisIndex));
		}

		// if the sourceFieldName and/or destinationFieldName are null, use the
		// default values from the nexus object provider
		if (sourceFieldName == null) {
			sourceFieldName = nexusObjectProvider.getDefaultDataFieldName();
			if (destinationFieldName == null) {
				destinationFieldName = nexusObjectProvider.getDefaultAxisName();
			}
		} else {
			if (destinationFieldName == null) {
				destinationFieldName = sourceFieldName;
			}
		}
		
		// get the data node to add
		final DataNode dataNode = getDataNode(nexusObjectProvider, sourceFieldName);
		
		// get the dimension mappings and validate them
		if (dimensionMappings.length != dataNode.getDataset().getRank()) {
			throw new IllegalArgumentException("The number of dimension mappings must equal the rank of the dataset for the field: " + sourceFieldName);
		}
		for (int dimensionMapping : dimensionMappings) {
			if (dimensionMapping < 0 || dimensionMapping >= rank) {
				throw new IllegalArgumentException(MessageFormat.format("Dimension mapping must be between {0} and {1}, was {2}.", 0, rank, dimensionMapping));
			}
		}
	
		// add new data node to the NXdata group
		nxData.addDataNode(destinationFieldName, dataNode);
	
		// if this is the primary device for an axis then update the
		// dataset holding the values for the 'axes' attribute
		if (defaultDataAxisIndex != null) {
			dimensionDefaultAxisNames.set(destinationFieldName, defaultDataAxisIndex);
		}
	
		// create the {axisname}_indices attribute of the NXdata group for this axis device
		final String attrName = destinationFieldName + ATTR_SUFFIX_INDICES;
		final IntegerDataset indicesDataset = new IntegerDataset(dimensionMappings.length);
		for (int i = 0; i < dimensionMappings.length; i++) {
			indicesDataset.setItem(dimensionMappings[i], i);
		}
		final Attribute axisIndicesAttribute = TreeFactory.createAttribute(attrName, indicesDataset, false);
		nxData.addAttribute(axisIndicesAttribute);
	}

}
