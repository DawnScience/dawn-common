package org.dawnsci.conversion;

import java.io.File;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.services.conversion.IConversionContext.ConversionScheme;
import org.dawnsci.conversion.converters.AbstractImageConversion.ConversionInfoBean;
import org.junit.Test;


public class AVIImageConvertTest {

	
	@Test
	public void testAVISimple() throws Exception {
		
		ConversionServiceImpl service = new ConversionServiceImpl();
		
		// Determine path to test file
		final String path = getTestFilePath("export.h5");
		
		final IConversionContext context = service.open(path);
		final File avi = File.createTempFile("test_video", ".avi");
		avi.deleteOnExit();
        context.setOutputPath(avi.getAbsolutePath());
        context.setConversionScheme(ConversionScheme.AVI_FROM_3D);
        context.setDatasetName("/entry/edf/data");
        context.addSliceDimension(0, "all");
        
        final ConversionInfoBean info = new ConversionInfoBean();
        info.setBits(24);
        info.setDownsampleBin(2);
        context.setUserObject(info);
        
        service.process(context);
        
        System.out.println(avi);
   	}
	
	private String getTestFilePath(String fileName) {
		
		final File test = new File("test/org/dawnsci/conversion/"+fileName);
		return test.getAbsolutePath();
	
	}

}
