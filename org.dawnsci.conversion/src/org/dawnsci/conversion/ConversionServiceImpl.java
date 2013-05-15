package org.dawnsci.conversion;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.services.conversion.IConversionService;
import org.dawnsci.conversion.converters.AVIImageConverter;
import org.dawnsci.conversion.converters.AbstractConversion;
import org.dawnsci.conversion.converters.AsciiConvert1D;
import org.dawnsci.conversion.converters.AsciiConvert2D;
import org.dawnsci.conversion.converters.CustomNCDConverter;
import org.dawnsci.conversion.converters.CustomTomoConverter;
import org.dawnsci.conversion.converters.ImageConverter;
import org.dawnsci.conversion.converters.VisitorConversion;

class ConversionServiceImpl implements IConversionService {
	
	@Override
	public IConversionContext open(String... paths) {
		ConversionContext context = new ConversionContext();
		context.setFilePaths(paths);
		return context;
	}

	@Override
	public void process(IConversionContext context) throws Exception {
		AbstractConversion deligate=null;
		try {
			if (context.getConversionVisitor()!=null) {
				deligate = new VisitorConversion(context);
			}
			if (deligate==null) {
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
					deligate = new ImageConverter(context);
					break;
				case AVI_FROM_3D:
					deligate = new AVIImageConverter(context);
					break;
				case CUSTOM_TOMO:
					deligate = new CustomTomoConverter(context);
					break;
				default:
					break;
				}
			}
			deligate.process(context);
		} catch (Exception e){
			e.printStackTrace();
		}
		finally {
			if (deligate!=null) deligate.close(context);
		}
	}

}
