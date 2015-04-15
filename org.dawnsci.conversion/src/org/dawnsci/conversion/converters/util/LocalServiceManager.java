package org.dawnsci.conversion.converters.util;

import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;

public class LocalServiceManager {
	
	private static ILoaderService lservice;
	private static IOperationService oservice;
	
	public static void setLoaderService(ILoaderService s) {
		lservice = s;
	}
	
	public static ILoaderService getLoaderService(){
		return lservice;
	}
	
	public static void setOperationService(IOperationService s) {
		oservice = s;
	}
	
	public static IOperationService getOperationService() {
		return oservice;
	}

}
