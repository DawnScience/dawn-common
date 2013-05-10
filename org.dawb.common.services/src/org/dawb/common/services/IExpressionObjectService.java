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

import java.util.List;

/**
 * Returns objects implementing IExpressionObject which can be used
 * as items in the workbench for evaluating expressions in tables. Usually
 * the IExpressionObject is added to the object used for the table data
 * as member data.
 */
public interface IExpressionObjectService {
	
	/**
	 * Validate the expression name and return a cleaned up one if you can.
	 * @param manager
	 * @param variableName
	 * @return 
	 * @throws Exception if name not ok with message about why it is not ok
	 */
	public String validate(IVariableManager manager, String variableName) throws Exception;

	/**
	 * Create an IExpressionObject
	 * @param manager
	 * @param expressionName, may be null.
	 * @param expression,     may be null.
	 * @return
	 */
	public IExpressionObject createExpressionObject(IVariableManager manager, String expressionName, String expression);

	/**
	 * Creates a safe variable name from the suggested name, may return the 
	 * same name back again, if it is legal or may return empty string if all characters
	 * are illegal for variable names. Replaces space with _
	 * @param name
	 * @return
	 */
	public String getSafeName(String name);
	
	/**
	 * Returns the active expressions for a given file path. 
	 * @param sourcePath
	 * @return
	 * @throws Exception
	 */
	public List<IExpressionObject> getActiveExpressions(String sourcePath) throws Exception;

} 
