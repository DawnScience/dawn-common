package org.dawnsci.nexus;

import org.eclipse.dawnsci.nexus.INexusFileFactory;

public class ServiceHolder {
	
	private static INexusFileFactory nexusFileFactory;
	
	public static INexusFileFactory getNexusFileFactory() {
		return nexusFileFactory;
	}
	
	public static void setNexusFileFactory(INexusFileFactory nexusFileFactory) {
		ServiceHolder.nexusFileFactory = nexusFileFactory;
	}

}
