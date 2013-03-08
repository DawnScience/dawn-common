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

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;


/**
 * An interface used to provide a connection with the other expressions
 * For instance is a given string is a defined variable name in the current 
 * context.
 */
public interface IVariableManager {

	/**
	 * Test if variable name can be resolved.
	 * @param name
	 * @param monitor
	 * @return
	 */
	public boolean isVariableName(String name, IMonitor monitor);

	/**
	 * The same as getDataSet(...) however the expressionName has been
	 * parsed to be a legal expression variable.
	 * 
	 * @param expressionName
	 * @param monitor
	 * @return
	 */
	public AbstractDataset getVariableValue(String expressionName, final IMonitor monitor);

}
