package org.dawnsci.conversion.schemes;

import org.dawnsci.conversion.converters.AlignImagesConverter;

public class AlignImagesConverterScheme extends AbstractConversionScheme {
	public AlignImagesConverterScheme() {
		super(AlignImagesConverter.class, " align stack of images", true, false, 2, 3, 4, 5);
	}
}
