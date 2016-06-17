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

public class ExpressionUtils {

	/**
	 * 
	 * @param expression
	 * @param values
	 *            - Intentionally
	 * @return
	 * @throws Exception
	 */
	public static boolean isValidSyntax(final String expression) {
		try {
			final IExpressionEvaluator eval = ExpressionFactory.createExpressionEvaluator();
			eval.setExpression(expression);
			return eval.isValidSyntax();
		} catch (Throwable ne) {
			return false;
		}
	}

	/**
	 * 
	 * @param expression
	 * @param values
	 *            - Intentionally
	 * @return
	 * @throws Exception
	 */
	public static double evaluateExpression(final String expression, Map<String, ?> values) throws Exception {
		final IExpressionEvaluator eval = ExpressionFactory.createExpressionEvaluator();
		eval.setExpression(expression);
		return eval.evaluate(values);
	}
}