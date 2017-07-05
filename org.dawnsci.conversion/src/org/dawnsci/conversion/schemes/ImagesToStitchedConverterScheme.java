package org.dawnsci.conversion.schemes;

import org.dawnsci.conversion.converters.ImagesToStitchedConverter;

public class ImagesToStitchedConverterScheme extends AbstractConversionScheme {
	public ImagesToStitchedConverterScheme() {
		super(ImagesToStitchedConverter.class, " stitched/mosaic image from directory of images", true, false, 1, 2);
	}
}
