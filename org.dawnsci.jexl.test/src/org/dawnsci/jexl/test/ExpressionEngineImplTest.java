/*
 * Copyright (c) 2012-2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.jexl.test;

import static org.junit.Assert.*;

import java.util.Collection;

import org.dawnsci.jexl.internal.ExpressionEngineImpl;
import org.eclipse.dawnsci.analysis.api.expressions.IExpressionEngine;
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

}