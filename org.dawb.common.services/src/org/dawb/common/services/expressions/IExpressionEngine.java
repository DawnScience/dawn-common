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

package org.dawb.common.services.expressions;

import java.util.Collection;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This service can be called to save and/or load data from an hdf5 file.
 *
 */
public interface IExpressionEngine {
	
	/**
	 * Method to create an expression in the engine, throws an exception if the expression cannot be parsed
	 *  
	 * @param expression
	 * @throws Exception
	 */
	public void createExpression(String expression) throws Exception;
	
	/**
	 * Evaluates the expression set in the engine, throws exception if cannot evaluate, returns answer as object
	 *  
	 * @return Object
	 * @throws Exception
	 */
	public Object evaluate() throws Exception;
	
	/**
	 * Adds a listener for expression evaluated with event (calculation performed in separate thread)
	 * 
	 * @param listener
	 */
	public void addExpressionEngineListener(IExpressionEngineListener listener);
	
	/**
	 * Removes a listener for expression evaluated with event (calculation performed in separate thread)
	 * 
	 * @param listener
	 */
	public void removeExpressionEngineListener(IExpressionEngineListener listener);
	
	/**
	 * Evaluate the expression and fire an event on completion (for long calculations, or expressions on UI)
	 * Can be cancelled using the monitor
	 * 
	 * @param monitor
	 */
	public void evaluateWithEvent(IProgressMonitor monitor);
	
	/**
	 * Get all functions currently in the expression engine
	 * 
	 * @returns functions
	 */
	public Map<String,Object> getFunctions();
	
	/**
	 * Set all functions currently in the expression engine
	 * 
	 * @param functions
	 */
	public void setFunctions(Map<String,Object> functions);
	
	/**
	 * Set all variables currently in the expression engine
	 * 
	 * @param variables
	 */
	public void setLoadedVariables(Map<String,Object> variables);
	
	/**
	 * get named variable from engine
	 * 
	 * @param name
	 * @returns Object
	 */
	public Object getLoadedVariable(String name);
	
	/**
	 * Adds to variables currently in the expression engine
	 * 
	 * @param variables
	 */
	public void addLoadedVariables(Map<String,Object> variables);
	
	/**
	 * Adds single variable to variables currently in the expression engine
	 * 
	 * @param name
	 * @param value
	 */
	public void addLoadedVariable(String name, Object value);
	
	/**
	 * Gets names of *all* variables from the expression
	 * 
	 * @returns names
	 */
	public Collection<String> getVariableNamesFromExpression();
	
	/**
	 * Gets names of variables from the expression which can be
	 * provided as lazy datasets. The expression will then do the 
	 * relavent slicing during evaluation.
	 * 
	 * @returns names
	 */
	public Collection<String> getLazyVariableNamesFromExpression();

}
