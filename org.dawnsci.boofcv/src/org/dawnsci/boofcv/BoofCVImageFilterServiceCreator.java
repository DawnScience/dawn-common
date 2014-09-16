package org.dawnsci.boofcv;

import org.dawb.common.services.IImageFilterService;
import org.dawnsci.boofcv.internal.BoofCVImageFilterImpl;

/**
 * Class used to test the BoofCVImageFilterImpl
 * @author wqk87977
 *
 * @internal only use in unit tests.
 */
public class BoofCVImageFilterServiceCreator {

	public BoofCVImageFilterServiceCreator(){
		
	}

	/**
	 * Used only for testing - DO NOT USE Externally. Instead get the Service by class.
	 * @return
	 */
	public static IImageFilterService createPersistenceService(){
		return new BoofCVImageFilterImpl();
	}
}