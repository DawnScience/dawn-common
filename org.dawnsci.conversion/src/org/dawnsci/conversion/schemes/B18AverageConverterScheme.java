package org.dawnsci.conversion.schemes;

import org.dawnsci.conversion.converters.B18AverageConverter;

public class B18AverageConverterScheme extends AbstractConversionScheme {
	public B18AverageConverterScheme() {
		super(B18AverageConverter.class, " average B18 data", true, false, false, 1);
	}
}
