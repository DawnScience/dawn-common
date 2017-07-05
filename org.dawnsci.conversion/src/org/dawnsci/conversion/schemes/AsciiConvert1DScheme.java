package org.dawnsci.conversion.schemes;

import org.dawnsci.conversion.converters.AsciiConvert1D;

public class AsciiConvert1DScheme extends AbstractConversionScheme {
	public AsciiConvert1DScheme() {
		super(AsciiConvert1D.class, " ascii from 1D data", true,  1);
	}
}
