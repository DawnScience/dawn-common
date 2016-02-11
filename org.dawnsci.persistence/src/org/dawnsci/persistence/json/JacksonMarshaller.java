/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.persistence.json;

import org.dawnsci.common.widgets.gda.function.jexl.JexlExpressionFunction;
import org.dawnsci.persistence.json.function.FunctionBean;
import org.dawnsci.persistence.json.function.FunctionListBean;
import org.dawnsci.persistence.util.PersistenceUtils;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IOperator;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IParameter;
import org.eclipse.dawnsci.analysis.api.persistence.IJSonMarshaller;
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
import org.eclipse.dawnsci.analysis.dataset.roi.json.ROIBeanFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.fitting.functions.Parameter;

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
	public String marshal(Object obj) throws Exception {
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
			Object rbean = ROIBeanFactory.encapsulate(roi);
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
			Class<?> clazz = ROIBeanFactory.getClass(type);
			Object bean = mapper.readValue(json, clazz);
			return ROIBeanFactory.decapsulate(bean);
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
