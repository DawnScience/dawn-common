package org.dawnsci.nexus.builder.impl;

import java.text.MessageFormat;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.builder.AxisDevice;
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
	public void addAxisDevice(NexusObjectProvider<? extends NXobject> nexusObjectProvider) throws NexusException {
		addAxisDevice(new AxisDevice<>(nexusObjectProvider));
	}

	@Override
	public void addAxisDevice(
			NexusObjectProvider<? extends NXobject> nexusObjectProvider, int defaultAxisDimensionIndex) throws NexusException {
		addAxisDevice(new AxisDevice<>(nexusObjectProvider, defaultAxisDimensionIndex));
	}

	@Override
	public void addAxisDevice(
			NexusObjectProvider<? extends NXobject> nexusObjectProvider,
			int[] dimensionMappings) throws NexusException {
		addAxisDevice(new AxisDevice<>(nexusObjectProvider, dimensionMappings));
	}

	@Override
	public void addAxisDevice(
			NexusObjectProvider<? extends NXobject> nexusObjectProvider,
			int defaultAxisDimensionIndex, int[] dimensionMappings)
			throws NexusException {
		addAxisDevice(new AxisDevice<>(nexusObjectProvider, defaultAxisDimensionIndex, dimensionMappings));
	}

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
