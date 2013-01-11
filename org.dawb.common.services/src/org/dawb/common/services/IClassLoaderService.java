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
 * This service can be called to set the class loader on a thread.
 * The class loader will include classes which commonly are communicated between
 * two data analysis processes. For instance if workflows and the UI are 
 * communicating.
 * 
 * The class loader will include things like AbstractDataset and beans used
 * to commicate tool data.
 * 
 * Usage:
 * <code>
 * IClassLoaderService service = (IClassLoaderService)PlatformUI.getWorkbench().getService(IClassLoaderService.class);
 * 
 * try {
 *     service.setDataAnalysisClassLoaderActive(true);
 *     
 *     // Communicate
 *     
 * } finally {
 *     service.setDataAnalysisClassLoaderActive(false);
 * }
 *     
 *     
 */
public interface IClassLoaderService {

	/**
	 * Call to activate a classloader with data analysis classes available.
	 *  
	 * @param active
	 */
	public void setDataAnalysisClassLoaderActive(boolean active);
}
