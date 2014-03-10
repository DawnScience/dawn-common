package org.dawb.common.ui.wizard.persistence;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jface.wizard.Wizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPersistenceWizard extends Wizard {

	protected static final Logger logger = LoggerFactory.getLogger(PersistenceExportWizard.class);
	
	public AbstractPersistenceWizard() {
		
	}

	protected Map<String, Boolean> createDefaultOptions() {
		final Map<String, Boolean> options = new LinkedHashMap<String, Boolean>(3);
		options.put(PersistWizardConstants.ORIGINAL_DATA, true);
		options.put(PersistWizardConstants.IMAGE_HIST,    true);
		options.put(PersistWizardConstants.MASK,          true);
		options.put(PersistWizardConstants.REGIONS,       true);
		options.put(PersistWizardConstants.FUNCTIONS,     true);
		options.put(PersistWizardConstants.DIFF_META,     true);
		return options;
	}

}
