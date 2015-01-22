package org.dawnsci.conversion.converters;

import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean;

import uk.ac.diamond.scisoft.analysis.dataset.function.DownsampleMode;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * To be used as the user object to convey data about the conversion.
 * @author Matthew Gerring
 *
 */
public class ConversionInfoBean {
	
	// Main data
	private PlotType sliceType=PlotType.IMAGE;
	private int frameRate = 20;
	private int downsampleBin=1;
	private DownsampleMode downsampleMode=DownsampleMode.MAXIMUM;
	private String alternativeNamePrefix;
	private String extension = "tiff";
	private int    bits      = 33;
	private String sliceIndexFormat = "#000";
	private boolean alwaysShowTitle = false;
	
	// Optional stuff which does not appear in Json strings.
	@JsonIgnore
	private ImageServiceBean imageServiceBean;

	
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
		result = prime * result + (alwaysShowTitle ? 1231 : 1237);
		result = prime * result + bits;
		result = prime * result + downsampleBin;
		result = prime
				* result
				+ ((downsampleMode == null) ? 0 : downsampleMode.hashCode());
		result = prime * result
				+ ((extension == null) ? 0 : extension.hashCode());
		result = prime * result + frameRate;
		result = prime
				* result
				+ ((imageServiceBean == null) ? 0 : imageServiceBean
						.hashCode());
		result = prime
				* result
				+ ((sliceIndexFormat == null) ? 0 : sliceIndexFormat
						.hashCode());
		result = prime * result
				+ ((sliceType == null) ? 0 : sliceType.hashCode());
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
		if (alwaysShowTitle != other.alwaysShowTitle)
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
		if (frameRate != other.frameRate)
			return false;
		if (imageServiceBean == null) {
			if (other.imageServiceBean != null)
				return false;
		} else if (!imageServiceBean.equals(other.imageServiceBean))
			return false;
		if (sliceIndexFormat == null) {
			if (other.sliceIndexFormat != null)
				return false;
		} else if (!sliceIndexFormat.equals(other.sliceIndexFormat))
			return false;
		if (sliceType == null) {
			if (other.sliceType != null)
				return false;
		} else if (!sliceType.equals(other.sliceType))
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
	public int getFrameRate() {
		return frameRate;
	}
	public void setFrameRate(int frameRate) {
		this.frameRate = frameRate;
	}
	public String getSliceIndexFormat() {
		return sliceIndexFormat;
	}
	public void setSliceIndexFormat(String sliceIndexFormat) {
		this.sliceIndexFormat = sliceIndexFormat;
	}
	public PlotType getSliceType() {
		return sliceType;
	}
	public void setSliceType(PlotType sliceType) {
		this.sliceType = sliceType;
	}
	public boolean isAlwaysShowTitle() {
		return alwaysShowTitle;
	}
	public void setAlwaysShowTitle(boolean alwaysShowTitle) {
		this.alwaysShowTitle = alwaysShowTitle;
	}
}