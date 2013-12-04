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
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.CircularROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.FreedrawROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.LinearROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.PerimeterBoxROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.PointROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.PolygonalROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.PolylineROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.ROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.RectangularROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.RingROIBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.SectorROIBean;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Class used to implement java bean to JSon and JSon to java bean conversion using Google GSon
 * @author wqk87977
 *
 */
public class JSonMarshaller {

	private Gson gson;

	public JSonMarshaller() {
		// JSON serialisation
		GsonBuilder builder = new GsonBuilder();
		// TODO: serialiser to be worked on...
		// builder.registerTypeAdapter(ROIBean.class, new ROISerializer());
		gson = builder.create();
	}

	/**
	 * Returns a JSON string given a ROIBean
	 * @param roi
	 * @return String
	 */
	public String marshallFromROIBean(ROIBean roi) {
		return gson.toJson(roi);
	}

	/**
	 * Returns a JSon string given a FunctionBean
	 * @param function
	 * @return JSon
	 */
	public String marshallFromFunctionBean(FunctionBean function) {
		return gson.toJson(function);
	}

	/**
	 * Returns a Roi bean given a JSon String
	 * @param json
	 * @return ROIBean
	 */
	public ROIBean unmarshallToROIBean(String json) {
		ROIBean roibean = gson.fromJson(json, ROIBean.class);
		if(roibean == null) return null;
		if(roibean!=null && roibean.getType() == null) return null;

		if(roibean.getType().equals("PointROI")){
			return gson.fromJson(json, PointROIBean.class);
		} else if(roibean.getType().equals("PerimeterBoxROI")){
			return gson.fromJson(json, PerimeterBoxROIBean.class);
		} else if(roibean.getType().equals("RectangularROI")){
			return gson.fromJson(json, RectangularROIBean.class);
		} else if(roibean.getType().equals("CircularROI")){
			return gson.fromJson(json, CircularROIBean.class);
		} else if(roibean.getType().equals("LinearROI")){
			return gson.fromJson(json, LinearROIBean.class);
		} else if(roibean.getType().equals("PolylineROI")){
			return gson.fromJson(json, PolylineROIBean.class);
		} else if(roibean.getType().equals("PolygonalROI")){
			return gson.fromJson(json, PolygonalROIBean.class);
		} else if(roibean.getType().equals("FreedrawROI")){
			return gson.fromJson(json, FreedrawROIBean.class);
		} else if(roibean.getType().equals("RingROI")){
			return gson.fromJson(json, RingROIBean.class);
		} else if(roibean.getType().equals("SectorROI")){
			return gson.fromJson(json, SectorROIBean.class);
		}
		return null;
	}

	/**
	 * Returns a FunctionBean given a json String
	 * @param json
	 * @return FunctionBean
	 */
	public FunctionBean unmarshallToFunctionBean(String json) {
		return gson.fromJson(json, FunctionBean.class);
	}
}
