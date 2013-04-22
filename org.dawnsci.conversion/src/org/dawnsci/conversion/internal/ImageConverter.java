package org.dawnsci.conversion.internal;

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
public class ImageConverter extends AbstractConversion {

	public ImageConverter(IConversionContext context) {
		super(context);
	}

	@Override
	protected void convert(AbstractDataset slice) throws Exception {
		
		final JavaImageSaver saver = new JavaImageSaver(getFileName(slice), getExtension(), getBits(), true);
		final DataHolder     dh    = new DataHolder();
		dh.addDataset(slice.getName(), slice);
		saver.saveFile(dh);

	}
	@Override
	public void close(IConversionContext context) {
        
	}
	
	private String getFileName(AbstractDataset slice) {
		String fileName = slice.getName();
		if (context.getUserObject()!=null) {
			TiffInfoBean bean = (TiffInfoBean)context.getUserObject();
			if (bean.getAlternativeNamePrefix()!=null) {
				fileName = fileName.replace(fileName, bean.getAlternativeNamePrefix());
			}
		}
		fileName = fileName.replace('\\', '_');
		fileName = fileName.replace('/', '_');
		fileName = fileName.replace(' ', '_');
		fileName = fileName.replaceAll("[^a-zA-Z0-9_]", "");
		return context.getOutputPath()+"/"+fileName+"."+getExtension();
	}

	private String getExtension() {
		if (context.getUserObject()==null) return "tiff";
		return ((TiffInfoBean)context.getUserObject()).getExtension();
	}

	private int getBits() {
		if (context.getUserObject()==null) return 33;
		return ((TiffInfoBean)context.getUserObject()).getBits();
	}
	
	
	public static final class TiffInfoBean {
		private String alternativeNamePrefix;
		private String extension = "tiff";
		private int    bits      = 33;
		public String getExtension() {
			return extension;
		}
		public void setExtension(String extension) {
			this.extension = extension;
		}
		public int getBits() {
			return bits;
		}
		public void setBits(int bits) {
			this.bits = bits;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((alternativeNamePrefix == null) ? 0
							: alternativeNamePrefix.hashCode());
			result = prime * result + bits;
			result = prime * result
					+ ((extension == null) ? 0 : extension.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TiffInfoBean other = (TiffInfoBean) obj;
			if (alternativeNamePrefix == null) {
				if (other.alternativeNamePrefix != null)
					return false;
			} else if (!alternativeNamePrefix
					.equals(other.alternativeNamePrefix))
				return false;
			if (bits != other.bits)
				return false;
			if (extension == null) {
				if (other.extension != null)
					return false;
			} else if (!extension.equals(other.extension))
				return false;
			return true;
		}
		public String getAlternativeNamePrefix() {
			return alternativeNamePrefix;
		}
		public void setAlternativeNamePrefix(String alternativeNamePrefix) {
			this.alternativeNamePrefix = alternativeNamePrefix;
		}
	}

}
