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

import java.util.Map;

import org.nfunk.jep.JEP;
import org.nfunk.jep.Node;

public class ExpressionEvaluatorImpl implements IExpressionEvaluator {

	
	private JEP  jepParser;
	private Node node;

	@Override
	public void setExpression(String expr) {
		this.jepParser= new JEP();
		jepParser.addStandardFunctions();
		jepParser.addStandardConstants();
		jepParser.setAllowUndeclared(true);
		jepParser.setImplicitMul(true);
		
		this.node = jepParser.parseExpression(expr);
	}

	@Override
	public double evaluate(Map<String, ?> vals) throws Exception {
		
		for (String key : vals.keySet()) {
			Object value = vals.get(key);
			if (value instanceof Number) {
				jepParser.addVariable(key, ((Number)value).doubleValue());
			} else if (value instanceof String){
				try {
					value = Double.parseDouble((String)value);
				} catch (Exception igonred) {
					// Nothing
				}
				jepParser.addVariable(key, value);
			}
		}
		final Object val = jepParser.evaluate(node);
		return jepParser.getValue();
	}

	@Override
	public boolean isValidSyntax() {
		return node!=null;
	}

}
