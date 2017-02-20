package org.dawnsci.persistence;

import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.dawnsci.nexus.INexusFileFactory;

public class ServiceLoader {

	private static ILoaderService sloader;

	private static IMarshallerService mservice;

	private static INexusFileFactory nexusFactory;

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

	public static INexusFileFactory getNexusFactory() {
		return nexusFactory;
	}

	public static void setNexusFactory(INexusFileFactory nf) {
		nexusFactory = nf;
	}
}
