/*-
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dawb.common.services;

import java.util.Map;

/**
 * The conversion context which will drive what we are going to convert.
 */
public interface IConversionContext {

	/**
	 * Schemes to be edited as required, current list is a guess
	 * of the conversions we have spoken about before.
	 */
	public enum ConversionScheme {
		ASCII_FROM_2D("Convert to ascii from 2D data"), 
		ASCII_FROM_1D("Convert to ascii from 1D data"), 
		CUSTOM_NCD("Convert to ascii from NCD data"), 
		TIFF_FROM_3D("Convert to tiff from image stack");
		
		private String uiLabel;

		ConversionScheme(String uiLabel) {
			this.uiLabel = uiLabel;
		}

		public String getUiLabel() {
			return uiLabel;
		}
		
		public ConversionScheme fromLabel(String uiLabel) {
			for (ConversionScheme cs : values()) {
				if (cs.getUiLabel().equals(uiLabel)) return cs;
			}
			return null;
		}
	}
	
	/**
	 * Get the current conversion.
	 * @return
	 */
	public ConversionScheme getConversionScheme();
	
	/**
	 * Set the way in which we will convert
	 * @param cs
	 */
	public void setConversionScheme(ConversionScheme cs);
	
	/**
	 * The file that we are converting
	 * @return
	 */
	public String getFilePath();
	
	/**
	 * The dataset(s) we will process. Allows regular expressions for
	 * multiple processes.
	 * @return regexp of data set name (using / as the path separator)
	 */
	public String getDatasetName();
	
	/**
	 * The dataset(s) we will process. Allows regular expressions for
	 * multiple processes.
	 * @param datasetRegExp all Datasets (H5 definition) whose full path 
	 * matches this reg exp will be converted.
	 */
	public void setDatasetName(String datasetRegExp);
	
	/**
	 * 
	 * @return path export will process to
	 */
	public String getOutputFolder();
	
	/**
	 * Set the file path to output to.
	 * @param folderPath
	 */
	public void setOutputFolder(String folderPath);
	
	/**
	 * 
	 * @param dim
	 * @param sliceString either an integer to hold the dimension constant or
	 * a range of the form "start:end" where start is the start index and end is
	 * the end index or the string "all" to use the size of the dataset (start=0,
	 * end-length dimension).
	 * 
	 * There should be only one range set in the slicing, one set to "all" or one
	 * set to "start:end" where start is the start index. Only one range can be
	 * processed at a time.
	 */
	public void addSliceDimension(int dim, String sliceString);

	/**
	 * 
	 * @return the dimensions to slice in.
	 */
	public Map<Integer, String> getSliceDimensions();
	
	/**
	 * Get custom data which may be needed for a certain ConversionScheme
	 * @return
	 */
	public Object getUserObject();
	
	/**
	 * Set custom data which may be needed for a certain ConversionScheme
	 * @return
	 */
	public void setUserObject(Object object);
}
