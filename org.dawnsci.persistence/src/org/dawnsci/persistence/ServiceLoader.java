package org.dawnsci.persistence;

import org.eclipse.dawnsci.analysis.api.io.ILoaderService;

public class ServiceLoader {

	private static ILoaderService sloader;

	public ServiceLoader() {
		
	}

	public static void setLoaderService(ILoaderService service) {
		sloader = service;
	}

	public static ILoaderService getLoaderService() {
		return sloader;
	}
}
