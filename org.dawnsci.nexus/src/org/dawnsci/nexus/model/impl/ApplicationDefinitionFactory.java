package org.dawnsci.nexus.model.impl;

import org.eclipse.dawnsci.nexus.NexusApplicationDefinition;
import org.eclipse.dawnsci.nexus.impl.NXentryImpl;
import org.eclipse.dawnsci.nexus.impl.NXsubentryImpl;
import org.eclipse.dawnsci.nexus.impl.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.model.api.NexusApplicationDefinitionFactory;
import org.eclipse.dawnsci.nexus.model.api.NexusApplicationDefinitionModel;
import org.eclipse.dawnsci.nexus.model.api.NexusEntryModel;

/**
 * Factory class for application definition subentries.
 *
 */
public class ApplicationDefinitionFactory implements NexusApplicationDefinitionFactory{
	
	private static final String APPDEF_SUBENTRY_SUFFIX = "_entry";
	
	private static final ApplicationDefinitionFactory INSTANCE = new ApplicationDefinitionFactory();

	private ApplicationDefinitionFactory() {
		// private constructor to prevent external instantiation
	}
	
	public static ApplicationDefinitionFactory getApplicationDefinitionFactory() {
		return INSTANCE;
	}

	@Override
	public NexusApplicationDefinitionModel newApplicationDefinitionModel(NexusEntryModel entryModel,
			NexusApplicationDefinition appDef) {
		NexusNodeFactory nodeFactory = entryModel.getNodeFactory();
		NXsubentryImpl nxSubentry = nodeFactory.createNXsubentry();
		NexusApplicationDefinitionModel appDefModel = null;
		switch (appDef) {
		case NX_TOMO:
			appDefModel = new TomoApplicationDefinitionModel(entryModel, nxSubentry);
			break;
		default:
			throw new IllegalArgumentException("Unsupported application definition: " + appDef);
		}

		final NXentryImpl nxEntry = (NXentryImpl) entryModel.getNxEntry();
		final String appDefName = appDef.name();
		final String subentryName = appDefName.substring(appDefName.indexOf('_') + 1).toLowerCase() + APPDEF_SUBENTRY_SUFFIX;
		nxEntry.setSubentry(subentryName, nxSubentry);

		return appDefModel;
	}

}
