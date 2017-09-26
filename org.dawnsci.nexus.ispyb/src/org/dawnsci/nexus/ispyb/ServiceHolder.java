package org.dawnsci.nexus.ispyb;

import org.eclipse.dawnsci.nexus.INexusFileFactory;

import uk.ac.diamond.ispyb.api.IspybDataCollectionApi;
import uk.ac.diamond.ispyb.api.IspybDataCollectionFactoryService;
import uk.ac.diamond.ispyb.api.IspybFactoryService;

public class ServiceHolder {
	
	private static IspybDataCollectionFactoryService ispybDCFactory;
	private static INexusFileFactory nexusFactory;
	
	public static IspybDataCollectionFactoryService getIspybDataCollectionFactory() {
		return ispybDCFactory;
	}
	
	public static INexusFileFactory getNexusFactory() {
		return nexusFactory;
	}
	
	public static void setNexusFactory(INexusFileFactory nexus) {
		nexusFactory = nexus;
	}
	
	public static void setIspybDataCollectionFactory(IspybDataCollectionFactoryService ispyb) {
		ispybDCFactory = ispyb;
	}
	
}
