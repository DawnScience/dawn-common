package org.dawnsci.conversion.schemes;

import org.dawnsci.conversion.converters.ImagesToHDFConverter;

public class ImagesToHDFConverterScheme extends AbstractConversionScheme {
	public ImagesToHDFConverterScheme() {
		super(ImagesToHDFConverter.class, " nexus stack from directory of images", true, false, false, 2);
	}
}
