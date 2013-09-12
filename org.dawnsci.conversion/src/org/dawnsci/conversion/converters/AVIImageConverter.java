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
import java.io.File;
import java.io.IOException;

import org.dawb.common.services.IPaletteService;
import org.dawb.common.services.IThumbnailService;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.monte.media.Format;
import org.monte.media.FormatKeys.MediaType;
import org.monte.media.avi.AVIWriter;
import org.monte.media.math.Rational;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

/**
 * Class to create a video of slices.
 * 
 * @author fcp94556
 *
 */
public class AVIImageConverter extends AbstractImageConversion {
	
	private static final Logger logger = LoggerFactory.getLogger(AVIImageConverter.class);

	private IImageService    service;

	/**
	 * dir where we put the temporary images which we will later write to video.
	 */	
	public AVIImageConverter(IConversionContext context) throws Exception {
		super(context);
		
		final File avi = new File(context.getOutputPath());
		if (!avi.exists()) {
			avi.getParentFile().mkdirs();
			try {
				if (context.getFilePaths().size()<2) {
					avi.createNewFile();
				} else {
					avi.mkdir();
				}
			} catch (Throwable ne) {
				logger.error("Cannot create file "+avi, ne);
			}
		}
		if (!avi.isFile() && context.getFilePaths().size()<2) {
			throw new RuntimeException("The output path must be a single file when converting one file!");
		}
		avi.getParentFile().mkdirs();
				
		this.service = (IImageService)ServiceManager.getService(IImageService.class);

	}

	private File       selected=null;
	private AVIWriter  out;
	private ImageServiceBean bean;
	private Point XYD_PLOT_SIZE = new Point(1025,768);
	
	/**
	 * This convert cannot be asynchronous. (Like most actually)
	 * @param slice
	 * @throws Exception
	 */
	@Override
	protected synchronized void convert(AbstractDataset slice) throws Exception {
		
		try {
		
			if (context.getMonitor()!=null && context.getMonitor().isCancelled()) {
				throw new Exception(getClass().getSimpleName()+" is cancelled");
			}
			slice = (slice.getRank()==2) ? getDownsampled(slice) : slice;

			boolean newAVIFile = selected==null || !selected.equals(context.getSelectedConversionFile());
			if (context.isExpression() && out!=null) newAVIFile=false;
			if (newAVIFile) {
				if (out!=null) out.close();

				final File outputFile = getAVIFile();
				this.out = new AVIWriter(outputFile);
							
				if (context.getMonitor()!=null) {
					final String selectedName = context.getSelectedConversionFile()!=null ? context.getSelectedConversionFile().getName() : "";
					context.getMonitor().subTask("Converting '"+selectedName+"' to '"+outputFile.getName()+"'");
				}
			}
			
			if (newAVIFile && slice.getRank()==2) {
				this.bean = createImageServiceBean();

				Format format = new Format(EncodingKey, ENCODING_AVI_MJPG, DepthKey, 24, QualityKey, 1f);
			    format = format.prepend(MediaTypeKey, MediaType.VIDEO, //
										FrameRateKey, new Rational(getFrameRate(), 1),// frame rate
										WidthKey,     slice.getShape()[1], //
										HeightKey,    slice.getShape()[0]);
				out.addTrack(format);
			}

			
			ImageData data=null;
			if (slice.getRank()==2) {
				bean.setImage(slice);
				bean.setDepth(8);
				data = service.getImageData(bean);
			} else {
				data = getXYImage(slice);
			}
			
			BufferedImage   img  = service.getBufferedImage(data);
			
			if (newAVIFile && slice.getRank()==1) {
				Format format = new Format(EncodingKey, ENCODING_AVI_MJPG, DepthKey, 24, QualityKey, 1f);
				format = format.prepend(MediaTypeKey, MediaType.VIDEO, //
						FrameRateKey, new Rational(getFrameRate(), 1),// frame rate
						WidthKey,     img.getWidth(), //
						HeightKey,    img.getHeight());

				out.addTrack(format);
			}
			
			
	        if (newAVIFile) {
	        	out.setPalette(0, img.getColorModel());	       
	        }
	        out.write(0, img, 1);
	        
	        if (context.getMonitor()!=null) context.getMonitor().worked(1);
	        
		} finally {
			selected = context.getSelectedConversionFile();
		}
	}
	
	private ImageData getXYImage(AbstractDataset slice) throws Exception {
		final IThumbnailService service = (IThumbnailService)ServiceManager.getService(IThumbnailService.class);	            		
		final Image image = service.getThumbnailImage(slice, XYD_PLOT_SIZE.x, XYD_PLOT_SIZE.y);
		ImageData data = image.getImageData();
		image.dispose();
		return data;
	}

	private File getAVIFile() {
		if (context.getFilePaths().size()<2 || context.getSelectedConversionFile()==null) {
			return new File(context.getOutputPath());
		} else {
			final String name = getFileNameNoExtension(context.getSelectedConversionFile());
			return new File(context.getOutputPath()+"/"+name+".avi");
		}
	}
	
	private long getFrameRate() {
		if (context.getUserObject()==null) return 1;
		return ((ConversionInfoBean)context.getUserObject()).getFrameRate();
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

}
