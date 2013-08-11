package org.dawb.common.python.rpc;

import java.util.Map;

import uk.ac.diamond.scisoft.analysis.rpc.AnalysisRpcClient;
import uk.ac.diamond.scisoft.analysis.rpc.AnalysisRpcException;

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

}
