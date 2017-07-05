package org.dawnsci.conversion.schemes;

import org.dawnsci.conversion.converters.CompareConverter;

public class CompareConverterScheme extends AbstractConversionScheme {
	public CompareConverterScheme() {
		super(CompareConverter.class, " compare data",    true,  false, 0,1,2,3,4,5);
	}
}
