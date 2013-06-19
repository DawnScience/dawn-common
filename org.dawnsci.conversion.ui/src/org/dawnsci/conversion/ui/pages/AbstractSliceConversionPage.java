package org.dawnsci.conversion.ui.pages;

import org.dawnsci.conversion.ui.IConversionWizardPage;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * AbstractSliceConversionPage which declares as IConversionWizardPage specifically for use
 * with the conversion wizard.
 * @author fcp94556
 *
 */
public abstract class AbstractSliceConversionPage extends org.dawb.common.ui.wizard.AbstractSliceConversionPage implements IConversionWizardPage {

	public AbstractSliceConversionPage(String pageName, String description, ImageDescriptor icon) {
		super(pageName, description, icon);
	}

}
