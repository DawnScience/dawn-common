package org.dawnsci.conversion.ui;

import org.dawb.common.services.conversion.IConversionContext;
import org.eclipse.jface.wizard.IWizardPage;

public interface IConversionWizardPage extends IWizardPage {
	/**
	 * 
	 * @return the context, including any modifications (which includes path changes and 
	 * the setting of user objects)
	 */
	public abstract IConversionContext getContext();
	
	/**
	 * 
	 * @param context
	 */
	public abstract void setContext(IConversionContext context);

	/**
	 * Should open the converted file after the conversion has happened.
	 * @return
	 */
	public boolean isOpen();
}
