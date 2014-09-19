/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.common.ui.plot.function;

import java.util.EventListener;


/**
 * A listener which is notified when a viewer's selection changes.
 *
 */
public interface IFunctionModifiedListener extends EventListener{

	/**
	 * Notifies that the selection has changed.
	 *
	 * @param event FunctionModifiedEvent object describing the change
	 */
	public void functionModified(FunctionModifiedEvent event);
}
