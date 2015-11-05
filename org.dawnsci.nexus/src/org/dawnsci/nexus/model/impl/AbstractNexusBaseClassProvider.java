package org.dawnsci.nexus.model.impl;

import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.impl.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.model.api.NexusObjectProvider;

public abstract class AbstractNexusBaseClassProvider<N extends NXobject> implements
		NexusObjectProvider<N> {

	public static final String DEFAULT_DATA_NODE_NAME = "data";
	
	protected N baseClassInstance = null;
	
	protected final String name;
	
	protected final NexusBaseClass nexusBaseClass;
	
	protected final String dataNodeName;
	
	protected final NexusBaseClass category;
	
	public AbstractNexusBaseClassProvider(String name, NexusBaseClass nexusBaseClass) {
		this(name, nexusBaseClass, DEFAULT_DATA_NODE_NAME, null);
	}
	
	public AbstractNexusBaseClassProvider(String name, NexusBaseClass nexusBaseClass,
			String dataNodeName) {
		this(name, nexusBaseClass, dataNodeName, null);
	}
	
	public AbstractNexusBaseClassProvider(String name, NexusBaseClass nexusBaseClass,
			String dataNodeName, NexusBaseClass category) {
		this.name = name;
		this.nexusBaseClass = nexusBaseClass;
		this.dataNodeName = dataNodeName;
		this.category = category;
	}
	
	@Override
	public final N createNexusObject(NexusNodeFactory nodeFactory) {
		this.baseClassInstance = doCreateNexusBaseClassInstance(nodeFactory); 
		return baseClassInstance;
	}
	
	protected abstract N doCreateNexusBaseClassInstance(NexusNodeFactory nodeFactory);
	
	@Override
	public final N getNexusObject() {
		return baseClassInstance;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public NexusBaseClass getNexusBaseClass() {
		return nexusBaseClass;
	}

	@Override
	public String getDefaultDataNodeName() {
		return dataNodeName;
	}

	@Override
	public NexusBaseClass getDeviceCategory() {
		return category;
	}
	
	

}
