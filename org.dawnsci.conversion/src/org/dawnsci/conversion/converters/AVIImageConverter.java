package org.dawnsci.conversion.converters;

import static org.monte.media.FormatKeys.EncodingKey;
import static org.monte.media.FormatKeys.FrameRateKey;
import static org.monte.media.FormatKeys.MediaTypeKey;
import static org.monte.media.VideoFormatKeys.DepthKey;
import static org.monte.media.VideoFormatKeys.ENCODING_AVI_MJPG;
import static org.monte.media.VideoFormatKeys.HeightKey;
import static org.monte.media.VideoFormatKeys.QualityKey;
import static org.monte.media.VideoFormatKeys.WidthKey;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dawb.common.services.IPaletteService;
import org.dawb.common.services.ServiceManager;
import org.dawb.common.services.conversion.IConversionContext;
import org.dawnsci.plotting.api.histogram.HistogramBound;
import org.dawnsci.plotting.api.histogram.IImageService;
import org.dawnsci.plotting.api.histogram.ImageServiceBean;
import org.dawnsci.plotting.api.histogram.ImageServiceBean.HistoType;
import org.dawnsci.plotting.api.histogram.ImageServiceBean.ImageOrigin;
import org.dawnsci.plotting.api.preferences.BasePlottingConstants;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.monte.media.Format;
import org.monte.media.FormatKeys.MediaType;
import org.monte.media.avi.AVIWriter;
import org.monte.media.math.Rational;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

/**
 * Class to create a video of slices.
 * 
 * @author fcp94556
 *
 */
public class AVIImageConverter extends AbstractImageConversion {
	
	private static final Logger logger = LoggerFactory.getLogger(AVIImageConverter.class);

	private AVIWriter        out;
	private IImageService    service;
	private ImageServiceBean bean;

	/**
	 * dir where we put the temporary images which we will later write to video.
	 */	
	public AVIImageConverter(IConversionContext context) throws Exception {
		super(context);
		
		final File avi = new File(context.getOutputPath());
		if (!avi.isFile()) throw new RuntimeException("The output path must be a file!");
		avi.getParentFile().mkdirs();
		
		this.out = new AVIWriter(avi);
		this.service = (IImageService)ServiceManager.getService(IImageService.class);
		this.bean = createImageServiceBean();

	}

	private boolean first=true;
	
	@Override
	protected void convert(AbstractDataset slice) throws Exception {
		
		try {
		
			slice = getDownsampled(slice);

			if (first) {
				Format format = new Format(EncodingKey, ENCODING_AVI_MJPG, DepthKey, 24, QualityKey, 1f);
				format = format.prepend(MediaTypeKey, MediaType.VIDEO, //
										FrameRateKey, new Rational(1, 1),//
										WidthKey,     slice.getShape()[1], //
										HeightKey,    slice.getShape()[0]);

				out.addTrack(format);
			}
			
			bean.setImage(slice);
			bean.setDepth(8);
			
			final ImageData data = service.getImageData(bean);
			BufferedImage   img  = service.getBufferedImage(data);
	        if (first) {
	        	out.setPalette(0, img.getColorModel());	       
	        }
	        out.write(0, img, 1);
	        
	        if (context.getMonitor()!=null) context.getMonitor().worked(1);
	        
		} finally {
			first = false;
		}
	}
	private ImageServiceBean createImageServiceBean() {
		
		if (context.getUserObject()!=null && ((ConversionInfoBean)context.getUserObject()).getImageServiceBean()!=null) {
			return ((ConversionInfoBean)context.getUserObject()).getImageServiceBean();
		}
		ImageServiceBean imageServiceBean = new ImageServiceBean();
		try {
			final IPaletteService pservice = (IPaletteService)ServiceManager.getService(IPaletteService.class);
			String scheme = getPreferenceStore().getString(BasePlottingConstants.COLOUR_SCHEME);
			if (scheme == null || "".equals(scheme)) scheme = "Jet (Blue-Cyan-Green-Yellow-Red)";
			PaletteData pdata = pservice.getPaletteData(scheme);
			imageServiceBean.setPalette(pdata);	
		} catch (Exception e) {
			logger.error("Cannot create palette!", e);
		}	
		imageServiceBean.setOrigin(ImageOrigin.forLabel(getPreferenceStore().getString(BasePlottingConstants.ORIGIN_PREF)));
		imageServiceBean.setHistogramType(HistoType.forLabel(getPreferenceStore().getString(BasePlottingConstants.HISTO_PREF)));
		imageServiceBean.setMinimumCutBound(HistogramBound.fromString(getPreferenceStore().getString(BasePlottingConstants.MIN_CUT)));
		imageServiceBean.setMaximumCutBound(HistogramBound.fromString(getPreferenceStore().getString(BasePlottingConstants.MAX_CUT)));
		imageServiceBean.setNanBound(HistogramBound.fromString(getPreferenceStore().getString(BasePlottingConstants.NAN_CUT)));
		imageServiceBean.setLo(getPreferenceStore().getDouble(BasePlottingConstants.HISTO_LO));
		imageServiceBean.setHi(getPreferenceStore().getDouble(BasePlottingConstants.HISTO_HI));		
		
		return imageServiceBean;
	}
	
	private IPreferenceStore store;
	private IPreferenceStore getPreferenceStore() {
		if (store!=null) return store;
		store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawnsci.plotting");
		return store;
	}

	@Override
	public void close(IConversionContext context) throws IOException {
        out.close();
	}
	
	private int getBits() {
		if (context.getUserObject()==null) return 24;
		return ((ConversionInfoBean)context.getUserObject()).getBits();
	}
	

}
