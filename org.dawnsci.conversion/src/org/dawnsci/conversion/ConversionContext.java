package org.dawnsci.conversion;

import org.dawb.common.services.IConversionContext;

class ConversionContext implements IConversionContext {

	private ConversionScheme conversionScheme;
	private String           filePath;
	private String           datasetName;
	private String           outputFolder;
	
	public ConversionScheme getConversionScheme() {
		return conversionScheme;
	}
	public void setConversionScheme(ConversionScheme conversionScheme) {
		this.conversionScheme = conversionScheme;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getDatasetName() {
		return datasetName;
	}
	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}
	public String getOutputFolder() {
		return outputFolder;
	}
	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((conversionScheme == null) ? 0 : conversionScheme.hashCode());
		result = prime * result
				+ ((datasetName == null) ? 0 : datasetName.hashCode());
		result = prime * result
				+ ((filePath == null) ? 0 : filePath.hashCode());
		result = prime * result
				+ ((outputFolder == null) ? 0 : outputFolder.hashCode());
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
		ConversionContext other = (ConversionContext) obj;
		if (conversionScheme != other.conversionScheme)
			return false;
		if (datasetName == null) {
			if (other.datasetName != null)
				return false;
		} else if (!datasetName.equals(other.datasetName))
			return false;
		if (filePath == null) {
			if (other.filePath != null)
				return false;
		} else if (!filePath.equals(other.filePath))
			return false;
		if (outputFolder == null) {
			if (other.outputFolder != null)
				return false;
		} else if (!outputFolder.equals(other.outputFolder))
			return false;
		return true;
	}	

}
