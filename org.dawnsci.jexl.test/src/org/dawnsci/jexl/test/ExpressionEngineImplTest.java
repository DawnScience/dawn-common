/*
 * Copyright (c) 2012-2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.jexl.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.dawnsci.jexl.internal.ExpressionEngineImpl;
import org.eclipse.dawnsci.analysis.api.expressions.IExpressionEngine;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.junit.Test;

public class ExpressionEngineImplTest {

	public void assertVariablesEquals(String expression, String...expected) throws Exception {
		IExpressionEngine engine = new ExpressionEngineImpl();
		engine.createExpression(expression);
		Collection<String> variables = engine.getVariableNamesFromExpression();
		String[] actual = variables.toArray(new String[variables.size()]);
		assertArrayEquals(expected, actual);
	}

	@Test
	public void testGetVariableNamesFromExpressionMaintainsOrder() throws Exception {
		assertVariablesEquals("a+b+c", "a", "b", "c");
		assertVariablesEquals("a+(b+c)", "a", "b", "c");
		assertVariablesEquals("(a+b)+c", "a", "b", "c");
		assertVariablesEquals("c+b+a", "c", "b", "a");
		assertVariablesEquals("c+(b+a)", "c", "b", "a");
		assertVariablesEquals("(c+b)+a", "c", "b", "a");
	}

	@Test
	public void testDottedNames() throws Exception {
		assertVariablesEquals("a.b.c+d.e.f", "a.b.c", "d.e.f");
		assertVariablesEquals("my.'new'", "my.new");
		assertVariablesEquals("my['new']", "my.new");
	}

	@Test
	public void testDottedNamesWithKeyWords() throws Exception {
		// fails if expression longer than 10 char
		assertVariablesEquals("a.'new'.va", "a.new.va");
		assertVariablesEquals("a['new'].va", "a.new.va");
	}

	public void assertExpressionEquals(Object expected, String expression, Map<String, Object> vars) throws Exception {
		IExpressionEngine engine = new ExpressionEngineImpl();
		if (vars != null) engine.setLoadedVariables(vars);
		engine.createExpression(expression);
		Object obj = engine.evaluate();
		if (obj instanceof Double) {
			assertEquals(((Number) expected).doubleValue(), ((Double) obj).doubleValue(), 1e-15); 
		} else {
			assertEquals(expected, obj);
		}
	}

	enum Order {
		One,
		Two
	}

	@Test
	public void testEvaluation() throws Exception {
		assertExpressionEquals(5, "'hello'.size()", null);

		Map<String, Object> vars = new HashMap<>();
		vars.put("a","hello");
		vars.put("e1", Order.Two);
		assertExpressionEquals(5, "a.size()", vars);
		assertExpressionEquals(true, "a == 'hello'", vars);
		assertExpressionEquals(1, "e1.ordinal()", vars);
		assertExpressionEquals(true, "e1.ordinal() == 1", vars);
		assertExpressionEquals(true, "e1.toString() == 'Two'", vars);
	}

	@Test
	public void testReshape() throws Exception {
		Map<String, Object> vars = new HashMap<>();
		vars.put("a", DatasetFactory.zeros(12));

		Dataset z = DatasetFactory.zeros(12).reshape(4, 3);
		assertExpressionEquals(z, "dat:reshape(a, 4, 3)", vars);
	}
}