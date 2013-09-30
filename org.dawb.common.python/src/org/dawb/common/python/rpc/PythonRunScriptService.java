package org.dawb.common.python.rpc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.dawb.common.python.Activator;
import org.dawb.common.util.eclipse.BundleUtils;
import org.eclipse.core.resources.IProject;
import org.python.pydev.core.IInterpreterInfo;
import org.python.pydev.core.Tuple;

import uk.ac.diamond.scisoft.analysis.rpc.AnalysisRpcClient;
import uk.ac.diamond.scisoft.analysis.rpc.AnalysisRpcException;

/**
 * Implementation of IPythonRunScript that registers
 * {@value #PYTHON_SERVICE_RUNSCRIPT_PY}'s runScript function as a handler and
 * then can be used as a proxy
 */
public class PythonRunScriptService implements IPythonRunScript {
	private static final String SCRIPT_PATH = "/org/dawb/common/python/rpc/";
	private static final String PYTHON_SERVICE_RUNSCRIPT_PY = "python_service_runscript.py";
	private IPythonRunScript proxy;

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
	public PythonRunScriptService(IAnalysisRpcPythonService rpcservice)
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
	public PythonRunScriptService(IAnalysisRpcPythonService rpcservice,
			boolean debug) throws IOException, AnalysisRpcException {
		this(rpcservice, debug, false);
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
	 * @param skipAddHandler
	 *            if true, does not add the handler to the server. This should
	 *            only be true in cases where the handler has already been added
	 * @throws IOException
	 *             if there is a problem resolving the location of
	 *             {@value #PYTHON_SERVICE_RUNSCRIPT_PY}
	 * @throws AnalysisRpcException
	 *             if there is a problem registering the handler
	 */
	public PythonRunScriptService(IAnalysisRpcPythonService rpcservice,
			boolean debug, boolean skipAddHandler) throws IOException,
			AnalysisRpcException {
		if (!skipAddHandler) {
			final File path = BundleUtils
					.getBundleLocation(Activator.PLUGIN_ID);
			String script = path.getAbsolutePath() + SCRIPT_PATH
					+ PYTHON_SERVICE_RUNSCRIPT_PY;

			if (!new File(script).exists()) {
				script = path.getAbsolutePath() + "/src" + SCRIPT_PATH
						+ PYTHON_SERVICE_RUNSCRIPT_PY;
				if (!new File(script).exists()) {
					throw new RuntimeException("Couldn't find path to "
							+ PYTHON_SERVICE_RUNSCRIPT_PY + "!");
				}
			}

			// script has a function definition called "runScript", add a
			// handler for it, then create a proxy to run the function
			rpcservice.addHandlers("execfile(r'" + script + "')",
					new String[] { "runScript" });
		}
		proxy = rpcservice.getClient().newProxyInstance(IPythonRunScript.class,
				debug);
	}

	@Override
	public Map<String, Object> runScript(String scriptFullPath,
			Map<String, ?> data) throws AnalysisRpcException {
		return proxy.runScript(scriptFullPath, data);
	}

	@Override
	public Map<String, Object> runScript(String scriptFullPath,
			Map<String, ?> data, String funcName)
			throws AnalysisRpcException {
		return proxy.runScript(scriptFullPath, data, funcName);
	}

	
	private static List<Tuple<Tuple<IProject, IInterpreterInfo>, AnalysisRpcPythonPyDevService>> services = Collections.synchronizedList(new ArrayList<Tuple<Tuple<IProject, IInterpreterInfo>, AnalysisRpcPythonPyDevService>>());

	public static PythonRunScriptService getService(boolean isDebug, final IProject project, final IInterpreterInfo info) throws IOException, AnalysisRpcException {
		
		synchronized (services) {
			Tuple<IProject, IInterpreterInfo> tuple = new Tuple<IProject, IInterpreterInfo>(
					project, info);
			for (Tuple<Tuple<IProject, IInterpreterInfo>, AnalysisRpcPythonPyDevService> service : services) {
				if (service.o1.equals(tuple)) {
					return new PythonRunScriptService(service.o2, isDebug, true);
				}
			}
			AnalysisRpcPythonPyDevService rpcservice = new AnalysisRpcPythonPyDevService(
					info, project);
			services.add(new Tuple<Tuple<IProject, IInterpreterInfo>, AnalysisRpcPythonPyDevService>(
					tuple, rpcservice));
			return new PythonRunScriptService(rpcservice, isDebug);
		}
	}

}
