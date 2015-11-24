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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dawnsci.nexus.builder.appdef.impl.ApplicationDefinitionFactory;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.hdf5.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusApplicationDefinition;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.builder.CustomNexusEntryModification;
import org.eclipse.dawnsci.nexus.builder.NexusDataBuilder;
import org.eclipse.dawnsci.nexus.builder.NexusEntryBuilder;
import org.eclipse.dawnsci.nexus.builder.NexusEntryModification;
import org.eclipse.dawnsci.nexus.builder.NexusMetadataProvider;
import org.eclipse.dawnsci.nexus.builder.NexusMetadataProvider.MetadataEntry;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.appdef.NexusApplicationBuilder;
import org.eclipse.dawnsci.nexus.impl.NXdataImpl;
import org.eclipse.dawnsci.nexus.impl.NXentryImpl;
import org.eclipse.dawnsci.nexus.impl.NXinstrumentImpl;
import org.eclipse.dawnsci.nexus.impl.NXobjectImpl;
import org.eclipse.dawnsci.nexus.impl.NXsampleImpl;
import org.eclipse.dawnsci.nexus.impl.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.validation.NexusValidationException;

/**
 * Default implementation of {@link NexusEntryBuilder}
 */
public class DefaultNexusEntryBuilder implements NexusEntryBuilder {
	
	private final NexusNodeFactory nexusNodeFactory;

	private NXentryImpl nxEntry = null;

	private NXinstrumentImpl nxInstrument = null;

	private NXsampleImpl nxSample = null;
	
	private List<NXobject> defaultGroups = null;
	
	private Map<NexusObjectProvider<?>, NXobject> nexusObjectByProviderMap = new HashMap<>();
	
	private List<NexusApplicationBuilder> appDefs = new ArrayList<>();
	
	/**
	 * Creates a new {@link DefaultNexusEntryBuilder}. This constructor should only be called
	 * by {@link DefaultNexusFileBuilder}.
	 * @param nexusNodeFactory node factory
	 * @param nxEntry entry to wrap
	 */
	protected DefaultNexusEntryBuilder(final NexusNodeFactory nexusNodeFactory, final NXentryImpl nxEntry) {
		this.nexusNodeFactory = nexusNodeFactory;
		this.nxEntry = nxEntry;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.NexusEntryBuilder#add(org.eclipse.dawnsci.nexus.builder.NexusObjectProvider)
	 */
	@Override
	public <N extends NXobject> N add(NexusObjectProvider<N> nexusAdapter) throws NexusException {
		N baseClassInstance = nexusAdapter.createNexusObject(nexusNodeFactory);
		addGroupToNexusTree(nexusAdapter, baseClassInstance);
		nexusObjectByProviderMap.put(nexusAdapter, baseClassInstance);

		return baseClassInstance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.NexusEntryBuilder#getNxEntry()
	 */
	@Override
	public NXentryImpl getNXentry() {
		return nxEntry;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.NexusEntryBuilder#getNodeFactory()
	 */
	public NexusNodeFactory getNodeFactory() {
		return nexusNodeFactory;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.NexusEntryBuilder#createDefaultData()
	 */
	@Override
	public NexusDataBuilder createDefaultData() {
		NXdataImpl nxData = nexusNodeFactory.createNXdata();
		nxEntry.setData(nxData);
		return new DefaultNexusDataBuilder(this, nxData);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.NexusEntryBuilder#newData(java.lang.String)
	 */
	@Override
	public NexusDataBuilder newData(final String name) {
		NXdataImpl nxData = nexusNodeFactory.createNXdata();
		nxEntry.setData(name, nxData);
		return new DefaultNexusDataBuilder(this, nxData);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.NexusEntryBuilder#newApplication(org.eclipse.dawnsci.nexus.NexusApplicationDefinition)
	 */
	@Override
	public NexusApplicationBuilder newApplication(NexusApplicationDefinition appDef) throws NexusException {
		NexusApplicationBuilder appDefModel = ApplicationDefinitionFactory.getApplicationDefinitionFactory().newApplicationDefinitionModel(this, appDef);
		appDefs.add(appDefModel);
		
		return appDefModel;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.NexusEntryBuilder#add(java.util.Collection)
	 */
	@Override
	public List<NXobject> add(Collection<NexusObjectProvider<?>> nexusAdapters) throws NexusException {
		List<NXobject> nexusObjects = new ArrayList<NXobject>(nexusAdapters.size());
		for (NexusObjectProvider<?> nexusAdapter : nexusAdapters) {
			NXobject nexusObject = add(nexusAdapter);
			nexusObjects.add(nexusObject);
		}
		
		return nexusObjects;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.NexusEntryBuilder#modifyEntry(org.eclipse.dawnsci.nexus.builder.CustomNexusEntryModification)
	 */
	@Override
	public void modifyEntry(CustomNexusEntryModification modification) {
		modification.modifyEntry(nxEntry);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.NexusEntryBuilder#addMetadata(org.eclipse.dawnsci.nexus.builder.NexusMetadataProvider)
	 */
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

	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.NexusEntryBuilder#modifyEntry(org.eclipse.dawnsci.nexus.builder.NexusEntryModification)
	 */
	@Override
	public void modifyEntry(NexusEntryModification modification) throws NexusException {
		if (modification instanceof NexusObjectProvider) {
			add((NexusObjectProvider<?>) modification);
		} else if (modification instanceof NexusMetadataProvider) {
			addMetadata((NexusMetadataProvider) modification);
		} else if (modification instanceof CustomNexusEntryModification) {
			modifyEntry((CustomNexusEntryModification) modification);
		} else {
			throw new IllegalArgumentException("Unknown modification type: " + modification.getClass());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.NexusEntryBuilder#modifyEntry(java.util.Collection)
	 */
	@Override
	public void modifyEntry(
			Collection<NexusEntryModification> modifications) throws NexusException {
		for (NexusEntryModification modification : modifications) {
			modifyEntry(modification);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.NexusEntryBuilder#setInstrumentName(java.lang.String)
	 */
	@Override
	public void setInstrumentName(String instrumentName) {
		nxInstrument.setNameScalar(instrumentName);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.NexusEntryBuilder#getDataNode(java.lang.String)
	 */
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

	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.NexusEntryBuilder#validate()
	 */
	@Override
	public void validate() throws NexusValidationException {
		for (NexusApplicationBuilder appDef : appDefs) {
			appDef.validate();
		}
	}

	/**
	 * Returns the nexus object for the given provider, assuming that the
	 * nexus object has already been added to this entry.
	 * @param nexusObjectProvider
	 * @return nexus object
	 * @throws NexusException
	 */
	protected <N extends NXobject> N getNexusObject(NexusObjectProvider<N> nexusObjectProvider)
			throws NexusException {
		@SuppressWarnings("unchecked")
		N baseClassInstance = (N) nexusObjectByProviderMap.get(nexusObjectProvider);
		if (baseClassInstance == null) {
			throw new NexusException("No NeXus base class instance for given device.");
		}
		
		return baseClassInstance;
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
			NexusBaseClass category = adapter.getCategory();
			
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
		if (category == NexusBaseClass.NX_ENTRY) {
			return nxEntry;
		}
		
		for (NXobject group : defaultGroups) {
			if (category == group.getNexusBaseClass()) {
				return group;
			}
		}
		
		throw new NexusException("No group found for category " + category); 
	}

}
