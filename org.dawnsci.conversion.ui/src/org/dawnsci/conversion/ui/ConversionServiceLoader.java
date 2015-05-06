package org.dawnsci.conversion.ui;

import org.eclipse.dawnsci.analysis.api.conversion.IConversionService;

/**
 * This class is used to inject the conversion service using OSGI and retrieve in ConvertWizard
 * @author wqk87977
 *
 */
public class ConversionServiceLoader {

	private static IConversionService service;

	/**
	 * Used for OSGI injection
	 */
	public ConversionServiceLoader() {
		
	}

	/**
	 * Injected by OSGI
	 * @param cs
	 */
	public static void setService(IConversionService cs) {
		service = cs;
	}

	public static IConversionService getService() {
		return service;
	}

}
