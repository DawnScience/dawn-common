package org.dawnsci.conversion.schemes;

import org.dawnsci.conversion.converters.CustomNCDConverter;

public class CustomNCDConverterScheme extends AbstractConversionScheme {
	public CustomNCDConverterScheme() {
		super(CustomNCDConverter.class, " ascii from NCD data", true, 1,2,3,4,5,6);
	}
}
