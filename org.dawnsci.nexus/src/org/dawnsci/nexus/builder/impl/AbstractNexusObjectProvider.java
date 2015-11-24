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

import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.impl.NexusNodeFactory;

/**
 * Abstract implementation of {@link NexusObjectProvider}.
 *
 * @param <N> nexus base class type, a subinterface of {@link NXobject}
 */
public abstract class AbstractNexusObjectProvider<N extends NXobject> implements
		NexusObjectProvider<N> {

	public static final String DEFAULT_DATA_NODE_NAME = "data";
	
	protected N nexusObject = null;
	
	protected final String name;
	
	protected final NexusBaseClass nexusBaseClass;
	
	protected final String dataNodeName;
	
	protected NexusBaseClass category;
	
	private boolean useDeviceNameAsAxisName = false;
	
	private String axisName = null;
	
	public AbstractNexusObjectProvider(NexusBaseClass nexusBaseClass) {
		this(getDefaultName(nexusBaseClass), nexusBaseClass);
	}
	
	private static String getDefaultName(NexusBaseClass nexusBaseClass) {
		// the default name is the base class name without the initial 'NX',
		// e.g. for 'NXpositioner' the default name is 'positioner'
		return nexusBaseClass.toString().substring(2);
	}
	
	/**
	 * Creates a new {@link AbstractNexusObjectProvider} for given name and base class type
	 * @param name name
	 * @param nexusBaseClass base class type
	 */
	public AbstractNexusObjectProvider(String name, NexusBaseClass nexusBaseClass) {
		this(name, nexusBaseClass, DEFAULT_DATA_NODE_NAME, null);
	}
	
	/**
	 * Creates a new {@link AbstractNexusObjectProvider} for given name, base class type
	 * and data node name.
	 * @param name name
	 * @param nexusBaseClass base class type
	 */
	public AbstractNexusObjectProvider(String name, NexusBaseClass nexusBaseClass,
			String dataNodeName) {
		this(name, nexusBaseClass, dataNodeName, null);
	}
	
	/**
	 * Creates a new {@link AbstractNexusObjectProvider} for given name, base class type,
	 * data node name and category.
	 * @param name name
	 * @param nexusBaseClass base class type
	 */
	public AbstractNexusObjectProvider(String name, NexusBaseClass nexusBaseClass,
			String dataNodeName, NexusBaseClass category) {
		this.name = name;
		this.nexusBaseClass = nexusBaseClass;
		this.dataNodeName = dataNodeName;
		this.category = category;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.NexusObjectProvider#createNexusObject(org.eclipse.dawnsci.nexus.impl.NexusNodeFactory)
	 */
	@Override
	public final N createNexusObject(NexusNodeFactory nodeFactory) {
		this.nexusObject = doCreateNexusObject(nodeFactory); 
		return nexusObject;
	}
	
	/**
	 * Creates the nexus object for this {@link NexusObjectProvider} using the
	 * given {@link NexusNodeFactory}.
	 * @param nodeFactory node factory
	 * @return new nexus object
	 */
	protected abstract N doCreateNexusObject(NexusNodeFactory nodeFactory);
	
	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.NexusObjectProvider#getNexusObject()
	 */
	@Override
	public final N getNexusObject() {
		return nexusObject;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.NexusObjectProvider#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.NexusObjectProvider#getNexusBaseClass()
	 */
	@Override
	public NexusBaseClass getNexusBaseClass() {
		return nexusBaseClass;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.NexusObjectProvider#getDefaultDataFieldName()
	 */
	@Override
	public String getDefaultDataFieldName() {
		return dataNodeName;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.NexusObjectProvider#getDefaultAxisName()
	 */
	public String getDefaultAxisName() {
		if (axisName != null) {
			return axisName;
		}
		
		if (useDeviceNameAsAxisName) {
			return getName();
		}
		
		return getDefaultDataFieldName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.NexusObjectProvider#getCategory()
	 */
	@Override
	public NexusBaseClass getCategory() {
		return category;
	}
	
	public AbstractNexusObjectProvider<N> setCategory(NexusBaseClass category) {
		this.category = category;
		return this;
	}
	
	public AbstractNexusObjectProvider<N> useDeviceNameAsAxisName(boolean useDeviceNameAsAxisName) {
		this.useDeviceNameAsAxisName = useDeviceNameAsAxisName;
		this.axisName = null;
		return this;
	}
	
	public AbstractNexusObjectProvider<N> setAxisName(String axisName) {
		this.axisName = axisName;
		return this;
	}

}
