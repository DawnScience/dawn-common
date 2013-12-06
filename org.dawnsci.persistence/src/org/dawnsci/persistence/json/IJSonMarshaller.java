/*-
 * Copyright 2013 Diamond Light Source Ltd.
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
package org.dawnsci.persistence.json;

import uk.ac.diamond.scisoft.analysis.persistence.bean.function.FunctionBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.ROIBean;

/**
 * Interface used to used to marshall from ROIBean/FunctionBean to JSON
 * and unmarshall from JSON to ROIBean/FunctionBean
 * 
 * @author wqk87977
 *
 */
public interface IJSonMarshaller {

	/**
	 * Returns a JSON string given a ROIBean
	 * @param roi
	 * @return String
	 */
	public String marshallFromROIBean(ROIBean roi);

	/**
	 * Returns a JSon string given a FunctionBean
	 * @param function
	 * @return JSon
	 */
	public String marshallFromFunctionBean(FunctionBean function) ;

	/**
	 * Returns a Roi bean given a JSon String
	 * @param json
	 * @return ROIBean
	 */
	public ROIBean unmarshallToROIBean(String json);

	/**
	 * Returns a FunctionBean given a json String
	 * @param json
	 * @return FunctionBean
	 */
	public FunctionBean unmarshallToFunctionBean(String json);
}
