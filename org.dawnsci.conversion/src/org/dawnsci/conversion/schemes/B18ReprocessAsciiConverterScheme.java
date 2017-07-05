package org.dawnsci.conversion.schemes;

import org.dawnsci.conversion.converters.B18ReprocessAsciiConverter;

public class B18ReprocessAsciiConverterScheme extends AbstractConversionScheme {
	public B18ReprocessAsciiConverterScheme() {
		super(B18ReprocessAsciiConverter.class, " reprocess B18 ascii", true, false, false, 1);
	}
}
