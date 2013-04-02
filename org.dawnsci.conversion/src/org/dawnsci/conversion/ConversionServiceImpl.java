package org.dawnsci.conversion;

import org.dawb.common.services.IConversionContext;
import org.dawb.common.services.IConversionService;
import org.dawnsci.conversion.internal.AsciiConvert1D;
import org.dawnsci.conversion.internal.AsciiConvert2D;
import org.dawnsci.conversion.internal.CustomNCDConverter;
import org.dawnsci.conversion.internal.TiffConverter;

class ConversionServiceImpl implements IConversionService {

	@Override
	public IConversionContext open(String filePathRegEx) {
		ConversionContext context = new ConversionContext();
		context.setFilePath(filePathRegEx);
		return context;
	}

	@Override
	public void process(IConversionContext context) throws Exception {
		IConversionService deligate=null;
		switch(context.getConversionScheme()) {
		case ASCII_FROM_2D:
			deligate = new AsciiConvert2D();
			break;
		case ASCII_FROM_1D:
			deligate = new AsciiConvert1D();
			break;
		case CUSTOM_NCD:
			deligate = new CustomNCDConverter();
			break;
		case TIFF_FROM_3D:
			deligate = new TiffConverter();
			break;
		}
		deligate.process(context);
	}

}
