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

/**
 * This service can be called to save get an engine for parsing mathematical expression strings and performing
 * calculations on datasets
 */
public interface IExpressionService {
	
	/**
	 * Method to rget an expression engine from the service
	 *  
	 * @return IExpressionEngine
	 */
	public IExpressionEngine getExpressionEngine();
	
}
