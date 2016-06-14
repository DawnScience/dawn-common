package org.dawnsci.persistence;

import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.dawnsci.json.MarshallerService;

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
		// For unit tests, OSGi won't find the service, so we directly implement it
		if (mservice == null)
			mservice = new MarshallerService();
		return mservice;
	}
}
