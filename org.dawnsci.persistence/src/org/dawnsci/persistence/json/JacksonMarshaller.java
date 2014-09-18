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

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.persistence.json.function.FunctionBean;
import org.dawnsci.persistence.json.function.FunctionListBean;
import org.dawnsci.persistence.json.roi.CircularFitROIBean;
import org.dawnsci.persistence.json.roi.CircularROIBean;
import org.dawnsci.persistence.json.roi.EllipticalFitROIBean;
import org.dawnsci.persistence.json.roi.EllipticalROIBean;
import org.dawnsci.persistence.json.roi.FreedrawROIBean;
import org.dawnsci.persistence.json.roi.GridROIBean;
import org.dawnsci.persistence.json.roi.HyperbolicROIBean;
import org.dawnsci.persistence.json.roi.LinearROIBean;
import org.dawnsci.persistence.json.roi.ParabolicROIBean;
import org.dawnsci.persistence.json.roi.PerimeterBoxROIBean;
import org.dawnsci.persistence.json.roi.PointROIBean;
import org.dawnsci.persistence.json.roi.PolygonalROIBean;
import org.dawnsci.persistence.json.roi.PolylineROIBean;
import org.dawnsci.persistence.json.roi.ROIBean;
import org.dawnsci.persistence.json.roi.RectangularROIBean;
import org.dawnsci.persistence.json.roi.RingROIBean;
import org.dawnsci.persistence.json.roi.SectorROIBean;
import org.dawnsci.persistence.util.PersistenceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.fitting.functions.Parameter;

import org.dawnsci.common.widgets.gda.function.jexl.JexlExpressionFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IOperator;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IParameter;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularFitROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.EllipticalFitROI;
import org.eclipse.dawnsci.analysis.dataset.roi.EllipticalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.FreeDrawROI;
import org.eclipse.dawnsci.analysis.dataset.roi.GridROI;
import org.eclipse.dawnsci.analysis.dataset.roi.HyperbolicROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.ParabolicROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PerimeterBoxROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolygonalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolylineROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RingROI;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
import org.eclipse.dawnsci.analysis.dataset.roi.XAxisBoxROI;
import org.eclipse.dawnsci.analysis.dataset.roi.YAxisBoxROI;

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
	private Logger logger = LoggerFactory.getLogger(JacksonMarshaller.class);

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
			if (function instanceof IOperator) {
				FunctionListBean bean = getFunctionListBean(function);
				return mapper.writeValueAsString(bean);
			} else {
				FunctionBean bean = getFunctionBean(function);
				return mapper.writeValueAsString(bean);
			}
		} else if (obj instanceof IROI) {
			IROI roi = (IROI)obj;
			ROIBean rbean = getROIBean(roi);
			return mapper.writeValueAsString(rbean);
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
		// if the ROI keyword is present we assume the data is a roi
		if (type.contains("ROI")) {
			// Return the corresponding ROIBean class name
			Class<?> clazz = Class.forName("org.dawnsci.persistence.json.roi." + type + "Bean");
			ROIBean bean = (ROIBean) mapper.readValue(json, clazz);
			return bean.getROI();
		}
		// if the function keyword is present we assume the data is a function
		if (type.contains("function")) {
			if (PersistenceUtils.getInstance(type) instanceof IOperator) {
				FunctionListBean fbean = mapper.readValue(json, FunctionListBean.class);
				return fbean.getIFunction();
			} else {
				FunctionBean fbean = mapper.readValue(json, FunctionBean.class);
				return fbean.getIFunction();
			}
		}
		return null;
	}

	/**
	 * Method that converts an IFunction to a FunctionBean
	 * @param function
	 * @return FunctionBean
	 */
	private FunctionBean getFunctionBean(IFunction function) {
		FunctionBean fBean = new FunctionBean();
		if (function instanceof JexlExpressionFunction) {
			JexlExpressionFunction jexl = (JexlExpressionFunction)function;
			fBean.setName(jexl.getExpression());
		} else {
			fBean.setName(function.getName());
		}
		IParameter[] iParameters = function.getParameters();
		Parameter[] parameters = new Parameter[iParameters.length];
		for (int i = 0; i < parameters.length; i++) {
			parameters[i] = new Parameter(iParameters[i]);
		}
		fBean.setParameters(parameters);
		fBean.setType(function.getClass().getName());
		return fBean;
	}

	/**
	 * Method that converts an IOperator to a FunctionListBean
	 * @param function
	 * @return FunctionListBean
	 */
	private FunctionListBean getFunctionListBean(IFunction function) {
		FunctionListBean fBean = new FunctionListBean();
		fBean.setName(function.getName());
		IFunction[] functions = ((IOperator)function).getFunctions();
		FunctionBean[] funcBeans = new FunctionBean[functions.length];
		for (int i = 0; i < functions.length; i++) {
			funcBeans[i] = getFunctionBean(functions[i]);
		}
		fBean.setFunctions(funcBeans);
		fBean.setType(function.getClass().getName());
		return fBean;
	}

	/**
	 * Method that converts an IROI to a ROIBean
	 * @param roi
	 * @return ROIBean
	 */
	private ROIBean getROIBean(IROI roi){
		Class<? extends IROI> roiClass = roi.getClass();
		String name = roi.getName();
		if(roiClass == PointROI.class){
			PointROI proi = (PointROI) roi;
			PointROIBean proibean = new PointROIBean();
			proibean.setName(name);
			proibean.setStartPoint(proi.getPoint());
			return proibean;

		} else if(roiClass == PerimeterBoxROI.class){
			PerimeterBoxROI proi = (PerimeterBoxROI) roi;
			PerimeterBoxROIBean proibean = new PerimeterBoxROIBean();
			proibean.setName(name);
			proibean.setStartPoint(proi.getPoint());
			proibean.setEndPoint(proi.getEndPoint());
			proibean.setAngle(proi.getAngle());
			proibean.setLengths(proi.getLengths());
			return proibean;

		} else if(roiClass == RectangularROI.class){
			RectangularROI rroi = (RectangularROI) roi;
			RectangularROIBean rroibean = new RectangularROIBean();
			rroibean.setName(name);
			rroibean.setStartPoint(rroi.getPoint());
			rroibean.setEndPoint(rroi.getEndPoint());
			rroibean.setAngle(rroi.getAngle());
			rroibean.setLengths(rroi.getLengths());
			return rroibean;

		} else if(roiClass == GridROI.class){
			GridROI groi = (GridROI) roi;
			GridROIBean groibean = new GridROIBean();
			groibean.setName(name);
			groibean.setStartPoint(groi.getPoint());
			groibean.setEndPoint(groi.getEndPoint());
			groibean.setAngle(groi.getAngle());
			groibean.setLengths(groi.getLengths());
			groibean.setxSpacing(groi.getxSpacing());
			groibean.setySpacing(groi.getySpacing());
			groibean.setGridLinesOn(groi.isGridLineOn());
			groibean.setMidPointOn(groi.isMidPointOn());
			return groibean;

		} else if(roiClass == LinearROI.class){
			LinearROI lroi = (LinearROI) roi;
			LinearROIBean lroibean = new LinearROIBean();
			lroibean.setName(name);
			lroibean.setStartPoint(lroi.getPoint());
			lroibean.setEndPoint(lroi.getEndPoint());
			return lroibean;

		} else if(roiClass == PolylineROI.class){
			PolylineROI plroi = (PolylineROI) roi;
			PolylineROIBean plroibean = new PolylineROIBean();
			plroibean.setName(name);
			plroibean.setStartPoint(plroi.getPoint());
			List<double[]> points = new ArrayList<double[]>();
			for (IROI p: plroi) {
				points.add(p.getPoint());
			}
			plroibean.setPoints(points);
			return plroibean;

		} else if(roiClass == PolygonalROI.class){
			PolygonalROI pgroi = (PolygonalROI) roi;
			PolygonalROIBean pgroibean = new PolygonalROIBean();
			pgroibean.setName(name);
			pgroibean.setStartPoint(pgroi.getPoint());
			List<double[]> points = new ArrayList<double[]>();
			for (IROI p: pgroi) {
				points.add(p.getPoint());
			}
			pgroibean.setPoints(points);
			return pgroibean;

		} else if(roiClass == FreeDrawROI.class){
			FreeDrawROI fdroi = (FreeDrawROI) roi;
			FreedrawROIBean fdroibean = new FreedrawROIBean();
			fdroibean.setName(name);
			fdroibean.setStartPoint(fdroi.getPoint());
			List<double[]> points = new ArrayList<double[]>();
			for (IROI p: fdroi) {
				points.add(p.getPoint());
			}
			fdroibean.setPoints(points);
			return fdroibean;
		} else if (roiClass == SectorROI.class) {
			SectorROI sroi = (SectorROI)roi;
			SectorROIBean sroibean = new SectorROIBean();
			sroibean.setName(name);
			sroibean.setStartPoint(sroi.getPoint());
			sroibean.setAngles(sroi.getAngles());
			sroibean.setRadii(sroi.getRadii());
			sroibean.setSymmetry(sroi.getSymmetry());
			sroibean.setDpp(sroi.getDpp());
			sroibean.setClippingCompensation(sroi.isClippingCompensation());
			sroibean.setCombineSymmetry(sroi.isCombineSymmetry());
			sroibean.setAverageArea(sroi.isAverageArea());
			return sroibean;
		} else if (roiClass == RingROI.class) {
			RingROI rroi = (RingROI) roi;
			RingROIBean rroibean = new RingROIBean();
			rroibean.setName(name);
			rroibean.setStartPoint(rroi.getPoint());
			rroibean.setRadii(rroi.getRadii());
			rroibean.setDpp(rroi.getDpp());
			rroibean.setAverageArea(rroi.isAverageArea());
			rroibean.setClippingCompensation(rroi.isClippingCompensation());
			return rroibean;
		} else if(roiClass == CircularROI.class){
			CircularROI croi = (CircularROI) roi;
			CircularROIBean croibean = new CircularROIBean();
			croibean.setName(name);
			croibean.setStartPoint(croi.getCentre());
			croibean.setRadius(croi.getRadius());
			return croibean;

		} else if(roiClass == EllipticalROI.class){
			EllipticalROI eroi = (EllipticalROI) roi;
			EllipticalROIBean eroibean = new EllipticalROIBean();
			eroibean.setName(name);
			eroibean.setStartPoint(eroi.getPoint());
			eroibean.setSemiAxes(eroi.getSemiAxes());
			eroibean.setAngle(eroi.getAngle());
			return eroibean;

		} else if(roiClass == CircularFitROI.class){
			CircularFitROI croi = (CircularFitROI) roi;
			CircularFitROIBean croibean = new CircularFitROIBean();
			croibean.setName(name);
			croibean.setRadius(croi.getRadius());
			croibean.setStartPoint(croi.getPoint());
			List<double[]> points = new ArrayList<double[]>();
			for (IROI p: croi.getPoints()) {
				points.add(p.getPoint());
			}
			croibean.setPoints(points);
			return croibean;

		} else if(roiClass == EllipticalFitROI.class){
			EllipticalFitROI eroi = (EllipticalFitROI) roi;
			EllipticalFitROIBean eroibean = new EllipticalFitROIBean();
			eroibean.setName(name);
			eroibean.setStartPoint(eroi.getPoint());
			eroibean.setSemiAxes(eroi.getSemiAxes());
			eroibean.setAngle(eroi.getAngle());
			List<double[]> points = new ArrayList<double[]>();
			for (IROI p: eroi.getPoints()) {
				points.add(p.getPoint());
			}
			eroibean.setPoints(points);
			return eroibean;

		} else if(roiClass == ParabolicROI.class){
			ParabolicROI proi = (ParabolicROI) roi;
			ParabolicROIBean proibean = new ParabolicROIBean();
			proibean.setName(proi.getName());
			proibean.setAngle(proi.getAngle());
			proibean.setFocalParameter(proi.getFocalParameter());
			proibean.setStartPoint(proi.getPoint());getClass();
			return proibean;

		} else if(roiClass == HyperbolicROI.class){
			HyperbolicROI hroi = (HyperbolicROI) roi;
			HyperbolicROIBean hroibean = new HyperbolicROIBean();
			hroibean.setName(hroi.getName());
			hroibean.setAngle(hroi.getAngle());
			hroibean.setEccentricity(hroi.getEccentricity());
			hroibean.setSemi(hroi.getSemilatusRectum());
			hroibean.setStartPoint(hroi.getPoint());getClass();
			return hroibean;

		} else {
			logger.debug("This type is not supported");
		}
		return null;
	}

	/**
	 * Method that returns true if the type of ROI is supported by the ROIBeanConverter
	 * @return boolean
	 */
	public static boolean isROISupported(IROI roi){
		if(roi instanceof PointROI
				|| roi instanceof LinearROI
				|| roi instanceof CircularROI
				|| roi instanceof GridROI
				|| roi instanceof PerimeterBoxROI
				|| roi instanceof RectangularROI
				|| roi instanceof RingROI
				|| roi instanceof SectorROI
				|| roi instanceof FreeDrawROI
				|| roi instanceof PolylineROI
				|| roi instanceof PolygonalROI
				|| roi instanceof XAxisBoxROI
				|| roi instanceof YAxisBoxROI
				|| roi instanceof EllipticalROI
				|| roi instanceof CircularFitROI
				|| roi instanceof EllipticalFitROI
				|| roi instanceof ParabolicROI
				|| roi instanceof HyperbolicROI)
			return true;
		return false;
	}
}
