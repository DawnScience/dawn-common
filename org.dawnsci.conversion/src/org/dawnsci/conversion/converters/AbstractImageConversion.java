package org.dawnsci.conversion.converters;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawnsci.plotting.api.histogram.ImageServiceBean;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.function.Downsample;
import uk.ac.diamond.scisoft.analysis.dataset.function.DownsampleMode;

public abstract class AbstractImageConversion extends AbstractConversion {

	
	
	public AbstractImageConversion(IConversionContext context) {
		super(context);
	}

	/**
	 * Please override getExtension() if using getFileName(...)
	 * @return
	 */
	protected final String getFilePath(AbstractDataset slice) {
		final String fileName = getFileName(slice);
		return context.getOutputPath()+"/"+fileName;
	}

	private String getFileName(AbstractDataset slice) {
		String fileName = slice.getName();
		if (context.getUserObject()!=null) {
			ConversionInfoBean bean = (ConversionInfoBean)context.getUserObject();
			if (bean.getAlternativeNamePrefix()!=null) {
				final String nameFrag = fileName.substring(0, fileName.indexOf("(Dim"));
				fileName = fileName.replace(nameFrag, bean.getAlternativeNamePrefix());
			}
		}
		fileName = fileName.replace('\\', '_');
		fileName = fileName.replace('/', '_');
		fileName = fileName.replace(' ', '_');
		fileName = fileName.replace('(', '_');
		fileName = fileName.replaceAll("[^a-zA-Z0-9_]", "");
		return fileName+"."+getExtension();
	}

	/**
	 * Please override if using getFileName
	 * @return
	 */
	protected String getExtension() {
		return "tiff";
	}

	protected int getDownsampleBin() {
		if (context.getUserObject()==null) return 1;
        return ((ConversionInfoBean)context.getUserObject()).getDownsampleBin();
	}
	protected DownsampleMode getDownsampleMode() {
		if (context.getUserObject()==null) return DownsampleMode.MAXIMUM;
        return ((ConversionInfoBean)context.getUserObject()).getDownsampleMode();
	}
	
	protected AbstractDataset getDownsampled(AbstractDataset slice) {
		if (getDownsampleBin()>1) {
			final Downsample down = new Downsample(getDownsampleMode(), getDownsampleBin(), getDownsampleBin());
			slice = down.value(slice).get(0);
		}
		return slice;
	}
	
	public static final class ConversionInfoBean {
		private ImageServiceBean imageServiceBean;
		private int downsampleBin=1;
		private DownsampleMode downsampleMode=DownsampleMode.MAXIMUM;
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
			result = prime * result + downsampleBin;
			result = prime
					* result
					+ ((downsampleMode == null) ? 0 : downsampleMode.hashCode());
			result = prime * result
					+ ((extension == null) ? 0 : extension.hashCode());
			result = prime
					* result
					+ ((imageServiceBean == null) ? 0 : imageServiceBean
							.hashCode());
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
			ConversionInfoBean other = (ConversionInfoBean) obj;
			if (alternativeNamePrefix == null) {
				if (other.alternativeNamePrefix != null)
					return false;
			} else if (!alternativeNamePrefix
					.equals(other.alternativeNamePrefix))
				return false;
			if (bits != other.bits)
				return false;
			if (downsampleBin != other.downsampleBin)
				return false;
			if (downsampleMode != other.downsampleMode)
				return false;
			if (extension == null) {
				if (other.extension != null)
					return false;
			} else if (!extension.equals(other.extension))
				return false;
			if (imageServiceBean == null) {
				if (other.imageServiceBean != null)
					return false;
			} else if (!imageServiceBean.equals(other.imageServiceBean))
				return false;
			return true;
		}
		public String getAlternativeNamePrefix() {
			return alternativeNamePrefix;
		}
		public void setAlternativeNamePrefix(String alternativeNamePrefix) {
			this.alternativeNamePrefix = alternativeNamePrefix;
		}
		public int getDownsampleBin() {
			return downsampleBin;
		}
		public void setDownsampleBin(int downsampleBin) {
			this.downsampleBin = downsampleBin;
		}
		public DownsampleMode getDownsampleMode() {
			return downsampleMode;
		}
		public void setDownsampleMode(DownsampleMode downsampleMode) {
			this.downsampleMode = downsampleMode;
		}
		public ImageServiceBean getImageServiceBean() {
			return imageServiceBean;
		}
		public void setImageServiceBean(ImageServiceBean imageServiceBean) {
			this.imageServiceBean = imageServiceBean;
		}
	}

}
