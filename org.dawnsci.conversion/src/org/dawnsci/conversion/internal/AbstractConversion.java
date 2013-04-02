package org.dawnsci.conversion.internal;

import org.dawb.common.services.IConversionContext;
import org.dawb.common.services.IConversionService;

abstract class AbstractConversion implements IConversionService {

	@Override
	public IConversionContext open(String filePathRegEx) {
		throw new RuntimeException("Please use this class only with the process(...) method!");
	}

}
