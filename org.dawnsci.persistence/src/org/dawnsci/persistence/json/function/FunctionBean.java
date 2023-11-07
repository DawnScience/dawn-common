/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.persistence.json.function;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.eclipse.dawnsci.analysis.api.expressions.IExpressionService;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IParameter;

import com.fasterxml.jackson.annotation.JsonIgnore;

import uk.ac.diamond.osgi.services.ServiceProvider;
import uk.ac.diamond.scisoft.analysis.fitting.functions.JexlExpressionFunction;

/**
 * Function bean used to marshall/unmarshall to / from JSON strings <br>
 * A converter can be used to convert this bean to IFunction<br>
 * (See uk.ac.diamond.scisoft.analysis.persistence.bean.function.
 * FunctionBeanConverter)
 */
public class FunctionBean {

	protected String name;
	protected String type;
	protected IParameter[] parameters;

	public FunctionBean() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public IParameter[] getParameters() {
		return parameters;
	}

	public void setParameters(IParameter[] parameters) {
		this.parameters = parameters;
	}

	/**
	 * Method that converts a function bean to an IFunction using reflection
	 * 
	 * @return IFunction
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@JsonIgnore
	public IFunction getIFunction() throws ClassNotFoundException,
			NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		IFunction function = null;
		IParameter[] params = getParameters();
		Class<?> clazz = Class.forName(getType());
		// If a Jexl expression
		if (clazz.equals(JexlExpressionFunction.class)) {
			Constructor<?> constructor = clazz.getConstructor(IExpressionService.class, String.class);
			function = (IFunction) constructor.newInstance(ServiceProvider.getService(IExpressionService.class), getName());
			for (int i = 0; i < params.length; i++) {
				((JexlExpressionFunction)function).setParameter(i, params[i]);
			}
		} else { // For all other cases try to return an instance of IFunction with parameters
			Constructor<?> constructor = clazz.getConstructor(IParameter[].class);
			function = (IFunction) constructor.newInstance((Object) params);
		}
		return function;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + Arrays.hashCode(parameters);
		result = prime * result + type.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FunctionBean other = (FunctionBean) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (!Arrays.equals(parameters, other.parameters))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
}
