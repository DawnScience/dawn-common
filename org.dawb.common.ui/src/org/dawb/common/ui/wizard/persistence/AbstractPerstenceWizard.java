package org.dawb.common.ui.wizard.persistence;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jface.wizard.Wizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPerstenceWizard extends Wizard {

	protected static final Logger logger = LoggerFactory.getLogger(PersistenceExportWizard.class);
	
	public AbstractPerstenceWizard() {
		
	}


	protected Map<String, Boolean> createDefaultOptions() {
		final Map<String, Boolean> options = new LinkedHashMap<String, Boolean>(3);
		options.put("Original Data",        true);
		options.put("Mask",                 true);
		options.put("Regions",              true);
		options.put("Functions",            true);
		options.put("Diffraction Meta Data",true);
		return options;
	}

}
