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

import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IParameter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Parameter;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.CircularFitROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.CircularROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.EllipticalFitROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.EllipticalROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.FreedrawROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.GridROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.LinearROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.PerimeterBoxROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.PointROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.PolygonalROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.PolylineROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.RectangularROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.RingROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.SectorROIBean;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Class used to implement java bean to JSon and JSon to java bean conversion using Jackson
 * @author wqk87977
 *
 */
public class JacksonMarshaller implements IJSonMarshaller{

	private ObjectMapper mapper;

	public JacksonMarshaller() {
		mapper = new ObjectMapper();
		// mapping for deserializing FunctionBean
		SimpleModule module = new SimpleModule("ParameterMapping", Version.unknownVersion());
		module.addAbstractTypeMapping(IParameter.class, Parameter.class);
		mapper.registerModule(module);
	}

	@Override
	public String marshal(Object obj) throws JsonProcessingException {
		if (obj instanceof IFunction) {
			IFunction function = (IFunction)obj;
			FunctionBean fBean = new FunctionBean();
			fBean.setName(function.getName());
			IParameter[] iParameters = function.getParameters();
			Parameter[] parameters = new Parameter[iParameters.length];
			for (int i = 0; i < parameters.length; i++) {
				parameters[i] = new Parameter(iParameters[i]);
			}
			fBean.setParameters(parameters);
			fBean.setType(function.getClass().getName());

			return mapper.writeValueAsString(fBean);
		} else if (obj instanceof ROIBase) {
			return mapper.writeValueAsString(obj);
		} else {
			return mapper.writeValueAsString(obj);
		}
	}

	@Override
	public Object unmarshal(String json) throws Exception {
		// read JSON like DOM Parser
		JsonNode rootNode = mapper.readTree(json);
		JsonNode typeNode = rootNode.path("type");
		if (typeNode == null) // if no type we return an Object
			return mapper.readValue(json, Object.class);
		if (typeNode.asText() == null)
			return mapper.readValue(json, Object.class);

		String type = typeNode.asText();
		if (type.equals(PointROIBean.TYPE)) {
			return mapper.readValue(json, PointROIBean.class);
		} else if (type.equals(PerimeterBoxROIBean.TYPE)) {
			return mapper.readValue(json, PerimeterBoxROIBean.class);
		} else if (type.equals(GridROIBean.TYPE)) {
			return mapper.readValue(json, GridROIBean.class);
		} else if (type.equals(RectangularROIBean.TYPE)) {
			return mapper.readValue(json, RectangularROIBean.class);
		} else if (type.equals(CircularROIBean.TYPE)) {
			return mapper.readValue(json, CircularROIBean.class);
		} else if (type.equals(LinearROIBean.TYPE)) {
			return mapper.readValue(json, LinearROIBean.class);
		} else if (type.equals(PolylineROIBean.TYPE)) {
			return mapper.readValue(json, PolylineROIBean.class);
		} else if (type.equals(PolygonalROIBean.TYPE)) {
			return mapper.readValue(json, PolygonalROIBean.class);
		} else if (type.equals(FreedrawROIBean.TYPE)) {
			return mapper.readValue(json, FreedrawROIBean.class);
		} else if (type.equals(RingROIBean.TYPE)) {
			return mapper.readValue(json, RingROIBean.class);
		} else if (type.equals(SectorROIBean.TYPE)) {
			return mapper.readValue(json, SectorROIBean.class);
		} else if (type.equals(EllipticalROIBean.TYPE)) {
			return mapper.readValue(json, EllipticalROIBean.class);
		} else if (type.equals(CircularFitROIBean.TYPE)) {
			return mapper.readValue(json, CircularFitROIBean.class);
		} else if (type.equals(EllipticalFitROIBean.TYPE)) {
			return mapper.readValue(json, EllipticalFitROIBean.class);
		} else if (type.contains("functions")) {
			FunctionBean fbean = mapper.readValue(json, FunctionBean.class);
			return fbean.getIFunction();
		}
		return null;
	}
}
