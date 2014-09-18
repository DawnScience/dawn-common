/*-
 * Copyright (c) 2014 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.persistence.json.function;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.dawnsci.persistence.util.PersistenceUtils;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IOperator;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Function list bean used to marshall/unmarshall to / from JSON strings <br>
 * Used to serialise/un-serialise Composite functions
 */
public class FunctionListBean {

	protected String name;
	protected String type;
	protected FunctionBean[] functions;

	public FunctionListBean() {

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

	public FunctionBean[] getFunctions() {
		return functions;
	}

	public void setFunctions(FunctionBean[] functions) {
		this.functions = functions;
	}

	/**
	 * Method that converts a function bean to an IFunction using reflection
	 * 
	 * @return IFunction
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws ClassNotFoundException
	 */
	@JsonIgnore
	public IFunction getIFunction() throws ClassNotFoundException,
			NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		FunctionBean[] funcBeans = getFunctions();
		IFunction[] functions = new IFunction[funcBeans.length];

		IFunction operator = (IFunction) PersistenceUtils.getInstance(type);
		operator.setName(name);

		for (int i = 0; i < funcBeans.length; i++) {
			functions[i] = funcBeans[i].getIFunction();
			((IOperator)operator).addFunction(functions[i]);
		}
		return operator;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + Arrays.hashCode(functions);
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
		FunctionListBean other = (FunctionListBean) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (!Arrays.equals(functions, other.functions))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

}
