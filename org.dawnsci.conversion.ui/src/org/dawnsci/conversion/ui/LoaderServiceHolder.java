package org.dawnsci.conversion.ui;

import org.eclipse.dawnsci.analysis.api.io.ILoaderService;

/**
 * This class is used to inject the Loader service using OSGI and retrieve in ConvertWizard
 *
 */
public class LoaderServiceHolder {

	private static ILoaderService service;

	/**
	 * Used for OSGI injection
	 */
	public LoaderServiceHolder() {
		
	}

	/**
	 * Injected by OSGI
	 * @param ls
	 */
	public static void setLoaderService(ILoaderService ls) {
		service = ls;
	}

	public static ILoaderService getLoaderService() {
		return service;
	}
}
