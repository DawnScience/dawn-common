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

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;

/**
 * An object which can resolve data maths and can be used
 * as data in Viewer models in jface.
 */
public interface IExpressionObject {

	/**
	 * The name of the expression, e.g. "Q"
	 * @return
	 */
	public String getExpressionName();
	
	/**
	 * Method to set the expression string. For instance when the user
	 * types in a new string.
	 * 
	 * @param expression
	 */
	public void setExpressionName(String name);


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
	 * @param suggestedName may be null
	 * @param monitor
	 * @return the evaluated value of the expression.
	 * @throws Exception
	 */
	public IDataset getDataSet(String suggestedName, IMonitor monitor) throws Exception;

	/**
	 * Guesses the data without evaluating the expression, instead looks for
	 * a reference to a concrete data set and uses the attributes (shape etc)
	 * of this.
	 * 
	 * *WARNING* Is educated guess at lazy dataset, will not always work.
	 * 
	 * @param suggestedName should not be null, should be the data name or the variable name for expressions.
	 * @param monitor
	 * @return null if the guess cannot be made or there was any kind of error.
	 */
	public ILazyDataset getLazyDataSet(String suggestedName, IMonitor monitor);

	/**
	 * 
	 * @param monitor
	 * @return true if expression contained in the object has legal syntax.
	 */
	public boolean isValid(IMonitor monitor);
	
	/**
	 * Get all functions currently in the expression engine
	 * 
	 * @returns functions
	 */
	public Map<String,Object> getFunctions();

}
