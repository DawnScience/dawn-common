package org.dawnsci.conversion;

import org.dawb.common.services.IConversionContext;
import org.dawb.common.services.IConversionService;
import org.dawnsci.conversion.internal.AbstractConversion;
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
		AbstractConversion deligate=null;
		try {
			switch(context.getConversionScheme()) {
			case ASCII_FROM_2D:
				deligate = new AsciiConvert2D(context);
				break;
			case ASCII_FROM_1D:
				deligate = new AsciiConvert1D(context);
				break;
			case CUSTOM_NCD:
				deligate = new CustomNCDConverter(context);
				break;
			case TIFF_FROM_3D:
				deligate = new TiffConverter(context);
				break;
			}
			deligate.process(context);
		} finally {
			if (deligate!=null) deligate.close(context);
		}
	}

}
