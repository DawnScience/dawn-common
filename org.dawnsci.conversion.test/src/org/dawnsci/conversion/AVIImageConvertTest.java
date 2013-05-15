package org.dawnsci.conversion;

import java.awt.Dimension;
import java.io.File;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.services.conversion.IConversionContext.ConversionScheme;
import org.dawnsci.conversion.converters.AbstractImageConversion.ConversionInfoBean;
import org.junit.Test;
import org.monte.media.avi.AVIReader;
import org.monte.media.math.Rational;

import uk.ac.diamond.scisoft.analysis.dataset.function.DownsampleMode;


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
        info.setDownsampleBin(2);
        info.setDownsampleMode(DownsampleMode.MEAN);
        context.setUserObject(info);
        
        service.process(context);
        
        // Check avi file
        final AVIReader reader = new AVIReader(avi);
        int trackCount = reader.getTrackCount();
        if (trackCount!=1) throw new Exception("Incorrect number of tracks!");
        Rational r = reader.getDuration(0);
        if (r.getNumerator()!=4) throw new Exception("Incorrect number of frames!");
        Dimension d = reader.getVideoDimension();
        if (d.width!=1024) throw new Exception("Incorrect downsampling applied!");
        if (d.height!=1024) throw new Exception("Incorrect downsampling applied!");
        
        // Done
        System.out.println("Test passed, avi file written!");
   	}
	
	private String getTestFilePath(String fileName) {
		
		final File test = new File("testfiles/"+fileName);
		return test.getAbsolutePath();
	
	}

}
