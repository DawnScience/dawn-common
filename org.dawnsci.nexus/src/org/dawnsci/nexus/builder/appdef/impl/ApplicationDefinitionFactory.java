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

package org.dawnsci.nexus.builder.appdef.impl;

import org.eclipse.dawnsci.hdf5.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusApplicationDefinition;
import org.eclipse.dawnsci.nexus.builder.NexusEntryBuilder;
import org.eclipse.dawnsci.nexus.builder.appdef.NexusApplicationBuilder;
import org.eclipse.dawnsci.nexus.builder.appdef.NexusApplicationFactory;
import org.eclipse.dawnsci.nexus.impl.NXentryImpl;
import org.eclipse.dawnsci.nexus.impl.NXsubentryImpl;
import org.eclipse.dawnsci.nexus.impl.NexusNodeFactory;

/**
 * Factory class for application definition subentries.
 *
 */
public class ApplicationDefinitionFactory implements NexusApplicationFactory{
	
	private static final String APPDEF_SUBENTRY_SUFFIX = "_entry";
	
	private static final ApplicationDefinitionFactory INSTANCE = new ApplicationDefinitionFactory();

	private ApplicationDefinitionFactory() {
		// private constructor to prevent external instantiation
	}
	
	/**
	 * Returns the singleton instance of this class.
	 * @return singleton instance of this class
	 */
	public static ApplicationDefinitionFactory getApplicationDefinitionFactory() {
		return INSTANCE;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.builder.appdef.NexusApplicationFactory#newApplicationDefinitionModel(org.eclipse.dawnsci.nexus.builder.NexusEntryBuilder, org.eclipse.dawnsci.nexus.NexusApplicationDefinition)
	 */
	@Override
	public NexusApplicationBuilder newApplicationDefinitionModel(NexusEntryBuilder entryModel,
			NexusApplicationDefinition appDef) throws NexusException {
		NexusNodeFactory nodeFactory = entryModel.getNodeFactory();
		NXsubentryImpl nxSubentry = nodeFactory.createNXsubentry();
		NexusApplicationBuilder appDefModel = null;
		switch (appDef) {
		case NX_TOMO:
			appDefModel = new TomoApplicationBuilder(entryModel, nxSubentry);
			break;
		default:
			throw new NexusException("Unsupported application definition: " + appDef);
		}

		final NXentryImpl nxEntry = (NXentryImpl) entryModel.getNxEntry();
		final String appDefName = appDef.name();
		final String subentryName = appDefName.substring(appDefName.indexOf('_') + 1).toLowerCase() + APPDEF_SUBENTRY_SUFFIX;
		if (nxEntry.containsGroupNode(subentryName)) {
			throw new NexusException("A subentry with the name " + subentryName + " already exists in this entry.");
		}
		nxEntry.setSubentry(subentryName, nxSubentry);

		return appDefModel;
	}

}
