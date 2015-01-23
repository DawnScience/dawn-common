/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.persistence.json;

import org.dawnsci.persistence.json.function.FunctionBean;
import org.eclipse.dawnsci.analysis.dataset.roi.json.CircularROIBean;
import org.eclipse.dawnsci.analysis.dataset.roi.json.FreeDrawROIBean;
import org.eclipse.dawnsci.analysis.dataset.roi.json.LinearROIBean;
import org.eclipse.dawnsci.analysis.dataset.roi.json.PerimeterBoxROIBean;
import org.eclipse.dawnsci.analysis.dataset.roi.json.PointROIBean;
import org.eclipse.dawnsci.analysis.dataset.roi.json.PolygonalROIBean;
import org.eclipse.dawnsci.analysis.dataset.roi.json.PolylineROIBean;
import org.eclipse.dawnsci.analysis.dataset.roi.json.RectangularROIBean;
import org.eclipse.dawnsci.analysis.dataset.roi.json.RingROIBean;
import org.eclipse.dawnsci.analysis.dataset.roi.json.SectorROIBean;

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
			return gson.fromJson(json, FreeDrawROIBean.class);
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
