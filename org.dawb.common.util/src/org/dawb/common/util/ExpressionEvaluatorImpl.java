/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;


public class ExpressionEvaluatorImpl implements IExpressionEvaluator {

	private JexlEngine jexl;
	private String     expressionString;
	private Expression expression;

	@Override
	public void setExpression(String expr) {
		if (jexl==null) jexl = new JexlEngine();
		this.expressionString = expr;
	}

	@Override
	public double evaluate(Map<String, ?> vals) throws Exception {
		
		if (!isValidSyntax()) throw new Exception("Expression '"+expressionString+"' is not valid!");
		
		Map<String, Object> parsedValues = new HashMap<String,Object>(vals.size());
		for (String name : vals.keySet()) {
			final Object value = vals.get(name);
			try {
				parsedValues.put(name, Long.parseLong(value.toString()));
			} catch (Throwable ne) {
				try {
					parsedValues.put(name, Double.parseDouble(value.toString()));
					
				} catch (Throwable neOther) {
					parsedValues.put(name, value);
				}
			}
		}
		
		JexlContext context = new MapContext();
		for (String name : parsedValues.keySet()) {
			context.set(name, parsedValues.get(name));
		}
		
		final Object output = expression.evaluate(context);
		if (output instanceof Number) {
			return ((Number)expression.evaluate(context)).doubleValue();
		} else if (output instanceof Boolean) {
			return ((Boolean) output).booleanValue() ? 1.0 : 0.0;
		}
		return Double.NaN;
	}

	@Override
	public boolean isValidSyntax() {
		if (expression!=null) return true;
		try {
			expression    = jexl.createExpression(expressionString);
		}catch (Exception ne) {
			return false;
		}
		return true;
	}

}
