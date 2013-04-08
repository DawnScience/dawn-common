package org.dawnsci.conversion;

import java.util.HashMap;
import java.util.Map;

import org.dawb.common.services.IConversionContext;

import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;

class ConversionContext implements IConversionContext {

	private ConversionScheme    conversionScheme;
	private String              filePath;
	private String              datasetName;
	private String              outputFolder;
	private Map<Integer,String> sliceDimensions;
	private Object              userObject;
	private IMonitor            monitor;
	
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
	public String getOutputPath() {
		return outputFolder;
	}
	public void setOutputPath(String outputFolder) {
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
		result = prime * result
				+ ((sliceDimensions == null) ? 0 : sliceDimensions.hashCode());
		result = prime * result
				+ ((userObject == null) ? 0 : userObject.hashCode());
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
		if (sliceDimensions == null) {
			if (other.sliceDimensions != null)
				return false;
		} else if (!sliceDimensions.equals(other.sliceDimensions))
			return false;
		if (userObject == null) {
			if (other.userObject != null)
				return false;
		} else if (!userObject.equals(other.userObject))
			return false;
		return true;
	}	

	
	/**
	 * 
	 * @param dim
	 * @param sliceString either an integer to hold the dimension constant or
	 * a range of the form "start:end" where start is the start index and end is
	 * the end index or the string "all" to use the size of the dataset (start=0,
	 * end-length dimension).
	 */
	public void addSliceDimension(int dim, String sliceString) {
		if (sliceDimensions == null) sliceDimensions = new HashMap<Integer,String>(7);
		sliceDimensions.put(dim, sliceString);
	}
	/**
	 * 
	 * @return the dimensions to slice in, may be null, in which case no slice done.
	 */
	public Map<Integer, String> getSliceDimensions() {
		return sliceDimensions;
	}
	public Object getUserObject() {
		return userObject;
	}
	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}
	@Override
	public IMonitor getMonitor() {
		return monitor;
	}
	@Override
	public void getMonitor(IMonitor monitor) {
		this.monitor = monitor;
	}

}
