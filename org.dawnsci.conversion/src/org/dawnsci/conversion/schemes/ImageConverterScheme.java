package org.dawnsci.conversion.schemes;

import org.dawnsci.conversion.converters.ImageConverter;

public class ImageConverterScheme extends AbstractConversionScheme {
	public ImageConverterScheme() {
		super(ImageConverter.class, " image files from image stack", true, false, 2,3,4,5);
	}
}
