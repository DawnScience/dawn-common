package org.dawnsci.conversion;

import java.awt.Dimension;
import java.io.File;

import org.dawb.common.services.IPlotImageService;
import org.dawb.common.services.ServiceManager;
import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.services.conversion.IConversionContext.ConversionScheme;
import org.dawb.common.ui.image.PaletteFactory;
import org.dawnsci.conversion.converters.AbstractImageConversion.ConversionInfoBean;
import org.dawnsci.plotting.api.histogram.HistogramBound;
import org.dawnsci.plotting.api.histogram.IImageService;
import org.dawnsci.plotting.api.histogram.IPaletteService;
import org.dawnsci.plotting.api.histogram.ImageServiceBean;
import org.dawnsci.plotting.api.histogram.ImageServiceBean.HistoType;
import org.dawnsci.plotting.api.histogram.ImageServiceBean.ImageOrigin;
import org.dawnsci.plotting.services.ImageService;
import org.dawnsci.plotting.services.PlotImageService;
import org.dawnsci.rcp.service.PaletteService;
import org.junit.Test;
import org.monte.media.avi.AVIReader;

import uk.ac.diamond.scisoft.analysis.dataset.function.DownsampleMode;

public class AVIImageConvertTest {

	
	@Test
	public void testAVISimple() throws Exception {
		
		ConversionServiceImpl service = new ConversionServiceImpl();
		
		// Not sure of this will work...
		ServiceManager.setService(IImageService.class,       new ImageService());
		ServiceManager.setService(IPlotImageService.class,   new PlotImageService());
		ServiceManager.setService(IPaletteService.class,     new PaletteService());
		
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
        info.setImageServiceBean(createTestingBean());
        info.setDownsampleBin(2);
        info.setDownsampleMode(DownsampleMode.MAXIMUM);
        context.setUserObject(info);
        
        service.process(context);
        
        // Check avi file
        final AVIReader reader = new AVIReader(avi);
        int trackCount = reader.getTrackCount();
        if (trackCount!=1) throw new Exception("Incorrect number of tracks!");
        Dimension d = reader.getVideoDimension();
        if (d.width!=1024) throw new Exception("Incorrect downsampling applied!");
        if (d.height!=1024) throw new Exception("Incorrect downsampling applied!");
        
        // Done
        System.out.println("Test passed, avi file written!");
   	}
	
	private ImageServiceBean createTestingBean() {
		ImageServiceBean imageServiceBean = new ImageServiceBean();
		imageServiceBean.setPalette(PaletteFactory.makeBluesPalette());
		imageServiceBean.setOrigin(ImageOrigin.TOP_LEFT);
		imageServiceBean.setHistogramType(HistoType.MEAN);
		imageServiceBean.setMinimumCutBound(HistogramBound.DEFAULT_MINIMUM);
		imageServiceBean.setMaximumCutBound(HistogramBound.DEFAULT_MAXIMUM);
		imageServiceBean.setNanBound(HistogramBound.DEFAULT_NAN);
		imageServiceBean.setLo(0);
		imageServiceBean.setHi(300);		
		
		return imageServiceBean;
	}

	private String getTestFilePath(String fileName) {
		
		final File test = new File("testfiles/"+fileName);
		return test.getAbsolutePath();
	
	}

}
