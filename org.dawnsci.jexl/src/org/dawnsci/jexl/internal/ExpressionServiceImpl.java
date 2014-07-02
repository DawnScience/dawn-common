package org.dawnsci.jexl.internal;

import org.dawb.common.services.expressions.IExpressionEngine;
import org.dawb.common.services.expressions.IExpressionService;

public class ExpressionServiceImpl implements IExpressionService {

	public ExpressionServiceImpl() {
		System.out.println("Starting expression service.");
	}
	@Override
	public IExpressionEngine getExpressionEngine() {
		return new ExpressionEngineImpl();
	}

}
