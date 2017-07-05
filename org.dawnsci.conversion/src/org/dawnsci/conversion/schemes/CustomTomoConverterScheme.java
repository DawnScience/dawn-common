package org.dawnsci.conversion.schemes;

import org.dawnsci.conversion.converters.CustomTomoConverter;

public class CustomTomoConverterScheme extends AbstractConversionScheme {
	public CustomTomoConverterScheme() {
		super(CustomTomoConverter.class, " tiff from tomography nexus file(s) [nxtomo]", true, 3);
	}
}
