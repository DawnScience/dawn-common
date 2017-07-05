package org.dawnsci.conversion.schemes;

import org.dawnsci.conversion.converters.ProcessConversion;

public class ProcessConversionScheme extends AbstractConversionScheme {
	public ProcessConversionScheme() {
		super(ProcessConversion.class, " process data", false, true, 1,2,3,4,5);
	}
}
