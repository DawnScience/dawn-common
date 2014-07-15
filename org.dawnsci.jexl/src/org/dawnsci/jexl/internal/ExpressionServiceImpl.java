package org.dawnsci.jexl.internal;

import org.dawb.common.services.expressions.IExpressionEngine;
import org.dawb.common.services.expressions.IExpressionService;

public class ExpressionServiceImpl implements IExpressionService {

	static {
		System.out.println("Starting expression service.");
	}
	public ExpressionServiceImpl() {
		// Important do nothing here, OSGI may start the service more than once.
	}
	@Override
	public IExpressionEngine getExpressionEngine() {
		return new ExpressionEngineImpl();
	}

}
