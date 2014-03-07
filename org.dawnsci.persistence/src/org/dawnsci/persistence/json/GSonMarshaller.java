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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Class used to implement java bean to JSon and JSon to java bean conversion using Google GSon
 * TODO this marshaller won't work with abstract classes unless a specific deserializer is written for it
 * @author wqk87977
 *
 */
public class GSonMarshaller implements IJSonMarshaller {

	private Gson gson;

	public GSonMarshaller() {
		gson = new GsonBuilder().create();
		// .registerTypeAdapter(ROIBean.class, new Marshaller())
		// .registerTypeAdapter(FunctionBean.class, new Marshaller()).create();
		// JSON serialisation
		// TODO: serialiser to be worked on...
		// builder.registerTypeAdapter(ROIBean.class, new ROISerializer());
	}

	@Override
	public String marshal(Object obj) {
		return gson.toJson(obj);
	}

	@Override
	public Object unmarshal(String json) {
		JsonElement jelement = new JsonParser().parse(json);
		JsonObject  jobject = jelement.getAsJsonObject();
		JsonObject typeJobject = jobject.getAsJsonObject("type");
		if (typeJobject == null) // if no type we return an Object
			return gson.fromJson(json, Object.class);
		if (typeJobject.toString() == null)
			return gson.fromJson(json, Object.class);

		String type = typeJobject.toString();
		if(type.equals("PointROI")){
			return gson.fromJson(json, PointROIBean.class);
		} else if(type.equals("PerimeterBoxROI")){
			return gson.fromJson(json, PerimeterBoxROIBean.class);
		} else if(type.equals("RectangularROI")){
			return gson.fromJson(json, RectangularROIBean.class);
		} else if(type.equals("CircularROI")){
			return gson.fromJson(json, CircularROIBean.class);
		} else if(type.equals("LinearROI")){
			return gson.fromJson(json, LinearROIBean.class);
		} else if(type.equals("PolylineROI")){
			return gson.fromJson(json, PolylineROIBean.class);
		} else if(type.equals("PolygonalROI")){
			return gson.fromJson(json, PolygonalROIBean.class);
		} else if(type.equals("FreedrawROI")){
			return gson.fromJson(json, FreedrawROIBean.class);
		} else if(type.equals("RingROI")){
			return gson.fromJson(json, RingROIBean.class);
		} else if(type.equals("SectorROI")){
			return gson.fromJson(json, SectorROIBean.class);
		} else if(Integer.valueOf(type) != null && Integer.valueOf(type) >= 0) {
			// TODO write custom deserializer and/or instance creator so that IParameter can be deserialized
			return gson.fromJson(json, FunctionBean.class);
		}
		return null;
	}
}
