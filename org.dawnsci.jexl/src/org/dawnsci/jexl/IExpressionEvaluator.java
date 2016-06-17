/*
 * Copyright (c) 2012-2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.jexl;

import java.util.Map;

/**
 * Use to evaluate the same expression many times.
 * 
 * @author gerring
 *
 */
public interface IExpressionEvaluator {

	/**
	 * Set and parse the current expression
	 * 
	 * @param expr
	 */
	public void setExpression(String expr);

	/**
	 * Evaluate the current value from the passed in values.
	 * 
	 * @param vals
	 * @return
	 */
	public double evaluate(Map<String, ?> vals) throws Exception;

	/**
	 * Returns true if the expression is in valid syntax, false otherwise.
	 * 
	 * @return
	 */
	public boolean isValidSyntax();

}