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
import java.util.stream.IntStream;

import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.dataset.impl.IntegerDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.StringDataset;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.builder.DataDevice;
import org.eclipse.dawnsci.nexus.builder.NexusDataBuilder;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;

/**
 * Default implementation of {@link NexusDataBuilder}.
 *
 */
public class DefaultNexusDataBuilder extends AbstractNexusDataBuilder implements NexusDataBuilder {

	private DataNode defaultDataNode = null;
	private StringDataset dimensionDefaultAxisNames;
	private String signalFieldName;

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

	private boolean isPrimaryDeviceAdded() {
		return defaultDataNode != null;
	}
	
	@Override
	public void setPrimaryDevice(DataDevice<?> primaryDeviceModel)
			throws NexusException {
		NexusObjectProvider<?> nexusObjectProvider = primaryDeviceModel.getNexusObjectProvider();
		String sourceFieldName = nexusObjectProvider.getDefaultWritableDataFieldName();
		String destinationFieldName = primaryDeviceModel.getDestinationFieldName(sourceFieldName);
		addSignalAndAxesAttributes(nexusObjectProvider, sourceFieldName, destinationFieldName);
		
		addDevice(primaryDeviceModel, true);
	}

	@Override
	public void addDataDevice(NexusObjectProvider<?> dataDevice,
			Integer defaultAxisDimension, int... dimensionMappings) throws NexusException {
		addDataDevice(new DataDevice<>(dataDevice, true, defaultAxisDimension, dimensionMappings));
	}

	@Override
	public void addDataDevice(DataDevice<?> dataDeviceModel) throws NexusException {
		if (!isPrimaryDeviceAdded()) {
			throw new IllegalStateException("The primary device has not been set.");
		}
		
		addDevice(dataDeviceModel, false);
	}

	private void addDevice(DataDevice<?> dataDeviceModel, boolean isPrimary) throws NexusException {
		NexusObjectProvider<?> nexusObjectProvider = dataDeviceModel.getNexusObjectProvider();
		
		for (String sourceFieldName : dataDeviceModel.getSourceFieldNames()) {
			String destinationFieldName = dataDeviceModel.getDestinationFieldName(sourceFieldName);
			int[] dimensionMappings = dataDeviceModel.getDimensionMappings(sourceFieldName);
			Integer defaultAxisDimension = dataDeviceModel.getDefaultAxisDimension(sourceFieldName);
			
			if (isPrimary) {
				// the primary device (i.e. a detector) may know its own dimension mappings for some fields
				// as it is the owner of the main dataset (i.e. that referred to by the @signal attribute)
				dimensionMappings = nexusObjectProvider.getDimensionMappings(sourceFieldName);
				if (defaultAxisDimension == null) {
					defaultAxisDimension = nexusObjectProvider.getDefaultAxisDimension(sourceFieldName);
				}
			}
			addDataField(nexusObjectProvider, sourceFieldName, destinationFieldName, defaultAxisDimension, dimensionMappings);
		}
	}
	
	private void addDeviceToDefaultAxes(int defaultAxisDimension, String destinationFieldName) {
		// if this is the default axis for a dimension then update the dataset for the 'axes'
		// attribute of the NXdata group
		if (defaultAxisDimension < 0 || defaultAxisDimension > dimensionDefaultAxisNames.getSize() - 1) {
			throw new IllegalArgumentException("Default axis dimension for device must be between 0 and " +
					dimensionDefaultAxisNames.getSize() + ", was: " + defaultAxisDimension);
		}
		
		dimensionDefaultAxisNames.set(destinationFieldName, defaultAxisDimension);
	}
	
	private <N extends NXobject> void addDataField(NexusObjectProvider<?> nexusObjectProvider,
			String sourceFieldName, String destinationFieldName, Integer defaultAxisDimension, int[] dimensionMappings) throws NexusException {
		// get the data node for the given field (throws exception if no such data node exists)
		DataNode dataNode = getDataNode(nexusObjectProvider, sourceFieldName);
		// TODO add @target attribute
		// check that there is not an existing node with the same name
		if (nxData.containsDataNode(destinationFieldName)) {
			throw new IllegalArgumentException("The NXdata element already contains a data node with the name: " + destinationFieldName);
		}
		// add the data node to the nxData group
		nxData.addDataNode(destinationFieldName, dataNode);
		
		// create the axis indices attribute
		if (!destinationFieldName.equals(signalFieldName)) {
			final Attribute axisIndicesAttribute = createAxisIndicesAttribute(
					sourceFieldName, destinationFieldName, defaultAxisDimension, dimensionMappings, dataNode);
			nxData.addAttribute(axisIndicesAttribute);
		}
		
		// add the axis dimension to the default axes
		if (defaultAxisDimension != null) {
			addDeviceToDefaultAxes(defaultAxisDimension, destinationFieldName);
		}
	}
	
	private void addSignalAndAxesAttributes(
			NexusObjectProvider<? extends NXobject> nexusObjectProvider,
			String sourceFieldName, String destinationFieldName) throws NexusException {
		if (isPrimaryDeviceAdded()) {
			throw new IllegalArgumentException("Primary device already added");
		}
		
		signalFieldName = destinationFieldName;
		final Attribute signalAttribute = TreeFactory.createAttribute(ATTR_NAME_SIGNAL, signalFieldName, false);
		nxData.addAttribute(signalAttribute);
		
		// create the 'axes' attribute of the NXgroup and set each axis name
		// to the placeholder value "."
		defaultDataNode = getDataNode(nexusObjectProvider, sourceFieldName);
		dimensionDefaultAxisNames = new StringDataset(defaultDataNode.getDataset().getRank());
		dimensionDefaultAxisNames.fill(NO_DEFAULT_AXIS_PLACEHOLDER);
		
		final Attribute axesAttribute = TreeFactory.createAttribute(ATTR_NAME_AXES, dimensionDefaultAxisNames, false);
		nxData.addAttribute(axesAttribute);
	}
	
	private Attribute createAxisIndicesAttribute(String sourceFieldName,
			String destinationFieldName, Integer defaultAxisDimension, int[] dimensionMappings, DataNode dataNode) {
		// validate the dimension mappings if specified
		int rank = dataNode.getRank();
		
		// if the default axis dimension is specified and the dataset has a rank of 1,
		// then this has to be the dimension mapping as well
		if (defaultAxisDimension != null && rank == 1) {
			dimensionMappings = new int[] { defaultAxisDimension };
		}
		
		if (dimensionMappings != null) {
			if (dimensionMappings.length == 0) {
				dimensionMappings = null;
			} else {
				validateDimensionMappings(sourceFieldName, dimensionMappings, rank);
			}
		}
		
		// create the {axisname}_indices attribute of the NXdata group for this axis device
		final String attrName = destinationFieldName + ATTR_SUFFIX_INDICES;
		final IntegerDataset indicesDataset = new IntegerDataset(rank);

		// set the dimension mappings into the dataset, if not set use 0, 1, 2, etc...
		final int[] finalDimensionMappings = dimensionMappings;
		IntStream.range(0, rank).forEach(i -> indicesDataset.setItem(
				finalDimensionMappings == null ? i : finalDimensionMappings[i], i));
		
		return TreeFactory.createAttribute(attrName, indicesDataset, false);
	}

	private void validateDimensionMappings(String sourceFieldName,
			int[] dimensionMappings, int rank) {
		// size of dimensionMappings must equal rank of the dataset to add
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

}
