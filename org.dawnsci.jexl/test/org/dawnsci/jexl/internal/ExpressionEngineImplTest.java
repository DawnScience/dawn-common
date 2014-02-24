package org.dawnsci.jexl.internal;

import static org.junit.Assert.*;

import java.util.Collection;

import org.dawb.common.services.expressions.IExpressionEngine;
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

}
