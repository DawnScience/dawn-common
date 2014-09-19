/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.common.ui.plot.function;

import java.util.EventObject;

/**
 * Event object describing a selection change. The source of these
 * events is a selection provider.
 *
 */
public class FunctionModifiedEvent extends EventObject{

	/**
	 * Generated serial version UID for this class.
	 */
	private static final long serialVersionUID = 7844172734140467876L;

	/**
	 * Creates a new event for the given object.
	 *
	 * @param object the selection provider
	 */
	public FunctionModifiedEvent(Object object) {
		super(object);
	}

}
