/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawnsci.io.spec;

import java.util.EventListener;

public interface MultiScanDataListener extends EventListener {

	/**
	 * Called when data is found.
	 * @param specDataEvent
	 * @return true if still interested in listening, false otherwise
	 */
	public boolean specDataPerformed(MultiScanDataEvent specDataEvent);

}
