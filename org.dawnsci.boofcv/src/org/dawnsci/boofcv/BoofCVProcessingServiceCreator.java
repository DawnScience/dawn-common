package org.dawnsci.boofcv;

import org.dawb.common.services.IImageProcessingService;
import org.dawnsci.boofcv.internal.BoofCVProcessingImpl;

/**
 * Class used to test the BoofCVProcessingImpl
 * @author wqk87977
 *
 * @internal only use in unit tests.
 */
public class BoofCVProcessingServiceCreator {

	public BoofCVProcessingServiceCreator(){
		
	}

	/**
	 * Used only for testing - DO NOT USE Externally. Instead get the Service by class.
	 * @return
	 */
	public static IImageProcessingService createPersistenceService(){
		return new BoofCVProcessingImpl();
	}
}