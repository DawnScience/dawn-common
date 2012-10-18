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
 * This class contains different methods for creating transferrable
 * objects. For instance for creating an Object which can be used as 
 * a transferrable by the plotting for a ROISource in the workflows.
 * 
 * This allows plotting and workflows not to be connected.
 */
public interface ITransferService {

	
	/**
	 * Creates a ROISource for a transferrable without giving a hard dependency
	 * on ROISource.
	 * 
	 * @param name
	 * @param roi
	 * @return
	 */
	public Object createROISource(final String name, final Object roi) throws Exception;
}
