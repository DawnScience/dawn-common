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
import java.util.List;

import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.dataset.impl.IntegerDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.StringDataset;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.builder.AxisDevice;
import org.eclipse.dawnsci.nexus.builder.NexusDataBuilder;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;

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
			final NXdata nxData) {
		super(entryModel, nxData);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.NexusDataBuilder#getNexusData()
	 */
	@Override
	public NXdata getNxData() {
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
	
	@Override
	public void addAxisDevice(AxisDevice<? extends NXobject> axisDevice)
			throws NexusException {
		NexusObjectProvider<? extends NXobject> nexusObjectProvider = axisDevice.getNexusObjectProvider();
		int[] dimensionMappings = axisDevice.getDimensionMappings();
		Integer defaultAxisDimension = axisDevice.getDefaultAxisDimension();
		String defaultAxisSourceFieldName = axisDevice.getDefaultAxisSourceFieldName();
		
		// call addDataField for each source field to add
		List<String> sourceFields = axisDevice.getSourceFieldNames();
		for (String sourceFieldName : sourceFields) {
			Integer fieldDefaultAxisDimension = null;
			if (sourceFieldName.equals(defaultAxisSourceFieldName)) {
				// this is the field that the default axis dimension applies to
				fieldDefaultAxisDimension = defaultAxisDimension;
			}
			String destinationField = axisDevice.getDestinationFieldName(sourceFieldName);
			addDataField(nexusObjectProvider, sourceFieldName, destinationField,
					dimensionMappings, fieldDefaultAxisDimension);
		}
		
		// add the default axis field to the dataset of default axes for the main dataset of the NXdata
		if (defaultAxisDimension != null) {
			addDeviceToDefaultAxes(axisDevice, defaultAxisDimension,
					defaultAxisSourceFieldName);
		}
	}

	private void addDeviceToDefaultAxes(
			AxisDevice<? extends NXobject> axisDevice,
			Integer defaultAxisDimension, String defaultAxisSourceFieldName) {
		String defaultAxisDestinationFieldName = axisDevice.getDestinationFieldName(defaultAxisSourceFieldName);
		// if this is the default axis for an dimension then update the dataset for the
		// 'axes' attribute of the NXdata group
		if (defaultAxisDimension < 0 || defaultAxisDimension >= dimensionDefaultAxisNames.getSize()) {
			throw new IllegalArgumentException("Default axis dimension for device must be between 0 and "
					+ dimensionDefaultAxisNames.getSize() + ", was: " + defaultAxisDimension);
		}
		
		dimensionDefaultAxisNames.set(defaultAxisDestinationFieldName, defaultAxisDimension);
	}
	
	private <N extends NXobject> void addDataField(
			NexusObjectProvider<? extends NXobject> nexusObjectProvider,
			String sourceFieldName, String destinationField,
			int[] dimensionMappings, Integer defaultAxisForDimension) throws NexusException {
		// get the data node for the given field (throws exception if no such data node exists)
		DataNode dataNode = getDataNode(nexusObjectProvider, sourceFieldName);
		// check if there is an existing data node with the same name
		if (nxData.containsDataNode(destinationField)) {
			throw new IllegalArgumentException("The NXdata element already contains a data node with the name: " + destinationField);
		}
		// add the data node to the nxData group
		nxData.addDataNode(destinationField, dataNode);

		// create the axis indices attribute
		final Attribute axisIndicesAttribute = createAxisIndicesAttribute(
				sourceFieldName, destinationField, dimensionMappings,
				defaultAxisForDimension, dataNode);
		nxData.addAttribute(axisIndicesAttribute);
	}

	private Attribute createAxisIndicesAttribute(String sourceFieldName,
			String destinationField, int[] dimensionMappings,
			Integer defaultAxisForDimension, DataNode dataNode) {
		if (defaultAxisForDimension != null && dataNode.getRank() == 1) {
			// if this is the demand field, has a rank of 1 and this device is the default axis for
			// some dimension of the main data field of the NXdata, then use this as the dimension mapping
			// (this overrides the dimension mapping provided
			dimensionMappings = new int[] { defaultAxisForDimension };
		}
		
		// if the dimension mappings are specified, check that they're valid
		int rank = dataNode.getRank();
		if (dimensionMappings != null) {
			// size of dimensionMappings array must not exceed rank of the dataset to add
			if (dimensionMappings.length != rank) {
				throw new IllegalArgumentException("The size of the dimension mappings array must equal the rank of the dataset for the field: " + sourceFieldName);
			}
			// each element of the dimensionMappings array must between 0 and the rank of the default data node of the NXdata group
			for (int dimensionMapping : dimensionMappings) {
				if (dimensionMapping < 0 || dimensionMapping >= defaultDataNode.getRank()) {
					throw new IllegalArgumentException(MessageFormat.format("Dimension mapping must be between {0} and {1}, was {2}.", 0, defaultDataNode.getRank(), dimensionMapping));
				}
			}
		}
		
		// create the {axisname}_indices attribute of the NXdata group for this axis device
		final String attrName = destinationField + ATTR_SUFFIX_INDICES;
		final IntegerDataset indicesDataset = new IntegerDataset(rank);
		for (int i = 0; i < rank; i++) { // rank == dimensionMappings.length if dimensionMappings not null
			// if dimension mappings not specified, default to 0, 1, 2, etc... (i.e. dimensions of scan)
			int dimensionMapping = dimensionMappings == null ? i : dimensionMappings[i]; 
			indicesDataset.setItem(dimensionMapping, i);
		}
		final Attribute axisIndicesAttribute = TreeFactory.createAttribute(attrName, indicesDataset, false);
		return axisIndicesAttribute;
	}

}
