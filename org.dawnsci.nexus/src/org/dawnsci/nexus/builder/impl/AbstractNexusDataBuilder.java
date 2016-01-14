package org.dawnsci.nexus.builder.impl;

import java.text.MessageFormat;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.builder.NexusDataBuilder;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;

public abstract class AbstractNexusDataBuilder implements NexusDataBuilder {

	protected final NXdata nxData;

	protected final DefaultNexusEntryBuilder entryModel;

	/**
	 * Create a new {@link DefaultNexusDataBuilder}. This constructor should only be
	 * called by {@link DefaultNexusEntryBuilder}.
	 * @param entryModel parent entry model
	 * @param nxData {@link NXdata} object to wrap
	 */
	protected AbstractNexusDataBuilder(DefaultNexusEntryBuilder entryModel,
			final NXdata nxData) {
		this.entryModel = entryModel;
		this.nxData = nxData;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.NexusDataBuilder#getNexusData()
	 */
	@Override
	public NXdata getNxData() {
		return nxData;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.model.api.NexusDataModel#setDataDevice(org.eclipse.dawnsci.nexus.model.api.NexusObjectProvider)
	 */
	@Override
	public void setDataDevice(NexusObjectProvider<? extends NXobject> nexusObjectProvider) throws NexusException {
		setDataDevice(nexusObjectProvider, nexusObjectProvider.getName());
	}

	@Override
	public void addAxisDevice(
			NexusObjectProvider<? extends NXobject> nexusObjectProvider,
			int[] dimensionMappings) throws NexusException {
		addAxisDevice(nexusObjectProvider, null, null, dimensionMappings, null);
	}

	@Override
	public void addAxisDevice(
			NexusObjectProvider<? extends NXobject> nexusObjectProvider,
			String sourceFieldName, int[] dimensionMappings) throws NexusException {
		addAxisDevice(nexusObjectProvider, sourceFieldName, null, dimensionMappings, null);
	}

	@Override
	public void addAxisDevice(NexusObjectProvider<? extends NXobject> nexusObjectProvider,
			String sourceFieldName, String destinationFieldName, int[] dimensionMappings) throws NexusException {
		addAxisDevice(nexusObjectProvider, sourceFieldName, destinationFieldName, dimensionMappings, null);
	}
	
	@Override
	public void addAxisDevice(NexusObjectProvider<? extends NXobject> nexusObjectProvider,
			String sourceFieldName, String destinationFieldName, int[] dimensionMappings, int primaryAxisForDimensionIndex) throws NexusException {
		addAxisDevice(nexusObjectProvider, sourceFieldName, destinationFieldName, dimensionMappings, Integer.valueOf(primaryAxisForDimensionIndex));
	}
	
	@Override
	public void addAxisDevice(
			NexusObjectProvider<? extends NXobject> nexusObjectProvider,
			int[] dimensionMappings, int defaultAxisDimensionIndex)
			throws NexusException {
		addAxisDevice(nexusObjectProvider, null, null, dimensionMappings, defaultAxisDimensionIndex);
	}

	@Override
	public void addAxisDevice(
			NexusObjectProvider<? extends NXobject> nexusObjectProvider,
			String sourceFieldName, int[] dimensionMappings,
			int defaultAxisDimensionIndex) throws NexusException {
		addAxisDevice(nexusObjectProvider, sourceFieldName, null, dimensionMappings, defaultAxisDimensionIndex);
	}

	@Override
	public void addAxisDevice(
			NexusObjectProvider<? extends NXobject> nexusObjectProvider,
			String[] sourceFieldNames, int[] destinationMappings) throws NexusException {
		for (String sourceFieldName : sourceFieldNames) {
			addAxisDevice(nexusObjectProvider, sourceFieldName, destinationMappings);
		}
	}

	@Override
	public void addAxisDevice(
			NexusObjectProvider<? extends NXobject> nexusObjectProvider,
			String[] sourceFieldNames, int[] destinationMappings,
			String defaultAxisFieldName, int defaultAxisDimensionIndex)
			throws NexusException {
		for (String sourceFieldName : sourceFieldNames) {
			if (sourceFieldName.equals(defaultAxisFieldName)) {
				addAxisDevice(nexusObjectProvider, sourceFieldName, destinationMappings, defaultAxisDimensionIndex);
			} else {
				addAxisDevice(nexusObjectProvider, sourceFieldName, destinationMappings);
			}
		}
	}

	protected abstract void addAxisDevice(NexusObjectProvider<? extends NXobject> nexusObjectProvider,
			String sourceFieldName, String destinationFieldName, int[] dimensionMappings, Integer primaryAxisForDimensionIndex)
			throws NexusException;
	
	protected DataNode getDataNode(NexusObjectProvider<? extends NXobject> nexusObjectProvider,
			String fieldName) throws NexusException {
		final NXobject nexusObject = nexusObjectProvider.getNexusObject(entryModel.getNodeFactory(), true);
		if (fieldName == null) {
			fieldName = nexusObjectProvider.getDefaultDataFieldName();
		}
		final DataNode dataNode = nexusObject.getDataNode(fieldName);
		if (dataNode == null) {
			throw new IllegalArgumentException(MessageFormat.format("No such data node for group {0}: {1}",
					nexusObject.getNXclass().getSimpleName(), fieldName));
		}
	
		return dataNode;
	}

}
