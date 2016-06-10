package org.dawnsci.persistence;

import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;

public class ServiceLoader {

	private static ILoaderService sloader;

	private static IMarshallerService mservice;

	/**
	 * Do nothing, loaded by OSGi
	 */
	public ServiceLoader() {
		
	}

	public static void setLoaderService(ILoaderService service) {
		sloader = service;
	}

	public static ILoaderService getLoaderService() {
		return sloader;
	}

	public static void setJSONMarshallerService(IMarshallerService js) {
		mservice = js;
	}

	public static IMarshallerService getJSONMarshallerService() {
		return mservice;
	}
}
