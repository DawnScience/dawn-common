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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IParameter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Parameter;
import uk.ac.diamond.scisoft.analysis.persistence.bean.function.FunctionBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.CircularROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.FreedrawROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.LinearROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.PerimeterBoxROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.PointROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.PolygonalROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.PolylineROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.RectangularROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.RingROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.SectorROIBean;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Class used to implement java bean to JSon and JSon to java bean conversion using Jackson
 * @author wqk87977
 *
 */
public class JacksonMarshaller implements IJSonMarshaller{

	private final Logger logger = LoggerFactory.getLogger(JacksonMarshaller.class);

	private ObjectMapper mapper;

	public JacksonMarshaller() {
		mapper = new ObjectMapper();
		// mapping for deserializing FunctionBean
		SimpleModule module = new SimpleModule("ParameterMapping", Version.unknownVersion());
		module.addAbstractTypeMapping(IParameter.class, Parameter.class);
		mapper.registerModule(module);
	}

	@Override
	public String marshal(Object obj) {
		try {
			return mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			logger.error("Error marshalling from "+ obj.getClass() + ":" + e);
			return "";
		}
	}

	@Override
	public Object unmarshal(String json) {
		try {
			//read JSON like DOM Parser
			JsonNode rootNode = mapper.readTree(json);
			JsonNode typeNode = rootNode.path("type");
			if (typeNode == null) // if no type we return an Object
				return mapper.readValue(json, Object.class);
			if (typeNode.asText() == null)
				return mapper.readValue(json, Object.class);

			String type = typeNode.asText();
			if (type.equals("PointROI")) {
				return mapper.readValue(json, PointROIBean.class);
			} else if (type.equals("PerimeterBoxROI")) {
				return mapper.readValue(json, PerimeterBoxROIBean.class);
			} else if (type.equals("RectangularROI")) {
				return mapper.readValue(json, RectangularROIBean.class);
			} else if (type.equals("CircularROI")) {
				return mapper.readValue(json, CircularROIBean.class);
			} else if (type.equals("LinearROI")) {
				return mapper.readValue(json, LinearROIBean.class);
			} else if (type.equals("PolylineROI")) {
				return mapper.readValue(json, PolylineROIBean.class);
			} else if (type.equals("PolygonalROI")) {
				return mapper.readValue(json, PolygonalROIBean.class);
			} else if (type.equals("FreedrawROI")) {
				return mapper.readValue(json, FreedrawROIBean.class);
			} else if (type.equals("RingROI")) {
				return mapper.readValue(json, RingROIBean.class);
			} else if (type.equals("SectorROI")) {
				return mapper.readValue(json, SectorROIBean.class);
			} else if (Integer.valueOf(type) != null && Integer.valueOf(type) >= 0) { // if type is an integer we unmarshall to FunctionBean
				return mapper.readValue(json, FunctionBean.class);
			}
		} catch (JsonParseException e) {
			logger.error("Error unmarshalling :" + e);
			return null;
		} catch (JsonMappingException e) {
			logger.error("Error unmarshalling :" + e);
			return null;
		} catch (IOException e) {
			logger.error("Error unmarshalling :" + e);
			return null;
		}
		return null;
	}
}
