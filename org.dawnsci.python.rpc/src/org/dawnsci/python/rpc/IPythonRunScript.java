/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.python.rpc;

import java.util.Map;

import org.eclipse.dawnsci.analysis.api.rpc.AnalysisRpcException;

/**
 * Interface around function provided by python_service_runscript.py
 */
public interface IPythonRunScript {

	/**
	 * Run a Python script (using execfile) passing the Map script_inputs as a
	 * dictionary in the namespace of the exec'ed file. The function returns the
	 * contents of the dict named script_outputs.
	 * 
	 * @param scriptFullPath
	 *            the full path name to the script to exec
	 * @param script_inputs
	 *            the dict passed to the script
	 * @return contents of the script_outputs dict in the executed Python file
	 * @throws AnalysisRpcException
	 *             if the request fails,
	 * @see AnalysisRpcClient#request(String, Object[])
	 */
	public Map<String, Object> runScript(String scriptFullPath,
			Map<String, ?> script_inputs) throws AnalysisRpcException;

	/**
	 * Run a Python script (using execfile) passing the Map script_inputs as a
	 * dictionary in the namespace of the exec'ed file. The function returns the
	 * contents of the dict named script_outputs.
	 * 
	 * @param scriptFullPath
	 *            the full path name to the script to exec
	 * @param script_inputs
	 *            the dict passed to the script
	 * @param funcName
	 *            provide an alternate to the default function name to run
	 *            within what is defined by execfile'ing(scriptFullPath)
	 * @return contents of the script_outputs dict in the executed Python file
	 * @throws AnalysisRpcException
	 *             if the request fails,
	 * @see AnalysisRpcClient#request(String, Object[])
	 */
	public Map<String, Object> runScript(String scriptFullPath,
			Map<String, ?> script_inputs, String funcName)
			throws AnalysisRpcException;
	
	/**
	 * Clear python state cache without having to stop interpreter
	 * @throws AnalysisRpcException
	 */
	public void clearCache() throws AnalysisRpcException;
}
