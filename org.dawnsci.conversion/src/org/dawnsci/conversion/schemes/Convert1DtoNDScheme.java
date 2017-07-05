package org.dawnsci.conversion.schemes;

import org.dawnsci.conversion.converters.Convert1DtoND;

public class Convert1DtoNDScheme extends AbstractConversionScheme {
	public Convert1DtoNDScheme() {
		super(Convert1DtoND.class, " nexus from 1D data",   true, false,  1);
	}
}
