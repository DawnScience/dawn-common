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

/**
 * The conversion context which will drive what we are going to convert.
 */
public interface IConversionContext {

	/**
	 * Schemes to be edited as required, current list is a guess
	 * of the conversions we have spoken about before.
	 */
	public enum ConversionScheme {
		ASCII_FROM_2D, ASCII_FROM_1D, CUSTOM_NCD, TIFF_FROM_3D;
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

}
