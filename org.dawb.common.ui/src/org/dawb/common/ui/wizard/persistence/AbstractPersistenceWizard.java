/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.common.ui.wizard.persistence;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jface.wizard.Wizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPersistenceWizard extends Wizard {

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
