/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.python.rpc;

import uk.ac.diamond.scisoft.analysis.rpc.AnalysisRpcClient;
import uk.ac.diamond.scisoft.analysis.rpc.AnalysisRpcException;

public interface IAnalysisRpcPythonService extends IAnalysisRpcPythonRemote {

	/**
	 * Get the RPC Client so that additional calls can be made. newProxyInstance
	 * can be called on the resulting client to obtain a proxy for registered
	 * handlers.
	 * 
	 * @return instance of client.
	 */
	public AnalysisRpcClient getClient();

	/**
	 * Convenience method around {@link #addHandlers(String, String[])} to add a
	 * single handler
	 */
	public void addHandler(String pycode, String single_handler_name)
			throws AnalysisRpcException;

	public void stop();

}