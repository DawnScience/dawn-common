/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.python.rpc;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.dawb.common.util.eclipse.BundleUtils;
import org.eclipse.dawnsci.analysis.api.rpc.AnalysisRpcException;
import org.eclipse.dawnsci.analysis.api.rpc.AnalysisRpcRemoteException;
import org.eclipse.dawnsci.analysis.api.rpc.IAnalysisRpcPythonService;

import uk.ac.diamond.scisoft.analysis.rpc.AnalysisRpcClient;

/**
 * Implementation of IPythonRunScript that registers
 * {@value #PYTHON_SERVICE_RUNSCRIPT_PY}'s runScript function as a handler and
 * then can be used as a proxy
 */
public class PythonRunSavuService implements IPythonRunSavu{
	private static final String SCRIPT_PATH = "/org/dawnsci/python/rpc/";
	private static final String PYTHON_SERVICE_RUNSAVU_PY = "python_service_runsavu.py";
	private IPythonRunSavu proxy;

	/**
	 * Create a new proxy and register the runScript code. Calls to runScript
	 * are run the client's {@link AnalysisRpcClient#request(String, Object[])}
	 * (as opposed to
	 * {@link AnalysisRpcClient#request_debug(String, Object[], boolean)}.
	 * 
	 * @param rpcservice
	 *            the running service to register with
	 * @throws IOException
	 *             if there is a problem resolving the location of
	 *             {@value #PYTHON_SERVICE_RUNSCRIPT_PY}
	 * @throws AnalysisRpcException
	 *             if there is a problem registering the handler
	 */
	public PythonRunSavuService(IAnalysisRpcPythonService rpcservice)
			throws IOException, AnalysisRpcException {
		this(rpcservice, false);
	}

	/**
	 * Create a new proxy and register the runScript code.
	 * 
	 * @param rpcservice
	 *            the running service to register with
	 * @param debug
	 *            if true uses's the client's
	 *            {@link AnalysisRpcClient#request_debug(String, Object[], boolean)}
	 *            , if false uses
	 *            {@link AnalysisRpcClient#request(String, Object[])}
	 * @throws IOException
	 *             if there is a problem resolving the location of
	 *             {@value #PYTHON_SERVICE_RUNSCRIPT_PY}
	 * @throws AnalysisRpcException
	 *             if there is a problem registering the handler
	 */
	public PythonRunSavuService(IAnalysisRpcPythonService rpcservice,
			boolean debug) throws IOException, AnalysisRpcException {
		this(rpcservice, debug, false);
	}

	/**
	 * Create a new proxy and register the runScript code.
	 * 
	 * @param rpcservice
	 *            the running service to register with
	 * @param debug
	 *            if true uses's the client's	public Map <String, Object> getSanitizedParams() {
		Map<String, Object> out = null;
		Map<String, Object> params = getParameters();
		for (Map.Entry<String, Object> entry : params.entrySet())
			out.put(entry.getKey(), value)
		return out;
		
		
	}
	
	 *            {@link AnalysisRpcClient#request_debug(String, Object[], boolean)}
	 *            , if false uses
	 *            {@link AnalysisRpcClient#request(String, Object[])}
	 * @param skipAddHandler
	 *            if true, does not add the handler to the server. This should
	 *            only be true in cases where the handler has already been added
	 * @throws IOException
	 *             if there is a problem resolving the location of
	 *             {@value #PYTHON_SERVICE_RUNSCRIPT_PY}
	 * @throws AnalysisRpcException
	 *             if there is a problem registering the handler
	 */
	public PythonRunSavuService(IAnalysisRpcPythonService rpcservice,
			boolean debug, boolean skipAddHandler) throws IOException,
			AnalysisRpcException {
		if (!skipAddHandler) {
			final File path = BundleUtils
					.getBundleLocation(Activator.PLUGIN_ID);
			String script = path.getAbsolutePath() + SCRIPT_PATH
					+ PYTHON_SERVICE_RUNSAVU_PY;

			if (!new File(script).exists()) {
				script = path.getAbsolutePath() + "/src" + SCRIPT_PATH
						+ PYTHON_SERVICE_RUNSAVU_PY;
				if (!new File(script).exists()) {
					throw new RuntimeException("Couldn't find path to "
							+ PYTHON_SERVICE_RUNSAVU_PY + "!");
				}
			}

			// script has a function definition called "runScript", add a
			// handler for it, then create a proxy to run the function
			rpcservice.addHandlers("execfile(r'" + script + "')",
					new String[] {
						"runSavu",
						"populate_plugins",
						"get_plugin_info",
						"get_plugin_params",
						"get_output_rank"
					});
		}
		proxy = rpcservice.getClient().newProxyInstance(IPythonRunSavu.class,
				debug);
	}

	@Override
	public Map<String, Object> runSavu(String scriptFullPath,Map<String, Map<String, Object>> map,
			boolean metaOnly, Map<String, ?> data) throws AnalysisRpcException {
		return proxy.runSavu(scriptFullPath,map, metaOnly,data);
	}

	/**
	 * Formats a remote exception to limit the Python code that was not "users" code. 
	 * @param e Remote Exception to format
	 * @return a Python style exception format
	 */
	public String formatException(AnalysisRpcRemoteException e) {
		return e.getPythonFormattedStackTrace(PYTHON_SERVICE_RUNSAVU_PY);
	}

	@Override
	public Map<String, Object> get_plugin_info() throws AnalysisRpcException {
		return proxy.get_plugin_info();
	}
	
	@Override
	public void populate_plugins() throws AnalysisRpcException {
		proxy.populate_plugins();
	}
	@Override
	public Map<String, Object> get_plugin_params(String pluginName) throws AnalysisRpcException {
		return proxy.get_plugin_params(pluginName);
	}

	@Override
	public int get_output_rank(String path2plugin, Map<String, Object> inputs, Map<String, Map<String, Object>> map) throws AnalysisRpcException {
		return proxy.get_output_rank(path2plugin, inputs, map);
	}
}

