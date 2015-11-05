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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.hdf5.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusApplicationDefinition;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.impl.NXdataImpl;
import org.eclipse.dawnsci.nexus.impl.NXentryImpl;
import org.eclipse.dawnsci.nexus.impl.NXinstrumentImpl;
import org.eclipse.dawnsci.nexus.impl.NXobjectImpl;
import org.eclipse.dawnsci.nexus.impl.NXsampleImpl;
import org.eclipse.dawnsci.nexus.impl.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.model.api.CustomNexusTreeModification;
import org.eclipse.dawnsci.nexus.model.api.NexusApplicationDefinitionModel;
import org.eclipse.dawnsci.nexus.model.api.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.model.api.NexusDataModel;
import org.eclipse.dawnsci.nexus.model.api.NexusEntryModel;
import org.eclipse.dawnsci.nexus.model.api.NexusMetadataProvider;
import org.eclipse.dawnsci.nexus.model.api.NexusMetadataProvider.MetadataEntry;
import org.eclipse.dawnsci.nexus.model.api.NexusTreeModification;

/**
 * Default implementation of {@link NexusEntryModel}
 *
 */
public class DefaultNexusEntryModel implements NexusEntryModel {
	
	private final NexusNodeFactory nexusNodeFactory;

	private NXentryImpl nxEntry = null;

	private NXinstrumentImpl nxInstrument = null;

	private NXsampleImpl nxSample = null;
	
	private List<NXobject> defaultGroups = null;
	
	private Map<NexusObjectProvider<?>, NXobject> deviceBaseClassInstanceMap = new HashMap<>();
	
	public DefaultNexusEntryModel(final NexusNodeFactory nexusNodeFactory, final NXentryImpl nxEntry) {
		this.nexusNodeFactory = nexusNodeFactory;
		this.nxEntry = nxEntry;
	}

	@Override
	public <N extends NXobject> N addNexusObject(NexusObjectProvider<N> nexusAdapter) throws NexusException {
		N baseClassInstance = nexusAdapter.createNexusObject(nexusNodeFactory);
		addGroupToNexusTree(nexusAdapter, baseClassInstance);
		deviceBaseClassInstanceMap.put(nexusAdapter, baseClassInstance);

		return baseClassInstance;
	}

	@Override
	public NXentry getNxEntry() {
		return nxEntry;
	}

	public NexusNodeFactory getNodeFactory() {
		return nexusNodeFactory;
	}

	@Override
	public NexusDataModel createDefaultData() {
		NXdataImpl nxData = nexusNodeFactory.createNXdata();
		nxEntry.setData(nxData);
		return new DefaultNexusDataModel(this, nxData);
	}
	
	@Override
	public NexusDataModel newData(final String name) {
		NXdataImpl nxData = nexusNodeFactory.createNXdata();
		nxEntry.setData(name, nxData);
		return new DefaultNexusDataModel(this, nxData);
	}
	
	@Override
	public NexusApplicationDefinitionModel newApplicationDefinition(NexusApplicationDefinition appDef) {
		return ApplicationDefinitionFactory.getApplicationDefinitionFactory().newApplicationDefinitionModel(this, appDef);
	}

	@Override
	public List<NXobject> addNexusObjects(Collection<NexusObjectProvider<?>> nexusAdapters) throws NexusException {
		List<NXobject> nexusObjects = new ArrayList<NXobject>(nexusAdapters.size());
		for (NexusObjectProvider<?> nexusAdapter : nexusAdapters) {
			NXobject nexusObject = addNexusObject(nexusAdapter);
			nexusObjects.add(nexusObject);
		}
		
		return nexusObjects;
	}

	@Override
	public void modifyEntry(CustomNexusTreeModification modification) {
		modification.modifyEntry(nxEntry);
	}

	@Override
	public void modifyTree(CustomNexusTreeModification modification) {
		modification.modifyEntry(nxEntry);
	}
	
	@Override
	public void addMetadata(NexusMetadataProvider metadataProvider) throws NexusException {
		final NexusBaseClass category = metadataProvider.getCategory();
		NXobject group;
		if (category == null) {
			group = nxEntry;
		} else {
			group = findGroupForCategory(category);
		}
		
		Iterator<MetadataEntry> metadataEntryIterator = metadataProvider.getMetadataEntries();
		while (metadataEntryIterator.hasNext()) {
			final MetadataEntry entry = metadataEntryIterator.next();
			((NXobjectImpl) group).setField(entry.getName(), entry.getValue());
		}
	}

	@Override
	public void addNexusTreeModification(NexusTreeModification modification) throws NexusException {
		if (modification instanceof NexusObjectProvider) {
			addNexusObject((NexusObjectProvider<?>) modification);
		} else if (modification instanceof NexusMetadataProvider) {
			addMetadata((NexusMetadataProvider) modification);
		} else if (modification instanceof CustomNexusTreeModification) {
			modifyTree((CustomNexusTreeModification) modification);
		} else {
			throw new IllegalArgumentException("Unknown modification type: " + modification.getClass());
		}
	}

	@Override
	public void addNexusTreeModifications(
			Collection<NexusTreeModification> modifications) throws NexusException {
		for (NexusTreeModification modification : modifications) {
			addNexusTreeModification(modification);
		}
	}

	@Override
	public void setInstrumentName(String instrumentName) {
		nxInstrument.setNameScalar(instrumentName);
	}
	
	@Override
	public DataNode getDataNode(String relativePath) throws NexusException {
		NodeLink nodeLink = nxEntry.findNodeLink(relativePath);
		if (nodeLink == null) {
			throw new NexusException("Cannot find expected data node within the entry with relative path: " + relativePath);
		}
		if (!nodeLink.isDestinationData()) {
			throw new NexusException("Node found was not a data node, relative path within the entry: " + relativePath);
		}

		return (DataNode) nodeLink.getDestination();
	}

	protected <N extends NXobject> N getNexusBaseClassInstance(NexusObjectProvider<N> nexusObjectProvider)
			throws NexusException {
		@SuppressWarnings("unchecked")
		N baseClassInstance = (N) deviceBaseClassInstanceMap.get(nexusObjectProvider);
		if (baseClassInstance == null) {
			throw new NexusException("No NeXus base class instance for given device.");
		}
		
		return baseClassInstance;
	}
	
	/**
	 * Adds the default groups for the entry. Subclasses may override as appropriate.
	 * @return
	 */
	public void addDefaultGroups() {
		// TODO is this correct for all nexus trees we might want to create?
		// how do we configure it? (or just let subclasses override if they want?)
		defaultGroups = new ArrayList<>();
		defaultGroups.add(nxEntry);
		
		nxInstrument = nexusNodeFactory.createNXinstrument();
		defaultGroups.add(nxInstrument);
		nxEntry.setInstrument(nxInstrument);
	
		nxSample = nexusNodeFactory.createNXsample();
		defaultGroups.add(nxSample);
		nxEntry.setSample(nxSample);
	}

	/**
	 * Adds the new nexus object instance to the first skeleton class instance that it
	 * can be added to, unless category is specified, in which case it is added to the first
	 * element of that category that it can be added to.
	 * A special case is where the nexus object is of base class {@link NexusBaseClass#NX_SAMPLE},
	 * in which case this replaces the existing NXsample base class in the nexus tree.
	 * @param adapter
	 * @param nexusObject
	 * @throws NexusException 
	 */
	protected <N extends NXobject> void addGroupToNexusTree(NexusObjectProvider<N> adapter, N nexusObject) throws NexusException {
		if (nexusObject.getNexusBaseClass() == NexusBaseClass.NX_SAMPLE) {
			// special case for NXsample
			defaultGroups.remove(nxSample);
			nxSample = (NXsampleImpl) nexusObject;
			defaultGroups.add(nexusObject);
			nxEntry.setSample(nxSample);
		} else {
			// normal case
			final String name = adapter.getName();
			NexusBaseClass category = adapter.getDeviceCategory();
			
			NXobject parentGroup = null;
			if (category != null) {
				parentGroup = findGroupForCategory(category);
			} else {
				for (NXobject group : defaultGroups) {
					if (group.canAddChild(nexusObject)) {
						parentGroup = group;
						break;
					}
				}
				if (parentGroup == null) {
					throw new NexusException("Cannot find a parent group that accepts a " + nexusObject.getNexusBaseClass());
				}
			}
			
			parentGroup.addGroupNode(name, nexusObject);
		}
	}
	
	private NXobject findGroupForCategory(NexusBaseClass category) throws NexusException {
		for (NXobject group : defaultGroups) {
			if (category == group.getNexusBaseClass()) {
				return group;
			}
		}
		
		throw new NexusException("No group found for category " + category); 
	}

}
