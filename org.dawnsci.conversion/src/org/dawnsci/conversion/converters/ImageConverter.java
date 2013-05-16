package org.dawnsci.conversion.converters;

import java.io.File;

import org.dawb.common.services.conversion.IConversionContext;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.JavaImageSaver;

/**
 * Class to save tiff slices out from an hdf file.
 * 
 * @author fcp94556
 *
 * Mark I suggest that you extend this class either with a sublcass or a mode
 * of this class to process the other parts required.
 * 
 * Please add a Test to TiffConvertTest or similar.
 */
public class ImageConverter extends AbstractImageConversion {

	public ImageConverter(IConversionContext context) {
		super(context);
		
		final File dir = new File(context.getOutputPath());
		dir.mkdirs();
	}

	@Override
	protected void convert(AbstractDataset slice) throws Exception {
		
		if (context.getMonitor()!=null && context.getMonitor().isCancelled()) {
			throw new Exception(getClass().getSimpleName()+" is cancelled");
		}
		slice = getDownsampled(slice);

		final File sliceFile = new File(getFilePath(slice));
		if (!sliceFile.getParentFile().exists()) sliceFile.getParentFile().mkdirs();
		final JavaImageSaver saver = new JavaImageSaver(sliceFile.getAbsolutePath(), getExtension(), getBits(), true);
		final DataHolder     dh    = new DataHolder();
		dh.addDataset(slice.getName(), slice);
		saver.saveFile(dh);
        if (context.getMonitor()!=null) context.getMonitor().worked(1);
	}
	@Override
	public void close(IConversionContext context) {
        
	}

	protected String getExtension() {
		if (context.getUserObject()==null) return "tiff";
		return ((ConversionInfoBean)context.getUserObject()).getExtension();
	}

	private int getBits() {
		if (context.getUserObject()==null) return 33;
		return ((ConversionInfoBean)context.getUserObject()).getBits();
	}
	
	

}
