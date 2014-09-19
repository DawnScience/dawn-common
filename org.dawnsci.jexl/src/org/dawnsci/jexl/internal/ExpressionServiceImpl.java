/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
