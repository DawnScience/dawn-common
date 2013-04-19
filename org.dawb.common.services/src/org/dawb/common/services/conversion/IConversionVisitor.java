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

package org.dawb.common.services.conversion;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

/**
 * Optionally instead of a conversion scheme, a separate visitor
 * may be defined. This is called instead of the conversion scheme,
 * and overides it.
 */
public interface IConversionVisitor {
	
	/**
	 * A name that might be shown to the user, describing this conversion.
	 * @return
	 */
	String getConversionSchemeName();

	/**
	 * Called to process the slices for conversion.
	 * @param context
	 * @param slice
	 */
	void visit(IConversionContext context, IDataset slice);

}
