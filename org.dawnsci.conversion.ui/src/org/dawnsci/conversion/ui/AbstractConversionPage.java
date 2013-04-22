package org.dawnsci.conversion.ui;

import org.eclipse.jface.wizard.WizardPage;

/**
 * 
 * Class to be used to add custom wizard pages to the conversion wizard.
 * 
 * @author fcp94556
 *
 */
public abstract class AbstractConversionPage extends WizardPage implements IConversionWizardPage {

	protected AbstractConversionPage(String pageName) {
		super(pageName);
	}
	
	@Override
	public boolean isOpen() {
		return false;
	}
}
