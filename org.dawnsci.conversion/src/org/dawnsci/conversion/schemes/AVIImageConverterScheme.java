package org.dawnsci.conversion.schemes;

import org.dawnsci.conversion.converters.AVIImageConverter;

public class AVIImageConverterScheme extends AbstractConversionScheme {
	public AVIImageConverterScheme() {
		super(AVIImageConverter.class, " video from image stack", true, 2,3,4,5);
	}
}
