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
 * An object which can resolve data maths and can be used
 * as data in Viewer models in jface.
 */
public interface IExpressionObject {

	/**
	 * The string expression, e.g. "x*y"
	 * @return
	 */
	public String getExpressionString();
	
	/**
	 * Method to set the expression string. For instance when the user
	 * types in a new string.
	 * 
	 * @param expression
	 */
	public void setExpressionString(String expression);

	/**
	 * If the expression value is cached, the
	 * clear method will nullify this cache.
	 */
	public void clear();

	/**
	 * Evaluates and caches the expression if necessary.
	 * @return the evaluated value of the expression.
	 */
	public AbstractDataset getDataSet(IMonitor monitor) throws Exception;

	/**
	 * 
	 * @param stub
	 * @return
	 */
	public String getShape(IMonitor monitor);

	/**
	 * Get the total size of the data
	 * @param stub
	 * @return
	 */
	public int getSize(IMonitor monitor);

	/**
	 * 
	 * @param monitor
	 * @return true if expression contained in the object has legal syntax.
	 */
	public boolean isValid(IMonitor monitor);


}
