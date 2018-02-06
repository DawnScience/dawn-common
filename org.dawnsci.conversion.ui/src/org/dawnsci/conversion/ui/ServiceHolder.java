package org.dawnsci.conversion.ui;

import org.dawnsci.conversion.ui.api.IConversionWizardPageService;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;

/**
 * This class is used to inject the Loader service using OSGI and retrieve in ConvertWizard
 *
 */
public class ServiceHolder {

	private static ILoaderService service;
	private static IConversionWizardPageService conversionPageService;
	
	/**
	 * Used for OSGI injection
	 */
	public ServiceHolder() {
		
	}

	/**
	 * Injected by OSGI
	 * @param ls
	 */
	public void setLoaderService(ILoaderService ls) {
		service = ls;
	}

	public static ILoaderService getLoaderService() {
		return service;
	}
	
	public void setConversionWizardPageService(IConversionWizardPageService conversionPageService) {
		ServiceHolder.conversionPageService = conversionPageService;
	}
	
	public static IConversionWizardPageService getConversionWizardPageService() {
		return conversionPageService;
	}
}
