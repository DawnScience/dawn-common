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
 * This service is available as an OSGI service similar to IPersistenceService.
 * 
 * An OSGI Service can be used from anywhere in an OSG environment such as vergil
 * or equinox.
 * 
 * Usage something like:
 * <code>
 * IConversionService service = (IConversionService)ServiceManager.getService(IConversionService.class);
 * IConversionContext context = service.open("/dls/path_to_some_hdf5_file.h5");
 * context.setDatasetName("/entry1/signal/some_data");
 * context.setOutputFolder("/dls/some_place_I_want_my_data");
 * context.setScheme("IConversionContext.ConversionScheme.ASCII_FROM_2D");
 * service.process(context);
 * </code>
 * 
 * The core service support looping
 * 
 */
public interface IConversionService {

	/**
	 * Call to open a conversion context on a given path.
	 * The context returned should be configured and then the 'convert'
	 * method called to run a conversion.
	 * 
	 * @param filePath may be a folder path, or a regex. If a regex all matching files in the
	 * directory will be converted, for instance "C:/tmp/(.+).h5". The path separator must always
	 * be "/" even on windows. The regex is done after the last / the path preceeding the last /
	 * is the directory path and does not support regex. If the path ends with a / then all
	 * files in the directory will be processes. Sub-folders will not be traversed.
	 * 
	 * @return
	 */
	public IConversionContext open(String filePathRegEx);
	
	/**
	 * Call to run the conversion. Process all files matching the filePathRegEx
	 * @param context
	 * @throws Exception if problem processing the conversion.
	 */
	public void process(IConversionContext context) throws Exception;
}
