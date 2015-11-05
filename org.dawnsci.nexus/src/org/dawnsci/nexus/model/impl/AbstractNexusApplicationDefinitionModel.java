package org.dawnsci.nexus.model.impl;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.hdf5.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NXsubentry;
import org.eclipse.dawnsci.nexus.NexusApplicationDefinition;
import org.eclipse.dawnsci.nexus.impl.NXsubentryImpl;
import org.eclipse.dawnsci.nexus.impl.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.model.api.NexusApplicationDefinitionModel;
import org.eclipse.dawnsci.nexus.model.api.NexusEntryModel;

public abstract class AbstractNexusApplicationDefinitionModel implements
		NexusApplicationDefinitionModel {
	
	protected final NexusEntryModel nexusEntryModel;
	
	protected final NXsubentryImpl subentry;
	
	public AbstractNexusApplicationDefinitionModel(final NexusApplicationDefinition appDef, 
			final NexusEntryModel nexusEntryModel, final NXsubentryImpl subentry) {
		this.nexusEntryModel = nexusEntryModel;
		this.subentry = subentry;

		subentry.setDefinitionScalar(getAppDefName(appDef));
	}
	
	protected String getAppDefName(NexusApplicationDefinition appDef) {
		final String appDefEnumName = appDef.name();
		if (!appDefEnumName.startsWith("NX_")) {
			// sanity check that app def begins with 'NX_' - highly unlikely
			throw new RuntimeException("Application definition name doesn't begin with NX_");
		}
		
		return appDefEnumName.substring(0, 2) + appDefEnumName.substring(3);
	}
	
	@Override
	public NXsubentry getNXsubentry() {
		return subentry;
	}
	
	protected NexusNodeFactory getNexusNodeFactory() {
		return nexusEntryModel.getNodeFactory();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.model.api.NexusApplicationDefinitionModel#getDataNode(java.lang.String)
	 */
	@Override
	public DataNode getDataNode(String relativePath) throws NexusException {
		NodeLink nodeLink = subentry.findNodeLink(relativePath);
		if (nodeLink == null) {
			throw new NexusException("Cannot find expected data node within the subentry with relative path: " + relativePath);
		}
		if (!nodeLink.isDestinationData()) {
			throw new NexusException("Node found was not a data node, relative path within the subentry: " + relativePath);
		}

		return (DataNode) nodeLink.getDestination();
	}
	
}
