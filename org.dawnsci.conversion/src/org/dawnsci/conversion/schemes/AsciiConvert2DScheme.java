package org.dawnsci.conversion.schemes;

import org.dawnsci.conversion.converters.AsciiConvert2D;

public class AsciiConvert2DScheme extends AbstractConversionScheme {
	public AsciiConvert2DScheme() {
		super(AsciiConvert2D.class, " ascii from 2D data",   false, 2);
	}
}
