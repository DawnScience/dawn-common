package org.dawnsci.jexl.internal;

import org.dawb.common.services.expressions.IExpressionEngine;
import org.dawb.common.services.expressions.IExpressionService;

public class ExpressionServiceImpl implements IExpressionService {

	@Override
	public IExpressionEngine getExpressionEngine() {
		return new ExpressionEngineImpl();
	}

}
